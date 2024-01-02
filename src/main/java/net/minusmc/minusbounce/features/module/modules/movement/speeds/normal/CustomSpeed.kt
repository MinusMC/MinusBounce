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
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.value.ListValue
import net.minusmc.minusbounce.value.IntegerValue
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.utils.MovementUtils
import java.util.*

class CustomSpeed: SpeedMode("Custom", SpeedType.NORMAL) {
    private val speedValue = FloatValue("Speed", 1.6f, 0.2f, 2f)
    private val launchSpeedValue = FloatValue("LaunchSpeed", 1.6f, 0.2f, 2f)
    private val addYMotionValue = FloatValue("AddYMotion", 0f, 0f, 2f)
    private val yValue = FloatValue("MotionY", 0f, 0f, 4f)
    private val upTimerValue = FloatValue("UpTimer", 1f, 0.1f, 2f)
    private val downTimerValue = FloatValue("DownTimer", 1f, 0.1f, 2f)
    private val strafeValue = ListValue("Strafe", arrayOf("Strafe", "Boost", "Plus", "PlusOnlyUp", "Non-Strafe"), "Boost")
    private val groundStay = IntegerValue("GroundStay", 0, 0, 10)
    private val groundResetXZValue = BoolValue("GroundResetXZ", false)
    private val resetXZValue = BoolValue("ResetXZ", false)
    private val resetYValue = BoolValue("ResetY", false)
    private val doLaunchSpeedValue = BoolValue("DoLaunchSpeed", true)

    private var groundTick = 0

    override fun onMotion(eventMotion: MotionEvent) {
        val speed = MinusBounce.moduleManager.getModule(Speed::class.java)
        if (speed == null || eventMotion.eventState !== EventState.PRE) return
        if (MovementUtils.isMoving) {
            mc.timer.timerSpeed = if (mc.thePlayer.motionY > 0) upTimerValue.get() else downTimerValue.get()
            if (mc.thePlayer.onGround) {
                if (groundTick >= groundStay.get()) {
                    if (doLaunchSpeedValue.get()) {
                        MovementUtils.strafe(launchSpeedValue.get())
                    }
                    if (yValue.get() != 0f) {
                        mc.thePlayer.motionY = yValue.get().toDouble()
                    }
                } else if (groundResetXZValue.get()) {
                    mc.thePlayer.motionX = 0.0
                    mc.thePlayer.motionZ = 0.0
                }
                groundTick++
            } else {
                groundTick = 0
                when (strafeValue.get().lowercase(Locale.getDefault())) {
                    "strafe" -> MovementUtils.strafe(speedValue.get())
                    "boost" -> MovementUtils.strafe()
                    "plus" -> MovementUtils.accelerate(speedValue.get() * 0.1f)
                    "plusonlyup" -> if (mc.thePlayer.motionY > 0) {
                        MovementUtils.accelerate(speedValue.get() * 0.1f)
                    } else {
                        MovementUtils.strafe()
                    }
                }
                mc.thePlayer.motionY += addYMotionValue.get() * 0.03
            }
        } else if (resetXZValue.get()) {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
    }

    override fun onEnable() {
        val speed = MinusBounce.moduleManager[Speed::class.java]!!
        if (resetXZValue.get()) {
            mc.thePlayer.motionZ = 0.0
            mc.thePlayer.motionX = mc.thePlayer.motionZ
        }
        if (resetYValue.get()) mc.thePlayer.motionY = 0.0
        super.onEnable()
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f
        super.onDisable()
    }

}