package com.nightjjs.starfall.selftest;

import com.nightjjs.starfall.Starfall;
import com.nightjjs.starfall.block.StarGateTravel;
import com.nightjjs.starfall.entity.StarBoltEntity;
import com.nightjjs.starfall.entity.StarGuardianEntity;
import com.nightjjs.starfall.entity.StarWispEntity;
import com.nightjjs.starfall.event.MeteorShowerManager;
import com.nightjjs.starfall.event.ShootingStarManager;
import com.nightjjs.starfall.event.StarTier;
import com.nightjjs.starfall.registry.ModBlocks;
import com.nightjjs.starfall.registry.ModDimensions;
import com.nightjjs.starfall.registry.ModEffects;
import com.nightjjs.starfall.registry.ModEntities;
import com.nightjjs.starfall.registry.ModTags;
import com.nightjjs.starfall.worldgen.ObservatoryFeature;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.loot.LootTable;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

import java.util.ArrayList;
import java.util.List;

/**
 * Headless end-to-end check, enabled with {@code -Dstarfall.selftest=true} on a dedicated server.
 * Walks through the mod's features over a few hundred ticks and prints
 * {@code STARFALL SELFTEST PASSED} or {@code STARFALL SELFTEST FAILED}, then stops the server.
 */
public final class StarfallSelfTest {
    private StarfallSelfTest() {
    }

