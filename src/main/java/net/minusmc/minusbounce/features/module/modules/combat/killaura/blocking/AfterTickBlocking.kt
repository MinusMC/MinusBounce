package net.minusmc.minusbounce.features.module.modules.combat.killaura.blocking

import net.minusmc.minusbounce.features.module.modules.combat.killaura.KillAuraBlocking

class AfterTickBlocking: KillAuraBlocking("AfterTick") {
    override fun onPreAttack() {
        killAura.stopBlocking()
    }

    override fun onPostMotion() {
        killAura.startBlocking()
    }
}