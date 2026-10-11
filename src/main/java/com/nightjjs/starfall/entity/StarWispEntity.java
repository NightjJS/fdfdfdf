package com.nightjjs.starfall.entity;

import com.nightjjs.starfall.registry.ModAdvancements;
import com.nightjjs.starfall.registry.ModItems;
import com.nightjjs.starfall.registry.ModParticles;
import com.nightjjs.starfall.registry.ModSounds;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.goal.FlyGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/**
 * A Star Wisp: a shy drifting spark that lives in the Immortal Realm and gathers around fallen stars.
 * Feed it a star shard and it leaves star dust behind.
 */
public class StarWispEntity extends PathAwareEntity {
    public StarWispEntity(EntityType<? extends StarWispEntity> type, World world) {
        super(type, world);
        this.moveControl = new FlightMoveControl(this, 10, true);
        this.experiencePoints = 3;
        this.setNoGravity(true);
    }

    public static DefaultAttributeContainer.Builder createStarWispAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 10.0)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.FLYING_SPEED, 0.5)
                .add(EntityAttributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(2, new FlyGoal(this, 1.0));
        this.goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(4, new LookAroundGoal(this));
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        return new BirdNavigation(this, world);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (this.getWorld() instanceof ServerWorld world && this.age % 3 == 0) {
            world.spawnParticles(ModParticles.STARLIGHT, getX(), getBodyY(0.5), getZ(), 1, 0.2, 0.2, 0.2, 0.01);
        }
    }

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (!stack.isOf(ModItems.STAR_SHARD)) {
            return super.interactMob(player, hand);
        }
        if (!(this.getWorld() instanceof ServerWorld world)) {
            return ActionResult.SUCCESS;
        }
        if (!player.isCreative()) {
            stack.decrement(1);
        }
        world.spawnParticles(ParticleTypes.HEART, getX(), getBodyY(0.8), getZ(), 5, 0.3, 0.3, 0.3, 0.0);
        world.spawnParticles(ModParticles.STARLIGHT, getX(), getBodyY(0.5), getZ(), 12, 0.4, 0.4, 0.4, 0.05);
        world.playSound(null, getX(), getY(), getZ(), ModSounds.STAR_TWINKLE, SoundCategory.NEUTRAL, 1.0f, 1.2f);
        ItemStack dust = new ItemStack(ModItems.STAR_DUST, 2 + this.random.nextInt(2));
        world.spawnEntity(new ItemEntity(world, getX(), getY(), getZ(), dust));
        if (player instanceof ServerPlayerEntity serverPlayer) {
            ModAdvancements.grant(serverPlayer, ModAdvancements.WISP_FRIEND);
        }
        return ActionResult.SUCCESS;
    }

    /** Wisps are made of starlight: stars, star bolts and shockwaves pass right through them. */
    @Override
    public boolean isInvulnerableTo(ServerWorld world, DamageSource source) {
        return source.isIn(DamageTypeTags.IS_FALL)
                || source.isIn(DamageTypeTags.IS_EXPLOSION)
                || source.isOf(DamageTypes.FIREWORKS)
                || source.isOf(DamageTypes.FALLING_BLOCK)
                || super.isInvulnerableTo(world, source);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.WISP_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.WISP_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.WISP_HURT;
    }

    @Override
    protected float getSoundVolume() {
        return 0.6f;
    }
}
