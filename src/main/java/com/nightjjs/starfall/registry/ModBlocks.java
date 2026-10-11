package com.nightjjs.starfall.registry;

import com.nightjjs.starfall.Starfall;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;

import java.util.function.Function;

public final class ModBlocks {
    private ModBlocks() {
    }

    /** A star that has landed: glows at full brightness and drops star shards when mined. */
    public static final Block FALLEN_STAR_BLOCK = register(
            "fallen_star_block",
            Block::new,
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.GOLD)
                    .strength(3.0f, 9.0f)
                    .requiresTool()
                    .luminance(state -> 15)
                    .sounds(BlockSoundGroup.AMETHYST_BLOCK)
    );

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
