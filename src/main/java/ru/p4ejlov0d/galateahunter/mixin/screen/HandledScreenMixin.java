package ru.p4ejlov0d.galateahunter.mixin.screen;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(HandledScreen.class)
@Environment(EnvType.CLIENT)
public abstract class HandledScreenMixin extends Screen {
    @Shadow
    protected int x;

    @Shadow
    protected int y;

    protected HandledScreenMixin(Text title) {
        super(title);
    }

    @Shadow
    protected abstract boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top);

    @WrapMethod(method = "mouseClicked")
    protected boolean mouseClicked(Click click, boolean doubled, Operation<Boolean> original) {
        return original.call(click, doubled);
    }

    @WrapMethod(method = "mouseScrolled")
    protected boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount, Operation<Boolean> original) {
        return original.call(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @WrapMethod(method = "mouseDragged")
    protected boolean mouseDragged(Click click, double offsetX, double offsetY, Operation<Boolean> original) {
        return original.call(click, offsetX, offsetY);
    }
}
