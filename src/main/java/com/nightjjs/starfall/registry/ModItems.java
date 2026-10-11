package com.nightjjs.starfall.registry;

import com.nightjjs.starfall.Starfall;
import com.nightjjs.starfall.item.StarCompassItem;
import com.nightjjs.starfall.item.StarcasterItem;
import com.nightjjs.starfall.item.StarfallScepterItem;
import net.minecraft.block.Block;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.component.type.ConsumableComponents;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.consume.ApplyEffectsConsumeEffect;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.util.Rarity;

import java.util.List;
import java.util.function.Function;

public final class ModItems {
    private ModItems() {
    }

    /** Slightly above netherite: 2400 durability, +5 attack bonus, enchantability 20. */
    public static final ToolMaterial STAR_MATERIAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 2400, 10.0f, 5.0f, 20, ModTags.STAR_TOOL_MATERIALS);

    private static final FoodComponent ASTRAL_FRUIT_FOOD = new FoodComponent.Builder()
            .nutrition(4).saturationModifier(0.6f).alwaysEdible().build();
    private static final ConsumableComponent ASTRAL_FRUIT_CONSUMABLE = ConsumableComponents.food()
            .consumeEffect(new ApplyEffectsConsumeEffect(new StatusEffectInstance(ModEffects.STARLIGHT, 20 * 30, 0)))
            .build();

    // --- Materials ---
    public static final Item STAR_SHARD = register("star_shard", Item::new, new Item.Settings());
    public static final Item STAR_DUST = register("star_dust", Item::new, new Item.Settings());
    public static final Item STAR_CORE = register("star_core", Item::new, new Item.Settings().rarity(Rarity.RARE));
    public static final Item COMET_FRAGMENT = register("comet_fragment", Item::new, new Item.Settings().rarity(Rarity.RARE));
    public static final Item CELESTIAL_ESSENCE = register("celestial_essence", Item::new, new Item.Settings().rarity(Rarity.RARE).fireproof());
    public static final Item GUARDIAN_SIGIL = register("guardian_sigil", Item::new, new Item.Settings()
            .rarity(Rarity.EPIC).fireproof().component(DataComponentTypes.LORE, lore("item.starfall.guardian_sigil.lore")));
    public static final Item ASTRAL_FRUIT = register("astral_fruit", Item::new, new Item.Settings()
            .food(ASTRAL_FRUIT_FOOD, ASTRAL_FRUIT_CONSUMABLE));

    // --- Tools and weapons ---
    public static final Item STARLIGHT_BLADE = register("starlight_blade", Item::new, new Item.Settings()
            .sword(STAR_MATERIAL, 4.0f, -2.2f).rarity(Rarity.EPIC).fireproof()
            .component(DataComponentTypes.LORE, lore("item.starfall.starlight_blade.lore")));
    public static final Item COMET_HAMMER = register("comet_hammer", Item::new, new Item.Settings()
            .sword(STAR_MATERIAL, 9.0f, -3.4f).rarity(Rarity.EPIC).fireproof()
            .component(DataComponentTypes.LORE, lore("item.starfall.comet_hammer.lore")));
    public static final Item STAR_PICKAXE = register("star_pickaxe",
            settings -> new PickaxeItem(STAR_MATERIAL, 1.0f, -2.8f, settings), new Item.Settings().rarity(Rarity.RARE).fireproof());
    public static final Item STAR_AXE = register("star_axe",
            settings -> new AxeItem(STAR_MATERIAL, 6.0f, -3.0f, settings), new Item.Settings().rarity(Rarity.RARE).fireproof());
    public static final Item STARCASTER = register("starcaster", StarcasterItem::new, new Item.Settings()
            .maxCount(1).maxDamage(256).rarity(Rarity.EPIC).fireproof()
            .component(DataComponentTypes.LORE, lore("item.starfall.starcaster.lore")));
    public static final Item STARFALL_SCEPTER = register("starfall_scepter", StarfallScepterItem::new, new Item.Settings()
            .maxCount(1).maxDamage(128).rarity(Rarity.EPIC).fireproof()
            .component(DataComponentTypes.LORE, lore("item.starfall.starfall_scepter.lore")));

    // --- Armor ---
    public static final Item STARFORGED_HELMET = register("starforged_helmet", Item::new, new Item.Settings()
            .armor(ModArmorMaterials.STARFORGED, EquipmentType.HELMET).rarity(Rarity.EPIC).fireproof());
    public static final Item STARFORGED_CHESTPLATE = register("starforged_chestplate", Item::new, new Item.Settings()
            .armor(ModArmorMaterials.STARFORGED, EquipmentType.CHESTPLATE).rarity(Rarity.EPIC).fireproof());
    public static final Item STARFORGED_LEGGINGS = register("starforged_leggings", Item::new, new Item.Settings()
            .armor(ModArmorMaterials.STARFORGED, EquipmentType.LEGGINGS).rarity(Rarity.EPIC).fireproof());
    public static final Item STARFORGED_BOOTS = register("starforged_boots", Item::new, new Item.Settings()
            .armor(ModArmorMaterials.STARFORGED, EquipmentType.BOOTS).rarity(Rarity.EPIC).fireproof());

    // --- Trinkets ---
    /** Three charges; each one cancels a death. Recharge by crafting it with a star core. */
    public static final Item IMMORTAL_HEART = register("immortal_heart", Item::new, new Item.Settings()
            .maxCount(1).maxDamage(3).rarity(Rarity.EPIC).fireproof()
            .component(DataComponentTypes.LORE, lore("item.starfall.immortal_heart.lore")));
    public static final Item STAR_COMPASS = register("star_compass", StarCompassItem::new, new Item.Settings()
            .maxCount(1).component(DataComponentTypes.LORE, lore("item.starfall.star_compass.lore")));

    // --- Spawn eggs ---
    public static final Item STAR_GUARDIAN_SPAWN_EGG = register("star_guardian_spawn_egg",
            settings -> new SpawnEggItem(ModEntities.STAR_GUARDIAN, settings), new Item.Settings());
    public static final Item STAR_WISP_SPAWN_EGG = register("star_wisp_spawn_egg",
            settings -> new SpawnEggItem(ModEntities.STAR_WISP, settings), new Item.Settings());

    private static LoreComponent lore(String key) {
        return new LoreComponent(List.of(Text.translatable(key)));
    }

    private static Item register(String name, Function<Item.Settings, Item> factory, Item.Settings settings) {
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, Starfall.id(name));
        Item item = factory.apply(settings.registryKey(key));
        Registry.register(Registries.ITEM, key, item);
        return item;
    }

    public static boolean isStarforgedArmor(Item item) {
        return item == STARFORGED_HELMET || item == STARFORGED_CHESTPLATE || item == STARFORGED_LEGGINGS || item == STARFORGED_BOOTS;
    }

    public static void initialize() {
        // Touching the class triggers static registration; blocks must be registered first.
        Block ignored = ModBlocks.FALLEN_STAR_BLOCK;
    }
}
