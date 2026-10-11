package com.nightjjs.starfall.registry;

import com.nightjjs.starfall.Starfall;
import com.nightjjs.starfall.effect.StarlightEffect;
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;

public final class ModEffects {
    private ModEffects() {
    }

    /** Slow regeneration wrapped in drifting starlight. */
    public static final RegistryEntry<StatusEffect> STARLIGHT = Registry.registerReference(
            Registries.STATUS_EFFECT, Starfall.id("starlight"), new StarlightEffect());

    public static final RegistryEntry<Potion> STARLIGHT_POTION = Registry.registerReference(
            Registries.POTION, Starfall.id("starlight"),
            new Potion("starlight", new StatusEffectInstance(STARLIGHT, 20 * 180, 0)));
    public static final RegistryEntry<Potion> LONG_STARLIGHT_POTION = Registry.registerReference(
            Registries.POTION, Starfall.id("long_starlight"),
            new Potion("starlight", new StatusEffectInstance(STARLIGHT, 20 * 480, 0)));
    public static final RegistryEntry<Potion> STRONG_STARLIGHT_POTION = Registry.registerReference(
            Registries.POTION, Starfall.id("strong_starlight"),
            new Potion("starlight", new StatusEffectInstance(STARLIGHT, 20 * 90, 1)));

    public static void initialize() {
        FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> {
            builder.registerPotionRecipe(Potions.AWKWARD, ModItems.STAR_DUST, STARLIGHT_POTION);
            builder.registerPotionRecipe(STARLIGHT_POTION, net.minecraft.item.Items.REDSTONE, LONG_STARLIGHT_POTION);
            builder.registerPotionRecipe(STARLIGHT_POTION, net.minecraft.item.Items.GLOWSTONE_DUST, STRONG_STARLIGHT_POTION);
        });
    }
}