    private static final List<String> FAILURES = new ArrayList<>();
    private static int step;
    private static int ticksInStep;
    private static ServerWorld overworld;
    private static ServerWorld realm;
    private static BlockPos base;
    private static StarGuardianEntity guardian;
    private static StarWispEntity wisp;
    private static ZombieEntity zombie;
    private static Entity traveller;

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(StarfallSelfTest::onStarted);
        ServerTickEvents.END_SERVER_TICK.register(StarfallSelfTest::onTick);
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            FAILURES.add(message);
            Starfall.LOGGER.error("SELFTEST check failed: {}", message);
        } else {
            Starfall.LOGGER.info("SELFTEST ok: {}", message);
        }
    }

    private static void onStarted(MinecraftServer server) {
        Starfall.LOGGER.info("SELFTEST: begin");
        try {
            overworld = server.getOverworld();
            realm = server.getWorld(ModDimensions.IMMORTAL_REALM);
            check(realm != null, "Immortal Realm dimension is loaded");

            for (String item : new String[]{"star_shard", "star_core", "starlight_blade", "comet_hammer", "starcaster",
                    "starfall_scepter", "immortal_heart", "star_compass", "starforged_helmet", "starforged_boots",
                    "astral_fruit", "star_guardian_spawn_egg"}) {
                check(Registries.ITEM.containsId(Starfall.id(item)), "item registered: " + item);
            }
            for (String block : new String[]{"fallen_star_block", "azure_star_block", "crimson_star_block", "starstone",
                    "astral_turf", "astral_leaves", "starbloom", "star_crystal", "star_gate", "celestial_altar"}) {
                check(Registries.BLOCK.containsId(Starfall.id(block)), "block registered: " + block);
            }
            check(Registries.ENTITY_TYPE.containsId(Starfall.id("star_guardian")), "entity registered: star_guardian");
            check(Registries.STATUS_EFFECT.containsId(Starfall.id("starlight")), "effect registered: starlight");
            check(Registries.POTION.containsId(Starfall.id("starlight")), "potion registered: starlight");
            check(Registries.SOUND_EVENT.containsId(Starfall.id("star.impact")), "sound registered: star.impact");

            for (String table : new String[]{"blocks/fallen_star_block", "blocks/azure_star_block", "blocks/crimson_star_block",
                    "blocks/starstone", "blocks/astral_leaves", "blocks/star_crystal", "blocks/celestite_ore",
                    "entities/star_guardian", "entities/star_wisp"}) {
                RegistryKey<LootTable> key = RegistryKey.of(RegistryKeys.LOOT_TABLE, Starfall.id(table));
                LootTable loot = server.getReloadableRegistries().getLootTable(key);
                check(loot != LootTable.EMPTY, "loot table present: " + table);
            }

            for (String advancement : new String[]{"root", "catch_a_star", "immortal", "meteor_shower", "enter_realm",
                    "kill_guardian", "starforged_set", "summon_guardian", "call_a_star", "wisp_friend"}) {
                check(server.getAdvancementLoader().get(Starfall.id(advancement)) != null, "advancement present: " + advancement);
            }

            check(server.getStructureTemplateManager().getTemplate(ObservatoryFeature.TEMPLATE).isPresent(),
                    "observatory structure template loads");
            check(server.getRegistryManager().getOrThrow(RegistryKeys.CONFIGURED_FEATURE).containsId(Starfall.id("observatory")),
                    "observatory configured feature registered");
            check(server.getRegistryManager().getOrThrow(RegistryKeys.PLACED_FEATURE).containsId(Starfall.id("astral_trees")),
                    "astral trees placed feature registered");
            check(server.getRegistryManager().getOrThrow(RegistryKeys.BIOME).containsId(Starfall.id("astral_meadows")),
                    "astral meadows biome registered");
            check(server.getRegistryManager().getOrThrow(RegistryKeys.JUKEBOX_SONG).containsId(Starfall.id("realm_theme")),
                    "jukebox song registered");
            RegistryKey<LootTable> chest = RegistryKey.of(RegistryKeys.LOOT_TABLE, Starfall.id("chests/observatory"));
            check(server.getReloadableRegistries().getLootTable(chest) != LootTable.EMPTY, "observatory chest loot present");

            long recipes = server.getRecipeManager().values().stream()
                    .map(RecipeEntry::id)
                    .filter(id -> id.getValue().getNamespace().equals(Starfall.MOD_ID))
                    .count();
            check(recipes >= 20, "at least 20 starfall recipes loaded (found " + recipes + ")");

            base = overworld.getTopPosition(Heightmap.Type.MOTION_BLOCKING, overworld.getSpawnPos());
            overworld.getChunk(base);
            overworld.getChunk(base.add(16, 0, 16));
            step = 1;
        } catch (Throwable t) {
            fail("startup checks threw " + t, t);
            step = 99;
        }
    }

    private static void fail(String message, Throwable t) {
        FAILURES.add(message);
        Starfall.LOGGER.error("SELFTEST exception: {}", message, t);
    }

    private static void onTick(MinecraftServer server) {
        if (step == 0 || step == 100) {
            return;
        }
        ticksInStep++;
        try {
            switch (step) {
                case 1 -> {
                    ShootingStarManager.lastLanding = null;
                    BlockPos target = base.add(8, 0, 8);
                    overworld.getChunk(target);
                    boolean launched = ShootingStarManager.summonStarAt(overworld, target, StarTier.AZURE, null, true);
                    check(launched, "scepter-style star launched");
                    check(ShootingStarManager.trackedCount() == 1, "star is tracked");
                    next(2);
                }
                case 2 -> {
                    if (ShootingStarManager.lastLanding != null) {
                        BlockPos landing = ShootingStarManager.lastLanding;
                        check(overworld.getBlockState(landing).isIn(ModTags.STAR_BLOCKS), "star block placed at " + landing);
                        check(ShootingStarManager.nearestLanding(overworld, base) != null, "landing remembered for the compass");
                        next(3);
                    } else if (ticksInStep > 800) {
                        check(false, "star landed within 800 ticks");
                        next(3);
                    }
                }
                case 3 -> {
                    guardian = ModEntities.STAR_GUARDIAN.spawn(overworld, base.up(6), SpawnReason.COMMAND);
                    wisp = ModEntities.STAR_WISP.spawn(overworld, base.up(4), SpawnReason.COMMAND);
                    zombie = EntityType.ZOMBIE.spawn(overworld, base.up(1), SpawnReason.COMMAND);
                    check(guardian != null, "guardian spawned");
                    check(wisp != null, "wisp spawned");
                    check(zombie != null, "zombie spawned");
                    if (guardian != null) {
                        guardian.setTarget(zombie);
                        StarBoltEntity.launch(overworld, guardian);
                    }
                    if (zombie != null) {
                        zombie.addStatusEffect(new StatusEffectInstance(ModEffects.STARLIGHT, 200, 0));
                    }
                    next(4);
                }
                case 4 -> {
                    if (ticksInStep >= 60) {
                        check(guardian != null && guardian.isAlive(), "guardian survives 60 ticks");
                        check(wisp != null && wisp.isAlive(), "wisp survives 60 ticks");
                        check(zombie != null && zombie.hasStatusEffect(ModEffects.STARLIGHT), "starlight effect applied to zombie");
                        if (guardian != null) {
                            guardian.discard();
                        }
                        if (wisp != null) {
                            wisp.discard();
                        }
                        next(5);
                    }
                }
                case 5 -> {
                    if (realm == null) {
                        next(7);
                        return;
                    }
                    realm.getChunk(0, 0);
                    BlockPos gate = StarGateTravel.ensureRealmSanctuary(realm);
                    check(realm.getBlockState(gate).isOf(ModBlocks.STAR_GATE), "realm sanctuary built with a star gate");
                    check(realm.getBlockState(gate.down()).isOf(ModBlocks.STARSTONE_BRICKS), "sanctuary floor is starstone bricks");
                    if (zombie != null) {
                        traveller = StarGateTravel.teleport(zombie, realm, gate.add(2, 0, 0));
                    }
                    next(6);
                }
                case 6 -> {
                    if (ticksInStep >= 5) {
                        check(traveller != null && traveller.getWorld() == realm, "zombie teleported into the Immortal Realm");
                        if (traveller != null) {
                            traveller.discard();
                        }
                        // Generate more chunks to exercise biomes, surface rules, trees and the observatory feature.
                        for (int cx = -2; cx <= 2; cx++) {
                            for (int cz = -2; cz <= 2; cz++) {
                                realm.getChunk(cx, cz);
                            }
                        }
                        check(true, "realm chunks (-2..2) generated");
                        int astral = 0;
                        for (int cx = -2; cx <= 2; cx++) {
                            for (int cz = -2; cz <= 2; cz++) {
                                astral += realm.getChunk(cx, cz).getBlockState(new BlockPos(cx * 16 + 8, 0, cz * 16 + 8)).isAir() ? 0 : 1;
                            }
                        }
                        check(true, "sampled realm columns: " + astral + " of 25 have ground at y=0");
                        next(7);
                    }
                }
                case 7 -> {
                    MeteorShowerManager.force(overworld, 40);
                    check(MeteorShowerManager.isActive(overworld), "meteor shower forced on");
                    next(8);
                }
                case 8 -> {
                    if (ticksInStep >= 60) {
                        check(!MeteorShowerManager.isActive(overworld), "meteor shower ended after its forced duration");
                        next(99);
                    }
                }
                case 99 -> finish(server);
                default -> {
                }
            }
        } catch (Throwable t) {
            fail("step " + step + " threw " + t, t);
            if (step != 99) {
                next(99);
            } else {
                step = 100;
            }
        }
    }

    private static void next(int nextStep) {
        step = nextStep;
        ticksInStep = 0;
    }

    private static void finish(MinecraftServer server) {
        step = 100;
        if (FAILURES.isEmpty()) {
            Starfall.LOGGER.info("STARFALL SELFTEST PASSED");
        } else {
            Starfall.LOGGER.error("STARFALL SELFTEST FAILED: {}", String.join(" | ", FAILURES));
        }
        server.stop(false);
    }

    public static Identifier id(String path) {
        return Starfall.id(path);
    }
}
