package net.minusmc.minusbounce.features.module.modules.player.nofalls.hypixel

import net.minusmc.minusbounce.features.module.modules.player.nofalls.NoFallMode
import net.minecraft.network.play.client.C03PacketPlayer

class HypixelPacketNoFall: NoFallMode("HypixelPacket") {
	private var fallticks = 0

	override fun onEnable() {
		fallticks = 0
	}

	override fun onDisable() {
		fallticks = 0
	}

	override fun onUpdate() {
        val offset = 2.5
        if (!mc.thePlayer.onGround && mc.thePlayer.fallDistance - fallticks * offset >= 0.0) {
            mc.netHandler.addToSendQueue(C03PacketPlayer(true))
            fallticks++
        } else if (mc.thePlayer.onGround)
            fallticks = 1
	}
}