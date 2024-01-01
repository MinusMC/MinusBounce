package net.minusmc.minusbounce.features.module.modules.movement.speeds.blocksmc

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedMode
import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedType
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.utils.MovementUtils
import net.minecraft.potion.Potion
import net.minusmc.minusbounce.features.module.modules.combat.KillAura

class BlocksMCSpeed: SpeedMode("BlocksMC", SpeedType.BLOCKSMC) {

    private val resetMotionValue = BoolValue("ResetMotion", false)
    private val damageBoostValue = BoolValue("DamageBoost", false)
    private val timerValue = BoolValue("UsingTimer", false) { damageBoostValue.get() }

    override fun onUpdate() {

        if (MovementUtils.isMoving) {
            if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                MovementUtils.strafe(0.34555554F)
            } else MovementUtils.strafe()
            if(mc.thePlayer.onGround) {
                mc.thePlayer.jump()
                MovementUtils.strafe(0.38F)
            }
        }

        if (damageBoostValue.get() && mc.thePlayer.hurtTime > 0 && MinusBounce.moduleManager[KillAura::class.java]!!.target != null) {
            MovementUtils.strafe(0.45995554f)
        }


        if (damageBoostValue.get() && timerValue.get() && mc.thePlayer.hurtTime > 0) {
            mc.timer.timerSpeed = 1.05f
        }
        else
            mc.timer.timerSpeed = 1.0f

    }

    override fun onDisable() {
        if (resetMotionValue.get()){
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
    }
}