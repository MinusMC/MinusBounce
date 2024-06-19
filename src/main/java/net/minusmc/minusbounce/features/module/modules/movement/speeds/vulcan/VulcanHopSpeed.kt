package net.minusmc.minusbounce.features.module.modules.movement.speeds.vulcan

import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedMode
import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedType
import net.minusmc.minusbounce.utils.player.MovementUtils
import net.minusmc.minusbounce.utils.player.RotationUtils
import net.minecraft.client.settings.GameSettings
import kotlin.math.abs

class VulcanHopSpeed: SpeedMode("VulcanHop", SpeedType.VULCAN) {
    
    private var wasTimer = false

    override fun onUpdate() {
        if (wasTimer) {
            mc.timer.timerSpeed = 1.00f
            wasTimer = false
        }
        if ((RotationUtils.targetRotation == null && abs(mc.thePlayer.moveStrafing) < 0.1) || (RotationUtils.targetRotation != null && abs(
                RotationUtils.getAngleDifference(MovementUtils.direction, RotationUtils.targetRotation!!.yaw)
            ) < 45.0f)
        ) {
            mc.thePlayer.jumpMovementFactor = 0.026499f
        }else {
            mc.thePlayer.jumpMovementFactor = 0.0244f
        }
        mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)

        if (MovementUtils.speed < 0.215f && !mc.thePlayer.onGround) {
            MovementUtils.strafe(0.215f)
        }
        if (mc.thePlayer.onGround && MovementUtils.isMoving) {
            mc.gameSettings.keyBindJump.pressed = false
            mc.thePlayer.jump()
            if (!mc.thePlayer.isAirBorne) {
                return
            }
            mc.timer.timerSpeed = 1.25f
            wasTimer = true
            MovementUtils.strafe()
            if(MovementUtils.speed < 0.5f) {
                MovementUtils.strafe(0.4849f)
            }
        }else if (!MovementUtils.isMoving) {
            mc.timer.timerSpeed = 1.00f
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
    }
}