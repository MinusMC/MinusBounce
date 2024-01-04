package net.minusmc.minusbounce.features.module.modules.killaura.blocking

import net.minusmc.minusbounce.features.module.modules.killaura.KillAuraBlocking

class AfterTick: KillAuraBlocking("AfterTick") {
    override fun onPreAttack(){
        killAura.stopBlocking()
    }

    override fun onPostMotion(){
        killAura.startBlocking()
    }
}