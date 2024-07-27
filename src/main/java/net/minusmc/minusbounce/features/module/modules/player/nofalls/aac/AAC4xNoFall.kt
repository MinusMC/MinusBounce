package net.minusmc.minusbounce.features.module.modules.player.nofalls.aac

import net.minusmc.minusbounce.features.module.modules.player.nofalls.NoFallMode
import net.minusmc.minusbounce.utils.PlayerUtils
import net.minusmc.minusbounce.event.SentPacketEvent
import net.minusmc.minusbounce.event.PreMotionEvent
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.AxisAlignedBB

class AAC4xNoFall: NoFallMode("AAC4.x") {
    private var fakeLagging = false
    private var packetModify = false

    private val packets = mutableListOf<C03PacketPlayer>()

    override fun onEnable() {
        fakeLagging = false
        packetModify = false
        packets.clear()
    }

    override fun onDisable() {
        fakeLagging = false
        packetModify = false
        packets.clear()
    }

    override fun onPreMotion(event: PreMotionEvent) {
        if (!PlayerUtils.isBlockUnder) {
            if (fakeLagging) {
                fakeLagging = false
                clearPackets()
            }
            return
        }

        if (mc.thePlayer.onGround && fakeLagging) {
            fakeLagging = false
            clearPackets()
            return
        }

        if (mc.thePlayer.fallDistance > 2.5 && fakeLagging) {
            packetModify = true
            mc.thePlayer.fallDistance = 0f
        }

        if (PlayerUtils.isInAir(4.0, 1.0))
            return

        if (!fakeLagging) 
            fakeLagging = true
    }

    private fun clearPackets() {
        while (packets.size > 0) {
            val packet = packets.removeFirst()
            mc.netHandler.addToSendQueue(packet)
        }
    }

    override fun onSentPacket(event: SentPacketEvent) {
        val packet = event.packet

        if (fakeLagging && packet is C03PacketPlayer) {
            event.isCancelled = true
            if (packetModify) {
                packet.onGround = true
                packetModify = false
            }
            packets.add(packet)
        }
    }
}