package net.minusmc.minusbounce.features.module.modules.misc.autoplays.other


import net.minusmc.minusbounce.features.module.modules.misc.autoplays.AutoPlayMode
import net.minusmc.minusbounce.event.PacketEvent
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.network.play.server.S2FPacketSetSlot

/**
 * Auto join to other match in minigames in Universocraft
 * @author toidicakhia, cranci1
 */

class UniversoCraftAutoPlay: AutoPlayMode("UniversoCraft") {
    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        /**
         * We don't execute specific command in minigames, we get command and execute this.
         * When S02PacketChat is handled, get text and match with pattern.
         * After this, get siblings and find text component that it has click event
         * If it is found, execute command in it.
         */

        if (packet !is S02PacketChat)
            return

        val text = packet.chatComponent.unformattedText
        if (!text.contains("Jugar de nuevo", true))
            return

        val commandTextComponent = packet.chatComponent.siblings.firstOrNull {
            it.unformattedText.contains("Jugar de nuevo", true)
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