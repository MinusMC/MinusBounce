/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.injection.forge.mixins.item;

import net.minusmc.minusbounce.MinusBounce;
import net.minusmc.minusbounce.features.module.modules.combat.KillAura;
import net.minusmc.minusbounce.features.module.modules.client.Animations;
import net.minusmc.minusbounce.features.module.modules.render.AntiBlind;
import net.minusmc.minusbounce.utils.timer.MSTimer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.*;
import net.minecraft.init.Items;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer {

    float delay = 0.0F;
    MSTimer rotateTimer = new MSTimer();

    @Shadow
    private float prevEquippedProgress;

    @Shadow
    private float equippedProgress;


    @Shadow
    @Final
    private Minecraft mc;

    @Shadow
    protected abstract void rotateArroundXAndY(float angle, float angleY);

    @Shadow
    protected abstract void setLightMapFromPlayer(AbstractClientPlayer clientPlayer);

    @Shadow
    protected abstract void rotateWithPlayerRotations(EntityPlayerSP entityplayerspIn, float partialTicks);

    @Shadow
    private ItemStack itemToRender;

    @Shadow
    protected abstract void renderItemMap(AbstractClientPlayer clientPlayer, float pitch, float equipmentProgress, float swingProgress);

    @Shadow
    protected abstract void transformFirstPersonItem(float equipProgress, float swingProgress);

    @Shadow
    protected abstract void performDrinking(AbstractClientPlayer clientPlayer, float partialTicks);

    @Shadow
    protected abstract void doBlockTransformations();

    @Shadow
    protected abstract void doBowTransformations(float partialTicks, AbstractClientPlayer clientPlayer);

    @Shadow
    protected abstract void doItemUsedTransformations(float swingProgress);

    @Shadow
    public abstract void renderItem(EntityLivingBase entityIn, ItemStack heldStack, ItemCameraTransforms.TransformType transform);

    @Shadow
    protected abstract void renderPlayerArm(AbstractClientPlayer clientPlayer, float equipProgress, float swingProgress);

    private void func_178103_d(float qq) {
        GlStateManager.translate(-0.5F, qq, 0.0F);
        GlStateManager.rotate(30.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-80.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(60.0F, 0.0F, 1.0F, 0.0F);
    }

    private void func_178096_b(float p_178096_1_, float p_178096_2_) {
        GlStateManager.translate(0.56F, -0.52F, -0.71999997F);
        GlStateManager.translate(0.0F, p_178096_1_ * -0.6F, 0.0F);
        GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
        float var3 = MathHelper.sin(p_178096_2_ * p_178096_2_ * (float) Math.PI);
        float var4 = MathHelper.sin(MathHelper.sqrt_float(p_178096_2_) * (float) Math.PI);
        GlStateManager.rotate(var3 * -20.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(var4 * -20.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(var4 * -80.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(Animations.INSTANCE.getScale().get(), Animations.INSTANCE.getScale().get(), Animations.INSTANCE.getScale().get());
    }

    private void func_178103_d() {
        GlStateManager.translate(-0.5F, 0.2F, 0.0F);
        GlStateManager.rotate(30.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-80.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(60.0F, 0.0F, 1.0F, 0.0F);
    }

    /**
     * @author CCBlueX
     */
    @Overwrite
    public void renderItemInFirstPerson(float partialTicks) {
        float f = 1.0F - (this.prevEquippedProgress + (this.equippedProgress - this.prevEquippedProgress) * partialTicks);
        AbstractClientPlayer abstractclientplayer = this.mc.thePlayer;
        float f1 = abstractclientplayer.getSwingProgress(partialTicks);
        float f2 = abstractclientplayer.prevRotationPitch + (abstractclientplayer.rotationPitch - abstractclientplayer.prevRotationPitch) * partialTicks;
        float f3 = abstractclientplayer.prevRotationYaw + (abstractclientplayer.rotationYaw - abstractclientplayer.prevRotationYaw) * partialTicks;
        if (MinusBounce.moduleManager.getModule(Animations.class).getState()) {
            GL11.glTranslated(Animations.INSTANCE.getItemPosX().get().doubleValue(), Animations.INSTANCE.getItemPosY().get().doubleValue(), Animations.INSTANCE.getItemPosZ().get().doubleValue());
        }
        this.rotateArroundXAndY(f2, f3);
        this.setLightMapFromPlayer(abstractclientplayer);
        this.rotateWithPlayerRotations((EntityPlayerSP) abstractclientplayer, partialTicks);
        GlStateManager.enableRescaleNormal();
        GlStateManager.pushMatrix();

        if (MinusBounce.moduleManager.getModule(Animations.class).getState()) {
            GL11.glTranslated(Animations.INSTANCE.getItemPosX().get().doubleValue(), Animations.INSTANCE.getItemPosY().get().doubleValue(), Animations.INSTANCE.getItemPosZ().get().doubleValue());
        }

        if (this.itemToRender != null) {
            final KillAura killAura = MinusBounce.moduleManager.getModule(KillAura.class);
            final Animations animMod = MinusBounce.moduleManager.getModule(Animations.class);
            boolean canBlockEverything = animMod.getState() && killAura.getTarget() != null
                            && (itemToRender.getItem() instanceof ItemBucketMilk || itemToRender.getItem() instanceof ItemFood 
                                || itemToRender.getItem() instanceof ItemPotion || itemToRender.getItem() instanceof ItemAxe || itemToRender.getItem().equals(Items.stick));

            if (this.itemToRender.getItem() instanceof ItemMap) {
                this.renderItemMap(abstractclientplayer, f2, f, f1);
            } else if (abstractclientplayer.getItemInUseCount() > 0 
                        || (itemToRender.getItem() instanceof ItemSword && killAura.getBlockingStatus())
                        || (itemToRender.getItem() instanceof ItemSword && animMod.getState() && animMod.getFakeBlocking().get() && killAura.getTarget() != null)
                        || canBlockEverything) {

                EnumAction enumaction = (killAura.getBlockingStatus() || canBlockEverything) ? EnumAction.BLOCK : this.itemToRender.getItemUseAction();

                switch (enumaction) {
                    case NONE:
                        this.transformFirstPersonItem(f, 0.0F);
                        break;
                    case EAT:
                    case DRINK:
                        this.performDrinking(abstractclientplayer, partialTicks);
                        this.transformFirstPersonItem(f, f1);

                        if (animMod.getState() && Animations.INSTANCE.getRotateItems().get())
                            rotateItemAnim();
                        break;
                    case BLOCK:
                        if (animMod.getState()) {
                            GL11.glTranslated(Animations.INSTANCE.getBlockPosX().get().doubleValue(), Animations.INSTANCE.getBlockPosY().get().doubleValue(), Animations.INSTANCE.getBlockPosZ().get().doubleValue());                            
                            float f8 = MathHelper.sin(MathHelper.sqrt_float(this.mc.thePlayer.getSwingProgress(partialTicks)) * 3.1415927F);
                            this.func_178096_b(f, 0.0F);
                            GL11.glTranslated(0.0D, 0.25D, 0.07D);
                            GL11.glRotated((-f8 * 40.0F), (f8 / 2.0F), 0.0D, 9.0D);
                            GL11.glRotated((-f8 * 50.0F), 0.800000011920929D, (f8 / 2.0F), 0.0D);
                            this.func_178103_d(0.2F);
                        } else {
                            this.transformFirstPersonItem(f + 0.1F, f1);
                            this.doBlockTransformations();
                            GlStateManager.translate(-0.5F, 0.2F, 0.0F);
                        }
                        break;
                    case BOW:
                        this.transformFirstPersonItem(f, f1);
                        if (animMod.getState() && Animations.INSTANCE.getRotateItems().get())
                            rotateItemAnim();
                        this.doBowTransformations(partialTicks, abstractclientplayer);
                        if (animMod.getState() && Animations.INSTANCE.getRotateItems().get())
                            rotateItemAnim();
                }
            } else {
                this.doItemUsedTransformations(f1);
                this.transformFirstPersonItem(f, f1);
                if (MinusBounce.moduleManager.getModule(Animations.class).getState() && Animations.INSTANCE.getRotateItems().get())
                    rotateItemAnim();
            }

            this.renderItem(abstractclientplayer, this.itemToRender, ItemCameraTransforms.TransformType.FIRST_PERSON);
        } else if (!abstractclientplayer.isInvisible())
            this.renderPlayerArm(abstractclientplayer, f, f1);

        GlStateManager.popMatrix();
        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();

        if (MinusBounce.moduleManager.getModule(Animations.class).getState())
            GL11.glTranslated(-Animations.INSTANCE.getItemPosX().get().doubleValue(), -Animations.INSTANCE.getItemPosY().get().doubleValue(), -Animations.INSTANCE.getItemPosZ().get().doubleValue());
    }

    private void rotateItemAnim() {
        if (Animations.INSTANCE.getTransformFirstPersonRotate().get().equalsIgnoreCase("RotateY")) {
            GlStateManager.rotate(this.delay, 0.0F, 1.0F, 0.0F);
        }
        if (Animations.INSTANCE.getTransformFirstPersonRotate().get().equalsIgnoreCase("RotateXY")) {
            GlStateManager.rotate(this.delay, 1.0F, 1.0F, 0.0F);
        }

        if (Animations.INSTANCE.getTransformFirstPersonRotate().get().equalsIgnoreCase("Custom")) {
            GlStateManager.rotate(this.delay, Animations.INSTANCE.getCustomRotate1().get(), Animations.INSTANCE.getCustomRotate2().get(), Animations.INSTANCE.getCustomRotate3().get());
        }

        if (this.rotateTimer.hasTimePassed(1)) {
            ++this.delay;
            this.delay = this.delay + Animations.INSTANCE.getSpeedRotate().get();
            this.rotateTimer.reset();
        }
        if (this.delay > 360.0F) {
            this.delay = 0.0F;
        }
    }

    @Inject(method = "renderFireInFirstPerson", at = @At("HEAD"), cancellable = true)
    private void renderFireInFirstPerson(final CallbackInfo callbackInfo) {
        final AntiBlind antiBlind = MinusBounce.moduleManager.getModule(AntiBlind.class);

        if (antiBlind.getState() && antiBlind.getFireEffect().get()) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 0.9F);
            GlStateManager.depthFunc(519);
            GlStateManager.depthMask(false);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableBlend();
            GlStateManager.depthMask(true);
            GlStateManager.depthFunc(515);
            callbackInfo.cancel();
        }
    }
}
