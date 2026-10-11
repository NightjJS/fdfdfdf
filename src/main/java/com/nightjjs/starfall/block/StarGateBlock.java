package com.nightjjs.starfall.block;

import com.mojang.serialization.MapCodec;
import com.nightjjs.starfall.registry.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * The Star Gate. Right-click it with a Star Core to travel between the Overworld and the Immortal Realm.
 */
public class StarGateBlock extends Block {
    public static final MapCodec<StarGateBlock> CODEC = createCodec(StarGateBlock::new);

    public StarGateBlock(Settings settings) {
        super(settings);
    }

    @Override
    public MapCodec<StarGateBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!stack.isOf(ModItems.STAR_CORE)) {
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        }
        if (!(world instanceof ServerWorld serverWorld) || !(player instanceof ServerPlayerEntity serverPlayer)) {
            return ActionResult.SUCCESS;
        }
        if (StarGateTravel.travel(serverPlayer, serverWorld, pos) && !player.isCreative()) {
            stack.decrement(1);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient()) {
            player.sendMessage(Text.translatable("message.starfall.gate.needs_core"), true);
        }
        return ActionResult.SUCCESS;
    }
}
