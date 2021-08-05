package com.ant.mclangsplit.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ClickableWidget.class)
public abstract class MixinWidget extends DrawableHelper implements Drawable, Element {
    @Shadow
    public boolean active;
    @Shadow
    public boolean visible;
    @Shadow
    protected boolean hovered;
    @Shadow
    protected int width;
    @Shadow
    protected int height;
    @Shadow
    public int x;
    @Shadow
    public int y;
    @Shadow
    protected float alpha;
    @Shadow
    abstract void renderBackground(MatrixStack p_230441_1_, MinecraftClient p_230441_2_, int p_230441_3_, int p_230441_4_);
    @Shadow
    abstract int getYImage(boolean p_230989_1_);
    @Shadow
    abstract Text getMessage();

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ClickableWidget;drawCenteredText(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)V"), method = "renderButton", cancellable = true)
    public void renderButton(MatrixStack p_230431_1_, int p_230431_2_, int p_230431_3_, float p_230431_4_, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();

        Text buttonText = this.getMessage();
        int strWidth = mc.textRenderer.getWidth(buttonText);

        float scaleFactor = strWidth > width - 6 ? 1f / (strWidth / (width - 6f)) : 1f;

        p_230431_1_.push();
        p_230431_1_.scale(scaleFactor, 1f, 1f);
        int j = this.active ? 16777215 : 10526880;
        drawCenteredText(p_230431_1_, mc.textRenderer, buttonText, (int)(((float)this.x + (float)this.width / 2f) * (1f / scaleFactor)), this.y + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255.0F) << 24);
        p_230431_1_.pop();
        ci.cancel();
    }
}
