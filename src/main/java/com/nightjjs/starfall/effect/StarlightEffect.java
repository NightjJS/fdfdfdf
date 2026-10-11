package com.nightjjs.starfall.effect;

import com.nightjjs.starfall.registry.ModParticles;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.server.world.ServerWorld;

/**
 * Starlight: heals a little every few seconds and sheds glittering motes.
 */
public class StarlightEffect extends StatusEffect {
    public StarlightEffect() {
        super(StatusEffectCategory.BENEFICIAL, 0xFFE86A);
    }

    @Override
    public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {
        if (entity.getHealth() < entity.getMaxHealth()) {
            entity.heal(1.0f);
        }
        world.spawnParticles(ModParticles.STARLIGHT, entity.getX(), entity.getBodyY(0.5), entity.getZ(),
                3, 0.35, 0.5, 0.35, 0.01);
        return true;
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        int interval = 50 >> amplifier;
        return interval <= 0 || duration % interval == 0;
    }
}
