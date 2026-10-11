package com.nightjjs.starfall.event;

import com.nightjjs.starfall.item.StarCompassItem;
import com.nightjjs.starfall.registry.ModBlocks;
import com.nightjjs.starfall.registry.ModDimensions;
import com.nightjjs.starfall.registry.ModEntities;
import com.nightjjs.starfall.registry.ModItems;
import com.nightjjs.starfall.registry.ModParticles;
import com.nightjjs.starfall.registry.ModSounds;
import com.nightjjs.starfall.registry.ModTags;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Spawns and tracks shooting stars. A star is a falling star block launched high above the world
 * with a sideways drift, trailing light until it lands with a shockwave.
 */
public final class ShootingStarManager {
    private ShootingStarManager() {
    }

    /** Average: one star per player roughly every four minutes of a normal night. */
    private static final int CHANCE_PER_TICK = 4800;
    /** During a meteor shower stars rain much faster. */
    private static final int SHOWER_CHANCE_PER_TICK = 140;
    /** The Immortal Realm is always night; stars fall there a little more often. */
    private static final int REALM_CHANCE_PER_TICK = 3000;
    private static final double MIN_DISTANCE = 32.0;
    private static final double MAX_DISTANCE = 80.0;
    private static final double SPAWN_HEIGHT_ABOVE_PLAYER = 100.0;
    private static final double MAX_SPAWN_Y = 290.0;
    private static final double ANNOUNCE_RANGE_SQ = 256.0 * 256.0;
    private static final double SHOCKWAVE_RADIUS = 6.0;
    private static final int LANDING_HISTORY = 32;

    private record TrackedStar(FallingBlockEntity entity, StarTier tier, LivingEntity owner, boolean leaveBlock,
                               boolean announce, boolean harmless) {
    }

    /** A pillar of light that lingers over a fresh impact. */
    private record LightBeam(RegistryKey<World> world, BlockPos pos, int[] ticksLeft) {
    }

    private static final List<LightBeam> BEAMS = new ArrayList<>();
    private static final int BEAM_TICKS = 100;
    private static final int BEAM_HEIGHT = 48;

