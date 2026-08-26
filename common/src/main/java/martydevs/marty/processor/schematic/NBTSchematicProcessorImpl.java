package martydevs.marty.processor.schematic;

import martydevs.marty.model.image.MapArtImage;
import martydevs.marty.model.schematic.NBTSchematicProcessor;
import martydevs.marty.model.schematic.NBTSchematicWork;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.*;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector2ic;

import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class NBTSchematicProcessorImpl implements NBTSchematicProcessor {

    private static final int MAP_SIZE = 128;

    private final Executor executor;

    public NBTSchematicProcessorImpl(Executor executor) {
        this.executor = executor;
    }

    private static ListTag newIntegerList(int... values) {
        ListTag res = new ListTag();

        for (int value : values) {
            res.add(IntTag.valueOf(value));
        }

        return res;
    }

    private InputStream doWork(NBTSchematicWork work) {
        MapArtImage source = work.source();
        Vector2ic dimensionsInMaps = source.dimensionsInMaps();

        boolean shadePreservingLine = work.addShadePreservingLine();
        int width = MAP_SIZE * dimensionsInMaps.x();
        int height = 1;
        int length = MAP_SIZE * dimensionsInMaps.y();

        MapArtImage.Map[][] maps = source.maps();
        for (MapArtImage.Map[] row : maps) {
            for (MapArtImage.Map map : row) {
                if (map.height() > height) height = map.height();
            }
        }

        PaletteBuilder paletteBuilder = new PaletteBuilder();
        ListTag blocklist = new ListTag();
        int baseZ = shadePreservingLine ? 1 : 0;
        for (int mapX = 0; mapX < dimensionsInMaps.x(); mapX++) {
            int xOrigin = mapX * MAP_SIZE;
            for (int mapY = 0; mapY < dimensionsInMaps.y(); mapY++) {
                int zOrigin = mapY * MAP_SIZE;

                Map<Integer, BlockState> blocks = work.source().maps()[mapX][mapY].blocks();
                for (Map.Entry<Integer, BlockState> entry : blocks.entrySet()) {
                    int x = xOrigin + MapArtImage.decodedX(entry.getKey());
                    int y = MapArtImage.decodedY(entry.getKey()) + 128;
                    int z = zOrigin + MapArtImage.decodedZ(entry.getKey());

                    CompoundTag blockTag = new CompoundTag();
                    blockTag.put("pos", newIntegerList(x, y, baseZ + z));
                    blockTag.putInt("state", paletteBuilder.index(entry.getValue()));
                    blocklist.add(blockTag);

                    if(z == 0 && shadePreservingLine) {
                        CompoundTag block1Tag = new CompoundTag();
                        block1Tag.put("pos", newIntegerList(x, y, 0));
                        block1Tag.putInt("state", paletteBuilder.index(work.shadePreservingMaterial()));
                        blocklist.add(block1Tag);
                    }
                }
            }
        }

        CompoundTag rootTag = new CompoundTag();
        rootTag.put("palette", paletteBuilder.build());
        rootTag.put("blocks", blocklist);

        rootTag.putInt("DataVersion", SharedConstants.getCurrentVersion().dataVersion().version());
        rootTag.put("size", newIntegerList(width, height, work.addShadePreservingLine() ? length + 1 : length));

        byte[] result;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            NbtIo.writeCompressed(rootTag, outputStream);
            result = outputStream.toByteArray();
        }
        catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        return new ByteArrayInputStream(result);
    }

    @Override
    public CompletableFuture<InputStream> queue(NBTSchematicWork work) {
        return CompletableFuture.supplyAsync(() -> doWork(work), executor);
    }

    private static final class PaletteBuilder {

        private final LinkedHashMap<BlockState, Integer> palette = new LinkedHashMap<>();
        private int index;

        int index(BlockState state) {
            int paletteIndex = palette.getOrDefault(state, -1);
            if (paletteIndex == -1) {
                paletteIndex = index;
                palette.put(state, index++);
            }

            return paletteIndex;
        }

        ListTag build() {
            ListTag paletteTag = new ListTag();
            for (BlockState state : palette.sequencedKeySet()) {
                paletteTag.add(NbtUtils.writeBlockState(state));
            }
            return paletteTag;
        }

    }

}
