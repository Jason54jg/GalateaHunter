package ru.p4ejlov0d.galateahunter.screen.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.cursor.StandardCursors;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.input.MouseInput;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class ScalableWidget implements Drawable, Widget, Element, Selectable {
    private final Identifier background;
    private final IconButtonWidget zoomIn, zoomOut;
    private final int zoomInButton, zoomOutButton;
    private final Content content;

    public boolean visible = true;

    protected int x, y, width, height;
    protected boolean hovered;

    private boolean drawsBackground = true;
    private boolean focused;
    private boolean controlPressed;
    private Click click;
    private int step, zoomCeil;

    public ScalableWidget(int x, int y, int width, int height, @Nullable Content content, @Nullable Identifier background, @Nullable IconButtonWidget zoomIn, @Nullable IconButtonWidget zoomOut, int zoomInButton, int zoomOutButton) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.content = content;
        this.background = background;
        this.zoomIn = zoomIn;
        this.zoomOut = zoomOut;
        this.zoomInButton = zoomInButton;
        this.zoomOutButton = zoomOutButton;

        if (this.zoomIn != null) this.zoomIn.setOnPress(btn -> keyPressed(new KeyInput(zoomInButton, 0, 0)));
        if (this.zoomOut != null) this.zoomOut.setOnPress(btn -> keyPressed(new KeyInput(zoomOutButton, 0, 0)));

        if (this.content != null) {
            this.step = (int) Math.ceil((this.content.width + this.content.height) / 8D);
            this.zoomCeil = (this.content.width + this.content.height) * 8;
        }
    }

    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public void setDrawsBackground(boolean drawsBackground) {
        this.drawsBackground = drawsBackground;
    }

    @Override
    public final void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        if (!visible) return;
        if (background != null && drawsBackground)
            context.drawTexture(RenderPipelines.GUI_TEXTURED, background, x, y, 0f, 0f, width, height, width, height);

        if (content != null) {
            context.enableScissor(this.x, this.y, this.x + this.width, this.y + this.height);
            content.render(context, mouseX, mouseY, deltaTicks);
            context.disableScissor();
        }
        if (isMouseOver(mouseX, mouseY)) context.setCursor(StandardCursors.RESIZE_ALL);

        if (zoomIn != null) zoomIn.render(context, mouseX, mouseY, deltaTicks);
        if (zoomOut != null) zoomOut.render(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    public final boolean mouseClicked(Click click, boolean doubled) {
        this.click = click;
        return visible && hovered && zoomIn != null && !zoomIn.mouseClicked(click, doubled) && zoomOut != null && !zoomOut.mouseClicked(click, doubled);
    }

    @Override
    public final boolean mouseReleased(Click click) {
        if (this.click.equals(click)) {
            this.forEachChild(child -> child.mouseClicked(click, false));
            return true;
        }

        return false;
    }

    @Override
    public final boolean isMouseOver(double mouseX, double mouseY) {
        return hovered = visible && mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    @Override
    public final boolean mouseDragged(@NonNull Click click, double offsetX, double offsetY) {
        if (click.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT || content == null) return false;

        content.x += (int) offsetX;
        content.y += (int) offsetY;

        return true;
    }

    @Override
    public final boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        // zoom with touchpad or ctrl + scroll
        if (controlPressed && content != null) return verticalAmount < 0 ? tryChangeZoom(-step) : tryChangeZoom(step);

        final double sensitivity = 15; // I took it at random
        return mouseDragged(new Click(mouseX, mouseY, new MouseInput(GLFW.GLFW_MOUSE_BUTTON_LEFT, 0)), horizontalAmount * sensitivity, verticalAmount * sensitivity);
    }

    @Override
    public final boolean keyPressed(KeyInput input) {
        if (!visible || content == null) return false;

        final int keyCode = input.key();

        if (keyCode == GLFW.GLFW_KEY_LEFT_CONTROL) controlPressed = true;
        if (keyCode == zoomInButton) return tryChangeZoom(step);
        if (keyCode == zoomOutButton) return tryChangeZoom(-step);

        return false;
    }

    @Override
    public final boolean keyReleased(KeyInput input) {
        controlPressed = false;
        return true;
    }

    private boolean tryChangeZoom(int step) {
        if (content.width + step <= 0 || content.height + step <= 0) return false; // max zoom out limit check
        if (step * 2 + content.width + content.height > zoomCeil) return false; // max zoom in limit check
        if (content.width == 0 || content.height == 0) return false;

        // save ratio
        double k = (double) content.width / content.height;

        if (k < 1) content.width += (int) (step * k);
        else content.width += step;

        if (k > 1) content.height += (int) (step / k);
        else content.height += step;

        return true;
    }

    @Override
    public boolean isFocused() {
        return focused;
    }

    @Override
    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    @Override
    public SelectionType getType() {
        if (focused) return SelectionType.FOCUSED;
        if (hovered) return SelectionType.HOVERED;
        return SelectionType.NONE;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public ScreenRect getNavigationFocus() {
        return Widget.super.getNavigationFocus();
    }

    @Override
    public void forEachChild(Consumer<ClickableWidget> consumer) {
        if (content != null) for (ClickableWidget child : content.children)
            consumer.accept(child);
    }

    @Environment(EnvType.CLIENT)
    public static class Builder {
        protected int x, y, width, height;
        protected Content content;
        protected Identifier background;
        protected ButtonTextures zoomIn, zoomOut;
        protected int zoomInButton = GLFW.GLFW_KEY_EQUAL;
        protected int zoomOutButton = GLFW.GLFW_KEY_MINUS;

        public Builder x(int x) {
            this.x = x;
            return this;
        }

        public Builder y(int y) {
            this.y = y;
            return this;
        }

        public Builder position(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder width(int width) {
            this.width = width;
            return this;
        }

        public Builder height(int height) {
            this.height = height;
            return this;
        }

        public Builder content(int baseWidth, int baseHeight, @Nullable Content.DrawableContent render) {
            this.content = new Content(baseWidth, baseHeight, render);
            return this;
        }

        public Builder background(@Nullable Identifier background) {
            this.background = background;
            return this;
        }

        public Builder zoomIn(Identifier zoomIn, Identifier zoomInHovered) {
            this.zoomIn = new ButtonTextures(zoomIn, zoomInHovered);
            return this;
        }

        public Builder zoomOut(Identifier zoomOut, Identifier zoomOutHovered) {
            this.zoomOut = new ButtonTextures(zoomOut, zoomOutHovered);
            return this;
        }

        public Builder zoomInButton(int zoomInButton) {
            this.zoomInButton = zoomInButton;
            return this;
        }

        public Builder zoomOutButton(int zoomOutButton) {
            this.zoomOutButton = zoomOutButton;
            return this;
        }

        public Builder zoom(Identifier zoomIn, Identifier zoomInHovered, Identifier zoomOut, Identifier zoomOutHovered, int zoomInButton, int zoomOutButton) {
            this.zoomIn = new ButtonTextures(zoomIn, zoomInHovered);
            this.zoomOut = new ButtonTextures(zoomOut, zoomOutHovered);
            this.zoomInButton = zoomInButton;
            this.zoomOutButton = zoomOutButton;
            return this;
        }

        public ScalableWidget build() {
            IconButtonWidget zoomInButton = null;
            IconButtonWidget zoomOutButton = null;

            final String zoomInKeyName = GLFW.glfwGetKeyName(this.zoomInButton, GLFW.glfwGetKeyScancode(this.zoomInButton));
            final String zoomOutKeyName = GLFW.glfwGetKeyName(this.zoomOutButton, GLFW.glfwGetKeyScancode(this.zoomOutButton));

            if (this.zoomIn != null) {
                zoomInButton = new IconButtonWidget(this.x + this.width - 30, this.y + this.height - 60, 20, 20, this.zoomIn.enabled(), this.zoomIn.enabledFocused(), null);

                if (zoomInKeyName != null)
                    zoomInButton.setTooltip(Tooltip.of(Text.literal(zoomInKeyName).withColor(0xFF808080)));
            }
            if (this.zoomOut != null) {
                zoomOutButton = new IconButtonWidget(this.x + this.width - 30, this.y + this.height - 30, 20, 20, this.zoomOut.enabled(), this.zoomOut.enabledFocused(), null);

                if (zoomOutKeyName != null)
                    zoomOutButton.setTooltip(Tooltip.of(Text.literal(zoomOutKeyName).withColor(0xFF808080)));
            }

            // centered
            if (this.content != null) {
                this.content.x = this.x + this.width / 2 - this.content.width / 2;
                this.content.y = this.y + this.height / 2 - this.content.height / 2;
            }

            return new ScalableWidget(
                    this.x,
                    this.y,
                    this.width,
                    this.height,
                    this.content,
                    this.background,
                    zoomInButton,
                    zoomOutButton,
                    this.zoomInButton,
                    this.zoomOutButton
            );
        }
    }

    @Environment(EnvType.CLIENT)
    public static class Content implements Drawable {
        private final DrawableContent render;

        /**
         * Used for loop children in Scalable Widget
         */
        public List<ClickableWidget> children = new ArrayList<>();

        private int x, y, width, height;

        public Content(int width, int height, @Nullable DrawableContent render) {
            this.width = width;
            this.height = height;
            this.render = render;
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
            if (render != null) render.render(this, context, mouseX, mouseY, deltaTicks);
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        @FunctionalInterface
        @Environment(EnvType.CLIENT)
        public interface DrawableContent {
            void render(Content content, DrawContext context, int mouseX, int mouseY, float deltaTicks);
        }
    }
}
