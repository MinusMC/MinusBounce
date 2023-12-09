package net.minusmc.minusbounce.features.module.modules.misc.autoplays.other

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.ui.client.hud.element.elements.Notification
import net.minusmc.minusbounce.features.module.modules.misc.autoplays.AutoPlayMode
import net.minusmc.minusbounce.event.PacketEvent
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.network.play.server.S2FPacketSetSlot
import java.util.*
import kotlin.concurrent.schedule

class BlocksMCAutoPlay: AutoPlayMode("BlocksMC") {
	private var clickState = 0

	override fun onPacket(event: PacketEvent) {
		val packet = event.packet
        
        if (packet is S2FPacketSetSlot) {
            val item = packet.func_149174_e() ?: return
            val windowId = packet.func_149175_c()
            val slot = packet.func_149173_d()
            val itemName = item.unlocalizedName

            if (clickState == 0 && windowId == 0 && slot == 43 && itemName.contains("paper", ignoreCase = true)) {
                queueAutoPlay {
                    mc.netHandler.addToSendQueue(C09PacketHeldItemChange(7))
                    repeat(2) {
                        mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(item))
                    }
                }
                clickState = 1
            }
            
        }

        if (packet is S02PacketChat) {
            val text = packet.chatComponent.unformattedText
            if (clickState == 1 && text.contains("Only VIP players can join full servers!", true)) {
                MinusBounce.hud.addNotification(Notification("Join failed! trying again...", Notification.Type.WARNING, 3000L))
                Timer().schedule(1500L) {
                    mc.netHandler.addToSendQueue(C09PacketHeldItemChange(7))
                    repeat(2) {
                        mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()))
                    }
                }
            }
        }
	}

    override fun onEnable() {
        clickState = 0
        queued = false
    }

    override fun onWorld() {
        clickState = 0
        queued = false
    }
} 