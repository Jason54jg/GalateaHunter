package ru.p4ejlov0d.galateahunter.screen.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.input.AbstractInput;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class IconButtonWidget extends PressableWidget {
    private final ButtonTextures textures;

    private Consumer<IconButtonWidget> onPress;

    public IconButtonWidget(int x, int y, int width, int height, @Nullable Identifier icon, @Nullable Identifier activeIcon, @Nullable Consumer<IconButtonWidget> onPress) {
        super(x, y, width, height, Text.empty());
        this.onPress = onPress;
        this.textures = new ButtonTextures(icon, activeIcon);
    }

    @Override
    public void onPress(AbstractInput input) {
        if (onPress != null) onPress.accept(this);
    }

    public void setOnPress(Consumer<IconButtonWidget> onPress) {
        this.onPress = onPress;
    }

    @Override
    protected void drawIcon(@NotNull DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.drawTexture(RenderPipelines.GUI_TEXTURED, textures.get(isInteractable(), isHovered()), getX(), getY(), 0f, 0f, width, height, width, height);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        this.appendDefaultNarrations(builder);
    }
}
