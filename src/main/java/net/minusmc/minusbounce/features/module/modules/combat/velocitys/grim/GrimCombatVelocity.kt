package net.minusmc.minusbounce.features.module.modules.combat.velocitys.grim

import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.client.C0APacketAnimation
import net.minusmc.minusbounce.MinusBounce
import net.minecraft.network.play.client.C0FPacketConfirmTransaction
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode


class GrimCombatVelocity: VelocityMode("GrimCombat") {

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is S12PacketEntityVelocity) {
            val target = MinusBounce.combatManager.getNearByEntity(3f)
            repeat(12) {
                mc.netHandler.addToSendQueue(C0FPacketConfirmTransaction())
                mc.thePlayer.sendQueue.addToSendQueue(C02PacketUseEntity(target, C02PacketUseEntity.Action.ATTACK))
                mc.thePlayer.sendQueue.addToSendQueue(C0APacketAnimation())
            }
            event.cancelEvent()
            mc.thePlayer.motionY = packet.motionY.toDouble() / 8000.0
            mc.thePlayer.motionX *= 0
            mc.thePlayer.motionZ *= 0
        }

    }
    // ê sẵn tiện m quay lại fix đi t đi vệ sinh
    // v khỏi skid hay s skid thu xem ntn
}