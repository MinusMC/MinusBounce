package net.minusmc.minusbounce.features.module.modules.movement.speeds.matrix

import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedType
import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedMode
import net.minusmc.minusbounce.utils.player.MovementUtils
import net.minecraft.client.settings.GameSettings
import net.minusmc.minusbounce.value.BoolValue

class MatrixHop2Speed: SpeedMode("MatrixHop2", SpeedType.MATRIX) {
    private val groundStrafe = BoolValue("GroundStrafe", false)

    override fun onUpdate() {
        mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
        if (MovementUtils.isMoving) {
            if (mc.thePlayer.onGround) {
                mc.gameSettings.keyBindJump.pressed = false
                mc.timer.timerSpeed = 1.0f
                if (groundStrafe.get()) MovementUtils.strafe()
                mc.thePlayer.jump()
            }
            
             if (mc.thePlayer.motionY > 0.003) {
                mc.thePlayer.motionX *= 1.0012
                mc.thePlayer.motionZ *= 1.0012
                mc.timer.timerSpeed = 1.05f
             }
        }
       
    }
}
