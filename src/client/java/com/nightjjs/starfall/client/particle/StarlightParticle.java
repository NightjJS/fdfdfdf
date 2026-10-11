package com.nightjjs.starfall.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;

/**
 * A slowly drifting, twinkling mote of starlight. Fully bright, fades out with age.
 */
public class StarlightParticle extends SpriteBillboardParticle {
    private final SpriteProvider spriteProvider;

    protected StarlightParticle(ClientWorld world, double x, double y, double z,
                                double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.spriteProvider = spriteProvider;
        this.velocityX = velocityX + (world.random.nextDouble() - 0.5) * 0.01;
        this.velocityY = velocityY + 0.005 + world.random.nextDouble() * 0.01;
        this.velocityZ = velocityZ + (world.random.nextDouble() - 0.5) * 0.01;
        this.scale = 0.08f + world.random.nextFloat() * 0.08f;
        this.maxAge = 25 + world.random.nextInt(25);
        this.gravityStrength = 0.0f;
        this.collidesWithWorld = false;
        float warmth = world.random.nextFloat();
        this.red = 1.0f;
        this.green = 0.85f + warmth * 0.15f;
        this.blue = 0.55f + warmth * 0.45f;
        this.setSpriteForAge(spriteProvider);
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public int getBrightness(float tint) {
        return 15728880;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteForAge(spriteProvider);
        float life = (float) this.age / (float) this.maxAge;
        this.alpha = life < 0.2f ? life / 0.2f : 1.0f - (life - 0.2f) / 0.8f;
        this.velocityX *= 0.96;
        this.velocityZ *= 0.96;
    }

    public static class Factory implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientWorld world, double x, double y, double z,
                                       double velocityX, double velocityY, double velocityZ) {
            return new StarlightParticle(world, x, y, z, velocityX, velocityY, velocityZ, spriteProvider);
        }
    }
}
