package com.nightjjs.starfall.registry;

import com.nightjjs.starfall.Starfall;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;

public final class ModItemGroups {
    private ModItemGroups() {
    }

    public static final RegistryKey<ItemGroup> STARFALL = RegistryKey.of(RegistryKeys.ITEM_GROUP, Starfall.id("starfall"));

    public static void initialize() {
        Registry.register(Registries.ITEM_GROUP, STARFALL, FabricItemGroup.builder()
                .icon(() -> new ItemStack(ModItems.STAR_CORE))
                .displayName(Text.translatable("itemGroup.starfall"))
                .entries((context, entries) -> {
                    entries.add(ModItems.STAR_SHARD);
                    entries.add(ModItems.STAR_DUST);
                    entries.add(ModItems.STAR_CORE);
                    entries.add(ModItems.COMET_FRAGMENT);
                    entries.add(ModItems.CELESTIAL_ESSENCE);
                    entries.add(ModItems.GUARDIAN_SIGIL);
                    entries.add(ModItems.ASTRAL_FRUIT);
                    entries.add(ModItems.STARLIGHT_BLADE);
                    entries.add(ModItems.COMET_HAMMER);
                    entries.add(ModItems.STAR_PICKAXE);
                    entries.add(ModItems.STAR_AXE);
                    entries.add(ModItems.STARCASTER);
                    entries.add(ModItems.STARFALL_SCEPTER);
                    entries.add(ModItems.STARFORGED_HELMET);
                    entries.add(ModItems.STARFORGED_CHESTPLATE);
                    entries.add(ModItems.STARFORGED_LEGGINGS);
                    entries.add(ModItems.STARFORGED_BOOTS);
                    entries.add(ModItems.IMMORTAL_HEART);
                    entries.add(ModItems.STAR_COMPASS);
                    entries.add(potion(ModEffects.STARLIGHT_POTION));
                    entries.add(potion(ModEffects.LONG_STARLIGHT_POTION));
                    entries.add(potion(ModEffects.STRONG_STARLIGHT_POTION));
                    entries.add(ModBlocks.FALLEN_STAR_BLOCK);
                    entries.add(ModBlocks.AZURE_STAR_BLOCK);
                    entries.add(ModBlocks.CRIMSON_STAR_BLOCK);
                    entries.add(ModBlocks.STARFALL_DUST);
                    entries.add(ModBlocks.STARSTONE);
                    entries.add(ModBlocks.POLISHED_STARSTONE);
                    entries.add(ModBlocks.STARSTONE_BRICKS);
                    entries.add(ModBlocks.STARSTONE_BRICK_STAIRS);
                    entries.add(ModBlocks.STARSTONE_BRICK_SLAB);
                    entries.add(ModBlocks.CELESTITE_ORE);
                    entries.add(ModBlocks.ASTRAL_SOIL);
                    entries.add(ModBlocks.ASTRAL_TURF);
                    entries.add(ModBlocks.ASTRAL_LOG);
                    entries.add(ModBlocks.ASTRAL_PLANKS);
                    entries.add(ModBlocks.ASTRAL_LEAVES);
                    entries.add(ModBlocks.STARBLOOM);
                    entries.add(ModBlocks.STAR_CRYSTAL);
                    entries.add(ModBlocks.STAR_LANTERN);
                    entries.add(ModBlocks.CELESTIAL_ALTAR);
                    entries.add(ModBlocks.STAR_GATE);
                    entries.add(ModItems.STAR_GUARDIAN_SPAWN_EGG);
                    entries.add(ModItems.STAR_WISP_SPAWN_EGG);
                })
                .build());

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
            entries.add(ModItems.STAR_SHARD);
            entries.add(ModItems.STAR_DUST);
            entries.add(ModItems.STAR_CORE);
            entries.add(ModItems.COMET_FRAGMENT);
            entries.add(ModItems.CELESTIAL_ESSENCE);
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
            entries.add(ModItems.STARLIGHT_BLADE);
            entries.add(ModItems.COMET_HAMMER);
            entries.add(ModItems.STARCASTER);
            entries.add(ModItems.STARFALL_SCEPTER);
            entries.add(ModItems.STARFORGED_HELMET);
            entries.add(ModItems.STARFORGED_CHESTPLATE);
            entries.add(ModItems.STARFORGED_LEGGINGS);
            entries.add(ModItems.STARFORGED_BOOTS);
            entries.add(ModItems.IMMORTAL_HEART);
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.add(ModItems.STAR_PICKAXE);
            entries.add(ModItems.STAR_AXE);
            entries.add(ModItems.STAR_COMPASS);
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(entries -> {
            entries.add(ModItems.STAR_GUARDIAN_SPAWN_EGG);
            entries.add(ModItems.STAR_WISP_SPAWN_EGG);
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL).register(entries -> {
            entries.add(ModBlocks.FALLEN_STAR_BLOCK);
            entries.add(ModBlocks.AZURE_STAR_BLOCK);
            entries.add(ModBlocks.CRIMSON_STAR_BLOCK);
            entries.add(ModBlocks.STARSTONE);
            entries.add(ModBlocks.CELESTITE_ORE);
            entries.add(ModBlocks.ASTRAL_SOIL);
            entries.add(ModBlocks.ASTRAL_TURF);
            entries.add(ModBlocks.ASTRAL_LOG);
            entries.add(ModBlocks.ASTRAL_LEAVES);
            entries.add(ModBlocks.STARBLOOM);
            entries.add(ModBlocks.STAR_CRYSTAL);
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(entries -> {
            entries.add(ModBlocks.POLISHED_STARSTONE);
            entries.add(ModBlocks.STARSTONE_BRICKS);
            entries.add(ModBlocks.STARSTONE_BRICK_STAIRS);
            entries.add(ModBlocks.STARSTONE_BRICK_SLAB);
            entries.add(ModBlocks.ASTRAL_PLANKS);
            entries.add(ModBlocks.STAR_LANTERN);
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> {
            entries.add(ModBlocks.STAR_LANTERN);
            entries.add(ModBlocks.CELESTIAL_ALTAR);
            entries.add(ModBlocks.STAR_GATE);
        });
    }

    private static ItemStack potion(RegistryEntry<Potion> potion) {
        return PotionContentsComponentHelper.of(Items.POTION, potion);
    }
}
