package net.minusmc.minusbounce.features.module.modules.misc.autoplays.other


import net.minusmc.minusbounce.features.module.modules.misc.autoplays.AutoPlayMode
import net.minusmc.minusbounce.event.PacketEvent
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.network.play.server.S2FPacketSetSlot


class UniversoSWAutoPlay: AutoPlayMode("UniversoSW") {
    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is S02PacketChat) {
            val text = packet.chatComponent.unformattedText
            if (text.contains("Jugar de nuevo", true))
                queueAutoPlay {
                    mc.thePlayer.sendChatMessage("/skywars random")
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