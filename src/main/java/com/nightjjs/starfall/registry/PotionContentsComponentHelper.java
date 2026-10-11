package com.nightjjs.starfall.registry;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.registry.entry.RegistryEntry;

/** Builds potion item stacks for creative tabs. */
final class PotionContentsComponentHelper {
    private PotionContentsComponentHelper() {
    }

    static ItemStack of(Item item, RegistryEntry<Potion> potion) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(potion));
        return stack;
    }
}
