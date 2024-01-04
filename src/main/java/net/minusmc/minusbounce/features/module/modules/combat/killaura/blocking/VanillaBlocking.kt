package net.minusmc.minusbounce.features.module.modules.killaura.blocking

import net.minusmc.minusbounce.features.module.modules.killaura.KillAuraBlocking

class Vanilla: KillAuraBlocking("Vanilla") {
    override fun onPreAttack(){
        killAura.stopBlocking()
    }

    override fun onPostAttack(){
        killAura.startBlocking()
    }
}