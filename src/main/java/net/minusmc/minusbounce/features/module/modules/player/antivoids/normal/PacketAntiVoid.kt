package net.minusmc.minusbounce.features.module.modules.player.antivoids.normal

import net.minusmc.minusbounce.features.module.modules.player.antivoids.AntiVoidMode
import net.minusmc.minusbounce.event.*
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.client.C03PacketPlayer


class PacketAntiVoid: AntiVoidMode("Packet") {
    private val packetCache = ArrayList<C03PacketPlayer>()
    private var canCancel = false

    override fun onEnable() {
        canCancel = false
    }

    override fun onUpdate() {
        if (isVoid)
            canCancel = true

        if (canCancel) {
            if (mc.thePlayer.onGround) {
                for (packet in packetCache) {
                    mc.netHandler.addToSendQueue(packet)
                }
                packetCache.clear()
            }
            canCancel = false
        }
    }

    override fun onSentPacket(event: SentPacketEvent) {
    	val packet = event.packet
    	if (canCancel && packet is C03PacketPlayer) {
            packetCache.add(packet)
            event.isCancelled = true
        }
    }

    override fun onReceivedPacket(event: ReceivedPacketEvent) {
        val packet = event.packet

        if (packet is S08PacketPlayerPosLook) {
            packetCache.clear()
            canCancel = false
        }
    }
}