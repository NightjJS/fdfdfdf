package com.nightjjs.starfall.item;

import com.nightjjs.starfall.entity.StarBoltEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/**
 * The Starcaster fires a volley of homing star bolts that burst in a shower of light on impact.
 */
public class StarcasterItem extends Item {
    private static final int BOLTS_PER_SHOT = 3;
    private static final int COOLDOWN_TICKS = 25;

    public StarcasterItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!(world instanceof ServerWorld serverWorld)) {
            return ActionResult.SUCCESS;
        }

        float pitch = 0.8f + world.random.nextFloat() * 0.4f;
        world.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.PLAYERS, 1.0f, pitch);

        for (int i = 0; i < BOLTS_PER_SHOT; i++) {
            StarBoltEntity.launch(serverWorld, user);
        }

        user.getItemCooldownManager().set(stack, COOLDOWN_TICKS);
        user.incrementStat(Stats.USED.getOrCreateStat(this));
        if (!user.isCreative()) {
            EquipmentSlot slot = hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
            stack.damage(1, user, slot);
        }
        return ActionResult.SUCCESS;
    }
}
