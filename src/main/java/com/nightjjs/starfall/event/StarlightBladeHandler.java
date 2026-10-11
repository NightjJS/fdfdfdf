package com.nightjjs.starfall.event;

import com.nightjjs.starfall.registry.ModItems;
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

/**
 * Hits with the Starlight Blade mark the target with starlight (glowing) and scatter sparks.
 * At night the blade also feeds a little of the stolen light back to its wielder.
 */
public final class StarlightBladeHandler {
    private StarlightBladeHandler() {
    }

    public static void register() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register(StarlightBladeHandler::afterDamage);
    }

    private static void afterDamage(LivingEntity target, DamageSource source, float baseDamage, float damageTaken, boolean blocked) {
        if (blocked || damageTaken <= 0.0f) {
            return;
        }
        if (!(source.getAttacker() instanceof LivingEntity attacker)) {
            return;
        }
        if (!attacker.getMainHandStack().isOf(ModItems.STARLIGHT_BLADE)) {
            return;
        }
        if (!(target.getWorld() instanceof ServerWorld world)) {
            return;
        }

        target.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 20 * 5, 0), attacker);
        world.spawnParticles(ParticleTypes.END_ROD, target.getX(), target.getBodyY(0.5), target.getZ(),
                12, 0.3, 0.4, 0.3, 0.05);
        world.spawnParticles(ParticleTypes.FIREWORK, target.getX(), target.getBodyY(0.5), target.getZ(),
                6, 0.2, 0.3, 0.2, 0.1);
        world.playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.PLAYERS, 1.0f, 1.4f);

        if (world.isNight() && attacker instanceof PlayerEntity) {
            attacker.heal(1.0f);
        }
    }
}
