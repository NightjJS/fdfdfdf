package com.nightjjs.starfall.event;

import com.nightjjs.starfall.registry.ModDimensions;
import com.nightjjs.starfall.registry.ModEntities;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;

/**
 * Keeps a handful of Star Wisps drifting around each player in the Immortal Realm.
 */
public final class WispSpawner {
    private WispSpawner() {
    }

    private static final int INTERVAL = 100;
    private static final int MAX_NEARBY = 5;
    private static final double RANGE = 48.0;

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(WispSpawner::onWorldTick);
    }

    private static void onWorldTick(ServerWorld world) {
        if (!ModDimensions.isImmortalRealm(world) || world.getTime() % INTERVAL != 0) {
            return;
        }
        for (ServerPlayerEntity player : world.getPlayers()) {
            if (player.isSpectator()) {
                continue;
            }
            Box area = Box.of(player.getPos(), RANGE * 2, RANGE * 2, RANGE * 2);
            int nearby = world.getEntitiesByType(ModEntities.STAR_WISP, area, wisp -> true).size();
            if (nearby >= MAX_NEARBY) {
                continue;
            }
            trySpawnNear(world, player);
        }
    }

    private static void trySpawnNear(ServerWorld world, ServerPlayerEntity player) {
        Random random = world.random;
        double angle = random.nextDouble() * Math.PI * 2.0;
        double distance = 16.0 + random.nextDouble() * 20.0;
        int x = (int) Math.floor(player.getX() + Math.cos(angle) * distance);
        int z = (int) Math.floor(player.getZ() + Math.sin(angle) * distance);
        if (!world.getChunkManager().isChunkLoaded(x >> 4, z >> 4)) {
            return;
        }
        BlockPos surface = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, new BlockPos(x, 0, z));
        if (surface.getY() <= world.getBottomY() + 1) {
            return; // over the void
        }
        BlockPos spawn = surface.up(2 + random.nextInt(4));
        if (!world.getBlockState(spawn).isAir()) {
            return;
        }
        ModEntities.STAR_WISP.spawn(world, spawn, SpawnReason.NATURAL);
    }
}
