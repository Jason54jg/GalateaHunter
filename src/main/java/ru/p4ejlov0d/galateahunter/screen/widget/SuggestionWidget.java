package ru.p4ejlov0d.galateahunter.screen.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.p4ejlov0d.galateahunter.model.Shard;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class SuggestionWidget extends PressableWidget {
    private final Consumer<SuggestionWidget> onPress;
    private final ButtonTextures textures;

    public SuggestionWidget(int x, int y, int width, int height, Identifier icon, Identifier hoveredIcon, @NotNull Consumer<SuggestionWidget> onPress) {
        super(x, y, width, height, Text.empty());
        this.onPress = onPress;
        textures = new ButtonTextures(icon, hoveredIcon);
    }

    @Override
    public void onPress() {
        onPress.accept(this);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        this.appendDefaultNarrations(builder);
    }

    public void renderWidget(@NotNull DrawContext context, @NotNull Shard shard, TextRenderer textRenderer, int mouseX, int mouseY) {
        context.drawTexture(RenderLayer::getGuiTextured, textures.get(isNarratable(), isMouseOver(mouseX, mouseY)), getX(), getY(), 0f, 0f, width, height, width, height);
        context.drawTexture(RenderLayer::getGuiTextured, shard.texture, getX() + 4, getY() + 2, 0f, 0f, height - 6, height - 6, height - 6, height - 6);
        context.drawText(textRenderer, Text.literal(shard.name).withColor(getColorByRarity(shard.rarity)).append(Text.literal(" (" + shard.id.toUpperCase() + ")").withColor(0xFF808080)), getX() + height + 2, getY() + height - height * 85 / 100, 0xFFFFFFFF, false);
        context.drawText(textRenderer, shard.family, getX() + height + 2, getY() + height * 62 / 100, 0xFFFFFFFF, false);
    }

    @Contract(pure = true)
    private int getColorByRarity(@NotNull String rarity) {
        return switch (rarity) {
            case "uncommon" -> 0xFF05DF72;
            case "rare" -> 0xFF51A2FF;
            case "epic" -> 0xFFC27AFF;
            case "legendary" -> 0xFFFFD700;
            default -> 0xFFFFFFFF;
        };
    }
}
