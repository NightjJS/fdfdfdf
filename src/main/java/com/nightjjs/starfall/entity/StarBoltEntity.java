package com.nightjjs.starfall.entity;

import com.nightjjs.starfall.registry.ModParticles;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FireworkExplosionComponent;
import net.minecraft.component.type.FireworksComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

import java.util.Comparator;
import java.util.List;

/**
 * A star bolt is a firework rocket that homes in on the nearest creature and bursts into a star.
 * It reuses the vanilla rocket entity type, so no client-side renderer is needed.
 */
public class StarBoltEntity extends FireworkRocketEntity {
    private static final int[] PLAYER_PALETTE = {0xFFF3A0, 0xFFD23F, 0xFF8C1A, 0x8FD3FF, 0xFFFFFF};
    private static final int[] GUARDIAN_PALETTE = {0xFFE86A, 0xD4262E, 0xFF8C1A, 0xFFFFFF};
    private static final double SEARCH_RADIUS = 12.0;
    private static final double MAX_SPEED = 1.4;
    private static final double MIN_SPEED = 0.6;

    private final LivingEntity shooterEntity;
    private int ticksAlive;

    public StarBoltEntity(ServerWorld world, double x, double y, double z, ItemStack stack, LivingEntity shooter) {
        super(world, x, y, z, stack);
        this.shooterEntity = shooter;
        this.setOwner(shooter);
        // The launcher plays one launch sound for the whole volley.
        this.setSilent(true);
    }

    /** Spawns one bolt slightly in front of the shooter's eyes, flying where they look. */
    public static StarBoltEntity launch(ServerWorld world, LivingEntity shooter) {
        Vec3d look = shooter.getRotationVector();
        Vec3d forward = new Vec3d(look.x, 0.0, look.z).normalize();
        Vec3d spawn = shooter.getEyePos().add(forward.multiply(0.8)).add(0.0, -0.2, 0.0);

        boolean hostile = shooter instanceof HostileEntity;
        StarBoltEntity bolt = new StarBoltEntity(world, spawn.x, spawn.y, spawn.z,
                createStarRocket(world.random, hostile ? GUARDIAN_PALETTE : PLAYER_PALETTE), shooter);
        bolt.setVelocity(look.x, look.y, look.z, 0.5f, 0.6f);
        world.spawnEntity(bolt);
        return bolt;
    }

    /** Builds the firework payload: a star-shaped burst in star colors with trail and twinkle. */
    private static ItemStack createStarRocket(Random random, int[] palette) {
        ItemStack stack = new ItemStack(Items.FIREWORK_ROCKET);
        IntArrayList colors = new IntArrayList(List.of(
                palette[random.nextInt(palette.length)],
                palette[random.nextInt(palette.length)]));
        IntArrayList fade = new IntArrayList(List.of(0xFFFFFF));
        FireworkExplosionComponent burst = new FireworkExplosionComponent(
                FireworkExplosionComponent.Type.STAR, colors, fade, true, true);
        // Flight duration 1 gives a 10-21 tick fuse, which is a short-range burst.
        stack.set(DataComponentTypes.FIREWORKS, new FireworksComponent(1, List.of(burst)));
        return stack;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isRemoved() || !(this.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }
        ticksAlive++;
        serverWorld.spawnParticles(ModParticles.STARLIGHT, getX(), getY(), getZ(), 1, 0.05, 0.05, 0.05, 0.0);

        // Let the bolt scatter for a moment before it starts tracking.
        if (ticksAlive < 3) {
            return;
        }
        LivingEntity target = findTarget(serverWorld);
        if (target == null) {
            return;
        }
        Vec3d velocity = this.getVelocity();
        double speed = Math.min(Math.max(velocity.length(), MIN_SPEED), MAX_SPEED);
        Vec3d toTarget = target.getBoundingBox().getCenter().subtract(this.getPos()).normalize();
        Vec3d direction = velocity.normalize().lerp(toTarget, 0.35).normalize();
        this.setVelocity(direction.multiply(speed));
    }

    private LivingEntity findTarget(ServerWorld world) {
        List<LivingEntity> candidates = world.getEntitiesByClass(LivingEntity.class,
                this.getBoundingBox().expand(SEARCH_RADIUS), this::isValidTarget);
        if (candidates.isEmpty()) {
            return null;
        }
        boolean shooterIsMonster = shooterEntity instanceof HostileEntity;
        // Player bolts prefer monsters; monster bolts prefer players. Among equals pick the closest.
        return candidates.stream()
                .min(Comparator.<LivingEntity>comparingInt(e -> preferred(e, shooterIsMonster) ? 0 : 1)
                        .thenComparingDouble(e -> e.squaredDistanceTo(this)))
                .orElse(null);
    }

    private static boolean preferred(LivingEntity entity, boolean shooterIsMonster) {
        return shooterIsMonster ? entity instanceof PlayerEntity : entity instanceof HostileEntity;
    }

    private boolean isValidTarget(LivingEntity entity) {
        if (entity == shooterEntity || !entity.isAlive() || entity.isSpectator() || entity instanceof StarWispEntity) {
            return false;
        }
        if (entity instanceof PlayerEntity player && player.isCreative()) {
            return false;
        }
        if (shooterEntity instanceof HostileEntity && entity instanceof HostileEntity) {
            return false; // the Guardian never targets other monsters
        }
        return shooterEntity == null || !entity.isTeammate(shooterEntity);
    }

    /** The burst must never hurt whoever fired it. */
    public static void registerEvents() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) ->
                !(source.getSource() instanceof StarBoltEntity bolt && bolt.shooterEntity == entity));
    }
}
