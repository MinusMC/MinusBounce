package net.minusmc.minusbounce.features.module.modules.combat.velocitys.normal

import net.minecraft.client.settings.GameSettings
import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode


class JumpResetVelocity: VelocityMode("JumpReset") {
    private var start = 0

    override fun onUpdate() {
        while (mc.thePlayer.hurtTime >= 8) {
            mc.gameSettings.keyBindJump.pressed = true
            break
        }

        while (mc.thePlayer.hurtTime >= 7 && !mc.gameSettings.keyBindForward.pressed) {
            mc.gameSettings.keyBindForward.pressed = true
            start = 1
            break
        }

        if (mc.thePlayer.hurtTime in 1..6) {
            mc.gameSettings.keyBindJump.pressed = false
            if (start == 1) {
                mc.gameSettings.keyBindForward.pressed = false
                start = 0
            }
        }

        if (mc.thePlayer.hurtTime == 1) {
            mc.gameSettings.keyBindForward.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindForward)
        }
    }
}
