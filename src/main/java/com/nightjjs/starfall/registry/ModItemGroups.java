package com.nightjjs.starfall.registry;

import com.nightjjs.starfall.Starfall;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
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
                    entries.add(ModItems.STAR_CORE);
                    entries.add(ModBlocks.FALLEN_STAR_BLOCK);
                    entries.add(ModItems.STARLIGHT_BLADE);
                    entries.add(ModItems.STARCASTER);
                    entries.add(ModItems.IMMORTAL_HEART);
                })
                .build());

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
            entries.add(ModItems.STAR_SHARD);
            entries.add(ModItems.STAR_CORE);
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
            entries.add(ModItems.STARLIGHT_BLADE);
            entries.add(ModItems.STARCASTER);
            entries.add(ModItems.IMMORTAL_HEART);
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL).register(entries ->
                entries.add(ModBlocks.FALLEN_STAR_BLOCK));
    }
}
