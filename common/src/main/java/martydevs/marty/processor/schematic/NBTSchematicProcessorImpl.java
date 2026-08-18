package martydevs.marty.processor.schematic;

import martydevs.marty.model.image.MapArtImage;
import martydevs.marty.model.schematic.NBTSchematicProcessor;
import martydevs.marty.model.schematic.NBTSchematicWork;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector2ic;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

        int width = MAP_SIZE * dimensionsInMaps.x();
        int height = 1;
        int length = MAP_SIZE * dimensionsInMaps.y();

        MapArtImage.Map[][] maps = source.maps();
        for (MapArtImage.Map[] row : maps) {
            for (MapArtImage.Map map : row) {
                if (map.height() > height) height = map.height();
            }
        }

        CompoundTag rootTag = new CompoundTag();
        List<BlockState> palette = new ArrayList<>();

        ListTag blocklist = new ListTag();
        /*
        for (MapArtImage.Map[] column : maps) {
            for (MapArtImage.Map map : column) {
                for (Map.Entry<Integer, BlockState> entry : map.blocks().entrySet()) {
                    int encodedCoordinates = entry.getKey();

                    BlockState state = entry.getValue();
                    if (state == null) state = Blocks.AIR.defaultBlockState();

                    int paletteIndex = palette.indexOf(state);
                    if (paletteIndex == -1) {
                        palette.add(state);
                        paletteIndex = palette.size() - 1;
                    }

                    CompoundTag blockTag = new CompoundTag();
                    blockTag.put("pos", newIntegerList(
                            MapArtImage.decodedX(encodedCoordinates),
                            MapArtImage.decodedY(encodedCoordinates),
                            MapArtImage.decodedZ(encodedCoordinates)
                    ));
                    blockTag.putInt("state", paletteIndex);
                    blocklist.add(blockTag);
                }
            }
        }
        */

        for (int y = 0; y < height; y++) {
            for (int z = 0; z < length; z++) {
                int localZ = z % MAP_SIZE;
                int mapY = (z - localZ) / MAP_SIZE;
                for (int x = 0; x < width; x++) {
                    int localX = x % MAP_SIZE;
                    int mapX = (x - localX) / MAP_SIZE;

                    BlockState state = maps[mapX][mapY].blocks().get(MapArtImage.encodedBlockCoordinates(localX, y, localZ));
                    if(state == null) state = Blocks.AIR.defaultBlockState();

                    int paletteIndex = palette.indexOf(state);
                    if (paletteIndex == -1) {
                        palette.add(state);
                        paletteIndex = palette.size() - 1;
                    }

                    CompoundTag blockTag = new CompoundTag();
                    blockTag.put("pos", newIntegerList(x, y, z));
                    blockTag.putInt("state", paletteIndex);
                    blocklist.add(blockTag);
                }
            }
        }

        ListTag paletteTag = new ListTag();
        for (BlockState state : palette) {
            paletteTag.add(NbtUtils.writeBlockState(state));
        }
        rootTag.put("palette", paletteTag);
        rootTag.put("blocks", blocklist);

        rootTag.putInt("DataVersion", SharedConstants.getCurrentVersion().dataVersion().version());
        rootTag.put("size", newIntegerList(width, height, length));

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

}
