package ru.p4ejlov0d.galateahunter.screen.widget;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
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
public class ScalableWidget implements Renderable, LayoutElement, GuiEventListener, NarratableEntry {
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
    private MouseButtonEvent click;
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

        if (this.zoomIn != null) this.zoomIn.setOnPress(btn -> keyPressed(new KeyEvent(zoomInButton, 0, 0)));
        if (this.zoomOut != null) this.zoomOut.setOnPress(btn -> keyPressed(new KeyEvent(zoomOutButton, 0, 0)));

        if (this.content != null) {
            this.step = (int) Math.ceil((this.content.width + this.content.height) / 8D);
            this.zoomCeil = (this.content.width + this.content.height) * 8;
            // centered
            this.content.x = this.x + this.width / 2 - this.content.width / 2;
            this.content.y = this.y + this.height / 2 - this.content.height / 2;
        }

        updateInnerPositions();
    }

    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public void setDrawsBackground(boolean drawsBackground) {
        this.drawsBackground = drawsBackground;
    }

    @Override
    public final void extractRenderState(@NonNull GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
        if (!visible) return;
        if (background != null && drawsBackground)
            context.blit(RenderPipelines.GUI_TEXTURED, background, x, y, 0f, 0f, width, height, width, height);

        if (isMouseOver(mouseX, mouseY)) context.requestCursor(CursorTypes.RESIZE_ALL);

        if (content != null) {
            context.enableScissor(this.x, this.y, this.x + this.width, this.y + this.height);
            content.extractRenderState(context, mouseX, mouseY, deltaTicks);
            context.disableScissor();
        }

        if (zoomIn != null) zoomIn.extractRenderState(context, mouseX, mouseY, deltaTicks);
        if (zoomOut != null) zoomOut.extractRenderState(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    public final boolean mouseClicked(@NonNull MouseButtonEvent click, boolean doubled) {
        this.click = click;
        return visible && hovered && zoomIn != null && !zoomIn.mouseClicked(click, doubled) && zoomOut != null && !zoomOut.mouseClicked(click, doubled);
    }

    // probably bug
    @Override
    public final boolean mouseReleased(@NonNull MouseButtonEvent click) {
        if (this.click.equals(click)) {
            this.visitWidgets(child -> child.mouseClicked(click, false));
            return true;
        }

        return false;
    }

    @Override
    public final boolean isMouseOver(double mouseX, double mouseY) {
        return hovered = visible && mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    @Override
    public final boolean mouseDragged(@NonNull MouseButtonEvent click, double offsetX, double offsetY) {
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
        return mouseDragged(new MouseButtonEvent(mouseX, mouseY, new MouseButtonInfo(GLFW.GLFW_MOUSE_BUTTON_LEFT, 0)), horizontalAmount * sensitivity, verticalAmount * sensitivity);
    }

    @Override
    public final boolean keyPressed(@NonNull KeyEvent input) {
        if (!visible || content == null) return false;

        final int keyCode = input.key();

        if (keyCode == GLFW.GLFW_KEY_LEFT_CONTROL) controlPressed = true;
        if (keyCode == zoomInButton) return tryChangeZoom(step);
        if (keyCode == zoomOutButton) return tryChangeZoom(-step);

        return false;
    }

    @Override
    public final boolean keyReleased(@Nullable KeyEvent input) {
        controlPressed = false;
        return true;
    }

    private boolean tryChangeZoom(int step) {
        if (content.width + step <= 0 || content.height + step <= 0) return false; // max zoom out limit check
        if (step * 2 + content.width + content.height > zoomCeil) return false; // max zoom in limit check
        if (content.width == 0 || content.height == 0) return false;

        // save ratio
        final double k = (double) content.width / content.height;

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
    public @NonNull NarrationPriority narrationPriority() {
        if (focused) return NarrationPriority.FOCUSED;
        if (hovered) return NarrationPriority.HOVERED;
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(@Nullable NarrationElementOutput builder) {
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public void setX(int x) {
        this.x = x;
        updateInnerPositions();
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public void setY(int y) {
        this.y = y;
        updateInnerPositions();
    }

    @Override
    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
        updateInnerPositions();
    }

    @Override
    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
        updateInnerPositions();
    }

    public void updateInnerPositions() {
        if (zoomIn != null) {
            zoomIn.setPosition(this.x + this.width - 30, this.y + this.height - 60);
        }
        if (zoomOut != null) {
            zoomOut.setPosition(this.x + this.width - 30, this.y + this.height - 30);
        }
    }

    @Override
    public @NonNull ScreenRectangle getRectangle() {
        return LayoutElement.super.getRectangle();
    }

    @Override
    public void visitWidgets(@NonNull Consumer<AbstractWidget> consumer) {
        if (content != null) for (AbstractWidget child : content.children)
            consumer.accept(child);
    }

    @Environment(EnvType.CLIENT)
    public static class Builder {
        protected int x, y, width, height;
        protected Content content;
        protected Identifier background;
        protected WidgetSprites zoomIn, zoomOut;
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
            this.zoomIn = new WidgetSprites(zoomIn, zoomInHovered);
            return this;
        }

        public Builder zoomOut(Identifier zoomOut, Identifier zoomOutHovered) {
            this.zoomOut = new WidgetSprites(zoomOut, zoomOutHovered);
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
            this.zoomIn = new WidgetSprites(zoomIn, zoomInHovered);
            this.zoomOut = new WidgetSprites(zoomOut, zoomOutHovered);
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
                    zoomInButton.setTooltip(Tooltip.create(Component.literal(zoomInKeyName).withColor(0xFF808080)));
            }
            if (this.zoomOut != null) {
                zoomOutButton = new IconButtonWidget(this.x + this.width - 30, this.y + this.height - 30, 20, 20, this.zoomOut.enabled(), this.zoomOut.enabledFocused(), null);

                if (zoomOutKeyName != null)
                    zoomOutButton.setTooltip(Tooltip.create(Component.literal(zoomOutKeyName).withColor(0xFF808080)));
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
    public static class Content implements Renderable {
        private final DrawableContent render;

        /**
         * Used for loop children in Scalable Widget
         */
        public List<AbstractWidget> children = new ArrayList<>();

        private int x, y, width, height;

        public Content(int width, int height, @Nullable DrawableContent render) {
            this.width = width;
            this.height = height;
            this.render = render;
        }

        @Override
        public void extractRenderState(@NonNull GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
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
            void render(Content content, GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks);
        }
    }
}
