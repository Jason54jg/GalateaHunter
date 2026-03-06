package ru.p4ejlov0d.galateahunter.screen.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import ru.p4ejlov0d.galateahunter.model.Shard;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import static ru.p4ejlov0d.galateahunter.GalateaHunter.LOGGER;
import static ru.p4ejlov0d.galateahunter.GalateaHunter.MOD_ID;

@Environment(EnvType.CLIENT)
public class TextFieldWidgetWithSuggestions extends TextFieldWidget {
    private final ButtonTextures TEXTURES = new ButtonTextures(Identifier.of(MOD_ID, "textures/gui/search-field.png"), Identifier.of(MOD_ID, "textures/gui/search-field-highlighted.png"));
    private final TextRenderer textRenderer;
    private final List<Shard> need2BeVisible = new ArrayList<>();
    private final Map<Shard, SuggestionWidget> children = new HashMap<>();

    public Collection<Shard> suggestions;

    private boolean isSelectedSuggestion = false;
    private Shard selectedSuggestion;

    public TextFieldWidgetWithSuggestions(TextRenderer textRenderer, int x, int y, int width, int height) {
        this(textRenderer, x, y, width, height, null);
    }

    public TextFieldWidgetWithSuggestions(TextRenderer textRenderer, int x, int y, int width, int height, Collection<Shard> suggestions) {
        super(textRenderer, x, y, width, height, Text.empty());

        this.textRenderer = textRenderer;
        this.suggestions = suggestions;

        super.setChangedListener(string -> {
            need2BeVisible.clear();
            children.clear();

            if (string == null || string.isBlank()) {
                super.setSuggestion(null);
                return;
            }

            string = string.toLowerCase();

            for (Shard suggestion : this.suggestions) {
                final String name = suggestion.name.toLowerCase();

                if (name.startsWith(string)) need2BeVisible.addFirst(suggestion);
                else if (name.contains(string)) need2BeVisible.add(suggestion);
            }

            if (!need2BeVisible.isEmpty()) {
                final String suggestion = need2BeVisible.getFirst().name.toLowerCase();
                super.setSuggestion(suggestion.substring(suggestion.indexOf(string) + string.length()));
            } else super.setSuggestion(null);
        });
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.renderWidget(context, mouseX, mouseY, deltaTicks);
        context.drawTexture(RenderLayer::getGuiTextured, TEXTURES.get(isNarratable(), isFocused()), getX() - 2, getY() - 2, 0f, 0f, width + 4, height + 4, width + 4, height + 4);

        if (isSelectedSuggestion)
            context.drawTexture(RenderLayer::getGuiTextured, selectedSuggestion.texture, getX() - 2 - height, getY() + height / 2 - (height - 6) / 2, 0f, 0f, height - 6, height - 6, height - 6, height - 6);

        if (active && isFocused()) renderSuggestion(context, getY() + height + 2, 0, mouseX, mouseY);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        try {
            final MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();
            final Class<TextFieldWidget> cls = TextFieldWidget.class;
            final String unmappedOwner = resolver.unmapClassName("intermediary", cls.getName());

            final Method onChanged = cls.getDeclaredMethod(resolver.mapMethodName("intermediary", unmappedOwner, "method_1874", null), String.class);

            onChanged.setAccessible(true);
            onChanged.invoke(this, getText());

            // do not invoke onChanged
            final Field text = cls.getDeclaredField(resolver.mapFieldName("intermediary", unmappedOwner, "field_2092", "Ljava/lang/String;"));

            text.setAccessible(true);
            text.set(this, "");
            setSelectionStart(0);
            setSelectionEnd(0);
        } catch (Exception e) {
            LOGGER.error("An error occurred in onClick method", e);
            setText("");
        }
    }

    private void renderSuggestion(DrawContext context, int y, int idx, int mouseX, int mouseY) {
        if (idx >= need2BeVisible.size()) return;

        final int x = this.getX() - 2;
        final int width = this.width + 4;
        final Shard shard = need2BeVisible.get(idx);

        final SuggestionWidget suggestion = new SuggestionWidget(x, y, width, height, Identifier.of(MOD_ID, "textures/gui/" + shard.rarity + ".png"), Identifier.of(MOD_ID, "textures/gui/" + shard.rarity + "-selected.png"), btn -> {
            selectedSuggestion = shard;
            setText(shard.name);
            setSuggestion(null);
            need2BeVisible.clear();
            isSelectedSuggestion = true;
            setFocused(false);
        });
        children.put(shard, suggestion);
        suggestion.renderWidget(context, shard, textRenderer, mouseX, mouseY);

        renderSuggestion(context, y + height, idx + 1, mouseX, mouseY);
    }

    public boolean interviewChildren(double mouseX, double mouseY, int button) {
        if (!active || !isFocused()) return false;

        for (SuggestionWidget suggestion : children.values())
            if (suggestion.mouseClicked(mouseX, mouseY, button)) return true;

        return false;
    }

    public Shard getSelectedSuggestion() {
        return selectedSuggestion;
    }
}
