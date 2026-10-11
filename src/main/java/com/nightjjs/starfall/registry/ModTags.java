package com.nightjjs.starfall.registry;

import com.nightjjs.starfall.Starfall;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public final class ModTags {
    private ModTags() {
    }

    /** Items that repair star tools and armor in an anvil. */
    public static final TagKey<Item> STAR_TOOL_MATERIALS = TagKey.of(RegistryKeys.ITEM, Starfall.id("star_tool_materials"));
    /** Every tier of landed star block. */
    public static final TagKey<Block> STAR_BLOCKS = TagKey.of(RegistryKeys.BLOCK, Starfall.id("star_blocks"));
    /** Ground that a star impact can turn into starfall dust. */
    public static final TagKey<Block> IMPACT_SCORCHABLE = TagKey.of(RegistryKeys.BLOCK, Starfall.id("impact_scorchable"));
}
