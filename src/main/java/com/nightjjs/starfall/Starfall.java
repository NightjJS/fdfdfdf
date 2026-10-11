package com.nightjjs.starfall;

import com.nightjjs.starfall.entity.StarBoltEntity;
import com.nightjjs.starfall.event.ImmortalHeartHandler;
import com.nightjjs.starfall.event.ShootingStarManager;
import com.nightjjs.starfall.event.StarfallCommands;
import com.nightjjs.starfall.event.StarlightBladeHandler;
import com.nightjjs.starfall.registry.ModBlocks;
import com.nightjjs.starfall.registry.ModItemGroups;
import com.nightjjs.starfall.registry.ModItems;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Starfall: shooting stars fall from the night sky, leaving glowing star blocks behind.
 * Mine them for star shards and forge star-powered gear.
 */
public class Starfall implements ModInitializer {
    public static final String MOD_ID = "starfall";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        ModBlocks.initialize();
        ModItems.initialize();
        ModItemGroups.initialize();

        ShootingStarManager.register();
        ImmortalHeartHandler.register();
        StarlightBladeHandler.register();
        StarBoltEntity.registerEvents();
        StarfallCommands.register();

        LOGGER.info("Starfall loaded: watch the night sky.");
    }
}
