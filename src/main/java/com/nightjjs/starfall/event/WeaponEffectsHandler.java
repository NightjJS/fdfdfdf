package com.nightjjs.starfall.event;

import com.nightjjs.starfall.registry.ModItems;
import com.nightjjs.starfall.registry.ModParticles;
import com.nightjjs.starfall.registry.ModSounds;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/**
 * Hit effects of the star weapons.
 * <ul>
 *   <li>Starlight Blade: marks the target with starlight (glowing), scatters sparks, heals its wielder at night.</li>
 *   <li>Comet Hammer: every hit detonates a shockwave that damages and launches everything around the target.</li>
 * </ul>
 */
public final class WeaponEffectsHandler {
    private WeaponEffectsHandler() {
    }

    private static final double HAMMER_RADIUS = 3.5;
    private static final float HAMMER_SPLASH_DAMAGE = 6.0f;
    private static boolean inHammerShockwave;

    public static void register() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register(WeaponEffectsHandler::afterDamage);
    }

    private static void afterDamage(LivingEntity target, DamageSource source, float baseDamage, float damageTaken, boolean blocked) {
        if (blocked || damageTaken <= 0.0f || inHammerShockwave) {
            return;
        }
        if (!(source.getAttacker() instanceof LivingEntity attacker)) {
            return;
        }
        if (!(target.getWorld() instanceof ServerWorld world)) {
            return;
        }
        if (attacker.getMainHandStack().isOf(ModItems.STARLIGHT_BLADE)) {
            bladeHit(world, target, attacker);
        } else if (attacker.getMainHandStack().isOf(ModItems.COMET_HAMMER)) {
            hammerHit(world, target, attacker);
        }
    }

    private static void bladeHit(ServerWorld world, LivingEntity target, LivingEntity attacker) {
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 20 * 5, 0), attacker);
        world.spawnParticles(ModParticles.STARLIGHT, target.getX(), target.getBodyY(0.5), target.getZ(), 14, 0.3, 0.4, 0.3, 0.05);
        world.spawnParticles(ParticleTypes.END_ROD, target.getX(), target.getBodyY(0.5), target.getZ(), 6, 0.3, 0.4, 0.3, 0.05);
        world.playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.PLAYERS, 1.0f, 1.4f);
        if (world.isNight() && attacker instanceof PlayerEntity) {
            attacker.heal(1.0f);
        }
    }

    private static void hammerHit(ServerWorld world, LivingEntity target, LivingEntity attacker) {
        Vec3d center = target.getPos();
        world.spawnParticles(ParticleTypes.EXPLOSION, center.x, center.y + 0.5, center.z, 3, 0.8, 0.3, 0.8, 0.0);
        world.spawnParticles(ParticleTypes.GUST, center.x, center.y + 0.2, center.z, 1, 0, 0, 0, 0);
        world.spawnParticles(ModParticles.STARLIGHT, center.x, center.y + 0.5, center.z, 40, 1.5, 0.5, 1.5, 0.1);
        world.playSound(null, center.x, center.y, center.z, ModSounds.STAR_IMPACT, SoundCategory.PLAYERS, 1.2f, 1.3f);

        List<LivingEntity> victims = world.getEntitiesByClass(LivingEntity.class,
                Box.of(center, HAMMER_RADIUS * 2, HAMMER_RADIUS * 2, HAMMER_RADIUS * 2),
                entity -> entity != attacker && entity != target && entity.isAlive() && !entity.isTeammate(attacker)
                        && !(entity instanceof PlayerEntity player && player.isCreative()));
        inHammerShockwave = true;
        try {
            for (LivingEntity victim : victims) {
                double distance = Math.sqrt(victim.squaredDistanceTo(center));
                if (distance > HAMMER_RADIUS) {
                    continue;
                }
                float falloff = (float) (1.0 - distance / HAMMER_RADIUS * 0.5);
                DamageSource splash = attacker instanceof PlayerEntity player
                        ? world.getDamageSources().playerAttack(player)
                        : world.getDamageSources().mobAttack(attacker);
                victim.damage(world, splash, HAMMER_SPLASH_DAMAGE * falloff);
                Vec3d push = victim.getPos().subtract(center);
                Vec3d direction = push.lengthSquared() < 1.0E-4 ? new Vec3d(0, 1, 0) : push.normalize();
                victim.addVelocity(direction.x * 0.9, 0.7, direction.z * 0.9);
                victim.velocityModified = true;
            }
        } finally {
            inHammerShockwave = false;
        }
        target.addVelocity(0.0, 0.8, 0.0);
        target.velocityModified = true;
    }
}
