package net.minusmc.minusbounce.features.module.modules.killaura.blocking

import net.minusmc.minusbounce.utils.MinecraftInstance
import net.minusmc.minusbounce.features.module.modules.combat.KillAura
import net.minecraft.item.ItemSword
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.utils.*
import net.minecraft.network.play.client.*
import net.minusmc.minusbounce.features.module.modules.combat.KillAura.currentTarget
import net.minusmc.minusbounce.features.module.modules.killaura.KillAuraBlocking
import net.minusmc.minusbounce.features.module.modules.combat.KillAura.blockingStatus

class Vanilla: KillAuraBlocking("Vanilla") {

    override fun onPreAttack(){
        KillAura.stopBlocking()
    }

    override fun onPostAttack(){
        KillAura.startBlocking()
    }
}