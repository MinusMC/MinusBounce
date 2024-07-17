/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.injection.forge.mixins.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.potion.Potion;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.FoodStats;
import net.minecraft.util.MathHelper;
import net.minusmc.minusbounce.MinusBounce;
import net.minusmc.minusbounce.event.KnockbackEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer extends MixinEntityLivingBase {

    @Shadow
    public abstract ItemStack getHeldItem();

    @Shadow
    public abstract GameProfile getGameProfile();

    @Shadow
    protected abstract boolean canTriggerWalking();

    @Shadow
    protected abstract String getSwimSound();

    @Shadow
    public abstract FoodStats getFoodStats();
    
    @Shadow
    public abstract ItemStack getCurrentEquippedItem();

    @Shadow
    protected int flyToggleTimer;

    @Shadow
    public PlayerCapabilities capabilities;

    @Shadow
    public abstract void onCriticalHit(Entity entityHit);

    @Shadow
    public abstract void onEnchantmentCritical(Entity entityHit);

    @Shadow
    public abstract void triggerAchievement(StatBase achievementIn);

    @Shadow
    public abstract int getItemInUseDuration();

    @Shadow
    public abstract void addExhaustion(float p_71020_1_);

    @Shadow
    public abstract ItemStack getItemInUse();

    @Shadow
    public abstract void destroyCurrentEquippedItem();

    @Shadow
    public abstract boolean isUsingItem();

    @Shadow
    public abstract void addStat(StatBase stat, int amount);

    /**
     * Attacks for the player the targeted entity with the currently equipped item. The equipped item has hitEntity
     * called on it. Args: targetEntity
     * @author fmcpe
     * @reason KeepSprint, KnockbackEvent
     */
    @Overwrite
    public void attackTargetEntityWithCurrentItem(Entity targetEntity) {
        if (targetEntity.canAttackWithItem() && !targetEntity.hitByEntity((EntityPlayer) (Object) this)) {

            float f = (float)this.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
            int i = 0;
            float f1;

            if (targetEntity instanceof EntityLivingBase)
                f1 = EnchantmentHelper.getModifierForCreature(this.getHeldItem(), ((EntityLivingBase)targetEntity).getCreatureAttribute());
            else
                f1 = EnchantmentHelper.getModifierForCreature(this.getHeldItem(), EnumCreatureAttribute.UNDEFINED);

            i = i + EnchantmentHelper.getKnockbackModifier((EntityLivingBase) (Object) this);

            if (this.isSprinting())
                ++i;

            if (f > 0.0F || f1 > 0.0F) {
                boolean flag = this.fallDistance > 0.0F && !this.onGround && !this.isOnLadder() && !this.isInWater() && !this.isPotionActive(Potion.blindness) && this.ridingEntity == null && targetEntity instanceof EntityLivingBase;

                if (flag && f > 0.0F)
                    f *= 1.5F;

                f = f + f1;
                boolean flag1 = false;
                int j = EnchantmentHelper.getFireAspectModifier((EntityLivingBase) (Object) this);

                if (targetEntity instanceof EntityLivingBase && j > 0 && !targetEntity.isBurning()) {
                    flag1 = true;
                    targetEntity.setFire(1);
                }

                double d0 = targetEntity.motionX;
                double d1 = targetEntity.motionY;
                double d2 = targetEntity.motionZ;
                boolean flag2 = targetEntity.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) (Object) this), f);

                if (flag2) {
                    KnockbackEvent event = new KnockbackEvent(false);
                    MinusBounce.eventManager.callEvent(event);
                    
                    if (i > 0 && !event.isCancelled()) {
                        targetEntity.addVelocity(-MathHelper.sin(this.rotationYaw * (float)Math.PI / 180.0F) * (float)i * 0.5F, event.getReduceY() ? 0.0D : 0.1D, MathHelper.cos(this.rotationYaw * (float)Math.PI / 180.0F) * (float)i * 0.5F);
                        this.motionX *= 0.6;
                        this.motionZ *= 0.6;
                        this.setSprinting(false);
                    }

                    if (targetEntity instanceof EntityPlayerMP && targetEntity.velocityChanged) {
                        ((EntityPlayerMP)targetEntity).playerNetServerHandler.sendPacket(new S12PacketEntityVelocity(targetEntity));
                        targetEntity.velocityChanged = false;
                        targetEntity.motionX = d0;
                        targetEntity.motionY = d1;
                        targetEntity.motionZ = d2;
                    }

                    if (flag)
                        this.onCriticalHit(targetEntity);

                    if (f1 > 0.0F)
                        this.onEnchantmentCritical(targetEntity);

                    if (f >= 18.0F)
                        this.triggerAchievement(AchievementList.overkill);

                    this.setLastAttacker(targetEntity);

                    if (targetEntity instanceof EntityLivingBase)
                        EnchantmentHelper.applyThornEnchantments((EntityLivingBase)targetEntity, (Entity) (Object) this);

                    EnchantmentHelper.applyArthropodEnchantments((EntityLivingBase) (Object) this, targetEntity);
                    ItemStack itemstack = this.getCurrentEquippedItem();
                    Entity entity = targetEntity;

                    if (targetEntity instanceof EntityDragonPart) {
                        IEntityMultiPart ientitymultipart = ((EntityDragonPart)targetEntity).entityDragonObj;

                        if (ientitymultipart instanceof EntityLivingBase)
                            entity = (EntityLivingBase)ientitymultipart;
                    }

                    if (itemstack != null && entity instanceof EntityLivingBase) {
                        itemstack.hitEntity((EntityLivingBase)entity, (EntityPlayer) (Object) this);

                        if (itemstack.stackSize <= 0)
                            this.destroyCurrentEquippedItem();
                    }

                    if (targetEntity instanceof EntityLivingBase) {
                        this.addStat(StatList.damageDealtStat, Math.round(f * 10.0F));

                        if (j > 0)
                            targetEntity.setFire(j * 4);
                    }

                    this.addExhaustion(0.3F);
                }
                else if (flag1)
                    targetEntity.extinguish();
            }
        }
    }
}