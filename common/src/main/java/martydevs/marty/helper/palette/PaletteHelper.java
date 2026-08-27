package martydevs.marty.helper.palette;

import martydevs.marty.helper.map.MapColorHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.Shapes;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public final class PaletteHelper {

    private static final class FakeBlockGetter implements BlockGetter {
        BlockState state;

        @Override
        public @Nullable BlockEntity getBlockEntity(@NonNull BlockPos pos) {
            return null;
        }

        @Override
        public @NonNull BlockState getBlockState(@NonNull BlockPos pos) {
            return state;
        }

        @Override
        public @NonNull FluidState getFluidState(@NonNull BlockPos pos) {
            return Fluids.EMPTY.defaultFluidState();
        }

        @Override
        public int getHeight() {
            return 0;
        }

        @Override
        public int getMinY() {
            return 0;
        }
    }

    private static final FakeBlockGetter FAKE_BLOCK_GETTER = new FakeBlockGetter();
    private static final BlockPos FAKE_BLOCK_POS = new BlockPos(0, 0, 0);

    private static boolean isSolid(Block block) {
        BlockState state = block.defaultBlockState();
        FAKE_BLOCK_GETTER.state = state;
        return Shapes.equal(state.getShape(FAKE_BLOCK_GETTER, FAKE_BLOCK_POS), Shapes.block());
    }

    public static final Predicate<Block> POSSIBLE_BLOCK_SEARCH_PREDICATE = block ->
            block instanceof SlabBlock || block instanceof CarpetBlock || block instanceof BasePressurePlateBlock || isSolid(block);

    public static final Set<Identifier>[] POSSIBLE_BLOCKS_FOR_USER = new Set[]{
            as("glass"),
            as("grass_block", "slime_block"),
            as("smooth_sandstone", "birch_planks", "stripped_birch_wood", "bone_block", "stripped_birch_log", "end_stone_bricks", "birch_slab", "glowstone", "birch_log", "smooth_sandstone_slab", "birch_wood", "sandstone", "chiseled_sandstone", "cut_sandstone_slab", "cut_sandstone", "end_stone", "end_stone_brick_slab", "sand", "sandstone_slab", "ochre_froglight", "suspicious_sand", "birch_pressure_plate"),
            as("cobweb", "mushroom_stem"),
            as("redstone_block", "tnt"),
            as("packed_ice", "ice", "blue_ice", "frosted_ice"),
            as("lodestone", "heavy_weighted_pressure_plate", "iron_block", "pale_oak_leaves"),
            as("azalea_leaves", "vine", "birch_leaves", "dark_oak_leaves", "peony", "lilac", "mangrove_leaves", "jungle_leaves", "tall_grass", "pitcher_plant", "firefly_bush", "spruce_leaves", "large_fern", "oak_leaves", "rose_bush", "acacia_leaves", "flowering_azalea_leaves", "sunflower"),
            as("powder_snow", "snow_block", "white_concrete_powder", "white_stained_glass", "white_concrete", "white_carpet", "white_wool", "white_shulker_box", "white_glazed_terracotta"),
            as("infested_cobblestone", "infested_stone", "clay", "infested_cracked_stone_bricks", "infested_mossy_stone_bricks", "infested_chiseled_stone_bricks", "infested_stone_bricks"),
            as("jungle_slab", "rooted_dirt", "stripped_jungle_log", "stripped_jungle_wood", "dirt", "coarse_dirt", "jungle_wood", "jungle_pressure_plate", "polished_granite_slab", "granite_slab", "polished_granite", "granite", "packed_mud", "jungle_log", "jukebox", "jungle_planks", "brown_mushroom_block"),
            as("chiseled_stone_bricks", "polished_andesite", "emerald_ore", "observer", "andesite", "coal_ore", "stone", "stone_pressure_plate", "smooth_stone", "redstone_ore", "cobblestone_slab", "crafter", "smoker", "diamond_ore", "mossy_cobblestone_slab", "cobblestone", "bedrock", "polished_andesite_slab", "spawner", "iron_ore", "copper_ore", "cracked_stone_bricks", "furnace", "pale_oak_wood", "andesite_slab", "mossy_cobblestone", "suspicious_gravel", "sticky_piston", "mossy_stone_bricks", "blast_furnace", "stone_bricks", "trial_spawner", "stone_slab", "dropper", "vault", "dispenser", "smooth_stone_slab", "stone_brick_slab", "gravel", "lapis_ore", "mossy_stone_brick_slab", "gold_ore", "piston"),
            as("water"),
            as("oak_planks", "cartography_table", "fletching_table", "oak_pressure_plate", "bookshelf", "beehive", "petrified_oak_slab", "chiseled_bookshelf", "loom", "barrel", "stripped_oak_log", "smithing_table", "stripped_oak_wood", "oak_wood", "crafting_table", "oak_slab", "note_block", "oak_log"),
            as("smooth_quartz_slab", "quartz_slab", "pale_oak_log", "sea_lantern", "quartz_pillar", "quartz_bricks", "polished_diorite_slab", "pale_oak_pressure_plate", "polished_diorite", "diorite_slab", "stripped_pale_oak_log", "stripped_pale_oak_wood", "quartz_block", "diorite", "smooth_quartz", "pale_oak_planks", "pale_oak_slab", "target", "chiseled_quartz_block"),
            as("stripped_acacia_log", "cut_red_sandstone_slab", "waxed_chiseled_copper", "acacia_pressure_plate", "acacia_slab", "terracotta", "cut_copper_slab", "smooth_red_sandstone", "red_sand", "orange_wool", "honey_block", "copper_block", "cut_red_sandstone", "orange_stained_glass", "copper_bulb", "smooth_red_sandstone_slab", "honeycomb_block", "stripped_acacia_wood", "waxed_copper_bulb", "orange_concrete_powder", "jack_o_lantern", "red_sandstone_slab", "waxed_copper_block", "chiseled_red_sandstone", "orange_shulker_box", "orange_carpet", "pumpkin", "raw_copper_block", "waxed_cut_copper", "creaking_heart", "orange_glazed_terracotta", "carved_pumpkin", "orange_concrete", "acacia_log", "acacia_planks", "cut_copper", "chiseled_copper", "waxed_copper_grate", "copper_grate", "waxed_cut_copper_slab", "red_sandstone"),
            as("magenta_glazed_terracotta", "magenta_wool", "magenta_concrete_powder", "purpur_slab", "magenta_stained_glass", "purpur_pillar", "magenta_concrete", "magenta_shulker_box", "magenta_carpet", "purpur_block"),
            as("light_blue_stained_glass", "light_blue_glazed_terracotta", "light_blue_concrete_powder", "light_blue_wool", "light_blue_shulker_box", "light_blue_concrete", "light_blue_carpet"),
            as("wet_sponge", "yellow_stained_glass", "bamboo_mosaic", "sulfur_slab", "bamboo_planks", "horn_coral_block", "stripped_bamboo_block", "bee_nest", "yellow_carpet", "yellow_concrete_powder", "yellow_wool", "bamboo_slab", "polished_sulfur", "yellow_shulker_box", "sulfur_bricks", "sponge", "bamboo_pressure_plate", "bamboo_mosaic_slab", "sulfur_brick_slab", "polished_sulfur_slab", "hay_block", "yellow_glazed_terracotta", "bamboo_block", "yellow_concrete", "chiseled_sulfur", "sulfur"),
            as("lime_shulker_box", "lime_glazed_terracotta", "melon", "lime_concrete_powder", "lime_wool", "lime_carpet", "lime_concrete", "lime_stained_glass"),
            as("brain_coral_block", "cherry_leaves", "pink_concrete", "pearlescent_froglight", "pink_shulker_box", "pink_wool", "pink_stained_glass", "pink_glazed_terracotta", "pink_concrete_powder", "pink_carpet"),
            as("gray_carpet", "dead_horn_coral_block", "dead_fire_coral_block", "gray_stained_glass", "dead_bubble_coral_block", "gray_shulker_box", "gray_concrete", "tinted_glass", "gray_concrete_powder", "acacia_wood", "gray_wool", "dead_tube_coral_block", "dead_brain_coral_block", "gray_glazed_terracotta"),
            as("light_gray_concrete", "light_gray_shulker_box", "jigsaw", "light_gray_carpet", "light_gray_stained_glass", "test_block", "light_gray_concrete_powder", "light_gray_glazed_terracotta", "structure_block", "pale_moss_block", "light_gray_wool"),
            as("cyan_glazed_terracotta", "cyan_wool", "cyan_shulker_box", "cyan_concrete_powder", "cyan_carpet", "prismarine", "cyan_stained_glass", "cyan_concrete", "prismarine_slab"),
            as("purple_concrete", "mycelium", "budding_amethyst", "purple_concrete_powder", "chorus_flower", "purple_stained_glass", "purple_glazed_terracotta", "bubble_coral_block", "purple_wool", "purple_carpet", "repeating_command_block", "shulker_box", "amethyst_block"),
            as("blue_glazed_terracotta", "blue_concrete", "blue_wool", "blue_stained_glass", "blue_carpet", "blue_concrete_powder", "tube_coral_block", "blue_shulker_box"),
            as("soul_soil", "stripped_dark_oak_wood", "brown_stained_glass", "soul_sand", "brown_shulker_box", "dark_oak_slab", "dark_oak_pressure_plate", "brown_concrete", "brown_carpet", "dark_oak_log", "command_block", "stripped_dark_oak_log", "brown_concrete_powder", "brown_wool", "dark_oak_planks", "dark_oak_wood", "brown_glazed_terracotta"),
            as("green_concrete_powder", "green_stained_glass", "dried_kelp_block", "green_shulker_box", "green_carpet", "moss_block", "moss_carpet", "green_concrete", "chain_command_block", "green_wool", "green_glazed_terracotta"),
            as("cinnabar_slab", "cinnabar_bricks", "chiseled_cinnabar", "red_stained_glass", "nether_wart_block", "cinnabar_brick_slab", "polished_cinnabar_slab", "red_concrete", "fire_coral_block", "brick_slab", "red_carpet", "polished_cinnabar", "red_shulker_box", "mangrove_planks", "stripped_mangrove_wood", "mangrove_wood", "red_wool", "mangrove_pressure_plate", "mangrove_log", "bricks", "red_mushroom_block", "red_concrete_powder", "mangrove_slab", "stripped_mangrove_log", "cinnabar", "shroomlight", "red_glazed_terracotta"),
            as("end_gateway", "black_carpet", "crying_obsidian", "blackstone_slab", "polished_blackstone_brick_slab", "gilded_blackstone", "polished_blackstone_pressure_plate", "netherite_block", "blackstone", "ancient_debris", "polished_blackstone_slab", "black_wool", "black_shulker_box", "respawn_anchor", "cracked_polished_blackstone_bricks", "black_concrete", "sculk_vein", "basalt", "sculk", "polished_blackstone_bricks", "sculk_catalyst", "coal_block", "black_concrete_powder", "obsidian", "sculk_shrieker", "smooth_basalt", "black_stained_glass", "polished_basalt", "polished_blackstone", "black_glazed_terracotta", "chiseled_polished_blackstone"),
            as("raw_gold_block", "light_weighted_pressure_plate", "potent_sulfur", "gold_block"),
            as("dark_prismarine_slab", "beacon", "prismarine_brick_slab", "diamond_block", "prismarine_bricks", "dark_prismarine"),
            as("lapis_block"),
            as("emerald_block"),
            as("mangrove_roots", "muddy_mangrove_roots", "spruce_log", "stripped_spruce_log", "spruce_slab", "stripped_spruce_wood", "podzol", "spruce_planks", "spruce_pressure_plate", "spruce_wood"),
            as("chiseled_nether_bricks", "nether_quartz_ore", "red_nether_bricks", "netherrack", "nether_bricks", "red_nether_brick_slab", "magma_block", "nether_gold_ore", "cracked_nether_bricks", "nether_brick_slab"),
            as("cherry_planks", "cherry_log", "cherry_slab", "calcite", "white_terracotta", "cherry_pressure_plate", "stripped_cherry_log"),
            as("redstone_lamp", "orange_terracotta", "chiseled_resin_bricks", "resin_clump", "resin_brick_slab", "resin_bricks", "resin_block"),
            as("magenta_terracotta"),
            as("light_blue_terracotta"),
            as("yellow_terracotta"),
            as("lime_terracotta"),
            as("pink_terracotta", "stripped_cherry_wood"),
            as("tuff", "gray_terracotta", "polished_tuff", "tuff_bricks", "tuff_slab", "chiseled_tuff", "chiseled_tuff_bricks", "polished_tuff_slab", "cherry_wood", "tuff_brick_slab"),
            as("exposed_cut_copper_slab", "exposed_copper_bulb", "waxed_exposed_cut_copper", "mud_brick_slab", "waxed_exposed_cut_copper_slab", "exposed_cut_copper", "exposed_copper_grate", "waxed_exposed_copper_bulb", "waxed_exposed_copper_grate", "waxed_exposed_chiseled_copper", "light_gray_terracotta", "exposed_chiseled_copper", "exposed_copper", "waxed_exposed_copper", "mud_bricks"),
            as("mud", "cyan_terracotta"),
            as("purple_terracotta", "purple_shulker_box"),
            as("blue_terracotta"),
            as("dripstone_block", "brown_terracotta"),
            as("green_terracotta"),
            as("red_terracotta"),
            as("black_terracotta"),
            as("crimson_nylium"),
            as("crimson_stem", "crimson_planks", "crimson_slab", "stripped_crimson_stem", "crimson_pressure_plate"),
            as("stripped_crimson_hyphae", "crimson_hyphae"),
            as("oxidized_copper_grate", "oxidized_chiseled_copper", "oxidized_copper_bulb", "waxed_oxidized_copper", "warped_nylium", "oxidized_cut_copper", "waxed_oxidized_copper_bulb", "waxed_oxidized_cut_copper_slab", "waxed_oxidized_cut_copper", "oxidized_copper", "oxidized_cut_copper_slab", "waxed_oxidized_chiseled_copper", "waxed_oxidized_copper_grate"),
            as("stripped_warped_stem", "waxed_weathered_cut_copper", "weathered_chiseled_copper", "warped_slab", "waxed_weathered_cut_copper_slab", "weathered_copper_grate", "weathered_cut_copper_slab", "weathered_copper_bulb", "warped_stem", "warped_pressure_plate", "waxed_weathered_copper", "weathered_cut_copper", "warped_planks", "waxed_weathered_copper_grate", "waxed_weathered_chiseled_copper", "weathered_copper", "waxed_weathered_copper_bulb"),
            as("warped_hyphae", "stripped_warped_hyphae"),
            as("warped_wart_block"),
            as("deepslate_iron_ore", "infested_deepslate", "reinforced_deepslate", "polished_deepslate", "cobbled_deepslate_slab", "deepslate", "deepslate_emerald_ore", "deepslate_tile_slab", "chiseled_deepslate", "deepslate_tiles", "deepslate_bricks", "deepslate_redstone_ore", "deepslate_lapis_ore", "deepslate_brick_slab", "cracked_deepslate_bricks", "deepslate_coal_ore", "cracked_deepslate_tiles", "polished_deepslate_slab", "deepslate_copper_ore", "deepslate_gold_ore", "cobbled_deepslate", "deepslate_diamond_ore"),
            as("raw_iron_block"),
            as("glow_lichen", "verdant_froglight"),
            as(),
            as()
    };

    static {
        if(POSSIBLE_BLOCKS_FOR_USER.length != MapColorHelper.MATERIAL_COLORS.length)
            throw new ExceptionInInitializerError("Declared POSSIBLE_BLOCKS array length does not equals MapColor.MATERIAL_COLORS.length.");
    }

    private static Set<Identifier> as(String... blocks) {
        if(blocks.length == 0) return Set.of();
        Set<Identifier> blocksIdentifiers = new HashSet<>();
        for (String block : blocks) {
            blocksIdentifiers.add(Identifier.parse(block));
        }
        return blocksIdentifiers;
    }

}
