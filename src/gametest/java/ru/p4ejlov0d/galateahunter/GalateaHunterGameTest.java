package ru.p4ejlov0d.galateahunter;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.world.Difficulty;
import org.jetbrains.annotations.NotNull;
import ru.p4ejlov0d.galateahunter.screen.RecipeScreen;
import ru.p4ejlov0d.galateahunter.screen.widget.IconButtonWidget;
import ru.p4ejlov0d.galateahunter.screen.widget.TextFieldWidgetWithSuggestions;

import java.lang.reflect.Field;

@SuppressWarnings("ALL")
public class GalateaHunterGameTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayerContext = context.worldBuilder()
                .adjustSettings(worldCreator -> {
                    worldCreator.setGameMode(WorldCreator.Mode.CREATIVE);
                    worldCreator.setDifficulty(Difficulty.PEACEFUL);
                }).create()
        ) {
            singleplayerContext.getClientWorld().waitForChunksRender();

            recipeCommandWithoutArgsTest(context);
            recipeCommandWithArgsTest(context);
            mainScreenCommandTest(context);
            recipeSelectTest(context);
            recipeSettingsButtonTest(context);
            recipeOverviewListButtonTest(context);
        }
    }

    private void recipeCommandWithoutArgsTest(@NotNull ClientGameTestContext context) {
        context.runOnClient(client -> client.player.networkHandler.sendChatCommand("ghrecipe"));

        context.waitForScreen(RecipeScreen.class);
    }

    private void recipeCommandWithArgsTest(@NotNull ClientGameTestContext context) {
        context.runOnClient(client -> client.player.networkHandler.sendChatCommand("ghrecipe Wyvern Shard"));

        context.waitForScreen(RecipeScreen.class);
        context.waitFor(client -> {
            for (Element el : client.currentScreen.children()) {
                if (el instanceof TextFieldWidgetWithSuggestions child) {
                    return child.getText().equals("Wyvern Shard");
                }
            }

            return false;
        });
    }

    private void mainScreenCommandTest(@NotNull ClientGameTestContext context) {
        context.runOnClient(client -> client.player.networkHandler.sendChatCommand("gh"));
        context.waitForScreen(Screen.class);
    }

    private void recipeSelectTest(@NotNull ClientGameTestContext context) {
        context.runOnClient(client -> client.player.networkHandler.sendChatCommand("ghrecipe Wither"));

        context.waitForScreen(RecipeScreen.class);
        context.getInput().moveCursor(0d, -70d);

        // delay
        context.getInput().holdKeyFor(0, 80);

        context.getInput().pressMouse(0);

        context.waitFor(client -> {
            for (Element el : client.currentScreen.children()) {
                if (el instanceof TextFieldWidgetWithSuggestions child) {
                    return child.getText().equals("Wither Specter");
                }
            }

            return false;
        });
    }

    private void recipeSettingsButtonTest(@NotNull ClientGameTestContext context) {
        context.runOnClient(client -> client.setScreen(new RecipeScreen()));
        context.waitForScreen(RecipeScreen.class);

        context.getInput().setCursorPos(800d, 40d);
        // delay
        context.getInput().holdKeyFor(0, 80);

        context.getInput().pressMouse(0);

        context.waitFor(client -> !(client.currentScreen instanceof RecipeScreen));
    }

    private void recipeOverviewListButtonTest(@NotNull ClientGameTestContext context) {
        context.runOnClient(client -> client.player.networkHandler.sendChatCommand("ghrecipe o"));
        context.waitForScreen(RecipeScreen.class);

        context.getInput().setCursorPos(400d, 100d);
        // delay
        context.getInput().holdKeyFor(0, 80);

        context.getInput().pressMouse(0);
        context.getInput().setCursorPos(12d, 45d);
        context.getInput().pressMouse(0);

        context.waitFor(client -> {
            RecipeScreen recipeScreen = (RecipeScreen) client.currentScreen;
            try {
                Field close = recipeScreen.getClass().getDeclaredField("close");
                close.setAccessible(true);
                boolean isVisible = ((IconButtonWidget) close.get(recipeScreen)).visible;

                return !isVisible;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
