package net.minusmc.minusbounce.features.module.modules.movement.speeds.vulcan

import net.minecraft.client.settings.GameSettings
import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedMode
import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedType
import net.minusmc.minusbounce.utils.player.MovementUtils

class VulcanYPortSpeed : SpeedMode("VulcanYPort", SpeedType.VULCAN) {

    private var wasTimer = false
    private var ticks = 0

    override fun onUpdate() {
        ticks++
        if (wasTimer) {
            mc.timer.timerSpeed = 1.00f
            wasTimer = false
        }
        mc.thePlayer.jumpMovementFactor = 0.0245f
        if (!mc.thePlayer.onGround && ticks > 3 && mc.thePlayer.motionY > 0) {
            mc.thePlayer.motionY = -0.27
        }

        mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
        if (MovementUtils.speed < 0.215f && !mc.thePlayer.onGround) {
            MovementUtils.strafe(0.215f)
        }
        if (mc.thePlayer.onGround && MovementUtils.isMoving) {
            ticks = 0
            mc.gameSettings.keyBindJump.pressed = false
            mc.thePlayer.jump()
            if (!mc.thePlayer.isAirBorne) {
                return
            }
            mc.timer.timerSpeed = 1.2f
            wasTimer = true
            if(MovementUtils.speed < 0.48f) {
                MovementUtils.strafe(0.48f)
            } else{
                MovementUtils.strafe((MovementUtils.speed*0.985).toFloat())
            }
        }else if (!MovementUtils.isMoving) {
            mc.timer.timerSpeed = 1.00f
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
    }
}
