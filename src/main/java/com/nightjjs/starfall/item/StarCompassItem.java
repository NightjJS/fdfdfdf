package com.nightjjs.starfall.item;

import com.nightjjs.starfall.event.ShootingStarManager;
import com.nightjjs.starfall.registry.ModSounds;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LodestoneTrackerComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;

import java.util.Optional;

/**
 * The Star Compass points at the most recent fallen star in this dimension. Right-click to retune it.
 */
public class StarCompassItem extends Item {
    public StarCompassItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!(world instanceof ServerWorld serverWorld)) {
            return ActionResult.SUCCESS;
        }
        BlockPos nearest = ShootingStarManager.nearestLanding(serverWorld, user.getBlockPos());
        if (nearest == null) {
            stack.remove(DataComponentTypes.LODESTONE_TRACKER);
            user.sendMessage(Text.translatable("message.starfall.compass.none"), true);
            return ActionResult.SUCCESS;
        }
        pointAt(stack, serverWorld, nearest);
        serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(), ModSounds.STAR_TWINKLE, SoundCategory.PLAYERS, 0.6f, 1.4f);
        user.sendMessage(Text.translatable("message.starfall.compass.tuned", nearest.getX(), nearest.getY(), nearest.getZ()), true);
        return ActionResult.SUCCESS;
    }

    public static void pointAt(ItemStack stack, ServerWorld world, BlockPos pos) {
        GlobalPos target = GlobalPos.create(world.getRegistryKey(), pos);
        stack.set(DataComponentTypes.LODESTONE_TRACKER, new LodestoneTrackerComponent(Optional.of(target), false));
    }
}
