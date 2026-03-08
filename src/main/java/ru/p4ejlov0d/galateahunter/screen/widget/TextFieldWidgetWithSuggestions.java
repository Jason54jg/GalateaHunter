package ru.p4ejlov0d.galateahunter.screen.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.cursor.StandardCursors;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class TextFieldWidgetWithSuggestions<E> extends TextFieldWidget {
    public final Consumer<String> DEFAULT_CHANGED_LISTENER;

    protected final Function<E, String> toStringFunction;
    protected final Function<E, Identifier> toTextureFunction;
    protected final Function<E, RenderableSuggestion> toSuggestionRender;

    private final ButtonTextures textures;
    private final List<E> need2BeVisible = new ArrayList<>();
    private final Map<E, Suggestion> children = new HashMap<>();

    public Iterable<E> suggestions;
    protected boolean drawsBackground;
    private E selectedSuggestion;

    public TextFieldWidgetWithSuggestions(TextRenderer textRenderer, @Nullable Identifier normal, @Nullable Identifier focused, boolean drawsBackground, int x, int y, int width, int height, @Nullable Iterable<E> suggestions, @Nullable Text placeholder, @Nullable Function<E, String> toStringFunction, @Nullable Function<E, Identifier> toTextureFunction, @Nullable Function<E, RenderableSuggestion> toSuggestionRender) {
        super(textRenderer, x, y, width, height, Text.empty());
        this.textures = new ButtonTextures(normal, focused);
        this.suggestions = suggestions;
        this.drawsBackground = drawsBackground;
        this.toStringFunction = toStringFunction;
        this.toTextureFunction = toTextureFunction;
        this.toSuggestionRender = toSuggestionRender;
        this.DEFAULT_CHANGED_LISTENER = string -> {
            if (this.toStringFunction != null) {
                this.need2BeVisible.clear();
                this.children.clear();

                if (string == null || string.isBlank()) {
                    super.setSuggestion(null);
                    return;
                }

                string = string.toLowerCase();

                for (E suggestion : this.suggestions) {
                    final String suggestionString = this.toStringFunction.apply(suggestion).toLowerCase();

                    if (suggestionString.startsWith(string)) this.need2BeVisible.addFirst(suggestion);
                    else if (suggestionString.contains(string)) this.need2BeVisible.add(suggestion);
                }

                if (!this.need2BeVisible.isEmpty()) {
                    final String suggestion = this.toStringFunction.apply(this.need2BeVisible.getFirst()).toLowerCase();
                    super.setSuggestion(suggestion.substring(suggestion.indexOf(string) + string.length()));
                } else super.setSuggestion(null);
            }
        };

        super.setTextShadow(false);
        if (placeholder != null) super.setPlaceholder(placeholder);
        super.setChangedListener(this.DEFAULT_CHANGED_LISTENER);
    }

    // Influencing on super.render
    @Override
    public int getInnerWidth() {
        return this.width;
    }

    // Influencing on super.render
    @Override
    public boolean drawsBackground() {
        return false;
    }

    @Override
    public void renderWidget(@NonNull DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        if (this.drawsBackground)
            context.drawTexture(RenderPipelines.GUI_TEXTURED, textures.get(isInteractable(), isFocused()), getX(), getY(), 0f, 0f, width, height, width, height);
        super.renderWidget(context, mouseX, mouseY, deltaTicks);

        if (selectedSuggestion != null && toTextureFunction != null) {
            final int textureSize = height - 6;
            context.drawTexture(RenderPipelines.GUI_TEXTURED, toTextureFunction.apply(selectedSuggestion), getX() - height, getY() + height / 2 - textureSize / 2, 0f, 0f, textureSize, textureSize, textureSize, textureSize);
        }

        if (active && isFocused()) renderSuggestion(context, getY() + height + 1, 0, mouseX, mouseY, deltaTicks);
    }

    @Override
    public void onClick(@NonNull Click click, boolean doubled) {
        final double x = click.x();
        final double y = click.y();

        if (isMouseOverSearchField(x, y)) {
            final List<E> savedNeed2BeVisible = List.copyOf(this.need2BeVisible);
            final Map<E, Suggestion> savedChildren = Map.copyOf(this.children);

            super.setText("");

            this.need2BeVisible.addAll(savedNeed2BeVisible);
            this.children.putAll(savedChildren);
            this.setFocused(true);
        } else {
            for (Suggestion suggestion : children.values()) {
                if (suggestion.isHovered(x, y)) {
                    suggestion.press();
                    break;
                }
            }
        }
    }

    @Override
    protected void onDrag(Click click, double offsetX, double offsetY) {
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        for (Suggestion suggestion : children.values())
            if (suggestion.isHovered(mouseX, mouseY) && isFocused() && active) return true;

        return active && visible && isMouseOverSearchField(mouseX, mouseY);
    }

    public boolean isMouseOverSearchField(double mouseX, double mouseY) {
        return active && visible && mouseX >= getX() && mouseX < getX() + width && mouseY >= getY() && mouseY < getY() + height;
    }

    private void renderSuggestion(DrawContext context, int y, int idx, int mouseX, int mouseY, float deltaTicks) {
        if (idx >= need2BeVisible.size()) return;

        final E element = need2BeVisible.get(idx);

        final Suggestion suggestion = new Suggestion(getX(), y, width, height, toSuggestionRender != null ? toSuggestionRender.apply(element) : null, btn -> {
            selectedSuggestion = element;
            setText(toStringFunction != null ? toStringFunction.apply(element) : "");
            setSuggestion(null);
            setFocused(false);
        });

        children.put(element, suggestion);
        suggestion.render(context, mouseX, mouseY, deltaTicks);

        renderSuggestion(context, y + height + 1, idx + 1, mouseX, mouseY, deltaTicks);
    }

    public E getSelectedSuggestion() {
        return selectedSuggestion;
    }

    @FunctionalInterface
    @Environment(EnvType.CLIENT)
    public interface RenderableSuggestion {
        void render(@NonNull DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY, float deltaTicks);
    }

    @Environment(EnvType.CLIENT)
    public static class Builder<E> {
        protected Identifier normal, focused;
        protected boolean drawsBackground = true;
        protected int x, y, width, height;
        protected Text placeholder;
        protected Function<E, Identifier> toTextureFunction;
        protected Function<E, RenderableSuggestion> toSuggestionRender;
        protected Function<E, String> toStringFunction;

        public Builder<E> normalTexture(Identifier normal) {
            this.normal = normal;
            return this;
        }

        public Builder<E> focusedTexture(Identifier focused) {
            this.focused = focused;
            return this;
        }

        public Builder<E> drawsBackground(boolean drawsBackground) {
            this.drawsBackground = drawsBackground;
            return this;
        }

        public Builder<E> x(int x) {
            this.x = x;
            return this;
        }

        public Builder<E> y(int y) {
            this.y = y;
            return this;
        }

        public Builder<E> width(int width) {
            this.width = width;
            return this;
        }

        public Builder<E> height(int height) {
            this.height = height;
            return this;
        }

        public Builder<E> position(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder<E> size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder<E> placeholder(Text placeholder) {
            this.placeholder = placeholder;
            return this;
        }

        public Builder<E> toTextureFunction(Function<E, Identifier> toTextureFunction) {
            this.toTextureFunction = toTextureFunction;
            return this;
        }

        public Builder<E> toSuggestionRender(Function<E, RenderableSuggestion> toSuggestionRender) {
            this.toSuggestionRender = toSuggestionRender;
            return this;
        }

        public Builder<E> toStringFunction(Function<E, String> toStringFunction) {
            this.toStringFunction = toStringFunction;
            return this;
        }

        public TextFieldWidgetWithSuggestions<E> build(final @NotNull TextRenderer textRenderer, @Nullable Iterable<E> suggestions) {
            return new TextFieldWidgetWithSuggestions<>(
                    Objects.requireNonNull(textRenderer),
                    normal,
                    focused,
                    drawsBackground,
                    this.x,
                    this.y,
                    this.width,
                    this.height,
                    suggestions,
                    this.placeholder,
                    this.toStringFunction,
                    this.toTextureFunction,
                    this.toSuggestionRender
            );
        }
    }

    @Environment(EnvType.CLIENT)
    record Suggestion(
            int x,
            int y,
            int width,
            int height,
            @Nullable RenderableSuggestion render,
            @Nullable Consumer<Suggestion> onPress
    ) {
        void render(@NonNull DrawContext context, int mouseX, int mouseY, float deltaTicks) {
            if (render != null) render.render(context, x, y, width, height, mouseX, mouseY, deltaTicks);
            if (isHovered(mouseX, mouseY)) context.setCursor(StandardCursors.POINTING_HAND);
        }

        boolean isHovered(double mouseX, double mouseY) {
            return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
        }

        void press() {
            if (onPress != null) {
                onPress.accept(this);
                playClickSound(MinecraftClient.getInstance().getSoundManager());
            }
        }
    }
}
