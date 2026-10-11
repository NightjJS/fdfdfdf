package com.nightjjs.starfall;

import com.nightjjs.starfall.entity.StarBoltEntity;
import com.nightjjs.starfall.event.ImmortalHeartHandler;
import com.nightjjs.starfall.event.MeteorShowerManager;
import com.nightjjs.starfall.event.ShootingStarManager;
import com.nightjjs.starfall.event.StarfallCommands;
import com.nightjjs.starfall.event.StarforgedArmorHandler;
import com.nightjjs.starfall.event.WeaponEffectsHandler;
import com.nightjjs.starfall.event.WispSpawner;
import com.nightjjs.starfall.registry.ModBlocks;
import com.nightjjs.starfall.registry.ModEffects;
import com.nightjjs.starfall.registry.ModEntities;
import com.nightjjs.starfall.registry.ModFeatures;
import com.nightjjs.starfall.registry.ModItemGroups;
import com.nightjjs.starfall.registry.ModItems;
import com.nightjjs.starfall.registry.ModParticles;
import com.nightjjs.starfall.registry.ModSounds;
import com.nightjjs.starfall.selftest.StarfallSelfTest;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Starfall: shooting stars, meteor showers, the Star Guardian and the Immortal Realm.
 */
public class Starfall implements ModInitializer {
    public static final String MOD_ID = "starfall";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        ModSounds.initialize();
        ModParticles.initialize();
        ModBlocks.initialize();
        ModFeatures.initialize();
        ModEntities.initialize();
        ModItems.initialize();
        ModEffects.initialize();
        ModItemGroups.initialize();

        ShootingStarManager.register();
        MeteorShowerManager.register();
        WispSpawner.register();
        ImmortalHeartHandler.register();
        WeaponEffectsHandler.register();
        StarforgedArmorHandler.register();
        StarBoltEntity.registerEvents();
        StarfallCommands.register();

        if (Boolean.getBoolean("starfall.selftest")) {
            StarfallSelfTest.register();
        }

        LOGGER.info("Starfall loaded: watch the night sky.");
    }
}
