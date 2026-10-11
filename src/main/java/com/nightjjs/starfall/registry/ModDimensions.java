package com.nightjjs.starfall.registry;

import com.nightjjs.starfall.Starfall;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;

public final class ModDimensions {
    private ModDimensions() {
    }

    /** Defined in data/starfall/dimension/immortal_realm.json. */
    public static final RegistryKey<World> IMMORTAL_REALM = RegistryKey.of(RegistryKeys.WORLD, Starfall.id("immortal_realm"));

    public static boolean isImmortalRealm(World world) {
        return world.getRegistryKey() == IMMORTAL_REALM;
    }
}
