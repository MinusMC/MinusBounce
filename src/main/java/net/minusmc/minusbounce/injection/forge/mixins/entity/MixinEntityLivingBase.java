/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.injection.forge.mixins.entity;

import net.minecraft.util.Vec3;
import net.minusmc.minusbounce.MinusBounce;
import net.minusmc.minusbounce.event.JumpEvent;
import net.minusmc.minusbounce.event.LookEvent;
import net.minusmc.minusbounce.features.module.modules.movement.NoJumpDelay;
import net.minusmc.minusbounce.features.module.modules.movement.Sprint;
import net.minusmc.minusbounce.features.module.modules.movement.TargetStrafe;
import net.minusmc.minusbounce.features.module.modules.client.Animations;
import net.minusmc.minusbounce.features.module.modules.render.AntiBlind;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;

import java.util.Iterator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase extends MixinEntity {


    @Shadow
    protected abstract float getJumpUpwardsMotion();

    @Shadow
    public abstract PotionEffect getActivePotionEffect(Potion potionIn);

    @Shadow
    public abstract boolean isPotionActive(Potion potionIn);

    @Shadow
    private int jumpTicks;

    @Shadow
    protected boolean isJumping;

    @Shadow
    public void onLivingUpdate() {
    }

    @Shadow
    protected abstract void updateFallState(double y, boolean onGroundIn, Block blockIn, BlockPos pos);

    @Shadow
    public abstract float getHealth();

    @Shadow
    public abstract ItemStack getHeldItem();

    @Shadow
    protected abstract void updateAITick();

    @Shadow
    public int swingProgressInt;

    @Shadow
    public boolean isSwingInProgress;
    
    @Shadow
    public float swingProgress;

    @Inject(method = "updatePotionEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/potion/PotionEffect;onUpdate(Lnet/minecraft/entity/EntityLivingBase;)Z"),
        locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private void checkPotionEffect(CallbackInfo ci, Iterator<Integer> iterator, Integer integer, PotionEffect potioneffect) {
        if (potioneffect == null)
            ci.cancel();
    }

    /**
     * @author fmcpe
     */
    @Overwrite
    protected void jump() {

        final JumpEvent jumpEvent = new JumpEvent(this.getJumpUpwardsMotion(), this.rotationYaw);

        MinusBounce.eventManager.callEvent(jumpEvent);
        if (jumpEvent.isCancelled())
            return;

        float yaw = jumpEvent.getYaw();

        final TargetStrafe tsMod = MinusBounce.moduleManager.getModule(TargetStrafe.class);
        final Sprint sprintMod = MinusBounce.moduleManager.getModule(Sprint.class);
        
        if (tsMod.getCanStrafe()) 
            yaw = tsMod.getMovingYaw();

        this.motionY = jumpEvent.getMotion();

        if (this.isPotionActive(Potion.jump))
            this.motionY += (double) ((float) (this.getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1F);

        if (this.isSprinting()) {
            final float f = yaw * 0.017453292F;
            this.motionX -= (double) (MathHelper.sin(f) * 0.2F);
            this.motionZ += (double) (MathHelper.cos(f) * 0.2F);
        }

        this.isAirBorne = true;
    }

    @Inject(method = "onLivingUpdate", at = @At("HEAD"))
    private void headLiving(CallbackInfo callbackInfo) {
        if (MinusBounce.moduleManager.getModule(NoJumpDelay.class).getState())
            jumpTicks = 0;
    }

    @Inject(method = "isPotionActive(Lnet/minecraft/potion/Potion;)Z", at = @At("HEAD"), cancellable = true)
    private void isPotionActive(Potion p_isPotionActive_1_, final CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        final AntiBlind antiBlind = MinusBounce.moduleManager.getModule(AntiBlind.class);

        if ((p_isPotionActive_1_ == Potion.confusion || p_isPotionActive_1_ == Potion.blindness) && antiBlind.getState() && antiBlind.getConfusionEffect().get())
            callbackInfoReturnable.setReturnValue(false);
    }

    //visionfx sucks
    @Overwrite
    private int getArmSwingAnimationEnd() {
        int speed = MinusBounce.moduleManager.getModule(Animations.class).getState() ? 2 + (20 - Animations.INSTANCE.getSpeedSwing().get()) : 6;
        return this.isPotionActive(Potion.digSpeed) ? speed - (1 + this.getActivePotionEffect(Potion.digSpeed).getAmplifier()) * 1 : (this.isPotionActive(Potion.digSlowdown) ? speed + (1 + this.getActivePotionEffect(Potion.digSlowdown).getAmplifier()) * 2 : speed);
    }

    @Overwrite
    public Vec3 getLook(float partialTicks)
    {
        final LookEvent event = new LookEvent(this.rotationYaw, this.rotationPitch);
        MinusBounce.eventManager.callEvent(event);

        return this.getVectorForRotation(event.getPitch(), event.getYaw());
    }

}
