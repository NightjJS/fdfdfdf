package com.nightjjs.starfall.registry;

import com.nightjjs.starfall.Starfall;
import com.nightjjs.starfall.entity.StarGuardianEntity;
import com.nightjjs.starfall.entity.StarWispEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public final class ModEntities {
    private ModEntities() {
    }

    public static final EntityType<StarGuardianEntity> STAR_GUARDIAN = register("star_guardian",
            EntityType.Builder.create(StarGuardianEntity::new, SpawnGroup.MONSTER)
                    .dimensions(1.0f, 2.2f)
                    .maxTrackingRange(10)
                    .makeFireImmune());

    public static final EntityType<StarWispEntity> STAR_WISP = register("star_wisp",
            EntityType.Builder.create(StarWispEntity::new, SpawnGroup.AMBIENT)
                    .dimensions(0.5f, 0.6f)
                    .maxTrackingRange(8)
                    .makeFireImmune());

    private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        RegistryKey<EntityType<?>> key = RegistryKey.of(RegistryKeys.ENTITY_TYPE, Starfall.id(name));
        return Registry.register(Registries.ENTITY_TYPE, key, builder.build(key));
    }

    public static void initialize() {
        FabricDefaultAttributeRegistry.register(STAR_GUARDIAN, StarGuardianEntity.createStarGuardianAttributes());
        FabricDefaultAttributeRegistry.register(STAR_WISP, StarWispEntity.createStarWispAttributes());
    }
}
