/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by MinusBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.minusmc.minusbounce.features.module.modules.render

import net.minusmc.minusbounce.event.AttackEvent
import net.minusmc.minusbounce.event.UpdateEvent
import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.utils.EntityUtils
import net.minusmc.minusbounce.utils.misc.RandomUtils
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.IntegerValue
import net.minusmc.minusbounce.value.ListValue
import net.minecraft.block.Block
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.Entity
import net.minecraft.entity.effect.EntityLightningBolt
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.S2CPacketSpawnGlobalEntity
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.ResourceLocation

@ModuleInfo(name = "HitEffect", description = "Attack with custom hit effect.",  category = ModuleCategory.RENDER)
object HitEffect : Module() {

    private val timingValue = ListValue("Timing", arrayOf("Attack", "Kill"), "Attack")
    private val modeValue = ListValue("Mode", arrayOf("Lighting", "Blood", "Fire", "Critical", "MagicCritical"), "Lighting")
    private val timesValue = IntegerValue("Times", 1, 1, 10)
    private val lightingSoundValue = BoolValue("LightingSound", true) { modeValue.equals("Lighting") }

    private val blockState = Block.getStateId(Blocks.redstone_block.defaultState)

    private var target: Entity? = null

    @EventTarget
    fun onAttack(event: AttackEvent) {
        target = event.targetEntity
        if(timingValue.equals("Attack") && event.targetEntity?.let { EntityUtils.isSelected(it, true) } == true) {
            displayEffectFor(event.targetEntity as EntityLivingBase)
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (target != null && target!!.isDead && timingValue.equals("Kill")) {
            displayEffectFor(target as EntityLivingBase)
            target = null
        }
    }

    private fun displayEffectFor(entity: EntityLivingBase) {
        repeat(timesValue.get()) {
            when(modeValue.get().lowercase()) {
                "lighting" -> {
                    mc.netHandler.handleSpawnGlobalEntity(S2CPacketSpawnGlobalEntity(EntityLightningBolt(mc.theWorld, entity.posX, entity.posY, entity.posZ)))
                    if(lightingSoundValue.get()) {
                        mc.soundHandler.playSound(PositionedSoundRecord.create(ResourceLocation("random.explode"), 1.0f))
                        mc.soundHandler.playSound(PositionedSoundRecord.create(ResourceLocation("ambient.weather.thunder"), 1.0f))
                    }
                }
                "blood" -> {
                    repeat(10) {
                        mc.effectRenderer.spawnEffectParticle(EnumParticleTypes.BLOCK_CRACK.particleID, entity.posX, entity.posY + entity.height / 2, entity.posZ,
                            entity.motionX + RandomUtils.nextFloat(-0.5f, 0.5f), entity.motionY + RandomUtils.nextFloat(-0.5f, 0.5f), entity.motionZ + RandomUtils.nextFloat(-0.5f, 0.5f), blockState)
                    }
                }
                "fire" ->
                    mc.effectRenderer.emitParticleAtEntity(entity, EnumParticleTypes.LAVA)
                "critical" -> mc.effectRenderer.emitParticleAtEntity(entity, EnumParticleTypes.CRIT)
                "magiccritical" -> mc.effectRenderer.emitParticleAtEntity(entity, EnumParticleTypes.CRIT_MAGIC)
            }
        }
    }
}