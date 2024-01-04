package net.minusmc.minusbounce.features.module.modules.killaura.blocking

import net.minusmc.minusbounce.features.module.modules.killaura.KillAuraBlocking

class PolarBlocking: KillAuraBlocking("Polar") {

    override fun onPreAttack() {
        mc.gameSettings.keyBindUseItem.pressed = false
    }

    override fun onPostAttack() {
        if (mc.thePlayer.hurtTime < 8 && mc.thePlayer.hurtTime != 1 && mc.thePlayer.fallDistance > 0)
            return
        mc.gameSettings.keyBindUseItem.pressed = true
    }
}