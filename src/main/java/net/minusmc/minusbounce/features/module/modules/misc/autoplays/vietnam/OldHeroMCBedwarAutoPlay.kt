package net.minusmc.minusbounce.features.module.modules.misc.autoplays.vietnam

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.ui.client.hud.element.elements.Notification
import net.minusmc.minusbounce.features.module.modules.misc.autoplays.AutoPlayMode
import net.minusmc.minusbounce.event.PacketEvent
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

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is S02PacketChat) {
            val text = packet.chatComponent.unformattedText

            if (text.contains("Bạn đã bị loại!", false)
                || text.contains("đã thắng trò chơi", false)) {
                mc.thePlayer.sendChatMessage("/bw leave")
                waitForLobby = true
            }
            if (((waitForLobby || autoStartValue.get()) && text.contains("¡Hiển thị", false))
                || (replayWhenKickedValue.get() && text.contains("[Anticheat] You have been kicked from the server!", false))) {
                queueAutoPlay {
                    mc.thePlayer.sendChatMessage("/bw join ${bwModeValue.get()}")
                }
                waitForLobby = false
            }
            if (showGuiWhenFailedValue.get() && text.contains("giây", false) && text.contains("thất bại", false)) {
                MinusBounce.hud.addNotification(Notification("Failed to join, showing GUI...", Notification.Type.ERROR, 1000L))
                mc.thePlayer.sendChatMessage("/bw gui ${bwModeValue.get()}")
            }
        }
    }

    override fun onEnable() {
        waitForLobby = false
    }
} 