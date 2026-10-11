package com.nightjjs.starfall.registry;

import com.nightjjs.starfall.Starfall;
import com.nightjjs.starfall.block.CelestialAltarBlock;
import com.nightjjs.starfall.block.StarCrystalBlock;
import com.nightjjs.starfall.block.StarGateBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.ExperienceDroppingBlock;
import net.minecraft.block.FlowerBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.PillarBlock;
import net.minecraft.block.SaplingBlock;
import net.minecraft.block.SaplingGenerator;
import net.minecraft.block.UntintedParticleLeavesBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.intprovider.UniformIntProvider;

import java.util.Optional;
import java.util.function.Function;

public final class ModBlocks {
    private ModBlocks() {
    }

    /** Grows the astral tree configured feature (data/starfall/worldgen/configured_feature/astral_tree.json). */
    public static final SaplingGenerator ASTRAL_SAPLING_GENERATOR = new SaplingGenerator(
            "starfall:astral", Optional.empty(),
            Optional.of(RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, Starfall.id("astral_tree"))),
            Optional.empty());

    // --- Landed stars (three tiers) ---
    public static final Block FALLEN_STAR_BLOCK = register("fallen_star_block", Block::new, star(MapColor.GOLD));
    public static final Block AZURE_STAR_BLOCK = register("azure_star_block", Block::new, star(MapColor.LIGHT_BLUE));
    public static final Block CRIMSON_STAR_BLOCK = register("crimson_star_block", Block::new, star(MapColor.RED));

    // --- Immortal Realm stone ---
    public static final Block STARSTONE = register("starstone", Block::new, stone());
    public static final Block POLISHED_STARSTONE = register("polished_starstone", Block::new, stone());
    public static final Block STARSTONE_BRICKS = register("starstone_bricks", Block::new, stone());
    public static final Block STARSTONE_BRICK_STAIRS = register("starstone_brick_stairs",
            settings -> new StairsBlock(STARSTONE_BRICKS.getDefaultState(), settings), stone());
    public static final Block STARSTONE_BRICK_SLAB = register("starstone_brick_slab", SlabBlock::new, stone());
    public static final Block CELESTITE_ORE = register("celestite_ore",
            settings -> new ExperienceDroppingBlock(UniformIntProvider.create(2, 5), settings),
            stone().strength(3.5f, 6.0f).luminance(state -> 4));

    // --- Immortal Realm soil and flora ---
    public static final Block ASTRAL_SOIL = register("astral_soil", Block::new, AbstractBlock.Settings.create()
            .mapColor(MapColor.TERRACOTTA_BLUE).strength(0.5f).sounds(BlockSoundGroup.ROOTED_DIRT));
    public static final Block ASTRAL_TURF = register("astral_turf", Block::new, AbstractBlock.Settings.create()
            .mapColor(MapColor.CYAN).strength(0.6f).sounds(BlockSoundGroup.GRASS).luminance(state -> 3));
    public static final Block ASTRAL_LOG = register("astral_log", PillarBlock::new, AbstractBlock.Settings.create()
            .mapColor(MapColor.DEEPSLATE_GRAY).strength(2.0f).sounds(BlockSoundGroup.WOOD).burnable());
    public static final Block ASTRAL_PLANKS = register("astral_planks", Block::new, AbstractBlock.Settings.create()
            .mapColor(MapColor.LIGHT_BLUE_GRAY).strength(2.0f, 3.0f).sounds(BlockSoundGroup.WOOD).burnable());
    public static final Block ASTRAL_LEAVES = register("astral_leaves",
            settings -> new UntintedParticleLeavesBlock(0.02f, ModParticles.STARLIGHT, settings), AbstractBlock.Settings.create()
            .mapColor(MapColor.CYAN).strength(0.2f).ticksRandomly().sounds(BlockSoundGroup.GRASS).nonOpaque().luminance(state -> 6)
            .allowsSpawning((state, world, pos, type) -> false)
            .suffocates((state, world, pos) -> false)
            .blockVision((state, world, pos) -> false)
            .burnable()
            .pistonBehavior(PistonBehavior.DESTROY)
            .solidBlock((state, world, pos) -> false));
    public static final Block ASTRAL_SAPLING = register("astral_sapling",
            settings -> new SaplingBlock(ASTRAL_SAPLING_GENERATOR, settings), AbstractBlock.Settings.create()
            .mapColor(MapColor.CYAN).noCollision().ticksRandomly().breakInstantly().sounds(BlockSoundGroup.GRASS)
            .luminance(state -> 4).pistonBehavior(PistonBehavior.DESTROY));
    public static final Block STARBLOOM = register("starbloom",
            settings -> new FlowerBlock(StatusEffects.GLOWING, 8.0f, settings), AbstractBlock.Settings.create()
            .mapColor(MapColor.YELLOW).noCollision().breakInstantly().sounds(BlockSoundGroup.GRASS)
            .luminance(state -> 8).offset(AbstractBlock.OffsetType.XZ).pistonBehavior(PistonBehavior.DESTROY));
    public static final Block STAR_CRYSTAL = register("star_crystal", StarCrystalBlock::new, AbstractBlock.Settings.create()
            .mapColor(MapColor.GOLD).strength(1.5f).sounds(BlockSoundGroup.AMETHYST_CLUSTER).nonOpaque()
            .luminance(state -> 11).requiresTool().pistonBehavior(PistonBehavior.DESTROY));

    // --- Decoration and devices ---
    public static final Block STAR_LANTERN = register("star_lantern", Block::new, AbstractBlock.Settings.create()
            .mapColor(MapColor.GOLD).strength(0.5f).sounds(BlockSoundGroup.GLASS).luminance(state -> 15));
    public static final Block STARFALL_DUST = register("starfall_dust", Block::new, AbstractBlock.Settings.create()
            .mapColor(MapColor.GOLD).strength(0.5f).sounds(BlockSoundGroup.SAND).luminance(state -> 7));
    public static final Block STAR_GATE = register("star_gate", StarGateBlock::new, AbstractBlock.Settings.create()
            .mapColor(MapColor.LIGHT_BLUE).strength(5.0f, 1200.0f).sounds(BlockSoundGroup.AMETHYST_BLOCK)
            .luminance(state -> 15).requiresTool());
    public static final Block CELESTIAL_ALTAR = register("celestial_altar", CelestialAltarBlock::new, AbstractBlock.Settings.create()
            .mapColor(MapColor.GOLD).strength(5.0f, 1200.0f).sounds(BlockSoundGroup.DEEPSLATE_BRICKS)
            .luminance(state -> 9).requiresTool());

    private static AbstractBlock.Settings star(MapColor color) {
        return AbstractBlock.Settings.create()
                .mapColor(color)
                .strength(3.0f, 9.0f)
                .requiresTool()
                .luminance(state -> 15)
                .sounds(BlockSoundGroup.AMETHYST_BLOCK);
    }

    private static AbstractBlock.Settings stone() {
        return AbstractBlock.Settings.create()
                .mapColor(MapColor.BLUE)
                .strength(2.0f, 6.0f)
                .requiresTool()
                .sounds(BlockSoundGroup.DEEPSLATE);
    }

    private static Block register(String name, Function<AbstractBlock.Settings, Block> factory, AbstractBlock.Settings settings) {
        RegistryKey<Block> blockKey = RegistryKey.of(RegistryKeys.BLOCK, Starfall.id(name));
        Block block = factory.apply(settings.registryKey(blockKey));
        Registry.register(Registries.BLOCK, blockKey, block);

        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Starfall.id(name));
        BlockItem item = new BlockItem(block, new Item.Settings().registryKey(itemKey).useBlockPrefixedTranslationKey());
        Registry.register(Registries.ITEM, itemKey, item);
        return block;
    }

    public static void initialize() {
        // Touching the class triggers static registration.
    }
}
