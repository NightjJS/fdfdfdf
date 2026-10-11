package com.nightjjs.starfall.block;

import com.nightjjs.starfall.registry.ModBlocks;
import com.nightjjs.starfall.registry.ModParticles;
import com.nightjjs.starfall.registry.ModSounds;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.nightjjs.starfall.registry.ModDimensions.IMMORTAL_REALM;

/**
 * Moves entities between the Overworld and the Immortal Realm through Star Gates.
 * The realm side always arrives at a sanctuary platform built around (0, 100, 0).
 */
public final class StarGateTravel {
    private StarGateTravel() {
    }

    public static final BlockPos REALM_GATE_POS = new BlockPos(0, 100, 0);
    private static final int PLATFORM_RADIUS = 4;
    private static final Map<UUID, GlobalPos> RETURN_GATES = new ConcurrentHashMap<>();

    /** Sends a player through the gate at {@code gatePos}. Returns false when the destination is unavailable. */
    public static boolean travel(ServerPlayerEntity player, ServerWorld from, BlockPos gatePos) {
        MinecraftServer server = from.getServer();
        boolean toRealm = from.getRegistryKey() != IMMORTAL_REALM;
        RegistryKey<World> targetKey = toRealm ? IMMORTAL_REALM : World.OVERWORLD;
        ServerWorld target = server.getWorld(targetKey);
        if (target == null) {
            player.sendMessage(Text.translatable("message.starfall.gate.no_realm"), true);
            return false;
        }

        BlockPos destination;
        if (toRealm) {
            RETURN_GATES.put(player.getUuid(), GlobalPos.create(from.getRegistryKey(), gatePos));
            destination = ensureRealmSanctuary(target).add(2, 0, 0);
        } else {
            GlobalPos back = RETURN_GATES.get(player.getUuid());
            if (back != null && back.dimension() == targetKey && target.getBlockState(back.pos()).isOf(ModBlocks.STAR_GATE)) {
                destination = back.pos().add(1, 0, 0);
            } else {
                destination = overworldArrival(target);
            }
        }

        departureEffects(from, gatePos);
        Entity result = teleport(player, target, destination);
        if (result instanceof ServerPlayerEntity arrived) {
            arrivalEffects(target, destination);
            arrived.sendMessage(Text.translatable(toRealm ? "message.starfall.gate.enter_realm" : "message.starfall.gate.return"), true);
        }
        return true;
    }

    /** Teleports any entity to a block position in another world and returns the resulting entity. */
    public static Entity teleport(Entity entity, ServerWorld target, BlockPos destination) {
        Vec3d at = Vec3d.ofBottomCenter(destination);
        TeleportTarget teleportTarget = new TeleportTarget(target, at, Vec3d.ZERO, entity.getYaw(), entity.getPitch(), TeleportTarget.NO_OP);
        return entity.teleportTo(teleportTarget);
    }

    /** Builds (once) the starstone sanctuary with the return gate in the realm and returns the gate position. */
    public static BlockPos ensureRealmSanctuary(ServerWorld realm) {
        BlockPos gate = REALM_GATE_POS;
        realm.getChunk(gate); // force-load before inspecting
        if (realm.getBlockState(gate).isOf(ModBlocks.STAR_GATE)) {
            return gate;
        }
        BlockState bricks = ModBlocks.STARSTONE_BRICKS.getDefaultState();
        BlockState polished = ModBlocks.POLISHED_STARSTONE.getDefaultState();
        BlockState air = net.minecraft.block.Blocks.AIR.getDefaultState();
        for (int dx = -PLATFORM_RADIUS; dx <= PLATFORM_RADIUS; dx++) {
            for (int dz = -PLATFORM_RADIUS; dz <= PLATFORM_RADIUS; dz++) {
                boolean edge = Math.abs(dx) == PLATFORM_RADIUS || Math.abs(dz) == PLATFORM_RADIUS;
                realm.setBlockState(gate.add(dx, -1, dz), edge ? polished : bricks);
                for (int dy = 0; dy <= 3; dy++) {
                    realm.setBlockState(gate.add(dx, dy, dz), air);
                }
            }
        }
        BlockState lantern = ModBlocks.STAR_LANTERN.getDefaultState();
        for (int dx : new int[]{-PLATFORM_RADIUS, PLATFORM_RADIUS}) {
            for (int dz : new int[]{-PLATFORM_RADIUS, PLATFORM_RADIUS}) {
                realm.setBlockState(gate.add(dx, 0, dz), bricks);
                realm.setBlockState(gate.add(dx, 1, dz), lantern);
            }
        }
        realm.setBlockState(gate, ModBlocks.STAR_GATE.getDefaultState());
        return gate;
    }

    private static BlockPos overworldArrival(ServerWorld overworld) {
        BlockPos spawn = overworld.getSpawnPos();
        return overworld.getTopPosition(Heightmap.Type.MOTION_BLOCKING, spawn);
    }

    private static void departureEffects(ServerWorld world, BlockPos gatePos) {
        double x = gatePos.getX() + 0.5;
        double y = gatePos.getY() + 1.0;
        double z = gatePos.getZ() + 0.5;
        world.playSound(null, gatePos, ModSounds.GATE_TRAVEL, SoundCategory.BLOCKS, 1.0f, 1.0f);
        world.spawnParticles(ParticleTypes.REVERSE_PORTAL, x, y, z, 60, 0.6, 0.8, 0.6, 0.2);
        world.spawnParticles(ModParticles.STARLIGHT, x, y, z, 40, 0.8, 1.0, 0.8, 0.05);
    }

    private static void arrivalEffects(ServerWorld world, BlockPos pos) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.0;
        double z = pos.getZ() + 0.5;
        world.playSound(null, pos, ModSounds.GATE_TRAVEL, SoundCategory.BLOCKS, 1.0f, 1.2f);
        world.spawnParticles(ParticleTypes.END_ROD, x, y, z, 40, 0.6, 0.8, 0.6, 0.1);
        world.spawnParticles(ModParticles.STARLIGHT, x, y, z, 40, 0.8, 1.0, 0.8, 0.05);
    }
}
