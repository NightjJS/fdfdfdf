package com.nightjjs.starfall.event;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/**
 * {@code /starfall star} drops a shooting star near the caller (operators only). Handy for testing.
 */
public final class StarfallCommands {
    private StarfallCommands() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(CommandManager.literal("starfall")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.literal("star").executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                            boolean spawned = ShootingStarManager.trySpawnStar(context.getSource().getWorld(), player);
                            String key = spawned ? "command.starfall.star.success" : "command.starfall.star.failure";
                            context.getSource().sendFeedback(() -> Text.translatable(key), true);
                            return spawned ? 1 : 0;
                        }))));
    }
}
