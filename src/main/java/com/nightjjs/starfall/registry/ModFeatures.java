package com.nightjjs.starfall.registry;

import com.nightjjs.starfall.Starfall;
import com.nightjjs.starfall.worldgen.ObservatoryFeature;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;

public final class ModFeatures {
    private ModFeatures() {
    }

    /** Configured in data/starfall/worldgen/configured_feature/observatory.json. */
    public static final Feature<DefaultFeatureConfig> OBSERVATORY = Registry.register(
            Registries.FEATURE, Starfall.id("observatory"), new ObservatoryFeature(DefaultFeatureConfig.CODEC));

    public static void initialize() {
        // Static registration.
    }
}
