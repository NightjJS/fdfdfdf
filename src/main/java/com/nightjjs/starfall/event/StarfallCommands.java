package com.nightjjs.starfall.event;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.nightjjs.starfall.block.CelestialAltarBlock;
import com.nightjjs.starfall.block.StarGateTravel;
import com.nightjjs.starfall.registry.ModDimensions;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

/**
 * Operator commands:
 * <ul>
 *   <li>{@code /starfall star [gold|azure|crimson]} drops a shooting star near you.</li>
 *   <li>{@code /starfall shower [seconds]} starts a meteor shower.</li>
 *   <li>{@code /starfall guardian} summons the Star Guardian above you.</li>
 *   <li>{@code /starfall realm} teleports you to the Immortal Realm sanctuary (or back).</li>
 * </ul>
 */
public final class StarfallCommands {
    private StarfallCommands() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(CommandManager.literal("starfall")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.literal("star")
                                .executes(context -> star(context.getSource(), StarTier.GOLD))
                                .then(CommandManager.literal("gold").executes(context -> star(context.getSource(), StarTier.GOLD)))
                                .then(CommandManager.literal("azure").executes(context -> star(context.getSource(), StarTier.AZURE)))
                                .then(CommandManager.literal("crimson").executes(context -> star(context.getSource(), StarTier.CRIMSON))))
                        .then(CommandManager.literal("shower")
                                .executes(context -> shower(context.getSource(), 120))
                                .then(CommandManager.argument("seconds", IntegerArgumentType.integer(5, 1200))
                                        .executes(context -> shower(context.getSource(), IntegerArgumentType.getInteger(context, "seconds")))))
                        .then(CommandManager.literal("guardian").executes(context -> guardian(context.getSource())))
                        .then(CommandManager.literal("realm").executes(context -> realm(context.getSource())))));
    }

    private static int star(ServerCommandSource source, StarTier tier) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        boolean spawned = ShootingStarManager.trySpawnStar(source.getWorld(), player, tier);
        String key = spawned ? "command.starfall.star.success" : "command.starfall.star.failure";
        source.sendFeedback(() -> Text.translatable(key), true);
        return spawned ? 1 : 0;
    }

    private static int shower(ServerCommandSource source, int seconds) {
        ServerWorld overworld = source.getServer().getOverworld();
        MeteorShowerManager.force(overworld, seconds * 20);
        source.sendFeedback(() -> Text.translatable("command.starfall.shower.success", seconds), true);
        return 1;
    }

    private static int guardian(ServerCommandSource source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        CelestialAltarBlock.summonGuardian(source.getWorld(), player.getBlockPos());
        source.sendFeedback(() -> Text.translatable("command.starfall.guardian.success"), true);
        return 1;
    }

    private static int realm(ServerCommandSource source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ServerWorld world = source.getWorld();
        if (ModDimensions.isImmortalRealm(world)) {
            StarGateTravel.travel(player, world, StarGateTravel.REALM_GATE_POS);
        } else {
            ServerWorld realm = source.getServer().getWorld(ModDimensions.IMMORTAL_REALM);
            if (realm == null) {
                source.sendError(Text.translatable("message.starfall.gate.no_realm"));
                return 0;
            }
            StarGateTravel.travel(player, world, player.getBlockPos());
        }
        return 1;
    }
}
