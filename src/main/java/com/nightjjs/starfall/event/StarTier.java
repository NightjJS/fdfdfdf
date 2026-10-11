package com.nightjjs.starfall.event;

import com.nightjjs.starfall.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.random.Random;

/** The three kinds of shooting star. */
public enum StarTier {
    GOLD(0xFFE86A, 0xF2B51E, 8.0f, 6.0f),
    AZURE(0x8FD3FF, 0x3F9BE0, 10.0f, 7.0f),
    CRIMSON(0xFF6A5A, 0xD4262E, 12.0f, 9.0f);

    public final int trailColor;
    public final int coreColor;
    /** Damage dealt to anything the star lands on. */
    public final float crushDamage;
    /** Peak damage of the landing shockwave. */
    public final float shockwaveDamage;

    StarTier(int trailColor, int coreColor, float crushDamage, float shockwaveDamage) {
        this.trailColor = trailColor;
        this.coreColor = coreColor;
        this.crushDamage = crushDamage;
        this.shockwaveDamage = shockwaveDamage;
    }

    public Block block() {
        return switch (this) {
            case GOLD -> ModBlocks.FALLEN_STAR_BLOCK;
            case AZURE -> ModBlocks.AZURE_STAR_BLOCK;
            case CRIMSON -> ModBlocks.CRIMSON_STAR_BLOCK;
        };
    }

    public String translationKey() {
        return "message.starfall.star_fell." + name().toLowerCase();
    }

    /** Weighted roll: ordinary nights favour gold; meteor showers favour crimson. */
    public static StarTier roll(Random random, boolean meteorShower) {
        int r = random.nextInt(100);
        if (meteorShower) {
            if (r < 45) return CRIMSON;
            if (r < 75) return AZURE;
            return GOLD;
        }
        if (r < 70) return GOLD;
        if (r < 93) return AZURE;
        return CRIMSON;
    }
}
