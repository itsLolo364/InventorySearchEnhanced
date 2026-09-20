package com.aldomoretti.inventorysearch.mixin;

import com.aldomoretti.inventorysearch.gui.SearchBarWidget;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    private void is_onCharTyped(long windowPointer, int codePoint, int modifiers, CallbackInfo ci) {
        if (this.minecraft.screen != null) {
            Screen screen = this.minecraft.screen;
            for (GuiEventListener child : screen.children()) {
                if (child instanceof SearchBarWidget bar) {
                    if (bar.isFocused()) {
                        bar.charTyped((char) codePoint, modifiers);
                        ci.cancel();
                        return;
                    }
                }
            }
        }
    }
}