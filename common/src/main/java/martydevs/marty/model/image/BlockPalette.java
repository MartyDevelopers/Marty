package martydevs.marty.model.image;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Unmodifiable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public interface BlockPalette {

    static BlockPalette createPalette(Set<Block> blocks) {
        Map<MapColor, Set<Block>> palette = new HashMap<>();
        for (Block block : blocks) {
            palette.computeIfAbsent(
                    block.defaultMapColor(),
                    (_) -> new HashSet<>()
            ).add(block);
        }
        return new BlockPaletteImpl(palette);
    }

    @Unmodifiable
    Set<MapColor> allColors();

    @Unmodifiable
    Set<Block> allBlocks();

    @Unmodifiable
    Set<Block> blocksPerColor(MapColor color);

    class BlockPaletteImpl implements BlockPalette {

        private final Map<MapColor, Set<Block>> palette;
        private final Set<Block> allBlocks;

        BlockPaletteImpl(Map<MapColor, Set<Block>> palette) {
            @SuppressWarnings("unchecked") Map.Entry<MapColor, Set<Block>>[] entries = new Map.Entry[palette.size()];

            var iterator = palette.entrySet().iterator();
            int index = 0;
            while (iterator.hasNext()) {
                var entry = iterator.next();
                entries[index++] = Map.entry(entry.getKey(), Set.copyOf(entry.getValue()));
            }

            this.palette = Map.ofEntries(entries);

            Set<Block> blocks = new HashSet<>();
            for (Set<Block> subSet : this.palette.values()) {
                blocks.addAll(subSet);
            }
            this.allBlocks = Set.copyOf(blocks);
        }

        @Override
        public Set<MapColor> allColors() {
            return palette.keySet();
        }

        @Override
        public Set<Block> allBlocks() {
            return allBlocks;
        }

        @Override
        public Set<Block> blocksPerColor(MapColor color) {
            return palette.getOrDefault(color, Set.of());
        }

    }

}
