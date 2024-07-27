package net.minusmc.minusbounce.features.module.modules.misc.autoplays.vietnam

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.ui.client.hud.element.elements.Notification
import net.minusmc.minusbounce.features.module.modules.misc.autoplays.AutoPlayMode
import net.minusmc.minusbounce.event.ReceivedPacketEvent
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.ListValue
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.network.play.server.S2FPacketSetSlot


class OldHeroMCAutoPlay: AutoPlayMode("OldHeroMC") {
    private val bwModeValue = ListValue("Mode", arrayOf("SOLO", "4v4v4v4"), "4v4v4v4")
    private val autoStartValue = BoolValue("AutoStartAtLobby", true)
    private val replayWhenKickedValue = BoolValue("ReplayWhenKicked", true)
    private val showGuiWhenFailedValue = BoolValue("ShowGuiWhenFailed", true)

    private var waitForLobby = false

    override fun onReceivedPacket(event: ReceivedPacketEvent) {
        val packet = event.packet

        if (packet is S02PacketChat) {
            val text = packet.chatComponent.unformattedText

            if (text.contains("Bạn đã bị loại!") || text.contains("đã thắng trò chơi")) {
                mc.thePlayer.sendChatMessage("/bw leave")
                waitForLobby = true
            }

            if (((waitForLobby || autoStartValue.get()) && text.contains("¡Hiển thị",))
                || (replayWhenKickedValue.get() && text.contains("[Anticheat] You have been kicked from the server!"))) {
                queueAutoPlay {
                    mc.thePlayer.sendChatMessage("/bw join ${bwModeValue.get()}")
                }
                waitForLobby = false
            }
            if (showGuiWhenFailedValue.get() && text.contains("giây") && text.contains("thất bại")) {
                MinusBounce.hud.addNotification(Notification("AutoPlay", "Failed to join, showing GUI...", Notification.Type.ERROR, 1000L))
                mc.thePlayer.sendChatMessage("/bw gui ${bwModeValue.get()}")
            }
        }
    }

    override fun onEnable() {
        waitForLobby = false
    }
} 