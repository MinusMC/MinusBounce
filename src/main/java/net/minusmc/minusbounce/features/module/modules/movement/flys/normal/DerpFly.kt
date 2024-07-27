package net.minusmc.minusbounce.features.module.modules.movement.flys.normal

import net.minecraft.network.play.client.C03PacketPlayer
import net.minusmc.minusbounce.event.SentPacketEvent
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyMode
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyType
import net.minusmc.minusbounce.utils.player.MovementUtils
import net.minusmc.minusbounce.utils.misc.RandomUtils
import net.minusmc.minusbounce.value.FloatValue


class DerpFly: FlyMode("Derp", FlyType.NORMAL) {
	private val vanillaSpeedValue = FloatValue("Speed", 2F, 0F, 5F)

	override fun onEnable() {
		super.onEnable()}

	override fun onUpdate() {
        mc.thePlayer.capabilities.isFlying = false

        mc.thePlayer.motionY = 0.0
        mc.thePlayer.motionX = 0.0
        mc.thePlayer.motionZ = 0.0

        if (mc.gameSettings.keyBindJump.isKeyDown)
            mc.thePlayer.motionY += vanillaSpeedValue.get()

        if (mc.gameSettings.keyBindSneak.isKeyDown)
            mc.thePlayer.motionY -= vanillaSpeedValue.get()

        MovementUtils.strafe(vanillaSpeedValue.get())
	}

    override fun onSentPacket(event: SentPacketEvent) {
        val packet = event.packet
        if (packet is C03PacketPlayer) {
            packet.yaw = RandomUtils.nextFloat(0f, 360f)
            packet.pitch = RandomUtils.nextFloat(-90f, 90f)
        }
    }
}

