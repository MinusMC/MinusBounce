package net.minusmc.minusbounce.features.module.modules.killaura.blocking

import net.minusmc.minusbounce.utils.MinecraftInstance
import net.minusmc.minusbounce.features.module.modules.combat.KillAura
import net.minusmc.minusbounce.MinusBounce

class SwingBlocking: KillAuraBlocking("Swing") {
    override fun onPostMotion() {
    	when (mc.thePlayer.swingProgressInt) {
            1 -> stopBlocking()
            2 -> startBlocking(currentTarget!!, mc.thePlayer.getDistanceToEntityBox(killAura.currentTarget!!) < killAura.rangeValue.get())
        }
    }
}