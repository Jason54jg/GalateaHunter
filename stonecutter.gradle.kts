plugins {
    id("dev.kikugie.stonecutter")
}

// Default version opened in the IDE / used by plain `./gradlew build`.
// Switch with the "Set active project to ..." Gradle tasks.
stonecutter active "1.21.11"

// 26.1+ replaced the immediate-mode GuiGraphics rendering API with a deferred
// "extract render state" model (GuiGraphics -> GuiGraphicsExtractor, render* -> extract*State)
// and renamed a couple of Fabric API entry points. Everything else stayed put between
// 1.21.11 and 26.1/26.2, so plain text swaps are enough instead of duplicating whole files.
stonecutter parameters {
    replacements {
        string(current.parsed >= "26.1") {
            replace("GuiGraphics", "GuiGraphicsExtractor")
            replace("render(", "extractRenderState(")
            replace("renderWidget", "extractWidgetRenderState")
            replace("renderContents", "extractContents")
            replace("renderBlurredBackground", "extractBlurredBackground")
            replace("renderBackground", "extractBackground")
            replace("drawString", "text")
            replace("ClientCommandManager", "ClientCommands")
            replace("registerReloader", "registerReloadListener")
            replace("getClientWorld", "getClientLevel")
            // Mixin @Inject target selector — a string, not a real identifier, so the
            // "render(" swap above doesn't touch it.
            replace("method = \"render\"", "method = \"extractRenderState\"")
        }

        // 26.2 renamed Minecraft#setScreen to #setScreenAndShow and moved the
        // current-screen getter behind the new Minecraft#gui field.
        string(current.parsed >= "26.2") {
            replace("setScreen(", "setScreenAndShow(")
            replace("client.screen", "client.gui.screen()")
        }
    }
}
