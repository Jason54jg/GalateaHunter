package ru.p4ejlov0d.galateahunter.utils;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;

public final class ServerUtil {
    private static boolean messageSent = false;

    private ServerUtil() {
    }

    public static boolean isOnHypixel(Minecraft client) {
        ServerData server = client.getCurrentServer();
        return server != null && server.ip.contains("hypixel.net");
    }

    public static boolean isOnSkyblock(Minecraft client) {
        if (!isOnHypixel(client)) return false;

        ClientLevel world = client.level;
        if (world == null) return false;

        Objective objective = world.getScoreboard().getDisplayObjective(DisplaySlot.SIDEBAR);
        if (objective == null) return false;

        final String title = objective.getDisplayName().getString().toLowerCase();

        return title.contains("skyblock");
    }

    public static void onServerLogin(Minecraft client) {
        if (isOnSkyblock(client) && !messageSent) {
            if (client.player != null) {
                final MutableComponent name = Component.literal("[GH] ").withColor(0xFF355E3B);

                Component message = switch (LanguageResourceHandler.getInstance().getCurrentLangCode()) {
                    case "ru_ru" ->
                            name.append(Component.literal("Используйте '/gh' чтобы открыть настройки мода и '/ghrecipe (Не обязательно: название шарда)' чтобы открыть меню рецептов.").withColor(0xFFFFFFFF));
                    case null -> name.append("Hi!"); // never happens
                    default ->
                            name.append(Component.literal("Use '/gh' to open mod settings and '/ghrecipe (Optional: shard name)' to open recipe menu.").withColor(0xFFFFFFFF));
                };

                //? if >=26.1 {
                /*client.player.sendSystemMessage(message);
                *///?} else
                client.player.displayClientMessage(message, false);
                messageSent = true;
            }
        }
    }

    public static void registerServerJoinListener() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> WorkerManager.scheduleTask(() -> Minecraft.getInstance().execute(() -> onServerLogin(Minecraft.getInstance())), 3));
    }
}
