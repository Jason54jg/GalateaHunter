package ru.p4ejlov0d.galateahunter.utils;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public final class ServerUtil {
    private static boolean messageSent = false;

    private ServerUtil() {
    }

    public static boolean isOnHypixel(MinecraftClient client) {
        ServerInfo server = client.getCurrentServerEntry();
        return server != null && server.address.contains("hypixel.net");
    }

    public static boolean isOnSkyblock(MinecraftClient client) {
        if (!isOnHypixel(client)) return false;

        ClientWorld world = client.world;
        if (world == null) return false;

        ScoreboardObjective objective = world.getScoreboard().getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (objective == null) return false;

        final String title = objective.getDisplayName().getString().toLowerCase();

        return title.contains("skyblock");
    }

    public static void onServerLogin(MinecraftClient client) {
        if (isOnSkyblock(client) && !messageSent) {
            if (client.player != null) {
                final MutableText name = Text.literal("[GH] ").withColor(0xFF355E3B);

                Text message = switch (LanguageResourceHandler.getInstance().getCurrentLangCode()) {
                    case "ru_ru" ->
                            name.append(Text.literal("Используйте '/gh' чтобы открыть настройки мода и '/ghrecipe (Не обязательно: название шарда)' чтобы открыть меню рецептов.").withColor(0xFFFFFFFF));
                    case null -> name.append("Hi!"); // never happens
                    default ->
                            name.append(Text.literal("Use '/gh' to open mod settings and '/ghrecipe (Optional: shard name)' to open recipe menu.").withColor(0xFFFFFFFF));
                };

                client.player.sendMessage(message, false);
                messageSent = true;
            }
        }
    }

    public static void registerServerJoinListener() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> WorkerManager.scheduleTask(() -> MinecraftClient.getInstance().execute(() -> onServerLogin(MinecraftClient.getInstance())), 3));
    }
}
