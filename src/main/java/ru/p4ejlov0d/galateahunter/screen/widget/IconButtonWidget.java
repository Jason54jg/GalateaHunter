package ru.p4ejlov0d.galateahunter.screen.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class IconButtonWidget extends AbstractButton {
    private final WidgetSprites textures;

    private Consumer<IconButtonWidget> onPress;

    public IconButtonWidget(int x, int y, int width, int height, @Nullable Identifier icon, @Nullable Identifier activeIcon, @Nullable Consumer<IconButtonWidget> onPress) {
        super(x, y, width, height, Component.empty());
        this.onPress = onPress;
        this.textures = new WidgetSprites(icon, activeIcon);
    }

    @Override
    public void onPress(InputWithModifiers input) {
        if (onPress != null) onPress.accept(this);
    }

    public void setOnPress(Consumer<IconButtonWidget> onPress) {
        this.onPress = onPress;
    }

    @Override
    protected void renderContents(@NotNull GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
        context.blit(RenderPipelines.GUI_TEXTURED, textures.get(isActive(), isHovered()), getX(), getY(), 0f, 0f, width, height, width, height);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {
        this.defaultButtonNarrationText(builder);
    }
}
