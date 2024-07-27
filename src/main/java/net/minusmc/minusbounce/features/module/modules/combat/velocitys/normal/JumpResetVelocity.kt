package net.minusmc.minusbounce.features.module.modules.combat.velocitys.normal

import net.minecraft.client.settings.GameSettings
import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode

class JumpResetVelocity: VelocityMode("JumpReset") {
    private var velocityInput = false

    override fun onUpdate() {
        if (mc.thePlayer.hurtTime >= 8)
            mc.gameSettings.keyBindJump.pressed = true

        if (mc.thePlayer.hurtTime >= 7 && !mc.gameSettings.keyBindForward.pressed) {
            mc.gameSettings.keyBindForward.pressed = true
            velocityInput = true
        }

        if (mc.thePlayer.hurtTime in 1..6) {
            mc.gameSettings.keyBindJump.pressed = false

            if (velocityInput) {
                mc.gameSettings.keyBindForward.pressed = false
                velocityInput = false
            }
        }

        if (mc.thePlayer.hurtTime == 1)
            mc.gameSettings.keyBindForward.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindForward)
    }
}