    private static final List<TrackedStar> STARS = new ArrayList<>();
    private static final Map<RegistryKey<World>, Deque<BlockPos>> LANDINGS = new ConcurrentHashMap<>();
    /** The last position any star landed at; used by the self-test. */
    public static volatile BlockPos lastLanding;

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(ShootingStarManager::onWorldTick);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            STARS.clear();
            BEAMS.clear();
            LANDINGS.clear();
        });
    }

    private static void onWorldTick(ServerWorld world) {
        boolean overworld = world.getRegistryKey() == World.OVERWORLD;
        boolean realm = ModDimensions.isImmortalRealm(world);
        if (overworld || realm) {
            boolean shower = overworld && MeteorShowerManager.isActive(world);
            int chance = realm ? REALM_CHANCE_PER_TICK : shower ? SHOWER_CHANCE_PER_TICK : CHANCE_PER_TICK;
            if (realm || shower || world.isNight()) {
                for (ServerPlayerEntity player : world.getPlayers()) {
                    if (!player.isSpectator() && world.random.nextInt(chance) == 0) {
                        trySpawnStar(world, player, StarTier.roll(world.random, shower));
                    }
                }
            }
        }
        tickStars(world);
    }

    /** Drops a random star somewhere around the player. Returns false when no clear sky was found. */
    public static boolean trySpawnStar(ServerWorld world, ServerPlayerEntity player, StarTier tier) {
        Random random = world.random;
        double angle = random.nextDouble() * Math.PI * 2.0;
        double distance = MIN_DISTANCE + random.nextDouble() * (MAX_DISTANCE - MIN_DISTANCE);
        double x = player.getX() + Math.cos(angle) * distance;
        double z = player.getZ() + Math.sin(angle) * distance;
        double y = Math.min(player.getY() + SPAWN_HEIGHT_ABOVE_PLAYER, MAX_SPAWN_Y);
        BlockPos pos = BlockPos.ofFloored(x, y, z);

        double drift = random.nextDouble() * Math.PI * 2.0;
        Vec3d velocity = new Vec3d(Math.cos(drift) * 0.45, -0.6, Math.sin(drift) * 0.45);
        FallingBlockEntity star = launch(world, pos, tier, velocity, null, true, true, false);
        if (star == null) {
            return false;
        }
        Text sighting = Text.translatable("message.starfall.star_sighted");
        for (ServerPlayerEntity other : world.getPlayers()) {
            if (other.squaredDistanceTo(x, y, z) <= ANNOUNCE_RANGE_SQ) {
                other.sendMessage(sighting, true);
                other.playSoundToPlayer(ModSounds.STAR_SIGHTING, SoundCategory.AMBIENT, 0.8f, 1.0f);
            }
        }
        return true;
    }

    /** Calls a star straight down onto {@code target} (scepter). Leaves the star block behind. */
    public static boolean summonStarAt(ServerWorld world, BlockPos target, StarTier tier, LivingEntity owner, boolean leaveBlock) {
        BlockPos pos = target.up(45);
        if (pos.getY() > MAX_SPAWN_Y) {
            pos = new BlockPos(target.getX(), (int) MAX_SPAWN_Y, target.getZ());
        }
        return launch(world, pos, tier, new Vec3d(0.0, -1.2, 0.0), owner, leaveBlock, false, false) != null;
    }

    /** A guardian strike: a star that shatters on landing instead of leaving a block. */
    public static void summonStrike(ServerWorld world, BlockPos target, StarTier tier, LivingEntity owner) {
        summonStrike(world, target, tier, owner, false);
    }

    /** A strike; when {@code harmless} it is pure spectacle (no crush damage, no shockwave). */
    public static void summonStrike(ServerWorld world, BlockPos target, StarTier tier, LivingEntity owner, boolean harmless) {
        Random random = world.random;
        BlockPos pos = target.add(random.nextInt(5) - 2, 30, random.nextInt(5) - 2);
        launch(world, pos, tier, new Vec3d(0.0, -1.0, 0.0), owner, false, false, harmless);
    }

    private static FallingBlockEntity launch(ServerWorld world, BlockPos pos, StarTier tier, Vec3d velocity,
                                             LivingEntity owner, boolean leaveBlock, boolean announce, boolean harmless) {
        int chunkX = MathHelper.floor(pos.getX()) >> 4;
        int chunkZ = MathHelper.floor(pos.getZ()) >> 4;
        if (!world.getChunkManager().isChunkLoaded(chunkX, chunkZ)) {
            return null;
        }
        if (!world.getBlockState(pos).isAir()) {
            return null;
        }
        FallingBlockEntity star = FallingBlockEntity.spawnFromBlock(world, pos, tier.block().getDefaultState());
        if (!harmless) {
            star.setHurtEntities(tier.crushDamage, 60);
        }
        star.dropItem = leaveBlock;
        star.setVelocity(velocity);
        STARS.add(new TrackedStar(star, tier, owner, leaveBlock, announce, harmless));
        return star;
    }

    private static void tickStars(ServerWorld world) {
        tickBeams(world);
        if (STARS.isEmpty()) {
            return;
        }
        Iterator<TrackedStar> iterator = STARS.iterator();
        while (iterator.hasNext()) {
            TrackedStar tracked = iterator.next();
            FallingBlockEntity star = tracked.entity();
            if (star.getWorld() != world) {
                continue;
            }
            if (star.isRemoved()) {
                iterator.remove();
                BlockPos landing = star.getBlockPos();
                if (world.getBlockState(landing).isIn(ModTags.STAR_BLOCKS)) {
                    onStarLanded(world, landing, tracked);
                }
                continue;
            }
            trail(world, star, tracked.tier());
        }
    }

    private static void trail(ServerWorld world, FallingBlockEntity star, StarTier tier) {
        double x = star.getX();
        double y = star.getY() + 0.5;
        double z = star.getZ();
        world.spawnParticles(ModParticles.STARLIGHT, x, y, z, 4, 0.3, 0.3, 0.3, 0.02);
        world.spawnParticles(new DustParticleEffect(tier.trailColor, 1.6f), x, y, z, 5, 0.35, 0.35, 0.35, 0.0);
        world.spawnParticles(ParticleTypes.END_ROD, x, y, z, 2, 0.2, 0.2, 0.2, 0.02);
        if (star.age % 8 == 0) {
            world.spawnParticles(ParticleTypes.FIREWORK, x, y, z, 2, 0.1, 0.1, 0.1, 0.05);
        }
    }

    private static void onStarLanded(ServerWorld world, BlockPos pos, TrackedStar tracked) {
        StarTier tier = tracked.tier();
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.0;
        double z = pos.getZ() + 0.5;

        world.spawnParticles(ParticleTypes.FLASH, x, y, z, 1, 0, 0, 0, 0);
        world.spawnParticles(ParticleTypes.FIREWORK, x, y, z, 90, 1.0, 0.8, 1.0, 0.25);
        world.spawnParticles(ParticleTypes.END_ROD, x, y, z, 50, 1.5, 1.0, 1.5, 0.1);
        world.spawnParticles(ModParticles.STARLIGHT, x, y, z, 80, 2.5, 1.5, 2.5, 0.08);
        world.spawnParticles(new DustParticleEffect(tier.coreColor, 2.0f), x, y, z, 60, 2.0, 0.5, 2.0, 0.0);
        world.spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y, z, 20, 1.5, 0.3, 1.5, 0.02);
        world.playSound(null, pos, ModSounds.STAR_IMPACT, SoundCategory.BLOCKS, 4.0f, 0.9f + world.random.nextFloat() * 0.2f);
        world.playSound(null, pos, ModSounds.STAR_TWINKLE, SoundCategory.BLOCKS, 2.0f, 1.0f);

        if (!tracked.harmless()) {
            shockwave(world, pos, tier, tracked.owner());
            scorchGround(world, pos);
        }
        BEAMS.add(new LightBeam(world.getRegistryKey(), pos, new int[]{BEAM_TICKS}));

        if (!tracked.leaveBlock()) {
            world.removeBlock(pos, false);
            return;
        }

        lastLanding = pos;
        LANDINGS.computeIfAbsent(world.getRegistryKey(), key -> new ArrayDeque<>()).addFirst(pos);
        Deque<BlockPos> history = LANDINGS.get(world.getRegistryKey());
        while (history.size() > LANDING_HISTORY) {
            history.removeLast();
        }

        if (world.random.nextFloat() < 0.35f) {
            spawnWisps(world, pos, 1 + world.random.nextInt(2));
        }

        if (tracked.announce()) {
            Text landed = Text.translatable(tier.translationKey(), pos.getX(), pos.getY(), pos.getZ());
            for (ServerPlayerEntity player : world.getPlayers()) {
                if (player.squaredDistanceTo(x, y, z) <= ANNOUNCE_RANGE_SQ) {
                    player.sendMessage(landed, false);
                    retuneCompasses(player, world, pos);
                }
            }
        }
    }

    private static void tickBeams(ServerWorld world) {
        if (BEAMS.isEmpty()) {
            return;
        }
        Iterator<LightBeam> iterator = BEAMS.iterator();
        while (iterator.hasNext()) {
            LightBeam beam = iterator.next();
            if (beam.world() != world.getRegistryKey()) {
                continue;
            }
            if (--beam.ticksLeft()[0] <= 0) {
                iterator.remove();
                continue;
            }
            double x = beam.pos().getX() + 0.5;
            double z = beam.pos().getZ() + 0.5;
            double intensity = beam.ticksLeft()[0] / (double) BEAM_TICKS;
            int motes = 2 + (int) (6 * intensity);
            for (int i = 0; i < motes; i++) {
                double y = beam.pos().getY() + 1.0 + world.random.nextDouble() * BEAM_HEIGHT * intensity;
                world.spawnParticles(ModParticles.STARLIGHT, x, y, z, 1, 0.25, 0.5, 0.25, 0.0);
            }
            if (world.random.nextInt(3) == 0) {
                double y = beam.pos().getY() + 1.0 + world.random.nextDouble() * BEAM_HEIGHT * intensity;
                world.spawnParticles(ParticleTypes.END_ROD, x, y, z, 1, 0.1, 0.4, 0.1, 0.0);
            }
        }
    }

    private static void shockwave(ServerWorld world, BlockPos pos, StarTier tier, LivingEntity owner) {
        Vec3d center = pos.toCenterPos();
        DamageSource source = world.getDamageSources().explosion(null, owner);
        List<LivingEntity> victims = world.getEntitiesByClass(LivingEntity.class,
                net.minecraft.util.math.Box.of(center, SHOCKWAVE_RADIUS * 2, SHOCKWAVE_RADIUS * 2, SHOCKWAVE_RADIUS * 2),
                entity -> entity != owner && entity.isAlive() && !entity.isSpectator()
                        && !(entity instanceof PlayerEntity player && player.isCreative()));
        for (LivingEntity victim : victims) {
            double distance = Math.sqrt(victim.squaredDistanceTo(center));
            if (distance > SHOCKWAVE_RADIUS) {
                continue;
            }
            float falloff = (float) (1.0 - distance / SHOCKWAVE_RADIUS);
            victim.damage(world, source, tier.shockwaveDamage * falloff + 1.0f);
            Vec3d push = victim.getPos().subtract(center);
            Vec3d direction = push.lengthSquared() < 1.0E-4 ? new Vec3d(0, 1, 0) : push.normalize();
            victim.addVelocity(direction.x * 1.2 * falloff, 0.5 + 0.6 * falloff, direction.z * 1.2 * falloff);
            victim.velocityModified = true;
        }
    }

    /** Turns nearby surface ground into glowing starfall dust. */
    private static void scorchGround(ServerWorld world, BlockPos impact) {
        Random random = world.random;
        BlockState dust = ModBlocks.STARFALL_DUST.getDefaultState();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }
                if (Math.abs(dx) == 2 && Math.abs(dz) == 2) {
                    continue; // round the corners
                }
                if (random.nextFloat() > 0.75f) {
                    continue;
                }
                for (int dy = 1; dy >= -3; dy--) {
                    BlockPos candidate = impact.add(dx, dy, dz);
                    BlockState state = world.getBlockState(candidate);
                    if (state.isAir()) {
                        continue;
                    }
                    if (state.isIn(ModTags.IMPACT_SCORCHABLE) && world.getBlockState(candidate.up()).isAir()) {
                        world.setBlockState(candidate, dust);
                    }
                    break;
                }
            }
        }
    }

    private static void spawnWisps(ServerWorld world, BlockPos pos, int count) {
        for (int i = 0; i < count; i++) {
            BlockPos spawn = pos.add(world.random.nextInt(5) - 2, 2 + world.random.nextInt(2), world.random.nextInt(5) - 2);
            if (world.getBlockState(spawn).isAir()) {
                ModEntities.STAR_WISP.spawn(world, spawn, SpawnReason.EVENT);
            }
        }
    }

    private static void retuneCompasses(ServerPlayerEntity player, ServerWorld world, BlockPos pos) {
        int size = player.getInventory().size();
        for (int i = 0; i < size; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isOf(ModItems.STAR_COMPASS)) {
                StarCompassItem.pointAt(stack, world, pos);
            }
        }
    }

    /** The nearest remembered landing in this world, or null. */
    public static BlockPos nearestLanding(ServerWorld world, BlockPos from) {
        Deque<BlockPos> history = LANDINGS.get(world.getRegistryKey());
        if (history == null || history.isEmpty()) {
            return null;
        }
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (BlockPos pos : history) {
            if (!world.getBlockState(pos).isIn(ModTags.STAR_BLOCKS)) {
                continue;
            }
            double distance = pos.getSquaredDistance(from);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = pos;
            }
        }
        return best;
    }

    public static int trackedCount() {
        return STARS.size();
    }

    public static boolean isTracked(Entity entity) {
        for (TrackedStar tracked : STARS) {
            if (tracked.entity() == entity) {
                return true;
            }
        }
        return false;
    }
}
