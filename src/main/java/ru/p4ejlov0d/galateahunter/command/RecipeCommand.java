package ru.p4ejlov0d.galateahunter.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import ru.p4ejlov0d.galateahunter.model.Shard;
import ru.p4ejlov0d.galateahunter.screen.RecipeScreen;
import ru.p4ejlov0d.galateahunter.service.ShardService;

import java.util.Collection;
import java.util.List;

public class RecipeCommand implements Command {
    private final SuggestionProvider<FabricClientCommandSource> shardSuggestionProvider = (context, builder) -> {
        final Collection<Shard> shards = List.copyOf(ShardService.INSTANCE.getShards());

        try {
            String string = StringArgumentType.getString(context, "recipe");

            if (string.isBlank()) for (Shard shard : shards)
                builder.suggest(shard.name);
            else {
                string = string.toLowerCase();

                for (Shard shard : shards) {
                    String name = shard.name.toLowerCase();

                    if (name.contains(string)) builder.suggest(shard.name);
                }
            }
        } catch (Exception e) {
            for (Shard shard : shards) {
                builder.suggest(shard.name);
            }
        }

        return builder.buildFuture();
    };

    @Override
    public void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess)
                -> dispatcher.register(ClientCommandManager.literal("ghrecipe").executes(this::openEmptyRecipeScreen)
                .then(ClientCommandManager.argument("recipe", StringArgumentType.greedyString())
                        .suggests(shardSuggestionProvider)
                        .executes(this::openFilledRecipeScreen)
                )
        ));
    }

    private int openEmptyRecipeScreen(@NotNull CommandContext<FabricClientCommandSource> context) {
        Minecraft client = context.getSource().getClient();
        client.execute(() -> client.setScreen(new RecipeScreen()));

        return 1;
    }

    private int openFilledRecipeScreen(@NotNull CommandContext<FabricClientCommandSource> context) {
        Minecraft client = context.getSource().getClient();
        client.execute(() -> client.setScreen(new RecipeScreen(StringArgumentType.getString(context, "recipe"))));

        return 1;
    }
}
