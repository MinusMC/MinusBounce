/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.minusmc.minusbounce.features.module.modules.movement.speeds.custom

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.EventState
import net.minusmc.minusbounce.event.MotionEvent
import net.minusmc.minusbounce.event.MoveEvent
import net.minusmc.minusbounce.features.module.modules.movement.Speed
import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedMode
import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedType
import net.minusmc.minusbounce.utils.MovementUtils
import java.util.*

class CustomSpeed: SpeedMode("Custom", SpeedType.CUSTOM) {
    private var groundTick = 0
    override fun onMotion(eventMotion: MotionEvent) {
        val speed = MinusBounce.moduleManager.getModule(Speed::class.java)
        if (speed == null || eventMotion.eventState !== EventState.PRE) return
        if (MovementUtils.isMoving) {
            mc.timer.timerSpeed = if (mc.thePlayer.motionY > 0) speed.upTimerValue.get() else speed.downTimerValue.get()
            if (mc.thePlayer.onGround) {
                if (groundTick >= speed.groundStay.get()) {
                    if (speed.doLaunchSpeedValue.get()) {
                        MovementUtils.strafe(speed.launchSpeedValue.get())
                    }
                    if (speed.yValue.get() != 0f) {
                        mc.thePlayer.motionY = speed.yValue.get().toDouble()
                    }
                } else if (speed.groundResetXZValue.get()) {
                    mc.thePlayer.motionX = 0.0
                    mc.thePlayer.motionZ = 0.0
                }
                groundTick++
            } else {
                groundTick = 0
                when (speed.strafeValue.get().lowercase(Locale.getDefault())) {
                    "strafe" -> MovementUtils.strafe(speed.speedValue.get())
                    "boost" -> MovementUtils.strafe()
                    "plus" -> MovementUtils.accelerate(speed.speedValue.get() * 0.1f)
                    "plusonlyup" -> if (mc.thePlayer.motionY > 0) {
                        MovementUtils.accelerate(speed.speedValue.get() * 0.1f)
                    } else {
                        MovementUtils.strafe()
                    }
                }
                mc.thePlayer.motionY += speed.addYMotionValue.get() * 0.03
            }
        } else if (speed.resetXZValue.get()) {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
    }

    override fun onEnable() {
        val speed = MinusBounce.moduleManager.getModule(Speed::class.java) ?: return
        if (speed.resetXZValue.get()) {
            mc.thePlayer.motionZ = 0.0
            mc.thePlayer.motionX = mc.thePlayer.motionZ
        }
        if (speed.resetYValue.get()) mc.thePlayer.motionY = 0.0
        super.onEnable()
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f
        super.onDisable()
    }

}