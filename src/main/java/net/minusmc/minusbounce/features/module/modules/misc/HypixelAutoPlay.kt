package net.minusmc.minusbounce.features.module.modules.misc

import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.network.play.client.C0DPacketCloseWindow
import net.minecraft.network.play.client.C0EPacketClickWindow
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.network.play.server.S2FPacketSetSlot
import net.minecraft.util.IChatComponent
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.ui.client.hud.element.elements.Notification
import java.util.*
import kotlin.concurrent.schedule

@ModuleInfo(name = "HypixelAutoPlay", spacedName = "Hypixel Auto Play", description = "Auto join new games.", category = ModuleCategory.MISC)
class HypixelAutoPlay : Module() {

    private var clickState = 0

    private var clicking = false
    private var queued = false
    private var waitForLobby = false

    override fun onEnable() {
        clickState = 0
        clicking = false
        queued = false
        waitForLobby = false
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (clickState == 1 && packet is S2DPacketOpenWindow) {
            event.cancelEvent()
        }

        if (packet is S2FPacketSetSlot) {
            val item = packet.func_149174_e() ?: return
            val windowId = packet.func_149175_c()
            val slot = packet.func_149173_d()
            val itemName = item.unlocalizedName
            val displayName = item.displayName

            if (clickState == 0 && windowId == 0 && slot == 43 && itemName.contains("paper", ignoreCase = true)) {
                queueAutoPlay {
                    mc.netHandler.addToSendQueue(C09PacketHeldItemChange(7))
                    repeat(2) {
                        mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(item))
                    }
                }
                clickState = 1
            }
        } else if (packet is S02PacketChat) {
            fun process(component: IChatComponent) {
                val value = component.chatStyle.chatClickEvent?.value
                if (value != null && value.startsWith("/play", true)) {
                    queueAutoPlay {
                        mc.thePlayer.sendChatMessage(value)
                    }
                }
                component.siblings.forEach {
                    process(it)
                }
            }
            process(packet.chatComponent)
        }
    }

    private fun queueAutoPlay(delay: Long = "1".toLong() * 1000, runnable: () -> Unit) {
        if (queued)
            return
        queued = true
        AutoDisable.handleGameEnd()
        if (this.state) {
            Timer().schedule(delay) {
                queued = false
                if (state) {
                    runnable()
                }
            }
            MinusBounce.hud.addNotification(Notification("Sending you to a new game in ${1}s!", Notification.Type.INFO, "1".toLong() * 1000L))
        }
    }
}