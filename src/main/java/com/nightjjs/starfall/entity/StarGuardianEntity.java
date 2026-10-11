package com.nightjjs.starfall.entity;

import com.nightjjs.starfall.entity.ai.StarBarrageGoal;
import com.nightjjs.starfall.registry.ModParticles;
import com.nightjjs.starfall.registry.ModSounds;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.FlyGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.world.World;

/**
 * The Star Guardian: a celestial spirit of crystal rods around a burning star heart.
 * Summoned at the Celestial Altar; drops the Guardian's Sigil that unlocks the Star Gate.
 */
public class StarGuardianEntity extends HostileEntity {
    private final ServerBossBar bossBar = new ServerBossBar(
            Text.translatable("entity.starfall.star_guardian"), BossBar.Color.YELLOW, BossBar.Style.NOTCHED_10);

    public StarGuardianEntity(EntityType<? extends StarGuardianEntity> type, World world) {
        super(type, world);
        this.moveControl = new FlightMoveControl(this, 20, true);
        this.experiencePoints = 150;
        this.setNoGravity(true);
    }

    public static DefaultAttributeContainer.Builder createStarGuardianAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.MAX_HEALTH, 320.0)
                .add(EntityAttributes.ATTACK_DAMAGE, 10.0)
                .add(EntityAttributes.FOLLOW_RANGE, 64.0)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.35)
                .add(EntityAttributes.FLYING_SPEED, 0.7)
                .add(EntityAttributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(EntityAttributes.ARMOR, 10.0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new StarBarrageGoal(this));
        this.goalSelector.add(4, new FlyGoal(this, 1.0));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 24.0f));
        this.goalSelector.add(7, new LookAroundGoal(this));
        this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        return new BirdNavigation(this, world);
    }

    public boolean isEnraged() {
        return this.getHealth() < this.getMaxHealth() * 0.5f;
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (!(this.getWorld() instanceof ServerWorld world)) {
            return;
        }
        // Hover: never sink below the ground and drift gently.
        if (this.getVelocity().y < -0.1) {
            this.setVelocity(this.getVelocity().multiply(1.0, 0.5, 1.0));
        }
        if (this.age % 2 == 0) {
            world.spawnParticles(ModParticles.STARLIGHT, getX(), getBodyY(0.6), getZ(), 2, 0.7, 0.9, 0.7, 0.02);
        }
        if (this.age % 6 == 0) {
            world.spawnParticles(ParticleTypes.END_ROD, getX(), getBodyY(0.5), getZ(), 1, 0.5, 0.8, 0.5, 0.01);
        }
        if (isEnraged() && this.age % 4 == 0) {
            world.spawnParticles(ParticleTypes.FLAME, getX(), getBodyY(0.5), getZ(), 2, 0.6, 0.8, 0.6, 0.02);
        }
    }

    @Override
    protected void mobTick(ServerWorld world) {
        super.mobTick(world);
        this.bossBar.setPercent(this.getHealth() / this.getMaxHealth());
    }

    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        super.onStartedTrackingBy(player);
        this.bossBar.addPlayer(player);
    }

    @Override
    public void onStoppedTrackingBy(ServerPlayerEntity player) {
        super.onStoppedTrackingBy(player);
        this.bossBar.removePlayer(player);
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        this.bossBar.clearPlayers();
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        super.onDeath(damageSource);
        if (this.getWorld() instanceof ServerWorld world) {
            world.spawnParticles(ParticleTypes.FLASH, getX(), getBodyY(0.5), getZ(), 1, 0, 0, 0, 0);
            world.spawnParticles(ParticleTypes.FIREWORK, getX(), getBodyY(0.5), getZ(), 200, 1.5, 1.5, 1.5, 0.3);
            world.spawnParticles(ModParticles.STARLIGHT, getX(), getBodyY(0.5), getZ(), 200, 3.0, 3.0, 3.0, 0.1);
            world.playSound(null, getX(), getY(), getZ(), ModSounds.GUARDIAN_DEATH, SoundCategory.HOSTILE, 4.0f, 1.0f);
            Text fallen = Text.translatable("message.starfall.guardian.defeated");
            for (ServerPlayerEntity player : world.getPlayers()) {
                if (player.squaredDistanceTo(this) <= 160 * 160) {
                    player.sendMessage(fallen, false);
                }
            }
        }
    }

    @Override
    public boolean isInvulnerableTo(ServerWorld world, DamageSource source) {
        return source.isIn(DamageTypeTags.IS_FALL) || source.isIn(DamageTypeTags.IS_DROWNING) || super.isInvulnerableTo(world, source);
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.GUARDIAN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.GUARDIAN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.GUARDIAN_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 2.0f;
    }

    @Override
    public boolean canTarget(LivingEntity target) {
        return !(target instanceof StarWispEntity) && super.canTarget(target);
    }
}
