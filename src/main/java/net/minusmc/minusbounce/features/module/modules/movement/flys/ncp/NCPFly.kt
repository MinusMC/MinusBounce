package net.minusmc.minusbounce.features.module.modules.movement.flys.ncp

import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyType
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyMode
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.utils.player.MovementUtils
import net.minusmc.minusbounce.event.SentPacketEvent
import net.minecraft.network.play.client.C03PacketPlayer


class NCPFly: FlyMode("NCP", FlyType.NCP) {
    private val ncpMotionValue = FloatValue("Motion", 0f, 0f, 1f)

	override fun onUpdate() {
        mc.thePlayer.motionY = -ncpMotionValue.get().toDouble()

        if(mc.gameSettings.keyBindSneak.isKeyDown)
            mc.thePlayer.motionY = -0.5

        MovementUtils.strafe()
	}

    override fun onSentPacket(event: SentPacketEvent) {
        val packet = event.packet
        if (packet is C03PacketPlayer)
            packet.onGround = true
    }
}