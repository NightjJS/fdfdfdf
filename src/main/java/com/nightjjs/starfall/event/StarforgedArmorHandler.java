package com.nightjjs.starfall.event;

import com.nightjjs.starfall.registry.ModAdvancements;
import com.nightjjs.starfall.registry.ModDimensions;
import com.nightjjs.starfall.registry.ModEffects;
import com.nightjjs.starfall.registry.ModItems;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Starforged armor bonuses:
 * <ul>
 *   <li>Boots: no fall damage.</li>
 *   <li>Helmet: night vision at night and in the Immortal Realm.</li>
 *   <li>Full set: permanent Starlight (slow regeneration) and Speed at night.</li>
 * </ul>
 */
public final class StarforgedArmorHandler {
    private StarforgedArmorHandler() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(StarforgedArmorHandler::onServerTick);
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) ->
                !(source.isIn(DamageTypeTags.IS_FALL) && entity.getEquippedStack(EquipmentSlot.FEET).isOf(ModItems.STARFORGED_BOOTS)));
    }

    private static void onServerTick(MinecraftServer server) {
        if (server.getTicks() % 20 != 0) {
            return;
        }
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            boolean helmet = player.getEquippedStack(EquipmentSlot.HEAD).isOf(ModItems.STARFORGED_HELMET);
            boolean chest = player.getEquippedStack(EquipmentSlot.CHEST).isOf(ModItems.STARFORGED_CHESTPLATE);
            boolean legs = player.getEquippedStack(EquipmentSlot.LEGS).isOf(ModItems.STARFORGED_LEGGINGS);
            boolean boots = player.getEquippedStack(EquipmentSlot.FEET).isOf(ModItems.STARFORGED_BOOTS);
            boolean night = player.getWorld().isNight() || ModDimensions.isImmortalRealm(player.getWorld());

            if (helmet && night) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 400, 0, true, false, true));
            }
            if (helmet && chest && legs && boots) {
                player.addStatusEffect(new StatusEffectInstance(ModEffects.STARLIGHT, 60, 0, true, false, true));
                if (night) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 60, 0, true, false, true));
                }
                ModAdvancements.grant(player, ModAdvancements.STARFORGED_SET);
            }
        }
    }
}
