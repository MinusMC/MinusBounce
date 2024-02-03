/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.injection.forge.mixins.entity;

import net.minusmc.minusbounce.MinusBounce;
import net.minusmc.minusbounce.event.StrafeEvent;
import net.minusmc.minusbounce.event.LookEvent;
import net.minusmc.minusbounce.features.module.modules.combat.HitBox;
import net.minusmc.minusbounce.features.module.modules.misc.Patcher;
import net.minusmc.minusbounce.utils.*;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minusmc.minusbounce.utils.RotationUtils.targetRotation;

import java.util.Random;
import java.util.UUID;

@Mixin(Entity.class)
public abstract class MixinEntity {

    @Shadow
    public double posX;

    @Shadow
    public double posY;

    @Shadow
    public double posZ;

    @Shadow
    public abstract boolean isSprinting();

    @Shadow
    public abstract void setSprinting(boolean sprinting);

    @Shadow
    public float rotationPitch;

    @Shadow
    public float rotationYaw;

    @Shadow
    public abstract AxisAlignedBB getEntityBoundingBox();

    @Shadow
    public abstract float getDistanceToEntity(Entity entityIn);

    @Shadow
    public Entity ridingEntity;

    @Shadow
    public double motionX;

    @Shadow
    public double motionY;

    @Shadow
    public double motionZ;

    @Shadow
    public boolean onGround;

    @Shadow
    public boolean isAirBorne;

    @Shadow
    public boolean noClip;

    @Shadow
    public World worldObj;

    @Shadow
    public void moveEntity(double x, double y, double z) {
    }

    @Shadow
    public boolean isInWeb;

    @Shadow
    public float stepHeight;

    @Shadow
    public boolean isCollidedHorizontally;

    @Shadow
    public boolean isCollidedVertically;

    @Shadow
    public boolean isCollided;

    @Shadow
    public float distanceWalkedModified;

    @Shadow
    public float distanceWalkedOnStepModified;

    @Shadow
    public abstract boolean isInWater();

    @Shadow
    protected Random rand;

    @Shadow
    public int fireResistance;

    @Shadow
    protected boolean inPortal;

    @Shadow
    public int timeUntilPortal;

    @Shadow
    public float width;

    @Shadow
    public abstract Vec3 getPositionEyes(float partialTicks);

    @Shadow
    public abstract boolean isRiding();

    @Shadow
    public abstract void setFire(int seconds);

    @Shadow
    protected abstract void dealFireDamage(int amount);

    @Shadow
    public abstract boolean isWet();

    @Shadow
    public abstract void addEntityCrashInfo(CrashReportCategory category);

    @Shadow
    protected abstract void doBlockCollisions();

    @Shadow
    protected abstract void playStepSound(BlockPos pos, Block blockIn);

    @Shadow
    public abstract void setEntityBoundingBox(AxisAlignedBB bb);

    @Shadow
    private int nextStepDistance;

    @Shadow
    private int fire;

    @Shadow
    public float prevRotationPitch;

    @Shadow
    public float prevRotationYaw;

    @Shadow
    protected abstract Vec3 getVectorForRotation(float pitch, float yaw);

    @Shadow
    public abstract UUID getUniqueID();

    @Shadow
    public abstract boolean isSneaking();

    @Shadow
    public abstract boolean isInsideOfMaterial(Material materialIn);

    @Shadow(remap = false) 
    private CapabilityDispatcher capabilities;

    public int getNextStepDistance() {
        return nextStepDistance;
    }

    public void setNextStepDistance(int nextStepDistance) {
        this.nextStepDistance = nextStepDistance;
    }

    public int getFire() {
        return fire;
    }

    @Inject(method = "getCollisionBorderSize", at = @At("HEAD"), cancellable = true)
    private void getCollisionBorderSize(final CallbackInfoReturnable<Float> callbackInfoReturnable) {
        final HitBox hitBox = MinusBounce.moduleManager.getModule(HitBox.class);

        if (hitBox.getState())
            callbackInfoReturnable.setReturnValue(0.1F + hitBox.getSizeValue().get());
    }

