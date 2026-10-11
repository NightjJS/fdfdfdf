package com.nightjjs.starfall.registry;

import com.nightjjs.starfall.Starfall;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public final class ModSounds {
    private ModSounds() {
    }

    public static final SoundEvent STAR_IMPACT = register("star.impact");
    public static final SoundEvent STAR_TWINKLE = register("star.twinkle");
    public static final SoundEvent STAR_SIGHTING = register("star.sighting");
    public static final SoundEvent GATE_TRAVEL = register("gate.travel");
    public static final SoundEvent GUARDIAN_AMBIENT = register("guardian.ambient");
    public static final SoundEvent GUARDIAN_HURT = register("guardian.hurt");
    public static final SoundEvent GUARDIAN_DEATH = register("guardian.death");
    public static final SoundEvent GUARDIAN_ATTACK = register("guardian.attack");
    public static final SoundEvent WISP_AMBIENT = register("wisp.ambient");
    public static final SoundEvent WISP_HURT = register("wisp.hurt");
    public static final SoundEvent METEOR_SHOWER = register("event.meteor_shower");
    public static final SoundEvent REALM_AMBIENT = register("ambient.immortal_realm.loop");
    public static final SoundEvent REALM_MUSIC = register("music.immortal_realm");

    private static SoundEvent register(String name) {
        Identifier id = Starfall.id(name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void initialize() {
        // Static registration.
    }
}
