package ru.p4ejlov0d.galateahunter.mixin.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.cursor.StandardCursors;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.p4ejlov0d.galateahunter.model.LanguageModel;
import ru.p4ejlov0d.galateahunter.screen.RecipeScreen;
import ru.p4ejlov0d.galateahunter.screen.widget.IconButtonWidget;
import ru.p4ejlov0d.galateahunter.screen.widget.ScalableWidget;
import ru.p4ejlov0d.galateahunter.utils.LanguageResourceHandler;

import static ru.p4ejlov0d.galateahunter.GalateaHunter.MOD_ID;

@Mixin(HandledScreen.class)
@Environment(EnvType.CLIENT)
public abstract class HandledScreenMixin extends Screen {
    @Shadow
    protected int x;

    @Shadow
    protected int y;

    @Unique
    private ScalableWidget recipeWidget;

    @Unique
    private IconButtonWidget maximize;

    @Unique
    private boolean clicked;

    protected HandledScreenMixin(Text title) {
        super(title);
    }

    @Shadow
    protected abstract boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top);

    @Inject(method = "init", at = @At("HEAD"))
    private void init(CallbackInfo ci) {
        if (RecipeScreen.minimized) {
            final LanguageModel languageModel = LanguageResourceHandler.getInstance().getLanguageModel();

            recipeWidget = RecipeScreen.getSavedRecipeWidget();
            recipeWidget.setPosition(0, 0);
            recipeWidget.setWidth(width);
            recipeWidget.setHeight(height);
            recipeWidget.setDrawsBackground(false);

            maximize = new IconButtonWidget(width - 30, height - 90, 20, 20, Identifier.of(MOD_ID, "textures/gui/maximize.png"), Identifier.of(MOD_ID, "textures/gui/maximize-highlighted.png"), btn -> {
                RecipeScreen.minimized = false;
                client.setScreen(RecipeScreen.restoreScreen());
            });
            if (languageModel != null) maximize.setTooltip(Tooltip.of(Text.literal(languageModel.maximize())));
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void render(DrawContext context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        if (recipeWidget != null) {
            recipeWidget.render(context, mouseX, mouseY, deltaTicks);
            if (!isClickOutsideBounds(mouseX, mouseY, x, y)) context.setCursor(StandardCursors.ARROW);
        }
        if (maximize != null) maximize.render(context, mouseX, mouseY, deltaTicks);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"))
    private void mouseClicked(Click click, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
        if (maximize != null && !maximize.mouseClicked(click, doubled) && recipeWidget != null) {
            recipeWidget.mouseClicked(click, doubled);
            if (isClickOutsideBounds(click.x(), click.y(), x, y)) clicked = true;
        }
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"))
    private void mouseReleased(Click click, CallbackInfoReturnable<Boolean> cir) {
        clicked = false;
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"))
    private void mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount, CallbackInfoReturnable<Boolean> cir) {
        if (isClickOutsideBounds(mouseX, mouseY, x, y) && hoveredElement(mouseX, mouseY).isEmpty() && recipeWidget != null) {
            recipeWidget.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
            setFocused(recipeWidget);
        }
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"))
    protected void mouseDragged(Click click, double offsetX, double offsetY, CallbackInfoReturnable<Boolean> cir) {
        if (clicked && hoveredElement(click.x(), click.y()).isEmpty() && recipeWidget != null) {
            recipeWidget.mouseDragged(click, offsetX, offsetY);
            setFocused(recipeWidget);
        }
    }
}
