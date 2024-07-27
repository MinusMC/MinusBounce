package net.minusmc.minusbounce.features.module.modules.combat.criticals.other


import net.minusmc.minusbounce.features.module.modules.combat.criticals.CriticalMode
import net.minusmc.minusbounce.event.SentPacketEvent
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action

class RedeskyCritical : CriticalMode("Redesky") {
	private var canCrits = true

	override fun onEnable() {
        canCrits = true
    }
	
	override fun onSentPacket(event: SentPacketEvent) {
        val packet = event.packet

        if (packet is C03PacketPlayer) {
            if (mc.thePlayer.onGround && canCrits) {
                packet.y += 0.000001
                packet.onGround = false
            }

            val entityBoundingBox = mc.thePlayer.entityBoundingBox.offset(0.0, mc.thePlayer.motionY * 0.98 - 0.0784, 0.0)

            if (mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, entityBoundingBox).isEmpty())
                packet.onGround = true
        }

        if(packet is C07PacketPlayerDigging)
            when (packet.status) {
                Action.START_DESTROY_BLOCK -> canCrits = false
                Action.STOP_DESTROY_BLOCK, Action.ABORT_DESTROY_BLOCK -> canCrits = true
            }
	}
}
