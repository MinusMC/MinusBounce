package net.minusmc.minusbounce.features.module.modules.misc.autoplays.other


import net.minusmc.minusbounce.features.module.modules.misc.autoplays.AutoPlayMode
import net.minusmc.minusbounce.event.ReceivedPacketEvent
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.network.play.server.S2FPacketSetSlot


class MimemoraAutoPlay: AutoPlayMode("Mimemora") {
    override fun onReceivedPacket(event: ReceivedPacketEvent) {
        val packet = event.packet

        if (packet is S02PacketChat) {
            val text = packet.chatComponent.unformattedText
            if (text.contains("Has click en alguna de las siguientes opciones", true))
                queueAutoPlay {
                    mc.thePlayer.sendChatMessage("/join")
                }
        }
    }

    override fun onEnable() {
        queued = false
    }

    override fun onWorld() {
        queued = false
    }
} 