package net.minusmc.minusbounce.features.module.modules.misc.autoplays.other


import net.minusmc.minusbounce.features.module.modules.misc.autoplays.AutoPlayMode
import net.minusmc.minusbounce.event.ReceivedPacketEvent
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.S02PacketChat
/**
 * Auto join to other match in minigames in MineManClub
 * Method is same as Universocraft
 */

class ZoneCraftAutoPlay: AutoPlayMode("ZoneCraft") {
    override fun onReceivedPacket(event: ReceivedPacketEvent) {
        val packet = event.packet

        if (packet !is S02PacketChat)
            return

        val text = packet.chatComponent.unformattedText
        if (!text.contains("Volver a jugar", true))
            return

        val commandTextComponent = packet.chatComponent.siblings.firstOrNull {
            it.unformattedText.contains("Volver a jugar", true)
        } ?: return

        val command = commandTextComponent.chatStyle.chatClickEvent.value
        queueAutoPlay {
            mc.thePlayer.sendChatMessage(command)
        }
    }

    override fun onEnable() {
        queued = false
    }

    override fun onWorld() {
        queued = false
    }
} 