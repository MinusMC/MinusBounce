package net.minusmc.minusbounce.features.module.modules.misc.autoplays.other


import net.minusmc.minusbounce.features.module.modules.misc.autoplays.AutoPlayMode
import net.minusmc.minusbounce.event.PacketEvent
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.network.play.server.S2FPacketSetSlot
import java.util.*
import kotlin.concurrent.schedule

class RedeskyAutoPlay: AutoPlayMode("Redesky") {
	private var clicking = false
	private var clickState = 0

	override fun onPacket(event: PacketEvent) {
		val packet = event.packet

		if (clicking && (packet is C0EPacketClickWindow || packet is C07PacketPlayerDigging)) {
            event.cancelEvent()
            return
        }
        if (clickState == 2 && packet is S2DPacketOpenWindow)
            event.cancelEvent()
        
        if (packet is S2FPacketSetSlot) {
            val item = packet.func_149174_e() ?: return
            val windowId = packet.func_149175_c()
            val slot = packet.func_149173_d()
            val itemName = item.unlocalizedName
            val displayName = item.displayName

            if (clickState == 0 && windowId == 0 && slot == 42 && itemName.contains("paper", ignoreCase = true) && displayName.contains("Jogar novamente", ignoreCase = true)) {
                clickState = 1
                clicking = true
                queueAutoPlay {
                    mc.netHandler.addToSendQueue(C09PacketHeldItemChange(6))
                    mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(item))
                    mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                    clickState = 2
                }
            } else if (clickState == 2 && windowId != 0 && slot == 11 && itemName.contains("enderPearl", ignoreCase = true))
                Timer().schedule(500L) {
                    clicking = false
                    clickState = 0
                    mc.netHandler.addToSendQueue(C0EPacketClickWindow(windowId, slot, 0, 0, item, 1919))
                }
            }
	}

    override fun onEnable() {
        clickState = 0
        queued = false
        clicking = false
    }

    override fun onWorld() {
        clickState = 0
        queued = false
        clicking = false
    }
} 