package net.minusmc.minusbounce.features.module.modules.combat.criticals.other

import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minusmc.minusbounce.event.AttackEvent
import net.minusmc.minusbounce.features.module.modules.combat.criticals.CriticalMode

class BlocksMCCritical : CriticalMode("BlocksMC") {
    override fun onAttack(event: AttackEvent) {
        val x = mc.thePlayer.posX
        val y = mc.thePlayer.posY
        val z = mc.thePlayer.posZ
        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.001091981, z, true))
        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.000114514, z, false))
        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y, z, false))
    }
}
