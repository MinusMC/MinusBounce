package net.minusmc.minusbounce.features.module.modules.combat.killaura.blocking

import net.minusmc.minusbounce.features.module.modules.combat.killaura.KillAuraBlocking

class PolarBlocking: KillAuraBlocking("Polar") {

    override fun onPreAttack() {
        mc.gameSettings.keyBindUseItem.pressed = false
    }

    override fun onPostAttack() {
        if (mc.thePlayer.hurtTime < 8 && mc.thePlayer.hurtTime != 1 && mc.thePlayer.fallDistance > 0)
            mc.gameSettings.keyBindUseItem.pressed = false
        else 
            mc.gameSettings.keyBindUseItem.pressed = true
    }
}