package net.minusmc.minusbounce.features.module.modules.combat.killaura.blocking

import net.minusmc.minusbounce.features.module.modules.combat.killaura.KillAuraBlocking
import net.minusmc.minusbounce.utils.extensions.*

class RightHoldBlocking: KillAuraBlocking("RightHold") {
    override fun onPreUpdate() {
        val target = killAura.target ?: return
        mc.gameSettings.keyBindUseItem.pressed = killAura.canBlock
            && mc.thePlayer.getDistanceToEntityBox(target) < killAura.rangeValue.get()
    }

    override fun onDisable() {
    	mc.gameSettings.keyBindUseItem.pressed = false
    }
}