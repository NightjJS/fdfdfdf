package com.nightjjs.starfall.event;

import com.nightjjs.starfall.registry.ModAdvancements;
import com.nightjjs.starfall.registry.ModItems;
import com.nightjjs.starfall.registry.ModParticles;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;

/**
 * The Immortal Heart works from anywhere in the inventory: when its owner would die, one charge is
 * spent, they are healed to full and granted protective effects instead.
 */
public final class ImmortalHeartHandler {
    private ImmortalHeartHandler() {
    }

    public static void register() {
        ServerLivingEntityEvents.ALLOW_DEATH.register(ImmortalHeartHandler::allowDeath);
    }

    private static boolean allowDeath(LivingEntity entity, DamageSource source, float amount) {
        if (!(entity instanceof ServerPlayerEntity player)) {
            return true;
        }
        if (source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return true; // /kill and the void still work
        }
        ItemStack heart = findHeart(player);
        if (heart.isEmpty()) {
            return true;
        }
        if (!(player.getWorld() instanceof ServerWorld world)) {
            return true;
        }

        spendCharge(heart);

        player.setHealth(player.getMaxHealth());
        player.clearStatusEffects();
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 20 * 45, 1));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 20 * 10, 1));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 20 * 40, 0));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 20 * 5, 2));
        player.setFireTicks(0);

        world.sendEntityStatus(player, EntityStatuses.USE_TOTEM_OF_UNDYING);
        world.spawnParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getBodyY(0.5), player.getZ(),
                60, 0.5, 0.8, 0.5, 0.3);
        world.spawnParticles(ParticleTypes.FLASH, player.getX(), player.getBodyY(0.5), player.getZ(), 1, 0, 0, 0, 0);
        world.spawnParticles(ModParticles.STARLIGHT, player.getX(), player.getBodyY(0.5), player.getZ(),
                60, 0.8, 1.0, 0.8, 0.08);
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ITEM_TOTEM_USE, SoundCategory.PLAYERS, 1.0f, 1.2f);

        player.incrementStat(Stats.USED.getOrCreateStat(ModItems.IMMORTAL_HEART));
        player.sendMessage(Text.translatable("message.starfall.heart_saved"), true);
        ModAdvancements.grant(player, ModAdvancements.IMMORTAL);
        return false;
    }

    private static ItemStack findHeart(ServerPlayerEntity player) {
        ItemStack main = player.getMainHandStack();
        if (main.isOf(ModItems.IMMORTAL_HEART)) {
            return main;
        }
        ItemStack off = player.getOffHandStack();
        if (off.isOf(ModItems.IMMORTAL_HEART)) {
            return off;
        }
        int size = player.getInventory().size();
        for (int i = 0; i < size; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isOf(ModItems.IMMORTAL_HEART)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static void spendCharge(ItemStack heart) {
        int damage = heart.getDamage() + 1;
        if (damage >= heart.getMaxDamage()) {
            heart.decrement(1); // the last charge consumes the heart
        } else {
            heart.setDamage(damage);
        }
    }
}
