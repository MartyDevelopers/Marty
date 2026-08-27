package martydevs.marty.processor.image.cpu;

import martydevs.marty.executor.SameThreadExecutor;
import martydevs.marty.helper.map.MapColorHelper;
import martydevs.marty.helper.map.PaletteEntry;
import martydevs.marty.model.image.*;
import martydevs.marty.model.work.WorkProgressListener;
import martydevs.marty.processor.image.cpu.color.NearestColorAlgorithm;
import martydevs.marty.processor.image.cpu.color.NearestColorAlgorithms;
import martydevs.marty.processor.image.cpu.dithering.DitheringAlgorithms;
import martydevs.marty.processor.image.cpu.dithering.DitheringResult;
import martydevs.marty.processor.image.cpu.dithering.DitheringUtil;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class CPUImageProcessor implements ImageProcessor {

    // Constants
    private static final int
            TRANSPARENCY_THRESHOLD = (int) Math.round(255 * 0.75), // Value from which pixel is considered transparent.
            TRANSPARENCY_PLACEHOLDER = -1,
            MAP_SIZE = 128,
            MAXIMUM_TASKS_FOR_STAIR_CASING = 4,
            MINIMUM_BATCH_SIZE_FOR_STAIR_CASING = 64; // Actually just amount of the lines per task

    private static final float
            SIZE_CALCULATION_PROGRESS = 0.01f,
            PALETTE_BUILDING_PROGRESS = 0.05f,
            DITHERING_PROGRESS = 0.10f,
            MAP_CONSTRUCTING_PROGRESS_BASE = DITHERING_PROGRESS;

    private static final BlockState GLASS_BLOCK_STATE = Blocks.GLASS.defaultBlockState();
    private static final int
            WATER_HEIGHT_LOW = 7,
            WATER_HEIGHT_NORMAL = 5,
            WATER_HEIGHT_HIGH = 1;

    private final Executor executor;
    private final Executor executorForStairCasing;
    private final @Nullable WorkProgressListener<MapArtImage, ImageWork> workProgressListener;

    public CPUImageProcessor(Executor executor, @Nullable Executor executorForStairCasing, @Nullable WorkProgressListener<MapArtImage, ImageWork> workProgressListener) {
        this.executor = executor;

        this.executorForStairCasing = (executorForStairCasing == null
                ? SameThreadExecutor.INSTANCE
                : executorForStairCasing);

        this.workProgressListener = workProgressListener;
    }

    private void progressUpdate(ImageWork work, float progress) {
        if(workProgressListener != null) {
            try {
                workProgressListener.update(work, progress);
            }
            catch (Throwable _) {}
        }
    }

    @Override
    public CompletableFuture<MapArtImage> queue(ImageWork work) {
        progressUpdate(work, -1);
        return CompletableFuture.supplyAsync(() -> doWork(work), executor);
    }

    @ApiStatus.Internal
    public static CroppedView createCenterCroppedView(BufferedImage image, Vector2ic targetBounds) {
        Vector2ic sourceBounds = new Vector2i(image.getWidth(), image.getHeight());

        float imageAspect = (float) sourceBounds.x() / sourceBounds.y();
        float targetAspect = (float) targetBounds.x() / targetBounds.y();

        int cropWidth, cropHeight;
        int cropX, cropY;

        if (imageAspect > targetAspect) {
            cropHeight = sourceBounds.y();
            cropWidth = Math.round(sourceBounds.y() * targetAspect);
            cropX = (sourceBounds.x() - cropWidth) / 2;
            cropY = 0;
        }
        else {
            cropWidth = sourceBounds.x();
            cropHeight = Math.round(sourceBounds.x() / targetAspect);
            cropX = 0;
            cropY = (sourceBounds.y() - cropHeight) / 2;
        }

        float scaleX = (float) targetBounds.x() / cropWidth;
        float scaleY = (float) targetBounds.y() / cropHeight;
        float scale = Math.min(scaleX, scaleY);

        return new CroppedView(
                image,
                new Vector2i(cropX, cropY),
                new Vector2i(cropX + cropWidth, cropY + cropHeight),
                scale
        );
    }

    private static PaletteEntry[] buildPalette(ImageWork work) {
        BlockPalette blockPalette = work.blockPalette();
        WaterPalette waterPalette = work.waterPalette();
        boolean stairCasing = work.useStairCasing();
        boolean water = waterPalette.enabled();

        int colorsCount = blockPalette.allColors().size();
        if(stairCasing) colorsCount *= 3;
        if(water) colorsCount += 3;

        PaletteEntry[] palette = new PaletteEntry[colorsCount];

        Iterator<MapColor> mapColorIterator = blockPalette.allColors().iterator();
        int individualIndex = 0;
        while (mapColorIterator.hasNext()) {
            MapColor mapColor = mapColorIterator.next();

            if(!stairCasing) palette[individualIndex] = MapColorHelper.paletteEntries(mapColor)[1];
            else System.arraycopy(MapColorHelper.paletteEntries(mapColor), 0, palette, individualIndex * 3, 3);

            ++individualIndex;
        }

        if(water)
            System.arraycopy(MapColorHelper.paletteEntries(MapColor.WATER), 0, palette, palette.length - 3, 3);

        return palette;
    }

    private static DitheringResult ditherByNearest(CroppedView croppedView, NearestColorAlgorithm nearestColorAlgorithm, boolean paletteIncludesWater, PaletteEntry[] palette, int[][] out) {
        int width = croppedView.bounds.x();
        int height = croppedView.bounds.y();

        int lowestWaterColorIndex = Integer.MAX_VALUE;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb = croppedView.getRGB(x, y);
                if(out[x][y] == TRANSPARENCY_PLACEHOLDER) continue;

                int nearestIndex = out[x][y] = nearestColorAlgorithm.findNearestColor(
                        DitheringUtil.clamp((rgb >> 16) & 0xFF),
                        DitheringUtil.clamp((rgb >> 8) & 0xFF),
                        DitheringUtil.clamp(rgb & 0xFF),
                        palette
                );

                if(paletteIncludesWater) {
                    int waterColorIndex = palette[nearestIndex].waterColorIndex();
                    if(waterColorIndex != -1 && waterColorIndex < lowestWaterColorIndex)
                        lowestWaterColorIndex = waterColorIndex;
                }
            }
        }

        return new DitheringResult(lowestWaterColorIndex == Integer.MAX_VALUE ? -1 : lowestWaterColorIndex);
    }

    private static int waterHeightByColorIndex(int colorIndex) {
        return switch (colorIndex) {
            case 2 -> WATER_HEIGHT_HIGH;
            case 1 -> WATER_HEIGHT_NORMAL;
            case 0 -> WATER_HEIGHT_LOW;
            default -> throw new ArrayIndexOutOfBoundsException("The only possible values for water color index are: 1, 2 and 3, but present is " + colorIndex);
        };
    }

    private void constructFlatMap(ImageWork work, Vector2ic outputDimensionsInMaps, PaletteEntry[] palette, int[][] paletteIndices, DitheringResult ditheringResult, MapArtImage.Map[][] maps) {
        float leftProgress = 1 - MAP_CONSTRUCTING_PROGRESS_BASE;

        int totalBlocks = (maps.length * maps[0].length) * (MAP_SIZE * MAP_SIZE);
        float progressPerBlock = leftProgress / (float) totalBlocks;

        int blockIndex = 0;
        int widthInMaps = outputDimensionsInMaps.x();
        for (int mapX = 0; mapX < widthInMaps; mapX++) {
            for (int mapY = 0; mapY < outputDimensionsInMaps.y(); mapY++) {
                int lowestWaterColorIndex = ditheringResult.lowestWaterColorIndexUsed();

                int height;
                if(lowestWaterColorIndex == -1) height = 1;
                else height = waterHeightByColorIndex(lowestWaterColorIndex);

                int mapOriginX = mapX * MAP_SIZE;
                int mapOriginY = mapY * MAP_SIZE;

                Map<Integer, BlockState> blocks = new HashMap<>(MAP_SIZE * MAP_SIZE);

                int highestYIndex = height - 1;
                for (int blockX = 0; blockX < MAP_SIZE; blockX++) {
                    for (int blockZ = 0; blockZ < MAP_SIZE; blockZ++) {
                        int encodedCoordinates = MapArtImage.encodedBlockCoordinates(blockX, highestYIndex, blockZ);
                        int colorIndex = paletteIndices[mapOriginX + blockX][mapOriginY + blockZ];
                        if(colorIndex == -1) {
                            blocks.put(encodedCoordinates, GLASS_BLOCK_STATE);
                            continue;
                        }

                        PaletteEntry paletteEntry = palette[colorIndex];
                        int waterIndex = paletteEntry.waterColorIndex();
                        if(waterIndex != -1) {
                            int waterHeight = waterHeightByColorIndex(waterIndex);

                            BlockState state = work.waterPalette().leaves().defaultBlockState()
                                    .setValue(BlockStateProperties.WATERLOGGED, true);
                            for (int blockY = 0; blockY < waterHeight; blockY++) {
                                blocks.put(
                                        MapArtImage.encodedBlockCoordinates(blockX, highestYIndex - blockY, blockZ),
                                        state
                                );
                            }
                            continue;
                        }

                        Block block = work.blockPalette().blockForColor(paletteEntry.mapColor());
                        blocks.put(
                                encodedCoordinates,
                                block.defaultBlockState()
                        );
                    }
                }

                maps[mapX][mapY] = new MapArtImage.Map(blocks, height);
                progressUpdate(work, (float) (blockIndex++) * progressPerBlock);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void constructStairCasedMap(ImageWork work, Vector2ic outputDimensionsInMaps, PaletteEntry[] palette, int[][] paletteIndices, DitheringResult ditheringResult, MapArtImage.Map[][] maps) {
        int width = outputDimensionsInMaps.x() * MAP_SIZE;
        int length = outputDimensionsInMaps.y() * MAP_SIZE;

        int tasksCount;
        int linesPerTask;
        if(width / MINIMUM_BATCH_SIZE_FOR_STAIR_CASING > MAXIMUM_TASKS_FOR_STAIR_CASING) {
            tasksCount = MAXIMUM_TASKS_FOR_STAIR_CASING;
            linesPerTask = width / MAXIMUM_TASKS_FOR_STAIR_CASING;
        }
        else {
            tasksCount = width / MINIMUM_BATCH_SIZE_FOR_STAIR_CASING;
            linesPerTask = width / tasksCount;
        }

        float progressPerLine = (1 - MAP_CONSTRUCTING_PROGRESS_BASE) / width;

        Map<Integer, BlockState>[][] flatMaps = new Map[maps.length][maps[0].length];
        for (int x = 0; x < maps.length; x++) {
            for (int y = 0; y < maps[x].length; y++) {
                flatMaps[x][y] = new ConcurrentHashMap<>();
            }
        }

        CompletableFuture<Void>[] tasks = new CompletableFuture[tasksCount];

        AtomicInteger heightAtomic = new AtomicInteger(1);
        AtomicInteger linesDone = new AtomicInteger(0);
        AtomicBoolean calculationsLock = new AtomicBoolean(false);

        int[] lineMinimumY = new int[width];
        for (int i = 0; i < tasksCount; i++) {
            final int taskIndex = i;
            tasks[i] = CompletableFuture.supplyAsync(() -> {
                int xOrigin = taskIndex * linesPerTask;

                int minimumY = 0, maximumY = 0;

                for (int x = xOrigin; x < linesPerTask + xOrigin; x++) {
                    int localX = x % MAP_SIZE;
                    int mapX = (x - localX) / MAP_SIZE;

                    int previousY = 0;
                    for (int z = 0; z < length; z++) {
                        int localZ = z % MAP_SIZE;
                        int mapY = (z - localZ) / MAP_SIZE;

                        if(previousY > maximumY) maximumY = previousY;
                        if(previousY < minimumY) minimumY = previousY;

                        Map<Integer, BlockState> map = flatMaps[mapX][mapY];

                        int colorIndex = paletteIndices[x][z];
                        if(colorIndex == TRANSPARENCY_PLACEHOLDER) {
                            map.put(MapArtImage.encodedBlockCoordinates(localX, previousY, localZ), GLASS_BLOCK_STATE);
                            continue;
                        }

                        PaletteEntry paletteEntry = palette[colorIndex];
                        int waterIndex = paletteEntry.waterColorIndex();
                        if(waterIndex != -1) {
                            int waterHeight = waterHeightByColorIndex(waterIndex);

                            BlockState state = work.waterPalette().leaves().defaultBlockState()
                                    .setValue(BlockStateProperties.WATERLOGGED, true);
                            for (int blockY = 0; blockY < waterHeight; blockY++) {
                                map.put(
                                        MapArtImage.encodedBlockCoordinates(localX, previousY - blockY, localZ),
                                        state
                                );
                            }

                            int lowestWaterPoint = (previousY - waterHeight) + 1;
                            if(lowestWaterPoint < minimumY) minimumY = lowestWaterPoint;
                            continue;
                        }

                        int shift = paletteEntry.brightness().id - 1;
                        previousY += shift;
                        if(lineMinimumY[x] > previousY) lineMinimumY[x] = previousY;

                        map.put(
                                MapArtImage.encodedBlockCoordinates(localX, previousY - 128, localZ),
                                work.blockPalette().blockForColor(paletteEntry.mapColor()).defaultBlockState()
                        );
                    }
                }

                while (!calculationsLock.compareAndSet(false, true)) {
                    Thread.onSpinWait();
                }

                progressUpdate(work, progressPerLine * (float) linesDone.getAndIncrement());

                int sectionHeight = maximumY + Math.abs(minimumY) + 1;
                if(sectionHeight > heightAtomic.get()) heightAtomic.set(sectionHeight);

                calculationsLock.set(false);

                return null;
            }, executorForStairCasing);
        }

        for (CompletableFuture<Void> task : tasks) {
            task.join();
        }

        final int height = heightAtomic.get();
        for (int x = 0; x < flatMaps.length; x++) {
            Map<Integer, BlockState>[] column = flatMaps[x];
            for (int y = 0; y < column.length; y++) {
                Map<Integer, BlockState> converted = new HashMap<>();
                maps[x][y] = new MapArtImage.Map(converted, height);
                for (Map.Entry<Integer, BlockState> entry : column[y].entrySet()) {
                    int encoded = entry.getKey();
                    int blockX = MapArtImage.decodedX(encoded);
                    int blockZ = MapArtImage.decodedZ(encoded);

                    converted.put(
                            MapArtImage.encodedBlockCoordinates(
                                    blockX,
                                    (MapArtImage.decodedY(encoded) + Math.abs(lineMinimumY[blockX])),
                                    blockZ
                            ),
                            entry.getValue()
                    );
                }
            }
        }
    }

    private MapArtImage doWork(ImageWork work) {
        progressUpdate(work, 0);

        // Size calculations.
        BufferedImage image = work.image();
        Vector2ic outputDimensionsInMaps = work.outputDimensionsInMaps();
        Vector2i targetBounds = new Vector2i(outputDimensionsInMaps).mul(MAP_SIZE);

        CroppedView croppedView = switch (work.cropping()) {
            case Cropping.Center _ -> createCenterCroppedView(image, targetBounds);
            case Cropping.Manual manual -> throw new UnsupportedOperationException();
        };
        progressUpdate(work, SIZE_CALCULATION_PROGRESS);

        // Build palette.
        PaletteEntry[] palette = buildPalette(work);
        int[][] paletteIndices = new int[targetBounds.x][targetBounds.y];
        progressUpdate(work, PALETTE_BUILDING_PROGRESS);

        // Dithering

        // If transparency is enabled, prefill transparent pixels with -1 (placeholder for transparency)
        if(work.useTransparency()) {
            for (int x = 0; x < targetBounds.x(); x++) {
                for (int y = 0; y < targetBounds.y(); y++) {
                    int alpha = croppedView.alpha(x, y);
                    if(alpha >= TRANSPARENCY_THRESHOLD) paletteIndices[x][y] = TRANSPARENCY_PLACEHOLDER;
                }
            }
        }

        // Dithering itself
        DitheringResult ditheringResult = Optional.ofNullable(work.dithering())
                .map(DitheringAlgorithms::algorithm)
                .orElse(CPUImageProcessor::ditherByNearest)
                .dither(
                        croppedView,
                        Optional.ofNullable(work.betterColor())
                                .map(NearestColorAlgorithms::algorithm)
                                .orElse(DitheringUtil::findNearestColor),
                        work.waterPalette().enabled(),
                        palette,
                        paletteIndices
                );

        progressUpdate(work, DITHERING_PROGRESS);

        // Constructing map
        MapArtImage.Map[][] maps = new MapArtImage.Map[outputDimensionsInMaps.x()][outputDimensionsInMaps.y()];
        if(work.useStairCasing()) constructStairCasedMap(work, outputDimensionsInMaps, palette, paletteIndices, ditheringResult, maps);
        else constructFlatMap(work, outputDimensionsInMaps, palette, paletteIndices, ditheringResult, maps);

        progressUpdate(work, 1);
        return new MapArtImage(work.outputDimensionsInMaps(), maps);
    }

}
