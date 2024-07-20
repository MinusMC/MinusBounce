/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.minusmc.minusbounce.features.module.modules.movement.speeds.normal

import net.minusmc.minusbounce.event.StrafeEvent
import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedMode
import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedType
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.value.ListValue
import net.minusmc.minusbounce.value.IntegerValue
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.utils.player.MovementUtils
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

    override fun onStrafe(event: StrafeEvent) {
        
        if (!MovementUtils.isMoving) {
            if (resetXZValue.get()) {
                mc.thePlayer.motionX = 0.0
                mc.thePlayer.motionZ = 0.0
            }

            return
        }

        mc.timer.timerSpeed = if (mc.thePlayer.motionY > 0) upTimerValue.get() else downTimerValue.get()
        if (mc.thePlayer.onGround) {
            if (groundTick >= groundStay.get()) {
                if (doLaunchSpeedValue.get())
                    MovementUtils.strafe(launchSpeedValue.get())

                if (yValue.get() != 0f)
                    mc.thePlayer.motionY = yValue.get().toDouble()

            } else if (groundResetXZValue.get()) {
                mc.thePlayer.motionX = 0.0
                mc.thePlayer.motionZ = 0.0
            }
            groundTick++
        } else {
            groundTick = 0
            when (strafeValue.get().lowercase()) {
                "strafe" -> MovementUtils.strafe(speedValue.get())
                "boost" -> MovementUtils.strafe()
                "plus" -> MovementUtils.boost(speedValue.get() * 0.1f)
                "plusonlyup" -> if (mc.thePlayer.motionY > 0)
                    MovementUtils.boost(speedValue.get() * 0.1f)
                else MovementUtils.strafe()
            }
            mc.thePlayer.motionY += addYMotionValue.get() * 0.03
        }
    }

    override fun onEnable() {
        if (resetXZValue.get()) {
            mc.thePlayer.motionZ = 0.0
            mc.thePlayer.motionX = 0.0
        }

        if (resetYValue.get())
            mc.thePlayer.motionY = 0.0
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f
        mc.thePlayer.speedInAir = 0.02f
    }

}