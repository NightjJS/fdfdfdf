package com.nightjjs.starfall.registry;

import com.nightjjs.starfall.Starfall;
import com.nightjjs.starfall.item.StarcasterItem;
import net.minecraft.block.Block;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Rarity;

import java.util.List;
import java.util.function.Function;

public final class ModItems {
    private ModItems() {
    }

    /** Items that can repair star tools in an anvil (see data/starfall/tags/item/star_tool_materials.json). */
    public static final TagKey<Item> STAR_TOOL_MATERIALS = TagKey.of(RegistryKeys.ITEM, Starfall.id("star_tool_materials"));

    /** Slightly above netherite: 2400 durability, +5 attack bonus, enchantability 20. */
    public static final ToolMaterial STAR_MATERIAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 2400, 10.0f, 5.0f, 20, STAR_TOOL_MATERIALS);

    public static final Item STAR_SHARD = register("star_shard", Item::new, new Item.Settings());

    public static final Item STAR_CORE = register("star_core", Item::new, new Item.Settings()
            .rarity(Rarity.RARE));

    public static final Item STARLIGHT_BLADE = register("starlight_blade", Item::new, new Item.Settings()
            .sword(STAR_MATERIAL, 4.0f, -2.2f)
            .rarity(Rarity.EPIC)
            .fireproof()
            .component(DataComponentTypes.LORE, lore("item.starfall.starlight_blade.lore")));

    public static final Item STARCASTER = register("starcaster", StarcasterItem::new, new Item.Settings()
            .maxCount(1)
            .maxDamage(256)
            .rarity(Rarity.EPIC)
            .fireproof()
            .component(DataComponentTypes.LORE, lore("item.starfall.starcaster.lore")));

    /** Three charges; each one cancels a death. Recharge by crafting it with a star core. */
    public static final Item IMMORTAL_HEART = register("immortal_heart", Item::new, new Item.Settings()
            .maxCount(1)
            .maxDamage(3)
            .rarity(Rarity.EPIC)
            .fireproof()
            .component(DataComponentTypes.LORE, lore("item.starfall.immortal_heart.lore")));

    private static LoreComponent lore(String key) {
        return new LoreComponent(List.of(Text.translatable(key)));
    }

    private static Item register(String name, Function<Item.Settings, Item> factory, Item.Settings settings) {
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, Starfall.id(name));
        Item item = factory.apply(settings.registryKey(key));
        Registry.register(Registries.ITEM, key, item);
        return item;
    }

    public static void initialize() {
        // Touching the class triggers static registration; blocks must be registered first.
        Block ignored = ModBlocks.FALLEN_STAR_BLOCK;
    }
}
