package com.ant.mclangsplit.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.option.ControlsListWidget;
import net.minecraft.client.gui.screen.option.ControlsOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ControlsListWidget.KeyBindingEntry.class)
public class MixinKeyBindingListKeyEntry {
    @Final
    @Shadow
    private KeyBinding binding;
    @Final
    @Shadow
    private Text bindingName;
    @Final
    @Shadow
    private ButtonWidget editButton;
    @Final
    @Shadow
    private ButtonWidget resetButton;

    @Final
    @Shadow
    private ControlsListWidget field_2742;

    @Inject(at = @At("HEAD"), method = "render", cancellable = true)
    public void render(MatrixStack p_230432_1_, int p_230432_2_, int p_230432_3_, int p_230432_4_, int p_230432_5_, int p_230432_6_, int p_230432_7_, int p_230432_8_, boolean p_230432_9_, float p_230432_10_, CallbackInfo ci) {
        ControlsOptionsScreen controlsScreen = ((ControlsListWidgetAccessorMixin) field_2742).getParent();

        MinecraftClient mc = MinecraftClient.getInstance();

        assert mc.currentScreen != null;

        float width = ((float) mc.currentScreen.width) / 2;
        int strWidth = mc.textRenderer.getWidth(bindingName);
        float scaleFactor = strWidth > width - 6 ? 1f / (strWidth / (width - 6f)) : 1f;

        boolean flag = controlsScreen.focusedBinding == this.binding;
        p_230432_1_.push();
        p_230432_1_.scale(scaleFactor, 1f, 1f);
        mc.textRenderer.draw(p_230432_1_, this.bindingName, 6f, (float)(p_230432_3_ + p_230432_6_ / 2 - 9 / 2), 16777215);
        p_230432_1_.pop();
        this.resetButton.x = p_230432_4_ + 190 + 20;
        this.resetButton.y = p_230432_3_;
        this.resetButton.active = !this.binding.isDefault();
        this.resetButton.render(p_230432_1_, p_230432_7_, p_230432_8_, p_230432_10_);
        this.editButton.x = p_230432_4_ + 105;
        this.editButton.y = p_230432_3_;
        this.editButton.setMessage(this.binding.getBoundKeyLocalizedText());
        boolean flag1 = false;
        if (!this.binding.isUnbound()) {
            for(KeyBinding keybinding : mc.options.keysAll) {
                if (keybinding != this.binding && this.binding.equals(keybinding)) {
                    flag1 = true;
                    break;
                }
            }
        }

        if (flag) {
            this.editButton.setMessage((new LiteralText("> ")).append(this.editButton.getMessage().shallowCopy().formatted(Formatting.YELLOW)).append(" <").formatted(Formatting.YELLOW));
        } else if (flag1) {
            this.editButton.setMessage(this.editButton.getMessage().shallowCopy().formatted(Formatting.RED));
        }

        this.editButton.render(p_230432_1_, p_230432_7_, p_230432_8_, p_230432_10_);

        ci.cancel();
    }
}
