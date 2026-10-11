package com.nightjjs.starfall.entity.ai;

import com.nightjjs.starfall.entity.StarBoltEntity;
import com.nightjjs.starfall.entity.StarGuardianEntity;
import com.nightjjs.starfall.event.ShootingStarManager;
import com.nightjjs.starfall.event.StarTier;
import com.nightjjs.starfall.registry.ModSounds;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;

import java.util.EnumSet;

/**
 * The Star Guardian circles above its target, fires volleys of homing star bolts
 * and periodically calls a crimson star down onto the target.
 */
public class StarBarrageGoal extends Goal {
    private final StarGuardianEntity guardian;
    private int boltCooldown;
    private int meteorCooldown;
    private int orbitTicks;

    public StarBarrageGoal(StarGuardianEntity guardian) {
        this.guardian = guardian;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        LivingEntity target = guardian.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean shouldContinue() {
        return canStart();
    }

    @Override
    public void start() {
        boltCooldown = 30;
        meteorCooldown = 120;
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = guardian.getTarget();
        if (target == null) {
            return;
        }
        guardian.getLookControl().lookAt(target, 30.0f, 30.0f);
        orbitTicks++;

        double distanceSq = guardian.squaredDistanceTo(target);
        boolean enraged = guardian.isEnraged();

        // Orbit a point a few blocks above the target instead of flying straight at it.
        double angle = orbitTicks * (enraged ? 0.07 : 0.045);
        double radius = 7.0;
        double goalX = target.getX() + Math.cos(angle) * radius;
        double goalY = target.getY() + 4.0;
        double goalZ = target.getZ() + Math.sin(angle) * radius;
        if (distanceSq > 40.0 * 40.0) {
            guardian.getNavigation().startMovingTo(target.getX(), target.getY() + 4.0, target.getZ(), 1.2);
        } else {
            guardian.getNavigation().startMovingTo(goalX, goalY, goalZ, 1.0);
        }

        if (--boltCooldown <= 0 && distanceSq < 28.0 * 28.0) {
            fireVolley(enraged ? 6 : 4);
            boltCooldown = (enraged ? 35 : 55) + guardian.getRandom().nextInt(20);
        }
        if (--meteorCooldown <= 0 && distanceSq < 40.0 * 40.0 && guardian.getWorld() instanceof ServerWorld world) {
            ShootingStarManager.summonStrike(world, target.getBlockPos(), StarTier.CRIMSON, guardian);
            meteorCooldown = (enraged ? 120 : 200) + guardian.getRandom().nextInt(60);
        }
    }

    private void fireVolley(int bolts) {
        if (!(guardian.getWorld() instanceof ServerWorld world)) {
            return;
        }
        world.playSound(null, guardian.getX(), guardian.getY(), guardian.getZ(),
                ModSounds.GUARDIAN_ATTACK, SoundCategory.HOSTILE, 2.0f, 0.9f + guardian.getRandom().nextFloat() * 0.2f);
        for (int i = 0; i < bolts; i++) {
            StarBoltEntity.launch(world, guardian);
        }
    }
}
