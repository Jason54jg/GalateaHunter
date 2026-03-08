package ru.p4ejlov0d.galateahunter.screen.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class IconButtonWidget extends PressableWidget {
    private final ButtonTextures textures;

    private Consumer<IconButtonWidget> onPress;

    public IconButtonWidget(int x, int y, int width, int height, Identifier icon, Identifier activeIcon, @Nullable Consumer<IconButtonWidget> onPress) {
        super(x, y, width, height, Text.empty());
        this.onPress = onPress;
        textures = new ButtonTextures(icon, activeIcon);
    }

    @Override
    public void onPress() {
        if (onPress != null) onPress.accept(this);
    }

    public void setOnPress(Consumer<IconButtonWidget> onPress) {
        this.onPress = onPress;
    }

    @Override
    protected void renderWidget(@NotNull DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.drawTexture(RenderLayer::getGuiTextured, textures.get(isNarratable(), isHovered()), getX(), getY(), 0f, 0f, width, height, width, height);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        this.appendDefaultNarrations(builder);
    }
}