    /**
     * interpolated look vector
     * 
     * @author fmcpe
     */
    @Overwrite
    public MovingObjectPosition rayTrace(double blockReachDistance, float partialTicks)
    {   
        final LookEvent event = new LookEvent(this.rotationYaw, this.rotationPitch);
        MinusBounce.eventManager.callEvent(event);
        
        float yaw = event.getYaw();
        float pitch = event.getPitch();

        final float prevYaw = RotationUtils.serverRotation.getYaw();
        final float prevPitch = RotationUtils.serverRotation.getPitch();

        if (partialTicks != 1.0F) {
            yaw = prevYaw + (yaw - prevYaw) * partialTicks;
            pitch = prevPitch + (pitch - prevPitch) * partialTicks;
        }

        final Vec3 vec3 = this.getPositionEyes(partialTicks);
        final Vec3 vec31 = this.getVectorForRotation(pitch, yaw);
        final Vec3 vec32 = vec3.addVector(vec31.xCoord * blockReachDistance, vec31.yCoord * blockReachDistance, vec31.zCoord * blockReachDistance);
        
        return this.worldObj.rayTraceBlocks(vec3, vec32, false, false, true);
    }

    /**
     * @author fmcpe
     */
    @Overwrite
    public void moveFlying(float strafe, float forward, float friction){
        if ((Entity) (Object) this != Minecraft.getMinecraft().thePlayer) 
            return;
        
        final StrafeEvent event = new StrafeEvent(strafe, forward, friction, this.rotationYaw, true);
        MinusBounce.eventManager.callEvent(event);

        if (event.isCancelled())
            return;

        strafe = event.getStrafe();
        forward = event.getForward();
        friction = event.getFriction();
        float yaw = targetRotation != null && event.getCorrection() ? targetRotation.getYaw() : event.getYaw();

        float f = strafe * strafe + forward * forward;

        if (f >= 1.0E-4F)
        {
            f = MathHelper.sqrt_float(f);

            if (f < 1.0F)
            {
                f = 1.0F;
            }

            f = friction / f;
            strafe = strafe * f;
            forward = forward * f;
            float f1 = MathHelper.sin(yaw * (float)Math.PI / 180.0F);
            float f2 = MathHelper.cos(yaw * (float)Math.PI / 180.0F);
            this.motionX += (double)(strafe * f2 - forward * f1);
            this.motionZ += (double)(forward * f2 + strafe * f1);
        }
    }

    @Redirect(method = "getBrightnessForRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isBlockLoaded(Lnet/minecraft/util/BlockPos;)Z"))
    public boolean alwaysReturnTrue(World world, BlockPos pos) {
        return true;
    }

    @Inject(method = "spawnRunningParticles", at = @At("HEAD"), cancellable = true)
    private void checkGroundState(CallbackInfo ci) {
        if (!this.onGround) ci.cancel();
    }

    /**
     * interpolated look vector
     * 
     * @author fmcpe
     */
    @Overwrite
    public Vec3 getLook(float partialTicks) {

        final LookEvent event = new LookEvent(this.rotationYaw, this.rotationPitch);
        System.out.println("Look event!!");
        MinusBounce.eventManager.callEvent(event);

        final float yaw = event.getYaw();
        final float pitch = event.getPitch();
        final float prevYaw = RotationUtils.serverRotation.getYaw();
        final float prevPitch = RotationUtils.serverRotation.getPitch();
        
        if (partialTicks == 1.0F) {
            return this.getVectorForRotation(pitch, yaw);
        }
        else {
            float f = prevPitch + (pitch - prevPitch) * partialTicks;
            float f1 = prevYaw + (yaw - prevYaw) * partialTicks;
            return this.getVectorForRotation(f, f1);
        }
    }


    /**
     * @author asbyth
     * @reason Faster capability check
     */
    @Overwrite(remap = false)
    public boolean hasCapability(Capability<?> capability, EnumFacing direction) {
        return this.capabilities != null && this.capabilities.hasCapability(capability, direction);
    }
}