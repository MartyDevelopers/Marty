package martydevs.marty.processor.schematic;

import martydevs.marty.model.image.MapArtImage;
import martydevs.marty.model.schematic.NBTSchematicProcessor;
import martydevs.marty.model.schematic.NBTSchematicWork;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.joml.Vector2ic;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.zip.GZIPOutputStream;

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
        Vector2ic dimensionsInMaps = work.source().dimensionsInMaps();
        int width = MAP_SIZE * dimensionsInMaps.x();
        int height = 1;
        int length = MAP_SIZE * dimensionsInMaps.y();
        MapArtImage.Map[][] maps = work.source().maps();
        if(maps[0][0] instanceof MapArtImage.FlatMap) {
            for (MapArtImage.Map[] row : maps) {
                for (MapArtImage.Map map : row) {
                    MapArtImage.FlatMap flatMap = (MapArtImage.FlatMap) map;
                    int mapHeight = flatMap.blockStates()[0].length;
                    if (mapHeight > height) height = mapHeight;
                }
            }
        }
        else {
            // staircase maps
            throw new UnsupportedOperationException();
        }

        CompoundTag rootTag = new CompoundTag();
        List<BlockState> palette = new ArrayList<>();

        ListTag blocklist = new ListTag();
        for (int y = 0; y < height; y++) {
            for (int z = 0; z < length; z++) {
                int localZ = z % MAP_SIZE;
                int mapY = (z - localZ) / MAP_SIZE;
                for (int x = 0; x < length; x++) {
                    int localX = x % MAP_SIZE;
                    int mapX = (x - localX) / MAP_SIZE;

                    BlockState state = (switch (maps[mapX][mapY]) {
                        case MapArtImage.FlatMap flatMap -> flatMap.blockStates();
                        default -> throw new IllegalStateException("Unexpected value: " + maps[mapX][mapY]);
                    })[localX][y][localZ];

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
