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

class OldIntave: KillAuraBlocking("OldIntave") {

    override fun onPreAttack(){
        mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1))
        mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
    }

    override fun onPostAttack(){
        KillAura.startBlocking()
    }
}