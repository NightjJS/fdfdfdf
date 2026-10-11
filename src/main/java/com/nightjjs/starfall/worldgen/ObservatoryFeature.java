package com.nightjjs.starfall.worldgen;

import com.mojang.serialization.Codec;
import com.nightjjs.starfall.Starfall;
import net.minecraft.block.BlockState;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

import java.util.Optional;

/**
 * Places the ruined Fallen Observatory template (data/starfall/structure/observatory.nbt) on the surface
 * of a floating island. Used as a configured feature so it can be placed with ordinary placement modifiers.
 */
public class ObservatoryFeature extends Feature<DefaultFeatureConfig> {
    public static final Identifier TEMPLATE = Starfall.id("observatory");
    private static final int MAX_DROP = 3;

    public ObservatoryFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        StructureWorldAccess world = context.getWorld();
        StructureTemplateManager manager = world.toServerWorld().getStructureTemplateManager();
        Optional<StructureTemplate> template = manager.getTemplate(TEMPLATE);
        if (template.isEmpty()) {
            return false;
        }
        BlockRotation rotation = BlockRotation.random(context.getRandom());
        Vec3i size = template.get().getRotatedSize(rotation);
        BlockPos origin = context.getOrigin();
        BlockPos corner = origin.add(-size.getX() / 2, 0, -size.getZ() / 2);

        // Every corner of the footprint must rest on solid ground within a few blocks; islands have cliffs.
        int lowest = Integer.MAX_VALUE;
        for (BlockPos probe : new BlockPos[]{corner, corner.add(size.getX() - 1, 0, 0),
                corner.add(0, 0, size.getZ() - 1), corner.add(size.getX() - 1, 0, size.getZ() - 1),
                origin}) {
            int ground = groundBelow(world, probe);
            if (ground == Integer.MIN_VALUE) {
                return false;
            }
            lowest = Math.min(lowest, ground);
        }
        if (origin.getY() - lowest > MAX_DROP) {
            return false;
        }
        BlockPos placeAt = new BlockPos(corner.getX(), lowest + 1, corner.getZ());
        // Rotate around the template's centre so every rotation keeps the same (square) footprint, which
        // stays inside the chunk neighbourhood a feature is allowed to write to.
        BlockPos pivot = new BlockPos(size.getX() / 2, 0, size.getZ() / 2);
        StructurePlacementData placement = new StructurePlacementData()
                .setRotation(rotation)
                .setPosition(pivot)
                .setIgnoreEntities(false);
        return template.get().place(world, placeAt, placeAt, placement, context.getRandom(), 2);
    }

    private static int groundBelow(StructureWorldAccess world, BlockPos from) {
        BlockPos.Mutable pos = from.mutableCopy();
        for (int i = 0; i <= MAX_DROP + 2; i++) {
            BlockState state = world.getBlockState(pos);
            if (!state.isAir() && state.isOpaque()) {
                return pos.getY();
            }
            pos.move(0, -1, 0);
        }
        return Integer.MIN_VALUE;
    }
}
