package net.minusmc.minusbounce.features.module.modules.movement.speeds.watchdog

import net.minecraft.potion.Potion
import net.minecraft.stats.StatList
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.JumpEvent
import net.minusmc.minusbounce.event.PreMotionEvent
import net.minusmc.minusbounce.features.module.modules.movement.Speed
import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedMode
import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedType
import net.minusmc.minusbounce.utils.player.MovementUtils

class WatchdogGround : SpeedMode("Hypixel", SpeedType.WATCHDOG) {

    override fun onJump(event: JumpEvent) {
        if (mc.thePlayer != null && MovementUtils.isMoving)
            event.isCancelled = true
    }

    override fun onPreMotion(event: PreMotionEvent) {
        val speed = MinusBounce.moduleManager.getModule(
            Speed::class.java
        )
        if (speed == null || mc.thePlayer.isInWater) return
        if (mc.thePlayer.onGround) {
            if (MovementUtils.isMoving) {
                mc.thePlayer.motionY = 0.41999998688698
                if (mc.thePlayer.isPotionActive(Potion.jump))
                    mc.thePlayer.motionY += ((mc.thePlayer.getActivePotionEffect(Potion.jump).amplifier + 1).toFloat() * 0.1f).toDouble()
                mc.thePlayer.isAirBorne = true
                mc.thePlayer.triggerAchievement(StatList.jumpStat)
                val baseSpeed = 0.482f
                if (mc.thePlayer.isPotionActive(Potion.moveSpeed))
                    MovementUtils.strafe(baseSpeed + ((mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).amplifier + 1).toFloat() * 0.0575f))
                else MovementUtils.strafe(baseSpeed)
            }
        } else {
            if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                mc.thePlayer.motionX *= (1.0002 + 0.0008 * (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).amplifier + 1))
                mc.thePlayer.motionZ *= (1.0002 + 0.0008 * (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).amplifier + 1))
                mc.thePlayer.speedInAir =
                    0.02f + 0.0003f * (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).amplifier + 1).toFloat()
            }
        }
    }
}
