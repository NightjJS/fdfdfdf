package com.nightjjs.starfall.event;

import com.nightjjs.starfall.registry.ModAdvancements;
import com.nightjjs.starfall.registry.ModSounds;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

/**
 * Meteor showers: on roughly every third night (and always on the first night) the sky rains stars
 * between 14500 and 18500 ticks of the day. Deterministic from the day number, so no save data is needed.
 */
public final class MeteorShowerManager {
    private MeteorShowerManager() {
    }

    private static final long SHOWER_START = 14500;
    private static final long SHOWER_END = 18500;

    private static boolean active;
    private static long activeDay = -1;
    private static long forcedUntil = -1;

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(MeteorShowerManager::onWorldTick);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            active = false;
            activeDay = -1;
            forcedUntil = -1;
        });
    }

    public static boolean isActive(ServerWorld world) {
        return world.getRegistryKey() == World.OVERWORLD && active;
    }

    /** Starts a shower right now for {@code ticks} ticks (command / self-test). */
    public static void force(ServerWorld world, int ticks) {
        forcedUntil = world.getTime() + ticks;
        if (!active) {
            begin(world);
        }
    }

    public static boolean isShowerNight(long day) {
        if (day == 0) {
            return true;
        }
        long hash = (day * 0x9E3779B97F4A7C15L) >>> 33;
        return hash % 3 == 0;
    }

    private static void onWorldTick(ServerWorld world) {
        if (world.getRegistryKey() != World.OVERWORLD) {
            return;
        }
        long timeOfDay = world.getTimeOfDay();
        long day = timeOfDay / 24000L;
        long tick = timeOfDay % 24000L;
        boolean forced = world.getTime() < forcedUntil;
        boolean scheduled = isShowerNight(day) && tick >= SHOWER_START && tick < SHOWER_END && world.getPlayers().size() > 0;

        if ((scheduled || forced) && !active) {
            activeDay = day;
            begin(world);
        } else if (active && !scheduled && !forced) {
            end(world);
        }
    }

    private static void begin(ServerWorld world) {
        active = true;
        Text title = Text.translatable("title.starfall.meteor_shower").formatted(Formatting.GOLD, Formatting.BOLD);
        Text subtitle = Text.translatable("title.starfall.meteor_shower.sub").formatted(Formatting.AQUA);
        for (ServerPlayerEntity player : world.getPlayers()) {
            player.networkHandler.sendPacket(new TitleFadeS2CPacket(15, 70, 25));
            player.networkHandler.sendPacket(new SubtitleS2CPacket(subtitle));
            player.networkHandler.sendPacket(new TitleS2CPacket(title));
            player.playSoundToPlayer(ModSounds.METEOR_SHOWER, SoundCategory.AMBIENT, 1.0f, 1.0f);
            player.sendMessage(Text.translatable("message.starfall.meteor_shower.begin"), false);
            ModAdvancements.grant(player, ModAdvancements.METEOR_SHOWER);
        }
    }

    private static void end(ServerWorld world) {
        active = false;
        for (ServerPlayerEntity player : world.getPlayers()) {
            player.sendMessage(Text.translatable("message.starfall.meteor_shower.end"), true);
        }
    }

    public static long getActiveDay() {
        return activeDay;
    }
}
