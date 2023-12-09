package net.minusmc.minusbounce.features.module.modules.misc.autoplays.other


import net.minusmc.minusbounce.features.module.modules.misc.autoplays.AutoPlayMode
import net.minusmc.minusbounce.event.PacketEvent
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.network.play.server.S2FPacketSetSlot
import net.minecraft.util.IChatComponent


class HypixelAutoPlay: AutoPlayMode("Hypixel") {
	private var clickState = 0

	override fun onPacket(event: PacketEvent) {
		val packet = event.packet
        
        if (clickState == 1 && packet is S2DPacketOpenWindow) {
            event.cancelEvent()
        }

        if (packet is S2FPacketSetSlot) {
            val item = packet.func_149174_e() ?: return
            val windowId = packet.func_149175_c()
            val slot = packet.func_149173_d()
            val itemName = item.unlocalizedName

            if (clickState == 0 && windowId == 0 && slot == 43 && itemName.contains("paper", true)) {
                queueAutoPlay {
                    mc.netHandler.addToSendQueue(C09PacketHeldItemChange(7))
                    repeat(2) {
                        mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(item))
                    }
                }
                clickState = 1
            }
            

            if (clickState == 1 && windowId != 0 && itemName.equals("item.fireworks", true)) {
                mc.netHandler.addToSendQueue(C0EPacketClickWindow(windowId, slot, 0, 0, item, 1919))
                mc.netHandler.addToSendQueue(C0DPacketCloseWindow(windowId))
            }
        }

        if (packet is S02PacketChat) {
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

    override fun onEnable() {
        clickState = 0
        queued = false
    }

    override fun onWorld() {
        clickState = 0
        queued = false
    }
} 