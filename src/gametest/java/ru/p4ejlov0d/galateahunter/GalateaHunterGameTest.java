package ru.p4ejlov0d.galateahunter;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.TestInput;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.world.Difficulty;
import org.jetbrains.annotations.NotNull;
import ru.p4ejlov0d.galateahunter.model.Shard;
import ru.p4ejlov0d.galateahunter.model.ShardData;
import ru.p4ejlov0d.galateahunter.screen.RecipeScreen;
import ru.p4ejlov0d.galateahunter.screen.widget.EditBoxWithSuggestions;
import ru.p4ejlov0d.galateahunter.screen.widget.IconButtonWidget;
import ru.p4ejlov0d.galateahunter.service.BazaarService;
import ru.p4ejlov0d.galateahunter.service.ShardService;

import java.lang.reflect.Field;

@SuppressWarnings("ALL")
public class GalateaHunterGameTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayerContext = context.worldBuilder()
                .adjustSettings(worldCreator -> {
                    worldCreator.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);
                    worldCreator.setDifficulty(Difficulty.PEACEFUL);
                }).create()
        ) {
            singleplayerContext.getClientLevel().waitForChunksRender();

            recipeCommandWithoutArgsTest(context);
            recipeCommandWithArgsTest(context);
            mainScreenCommandTest(context);
            recipeSelectTest(context);
            recipeSettingsButtonTest(context);
            recipeOverviewListButtonTest(context);
            updatePricesButtonTest(context);
        }
    }

    private void recipeCommandWithoutArgsTest(@NotNull ClientGameTestContext context) {
        context.runOnClient(client -> client.player.connection.sendCommand("ghrecipe"));
        context.waitForScreen(RecipeScreen.class);
    }

    private void recipeCommandWithArgsTest(@NotNull ClientGameTestContext context) {
        final String testText = "Wyvern Shard";

        context.runOnClient(client -> client.player.connection.sendCommand("ghrecipe " + testText));
        context.waitForScreen(RecipeScreen.class);
        context.waitFor(client -> {
            for (GuiEventListener el : client.gui.screen().children()) {
                if (el instanceof EditBoxWithSuggestions child) {
                    return child.getValue().equals(testText);
                }
            }

            return false;
        });
    }

    private void mainScreenCommandTest(@NotNull ClientGameTestContext context) {
        context.runOnClient(client -> client.player.connection.sendCommand("gh"));
        context.waitForScreen(Screen.class);
    }

    private void recipeSelectTest(@NotNull ClientGameTestContext context) {
        context.runOnClient(client -> client.player.connection.sendCommand("ghrecipe Wither"));
        context.waitForScreen(RecipeScreen.class);

        final TestInput input = context.getInput();

        input.moveCursor(0d, -70d);
        // delay
        input.holdKeyFor(0, 80);
        input.pressMouse(0);

        context.waitFor(client -> {
            for (GuiEventListener el : client.gui.screen().children()) {
                if (el instanceof EditBoxWithSuggestions child) {
                    return child.getValue().equals("Wither Specter");
                }
            }

            return false;
        });
    }

    private void recipeSettingsButtonTest(@NotNull ClientGameTestContext context) {
        context.runOnClient(client -> client.gui.setScreen(new RecipeScreen()));
        context.waitForScreen(RecipeScreen.class);

        final TestInput input = context.getInput();

        input.setCursorPos(800d, 40d);
        // delay
        input.holdKeyFor(0, 80);
        input.pressMouse(0);

        context.waitFor(client -> !(client.gui.screen() instanceof RecipeScreen || client.gui.screen() == null));
    }

    private void recipeOverviewListButtonTest(@NotNull ClientGameTestContext context) {
        context.runOnClient(client -> client.player.connection.sendCommand("ghrecipe o"));
        context.waitForScreen(RecipeScreen.class);

        final TestInput input = context.getInput();

        input.setCursorPos(400d, 100d);
        // delay
        input.holdKeyFor(0, 80);
        input.pressMouse(0);
        input.setCursorPos(12d, 45d);
        input.pressMouse(0);

        context.waitFor(client -> {
            if (!(client.gui.screen() instanceof RecipeScreen recipeScreen)) return false;
            try {
                final Field close = recipeScreen.getClass().getDeclaredField("close");
                close.setAccessible(true);

                final boolean isVisible = ((IconButtonWidget) close.get(recipeScreen)).visible;

                return !isVisible;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void updatePricesButtonTest(@NotNull ClientGameTestContext context) {
        context.runOnClient(client -> client.gui.setScreen(new RecipeScreen()));
        context.waitForScreen(RecipeScreen.class);

        final Shard testShard = ShardService.INSTANCE.get("l4");
        final ShardData testShardData = BazaarService.INSTANCE.get(testShard);

        final TestInput input = context.getInput();

        input.setCursorPos(775d, 40d);
        // delay
        input.holdKeyFor(0, 80);
        input.pressMouse(0);

        context.waitFor(client -> BazaarService.INSTANCE.get(testShard) != null);
        assert (testShardData != BazaarService.INSTANCE.get(testShard));
    }
}
