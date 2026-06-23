package ru.p4ejlov0d.galateahunter.mixin.screen;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
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

@Mixin(AbstractContainerScreen.class)
@Environment(EnvType.CLIENT)
public abstract class AbstractContainerScreenMixin extends Screen {
    @Shadow
    protected int leftPos;

    @Shadow
    protected int topPos;

    @Unique
    private ScalableWidget recipeWidget;

    @Unique
    private IconButtonWidget maximize;

    @Unique
    private boolean clicked;

    protected AbstractContainerScreenMixin(Component title) {
        super(title);
    }

    @Shadow
    protected abstract boolean hasClickedOutside(double mouseX, double mouseY, int left, int top);

    @Inject(method = "init", at = @At("HEAD"))
    private void init(CallbackInfo ci) {
        if (RecipeScreen.minimized) {
            final LanguageModel languageModel = LanguageResourceHandler.getInstance().getLanguageModel();

            recipeWidget = RecipeScreen.getSavedRecipeWidget();
            recipeWidget.setPosition(0, 0);
            recipeWidget.setWidth(width);
            recipeWidget.setHeight(height);
            recipeWidget.setDrawsBackground(false);

            maximize = new IconButtonWidget(width - 30, height - 90, 20, 20, Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/maximize.png"), Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/maximize-highlighted.png"), btn -> {
                RecipeScreen.minimized = false;
                minecraft.gui.setScreen(RecipeScreen.restoreScreen());
            });
            if (languageModel != null) maximize.setTooltip(Tooltip.create(Component.literal(languageModel.maximize())));
        }
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        if (recipeWidget != null) {
            recipeWidget.extractRenderState(graphics, mouseX, mouseY, deltaTicks);
            if (!hasClickedOutside(mouseX, mouseY, leftPos, topPos)) graphics.requestCursor(CursorTypes.ARROW);
        }
        if (maximize != null) maximize.extractRenderState(graphics, mouseX, mouseY, deltaTicks);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"))
    private void mouseClicked(MouseButtonEvent click, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
        if (maximize != null && !maximize.mouseClicked(click, doubled) && recipeWidget != null) {
            recipeWidget.mouseClicked(click, doubled);
            if (hasClickedOutside(click.x(), click.y(), leftPos, topPos)) clicked = true;
        }
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"))
    private void mouseReleased(MouseButtonEvent click, CallbackInfoReturnable<Boolean> cir) {
        clicked = false;
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"))
    private void mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount, CallbackInfoReturnable<Boolean> cir) {
        if (hasClickedOutside(mouseX, mouseY, leftPos, topPos) && getChildAt(mouseX, mouseY).isEmpty() && recipeWidget != null) {
            recipeWidget.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
            setFocused(recipeWidget);
        }
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"))
    protected void mouseDragged(MouseButtonEvent click, double offsetX, double offsetY, CallbackInfoReturnable<Boolean> cir) {
        if (clicked && getChildAt(click.x(), click.y()).isEmpty() && recipeWidget != null) {
            recipeWidget.mouseDragged(click, offsetX, offsetY);
            setFocused(recipeWidget);
        }
    }
}
