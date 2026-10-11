package com.nightjjs.starfall.block;

import com.mojang.serialization.MapCodec;
import com.nightjjs.starfall.entity.StarGuardianEntity;
import com.nightjjs.starfall.registry.ModAdvancements;
import com.nightjjs.starfall.registry.ModDimensions;
import com.nightjjs.starfall.registry.ModEntities;
import com.nightjjs.starfall.registry.ModItems;
import com.nightjjs.starfall.registry.ModParticles;
import com.nightjjs.starfall.registry.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * The Celestial Altar. Offer a Star Core at night (or anywhere in the Immortal Realm) to call the Star Guardian.
 */
public class CelestialAltarBlock extends Block {
    public static final MapCodec<CelestialAltarBlock> CODEC = createCodec(CelestialAltarBlock::new);

    public CelestialAltarBlock(Settings settings) {
        super(settings);
    }

    @Override
    public MapCodec<CelestialAltarBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!stack.isOf(ModItems.STAR_CORE)) {
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        }
        if (!(world instanceof ServerWorld serverWorld)) {
            return ActionResult.SUCCESS;
        }
        if (!world.isNight() && !ModDimensions.isImmortalRealm(world)) {
            player.sendMessage(Text.translatable("message.starfall.altar.daylight"), true);
            return ActionResult.SUCCESS;
        }
        if (!serverWorld.getEntitiesByType(ModEntities.STAR_GUARDIAN, net.minecraft.util.math.Box.of(pos.toCenterPos(), 96, 96, 96), e -> true).isEmpty()) {
            player.sendMessage(Text.translatable("message.starfall.altar.already"), true);
            return ActionResult.SUCCESS;
        }
        if (!player.isCreative()) {
            stack.decrement(1);
        }
        summonGuardian(serverWorld, pos);
        if (player instanceof ServerPlayerEntity serverPlayer) {
            ModAdvancements.grant(serverPlayer, ModAdvancements.SUMMON_GUARDIAN);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient()) {
            player.sendMessage(Text.translatable("message.starfall.altar.hint"), true);
        }
        return ActionResult.SUCCESS;
    }

    public static StarGuardianEntity summonGuardian(ServerWorld world, BlockPos altarPos) {
        BlockPos spawnPos = altarPos.up(4);
        double x = altarPos.getX() + 0.5;
        double y = altarPos.getY() + 1.0;
        double z = altarPos.getZ() + 0.5;
        world.spawnParticles(ParticleTypes.FLASH, x, y + 3, z, 1, 0, 0, 0, 0);
        world.spawnParticles(ParticleTypes.END_ROD, x, y, z, 120, 1.5, 2.5, 1.5, 0.15);
        world.spawnParticles(ModParticles.STARLIGHT, x, y, z, 120, 2.0, 3.0, 2.0, 0.05);
        LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world, SpawnReason.TRIGGERED);
        if (lightning != null) {
            lightning.setPosition(x, altarPos.getY() + 1.0, z);
            lightning.setCosmetic(true);
            world.spawnEntity(lightning);
        }
        world.playSound(null, altarPos, SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.HOSTILE, 3.0f, 0.7f);
        world.playSound(null, altarPos, ModSounds.GUARDIAN_AMBIENT, SoundCategory.HOSTILE, 3.0f, 0.8f);

        StarGuardianEntity guardian = ModEntities.STAR_GUARDIAN.spawn(world, spawnPos, SpawnReason.EVENT);
        if (guardian != null) {
            guardian.setPersistent();
        }
        Text roar = Text.translatable("message.starfall.altar.summoned");
        for (ServerPlayerEntity player : world.getPlayers()) {
            if (player.squaredDistanceTo(x, y, z) <= 128 * 128) {
                player.sendMessage(roar, false);
            }
        }
        return guardian;
    }
}
