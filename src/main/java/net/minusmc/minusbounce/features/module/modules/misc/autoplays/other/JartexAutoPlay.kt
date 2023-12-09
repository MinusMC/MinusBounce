package net.minusmc.minusbounce.features.module.modules.misc.autoplays.other


import net.minusmc.minusbounce.features.module.modules.misc.autoplays.AutoPlayMode
import net.minusmc.minusbounce.event.PacketEvent
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.network.play.server.S2FPacketSetSlot
import net.minecraft.event.ClickEvent

class JartexAutoPlay: AutoPlayMode("Jartex") {
    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is S02PacketChat) {
            val text = packet.chatComponent.unformattedText
            val component = packet.chatComponent
            if (text.contains("Click here to play again", true)) 
                component.siblings.forEach { sib ->
                    val clickEvent = sib.chatStyle.chatClickEvent
                    if (clickEvent != null && clickEvent.action == ClickEvent.Action.RUN_COMMAND && clickEvent.value.startsWith("/"))
                        queueAutoPlay {
                            mc.thePlayer.sendChatMessage(clickEvent.value)
                        }
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