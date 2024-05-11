/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.movement.flys.ncp

import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyMode
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyType
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.utils.player.MovementUtils
import net.minusmc.minusbounce.utils.PacketUtils
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition

class NCP2Fly : FlyMode("NCP2", FlyType.NCP) {
	private val ncpMotionValue = FloatValue("Motion", 0f, 0f, 1f)

	override fun onEnable() {
		if (!mc.thePlayer.onGround) return

		val x = mc.thePlayer.posX
		val y = mc.thePlayer.posY
		val z = mc.thePlayer.posZ

		repeat(65) {
			PacketUtils.sendPacketNoEvent(C04PacketPlayerPosition(x, y + 0.049, z, false))
			PacketUtils.sendPacketNoEvent(C04PacketPlayerPosition(x, y, z, false))
		}

		PacketUtils.sendPacketNoEvent(C04PacketPlayerPosition(x, y + 0.1, z, false))

		mc.thePlayer.motionX *= 0.1
		mc.thePlayer.motionZ *= 0.1
		mc.thePlayer.swingItem()
	}

	override fun onUpdate() {
		mc.thePlayer.motionY = if (mc.gameSettings.keyBindSneak.isKeyDown) -0.5 else -ncpMotionValue.get().toDouble()
		MovementUtils.strafe()
	}

	override fun onPacket(event: PacketEvent) {
		val packet = event.packet

		if (packet is C03PacketPlayer)
			packet.onGround = true
	}

}