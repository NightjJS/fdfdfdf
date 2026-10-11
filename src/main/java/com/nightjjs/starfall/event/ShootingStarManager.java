package com.nightjjs.starfall.event;

import com.nightjjs.starfall.registry.ModBlocks;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Spawns shooting stars at night. A star is a falling block of {@link ModBlocks#FALLEN_STAR_BLOCK}
 * launched high above the world with a sideways drift, trailing light until it lands.
 */
public final class ShootingStarManager {
    private ShootingStarManager() {
    }

    /** Average: one star per player roughly every four minutes of night. */
    private static final int CHANCE_PER_TICK = 4800;
    private static final double MIN_DISTANCE = 32.0;
    private static final double MAX_DISTANCE = 80.0;
    private static final double SPAWN_HEIGHT_ABOVE_PLAYER = 100.0;
    private static final double MAX_SPAWN_Y = 290.0;
    private static final double ANNOUNCE_RANGE_SQ = 256.0 * 256.0;

    private static final List<FallingBlockEntity> STARS = new ArrayList<>();

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(ShootingStarManager::onWorldTick);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> STARS.clear());
    }

    private static void onWorldTick(ServerWorld world) {
        if (world.getRegistryKey() != World.OVERWORLD) {
            return;
        }
        if (world.isNight()) {
            for (ServerPlayerEntity player : world.getPlayers()) {
                if (!player.isSpectator() && world.random.nextInt(CHANCE_PER_TICK) == 0) {
                    trySpawnStar(world, player);
                }
            }
        }
        tickStars(world);
    }

    /** Also used by commands/tests: drops a star near the given player right now. */
    public static boolean trySpawnStar(ServerWorld world, ServerPlayerEntity player) {
        Random random = world.random;
        double angle = random.nextDouble() * Math.PI * 2.0;
        double distance = MIN_DISTANCE + random.nextDouble() * (MAX_DISTANCE - MIN_DISTANCE);
        double x = player.getX() + Math.cos(angle) * distance;
        double z = player.getZ() + Math.sin(angle) * distance;

        int chunkX = MathHelper.floor(x) >> 4;
        int chunkZ = MathHelper.floor(z) >> 4;
        if (!world.getChunkManager().isChunkLoaded(chunkX, chunkZ)) {
            return false;
        }

        double y = Math.min(player.getY() + SPAWN_HEIGHT_ABOVE_PLAYER, MAX_SPAWN_Y);
        BlockPos pos = BlockPos.ofFloored(x, y, z);
        if (!world.getBlockState(pos).isAir()) {
            return false;
        }

        FallingBlockEntity star = FallingBlockEntity.spawnFromBlock(world, pos, ModBlocks.FALLEN_STAR_BLOCK.getDefaultState());
        star.setHurtEntities(8.0f, 40);
        star.dropItem = true;

        // Sideways drift makes it streak across the sky instead of dropping straight down.
        double drift = random.nextDouble() * Math.PI * 2.0;
        star.setVelocity(Math.cos(drift) * 0.45, -0.6, Math.sin(drift) * 0.45);
        STARS.add(star);

        Text sighting = Text.translatable("message.starfall.star_sighted");
        for (ServerPlayerEntity other : world.getPlayers()) {
            if (other.squaredDistanceTo(x, y, z) <= ANNOUNCE_RANGE_SQ) {
                other.sendMessage(sighting, true);
            }
        }
        return true;
    }

    private static void tickStars(ServerWorld world) {
        if (STARS.isEmpty()) {
            return;
        }
        Iterator<FallingBlockEntity> iterator = STARS.iterator();
        while (iterator.hasNext()) {
            FallingBlockEntity star = iterator.next();
            if (star.getWorld() != world) {
                continue;
            }
            if (star.isRemoved()) {
                iterator.remove();
                BlockPos landing = star.getBlockPos();
                if (world.getBlockState(landing).isOf(ModBlocks.FALLEN_STAR_BLOCK)) {
                    onStarLanded(world, landing);
                }
                continue;
            }
            double x = star.getX();
            double y = star.getY() + 0.5;
            double z = star.getZ();
            world.spawnParticles(ParticleTypes.END_ROD, x, y, z, 6, 0.3, 0.3, 0.3, 0.02);
            world.spawnParticles(ParticleTypes.FLAME, x, y, z, 3, 0.2, 0.2, 0.2, 0.01);
            if (star.age % 8 == 0) {
                world.spawnParticles(ParticleTypes.FIREWORK, x, y, z, 2, 0.1, 0.1, 0.1, 0.05);
            }
        }
    }

    private static void onStarLanded(ServerWorld world, BlockPos pos) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.0;
        double z = pos.getZ() + 0.5;
        world.spawnParticles(ParticleTypes.FIREWORK, x, y, z, 80, 1.0, 0.8, 1.0, 0.25);
        world.spawnParticles(ParticleTypes.END_ROD, x, y, z, 40, 1.5, 1.0, 1.5, 0.1);
        world.playSound(null, pos, SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.BLOCKS, 3.0f, 1.3f);
        world.playSound(null, pos, SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, SoundCategory.BLOCKS, 3.0f, 0.8f);

        Text landed = Text.translatable("message.starfall.star_fell", pos.getX(), pos.getY(), pos.getZ());
        for (ServerPlayerEntity player : world.getPlayers()) {
            if (player.squaredDistanceTo(x, y, z) <= ANNOUNCE_RANGE_SQ) {
                player.sendMessage(landed, false);
            }
        }
    }
}
