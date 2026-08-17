package martydevs.marty.processor.cpu;

import martydevs.marty.annotation.ThreadSafe;
import martydevs.marty.helper.MapColorHelper;
import martydevs.marty.model.image.*;
import martydevs.marty.processor.cpu.dithering.DitheringAlgorithms;
import martydevs.marty.processor.cpu.dithering.DitheringResult;
import martydevs.marty.processor.cpu.dithering.DitheringUtil;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class CPUImageProcessor implements ImageProcessor {

    // Value from which pixel is considered transparent.
    private static final int TRANSPARENCY_THRESHOLD = (int) Math.round(255 * 0.75);
    private static final int TRANSPARENCY_PLACEHOLDER = -1;
    private static final int MAP_SIZE = 128;
    private static final BlockState GLASS_BLOCK_STATE = Blocks.GLASS.defaultBlockState();
    private static final int WATER_HEIGHT_LOW = 7, WATER_HEIGHT_NORMAL = 5, WATER_HEIGHT_HIGH = 1;

    private final Executor executor;

    public CPUImageProcessor(Executor executor) {
        this.executor = executor;
    }

    @Override
    public CompletableFuture<MapArtImage> queue(ImageWork work) {
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

    private static int[] buildPalette(ImageWork work) {
        BlockPalette blockPalette = work.blockPalette();
        WaterPalette waterPalette = work.waterPalette();
        boolean stairCasing = work.useStairCasing();
        boolean water = waterPalette.enabled();

        int colorsCount = blockPalette.allColors().size();
        if(stairCasing) colorsCount *= 3;
        if(water) colorsCount += 3;

        int[] palette = new int[colorsCount];

        Iterator<MapColor> mapColorIterator = blockPalette.allColors().iterator();
        int individualIndex = 0;
        while (mapColorIterator.hasNext()) {
            MapColor mapColor = mapColorIterator.next();

            if(!stairCasing)
                palette[individualIndex] = MapColorHelper.rgbValue(mapColor);
            else {
                palette[individualIndex * 3] = MapColorHelper.scaledRgbValue(mapColor, MapColor.Brightness.LOW);
                palette[individualIndex * 3 + 1] = MapColorHelper.scaledRgbValue(mapColor, MapColor.Brightness.NORMAL);
                palette[individualIndex * 3 + 2] = MapColorHelper.scaledRgbValue(mapColor, MapColor.Brightness.HIGH);
            }

            ++individualIndex;
        }

        if(water) {
            int startIndex = palette.length - 3;
            for (int i = 0; i < 3; i++) {
                palette[startIndex + i] = MapColorHelper.scaledRgbValue(MapColor.WATER, MapColor.Brightness.byId(i));
            }
        }

        return palette;
    }

    private static DitheringResult ditherByNearest(CroppedView croppedView, boolean paletteIncludesWater, int[] palette, int[][] out) {
        int width = croppedView.bounds.x();
        int height = croppedView.bounds.y();

        int lowestWaterColorIndex = Integer.MAX_VALUE;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb = croppedView.getRGB(x, y);
                if(out[x][y] == -1) continue;

                int nearestIndex = out[x][y] = DitheringUtil.findNearestColor(
                        DitheringUtil.clamp((rgb >> 16) & 0xFF),
                        DitheringUtil.clamp((rgb >> 8) & 0xFF),
                        DitheringUtil.clamp(rgb & 0xFF),
                        palette
                );

                System.out.printf("%s %s: %s\n", x, y, palette[nearestIndex]);

                if(paletteIncludesWater) {
                    int waterColorIndex = MapColorHelper.waterColorIndex(palette[nearestIndex]);
                    if(waterColorIndex != -1 && waterColorIndex < lowestWaterColorIndex)
                        lowestWaterColorIndex = waterColorIndex;
                }
            }
        }

        return new DitheringResult(lowestWaterColorIndex == Integer.MAX_VALUE ? -1 : lowestWaterColorIndex);
    }

    private static int waterHeightByColorIndex(int colorIndex) {
        return switch (colorIndex) {
            case 3 -> WATER_HEIGHT_HIGH;
            case 2 -> WATER_HEIGHT_NORMAL;
            case 1 -> WATER_HEIGHT_LOW;
            default -> throw new ArrayIndexOutOfBoundsException("The only possible values for water color index are: 1, 2 and 3");
        };
    }

    @ThreadSafe
    private static MapArtImage doWork(ImageWork work) {
        // Size calculations.
        BufferedImage image = work.image();

        Vector2ic outputDimensionsInMaps = work.outputDimensionsInMaps();

        Vector2i targetBounds = new Vector2i(outputDimensionsInMaps).mul(MAP_SIZE);
        Cropping cropping = work.cropping();

        CroppedView croppedView = switch (cropping) {
            case Cropping.Center _ -> createCenterCroppedView(image, targetBounds);
            case Cropping.Manual manual -> throw new UnsupportedOperationException();
        };

        // Build palette.
        int[] palette = buildPalette(work);
        int[][] paletteIndices = new int[targetBounds.x][targetBounds.y];

        // If transparency is enabled, prefill transparent pixels with -1 (placeholder for transparency)
        if(work.useTransparency()) {
            for (int x = 0; x < targetBounds.x(); x++) {
                for (int y = 0; y < targetBounds.y(); y++) {
                    int alpha = croppedView.alpha(x, y);
                    if(alpha >= TRANSPARENCY_THRESHOLD) paletteIndices[x][y] = -1;
                }
            }
        }

        try (FileOutputStream outputStream = new FileOutputStream(new File("/home/just_lofe/IdeaProjects/MartyDevelopers/Marty/common/src/test/resources/pre_output.png"))) {
            BufferedImage bufferedImage = new BufferedImage(croppedView.bounds.x(), croppedView.bounds.y(), BufferedImage.TYPE_INT_ARGB);
            for (int x = 0; x < bufferedImage.getWidth(); x++) {
                for (int y = 0; y < bufferedImage.getHeight(); y++) {
                    bufferedImage.setRGB(x, y, croppedView.getRGB(x, y));
                }
            }
            ImageIO.write(bufferedImage, "png", outputStream);
        }
        catch (Exception exception) {
            throw new RuntimeException();
        }

        DitheringResult ditheringResult = Optional.ofNullable(work.dithering())
                .map(DitheringAlgorithms::algorithm)
                .orElse(CPUImageProcessor::ditherByNearest)
                .dither(croppedView, work.waterPalette().enabled(), palette, paletteIndices);

        MapArtImage.Map[][] maps = new MapArtImage.Map[outputDimensionsInMaps.x()][outputDimensionsInMaps.y()];
        if(!work.useStairCasing()) {
            int widthInMaps = outputDimensionsInMaps.x();
            for (int mapX = 0; mapX < widthInMaps; mapX++) {
                for (int mapY = 0; mapY < outputDimensionsInMaps.y(); mapY++) {
                    int lowestWaterColorIndex = ditheringResult.lowestWaterColorIndexUsed();

                    int height;
                    if(lowestWaterColorIndex == -1) height = 1;
                    else height = waterHeightByColorIndex(lowestWaterColorIndex);
                    
                    int mapOriginX = mapX * MAP_SIZE;
                    int mapOriginY = mapY * MAP_SIZE;

                    BlockPalette blockPalette = work.blockPalette();
                    BlockState[][][] blocks = new BlockState[MAP_SIZE][height][MAP_SIZE];

                    int highestYIndex = height - 1;
                    for (int blockX = 0; blockX < MAP_SIZE; blockX++) {
                        for (int blockZ = 0; blockZ < MAP_SIZE; blockZ++) {
                            int colorIndex = paletteIndices[mapOriginX + blockX][mapOriginY + blockZ];
                            if(colorIndex == -1) {
                                blocks[blockX][highestYIndex][blockZ] = GLASS_BLOCK_STATE;
                                continue;
                            }

                            int rgb = palette[colorIndex];
                            int waterIndex = MapColorHelper.waterColorIndex(rgb);
                            if(waterIndex != -1) {
                                int waterHeight = waterHeightByColorIndex(waterIndex);

                                BlockState state = work.waterPalette().leaves().defaultBlockState()
                                        .setValue(BlockStateProperties.WATERLOGGED, true);
                                for (int blockY = 0; blockY < waterHeight; blockY++) {
                                    blocks[blockX][highestYIndex - blockY][blockZ] = state;
                                }
                                continue;
                            }

                            MapColor color = MapColorHelper.colorByRgbValue(rgb);
                            assert color != null;
                            Block block = blockPalette.blockForColor(color);
                            if(block == null) {
                                System.out.println("Failed color: " + colorIndex);
                            }

                            blocks[blockX][highestYIndex][blockZ] = block.defaultBlockState();
                        }
                    }

                    maps[mapX][mapY] = new MapArtImage.FlatMap(blocks);
                }
            }
        }

        return new MapArtImage(work.outputDimensionsInMaps(), maps);
    }

}
