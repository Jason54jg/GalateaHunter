package ru.p4ejlov0d.galateahunter.mixin.screen;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.cursor.StandardCursors;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
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

@Mixin(InventoryScreen.class)
@Environment(EnvType.CLIENT)
public abstract class InventoryRenderMixin extends HandledScreenMixin {
    @Unique
    private ScalableWidget recipeWidget;

    @Unique
    private IconButtonWidget maximize;

    @Unique
    private boolean clicked;

    protected InventoryRenderMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/RecipeBookScreen;init()V"))
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
            if (!isClickOutsideBounds(mouseX, mouseY, super.x, super.y)) context.setCursor(StandardCursors.ARROW);
        }
        if (maximize != null) maximize.render(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    protected boolean mouseClicked(Click click, boolean doubled, Operation<Boolean> original) {
        if (maximize != null && !maximize.mouseClicked(click, doubled) && recipeWidget != null) {
            recipeWidget.mouseClicked(click, doubled);
            if (isClickOutsideBounds(click.x(), click.y(), super.x, super.y)) clicked = true;
        }
        return super.mouseClicked(click, doubled, original);
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"))
    private void mouseReleased(Click click, CallbackInfoReturnable<Boolean> cir) {
        clicked = false;
    }

    @Override
    protected boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount, Operation<Boolean> original) {
        if (isClickOutsideBounds(mouseX, mouseY, super.x, super.y) && hoveredElement(mouseX, mouseY).isEmpty() && recipeWidget != null) {
            recipeWidget.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
            setFocused(recipeWidget);
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount, original);
    }

    @Override
    protected boolean mouseDragged(Click click, double offsetX, double offsetY, Operation<Boolean> original) {
        if (clicked && hoveredElement(click.x(), click.y()).isEmpty() && recipeWidget != null) {
            recipeWidget.mouseDragged(click, offsetX, offsetY);
            setFocused(recipeWidget);
        }
        return super.mouseDragged(click, offsetX, offsetY, original);
    }
}
