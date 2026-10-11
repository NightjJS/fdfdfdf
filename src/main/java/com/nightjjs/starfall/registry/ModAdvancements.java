package com.nightjjs.starfall.registry;

import com.nightjjs.starfall.Starfall;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/** Grants code-driven advancements (those using the "impossible" trigger with a "triggered" criterion). */
public final class ModAdvancements {
    private ModAdvancements() {
    }

    public static final Identifier IMMORTAL = Starfall.id("immortal");
    public static final Identifier METEOR_SHOWER = Starfall.id("meteor_shower");
    public static final Identifier SUMMON_GUARDIAN = Starfall.id("summon_guardian");
    public static final Identifier STARFORGED_SET = Starfall.id("starforged_set");
    public static final Identifier CALL_A_STAR = Starfall.id("call_a_star");
    public static final Identifier WISP_FRIEND = Starfall.id("wisp_friend");

    public static void grant(ServerPlayerEntity player, Identifier id) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }
        AdvancementEntry entry = server.getAdvancementLoader().get(id);
        if (entry == null) {
            return;
        }
        if (!player.getAdvancementTracker().getProgress(entry).isDone()) {
            player.getAdvancementTracker().grantCriterion(entry, "triggered");
        }
    }
}
