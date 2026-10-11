package com.nightjjs.starfall.registry;

import com.nightjjs.starfall.Starfall;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModParticles {
    private ModParticles() {
    }

    /** A drifting, twinkling starlight mote (client factory in StarlightParticle). */
    public static final SimpleParticleType STARLIGHT = Registry.register(
            Registries.PARTICLE_TYPE, Starfall.id("starlight"), FabricParticleTypes.simple());

    public static void initialize() {
        // Static registration.
    }
}
