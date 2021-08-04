package com.ant.mclangsplit.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractButtonWidget.class)
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
    abstract void renderBg(MatrixStack p_230441_1_, MinecraftClient p_230441_2_, int p_230441_3_, int p_230441_4_);
    @Shadow
    abstract int getYImage(boolean p_230989_1_);
    @Shadow
    abstract Text getMessage();

    @Inject(at = @At("HEAD"), method = "renderButton", cancellable = true)
    public void renderButton(MatrixStack p_230431_1_, int p_230431_2_, int p_230431_3_, float p_230431_4_, CallbackInfo ci) {
        if (this.visible)
        {
            MinecraftClient mc = MinecraftClient.getInstance();
            this.hovered = p_230431_2_ >= this.x && p_230431_3_ >= this.y && p_230431_2_ < this.x + this.width && p_230431_3_ < this.y + this.height;
            int k = this.getYImage(this.hovered);
            mc.getTextureManager().bindTexture(AbstractButtonWidget.WIDGETS_LOCATION);
            drawTexture(p_230431_1_, this.x, this.y, 0, 46 + k * 20, this.width, this.height);
            drawTexture(p_230431_1_, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + k * 20, this.width / 2, this.height);
            this.renderBg(p_230431_1_, mc, p_230431_2_, p_230431_3_);

            Text buttonText = this.getMessage();
            int strWidth = mc.textRenderer.getWidth(buttonText);

            float scaleFactor = strWidth > width - 6 ? 1f / (strWidth / (width - 6f)) : 1f;

            p_230431_1_.push();
            p_230431_1_.scale(scaleFactor, 1f, 1f);
            int j = this.active ? 16777215 : 10526880;
            drawCenteredText(p_230431_1_, mc.textRenderer, buttonText, (int)(((float)this.x + (float)this.width / 2f) * (1f / scaleFactor)), this.y + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255.0F) << 24);
            p_230431_1_.pop();
        }
        ci.cancel();
    }
}
