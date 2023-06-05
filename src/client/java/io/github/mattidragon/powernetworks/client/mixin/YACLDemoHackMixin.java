package io.github.mattidragon.powernetworks.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.isxander.yacl.gui.tab.ScrollableNavigationBar;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.widget.TabButtonWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScrollableNavigationBar.class)
public abstract class YACLDemoHackMixin extends AbstractParentElement {
    @Shadow @Final private static TextRenderer font;

    @Redirect(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/TabButtonWidget;setWidth(I)V"))
    private void replaceButtonWidth(TabButtonWidget tab, int i) {
        tab.setWidth(font.getWidth(tab.getMessage()) + 8);
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/GridWidget;refreshPositions()V"))
    private void fillRemainingSpace(CallbackInfo ci, @Local(ordinal = 0) int noScrollWidth) {
        var tabs = this.children().stream().map(TabButtonWidget.class::cast).toList();

        var totalWidth = tabs.stream().mapToInt(TabButtonWidget::getWidth).sum();
        var emptySpace = noScrollWidth - totalWidth;
        if (emptySpace > 0) {
            var increase = emptySpace / children().size();
            for (TabButtonWidget tab : tabs) {
                tab.setWidth(tab.getWidth() + increase);
            }
        }
    }

    @ModifyVariable(method = "init", ordinal = 3, at = @At("STORE"))
    private int replaceTotalWidth(int original) {
        return this.children().stream().map(TabButtonWidget.class::cast).mapToInt(TabButtonWidget::getWidth).sum();
    }
}
