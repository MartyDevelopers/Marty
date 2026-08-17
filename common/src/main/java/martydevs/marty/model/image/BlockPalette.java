package martydevs.marty.model.image;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;

import java.util.*;

public interface BlockPalette {

    static BlockPalette createPalette(Set<Block> blocks) {
        Map<MapColor, Block> palette = new HashMap<>();
        for (Block block : blocks) {
            palette.put(block.defaultMapColor(), block);
        }
        return new BlockPaletteImpl(palette);
    }

    @Unmodifiable
    Set<MapColor> allColors();

    @Unmodifiable
    Block blockForColor(MapColor color);

    @Unmodifiable
    Collection<Block> allBlocks();

    class BlockPaletteImpl implements BlockPalette {

        private final Map<MapColor, Block> palette;

        BlockPaletteImpl(Map<MapColor, Block> palette) {
            this.palette = Map.copyOf(palette);
        }

        @Override
        public Set<MapColor> allColors() {
            return palette.keySet();
        }

        @Override
        public @Nullable Block blockForColor(MapColor color) {
            return palette.get(color);
        }

        @Override
        public Collection<Block> allBlocks() {
            return palette.values();
        }

    }

}
