package com.nightjjs.starfall.item;

import com.nightjjs.starfall.event.ShootingStarManager;
import com.nightjjs.starfall.event.StarTier;
import com.nightjjs.starfall.registry.ModAdvancements;
import com.nightjjs.starfall.registry.ModParticles;
import com.nightjjs.starfall.registry.ModSounds;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * The Starfall Scepter calls a star down onto the block you are looking at.
 */
public class StarfallScepterItem extends Item {
    private static final double RANGE = 64.0;
    private static final int COOLDOWN_TICKS = 160;

    public StarfallScepterItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!(world instanceof ServerWorld serverWorld)) {
            return ActionResult.SUCCESS;
        }
        HitResult hit = user.raycast(RANGE, 0.0f, false);
        if (hit.getType() != HitResult.Type.BLOCK) {
            user.sendMessage(Text.translatable("message.starfall.scepter.no_target"), true);
            return ActionResult.FAIL;
        }
        BlockPos target = ((BlockHitResult) hit).getBlockPos();
        if (!ShootingStarManager.summonStarAt(serverWorld, target, StarTier.GOLD, user, true)) {
            user.sendMessage(Text.translatable("message.starfall.scepter.blocked"), true);
            return ActionResult.FAIL;
        }

        serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(), ModSounds.STAR_TWINKLE, SoundCategory.PLAYERS, 1.0f, 0.8f);
        serverWorld.spawnParticles(ModParticles.STARLIGHT, user.getX(), user.getBodyY(0.8), user.getZ(), 20, 0.5, 0.5, 0.5, 0.05);
        user.sendMessage(Text.translatable("message.starfall.scepter.called"), true);

        user.getItemCooldownManager().set(stack, COOLDOWN_TICKS);
        user.incrementStat(Stats.USED.getOrCreateStat(this));
        if (!user.isCreative()) {
            stack.damage(1, user, hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
        }
        if (user instanceof ServerPlayerEntity serverPlayer) {
            ModAdvancements.grant(serverPlayer, ModAdvancements.CALL_A_STAR);
        }
        return ActionResult.SUCCESS;
    }
}
