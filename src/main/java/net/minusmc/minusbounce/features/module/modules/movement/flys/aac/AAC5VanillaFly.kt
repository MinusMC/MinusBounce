package net.minusmc.minusbounce.features.module.modules.movement.flys.aac

import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyType
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyMode
import net.minusmc.minusbounce.utils.PacketUtils
import net.minusmc.minusbounce.utils.player.MovementUtils
import net.minusmc.minusbounce.event.SentPacketEvent
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.ListValue
import net.minusmc.minusbounce.value.IntegerValue
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook

class AAC5VanillaFly: FlyMode("AAC5-Vanilla", FlyType.AAC) {
	private val speedValue = FloatValue("Speed", 2f, 0f, 5f)
	private val aac5NoClipValue = BoolValue("NoClip", true)
    private val aac5NofallValue = BoolValue("NoFall", true)
    private val aac5UseC04Packet = BoolValue("UseC04", true)
    private val aac5Packet = ListValue("Packet", arrayOf("Original", "Rise", "Other"), "Original")
    private val aac5PursePacketsValue = IntegerValue("Purse", 7, 3, 20)

    private val aac5C03List = ArrayList<C03PacketPlayer>()

    private fun sendAAC5Packets() {
        var yaw = mc.thePlayer.rotationYaw
        var pitch = mc.thePlayer.rotationPitch

        for (packet in aac5C03List) {
            PacketUtils.sendPacketNoEvent(packet)
            if (!packet.isMoving)
                continue

            if (packet.rotating) {
                yaw = packet.yaw
                pitch = packet.pitch
            }

            when (aac5Packet.get().lowercase()) {
                "original" -> {
                    if (aac5UseC04Packet.get()) {
                        PacketUtils.sendPacketNoEvent(C04PacketPlayerPosition(packet.x, 1e+159, packet.z, true))
                        PacketUtils.sendPacketNoEvent(C04PacketPlayerPosition(packet.x, packet.y, packet.z, true))
                    } else {
                        PacketUtils.sendPacketNoEvent(C06PacketPlayerPosLook(packet.x, 1e+159, packet.z, yaw, pitch, true))
                        PacketUtils.sendPacketNoEvent(C06PacketPlayerPosLook(packet.x, packet.y, packet.z, yaw, pitch, true))
                    }
                }
                "rise" -> {
                    if (aac5UseC04Packet.get()) {
                        PacketUtils.sendPacketNoEvent(C04PacketPlayerPosition(packet.x, -1e+159, packet.z + 10, true))
                        PacketUtils.sendPacketNoEvent(C04PacketPlayerPosition(packet.x, packet.y, packet.z, true))
                    } else {
                        PacketUtils.sendPacketNoEvent(C06PacketPlayerPosLook(packet.x, -1e+159, packet.z + 10, yaw, pitch, true))
                        PacketUtils.sendPacketNoEvent(C06PacketPlayerPosLook(packet.x, packet.y, packet.z, yaw, pitch, true))
                    }
                }
                "other" -> {
                    if (aac5UseC04Packet.get()) {
                        PacketUtils.sendPacketNoEvent(C04PacketPlayerPosition(packet.x, 1.7976931348623157E+308, packet.z, true))
                        PacketUtils.sendPacketNoEvent(C04PacketPlayerPosition(packet.x, packet.y, packet.z, true))
                    } else {
                        PacketUtils.sendPacketNoEvent(C06PacketPlayerPosLook(packet.x, 1.7976931348623157E+308, packet.z, yaw, pitch, true))
                        PacketUtils.sendPacketNoEvent(C06PacketPlayerPosLook(packet.x, packet.y, packet.z, yaw, pitch, true))
                    }
                } 
            }
        }

        aac5C03List.clear()
    }


	override fun onDisable() {
        if (!mc.isIntegratedServerRunning)
            sendAAC5Packets()
	}

	override fun onUpdate() {
		if (aac5NoClipValue.get())
            mc.thePlayer.noClip = true

		mc.thePlayer.capabilities.isFlying = false
	    mc.thePlayer.motionY = 0.0
	    mc.thePlayer.motionX = 0.0
	    mc.thePlayer.motionZ = 0.0

	    if (mc.gameSettings.keyBindJump.isKeyDown)
	        mc.thePlayer.motionY += speedValue.get()

	    if (mc.gameSettings.keyBindSneak.isKeyDown)
	        mc.thePlayer.motionY -= speedValue.get()

	    MovementUtils.strafe(speedValue.get())
	}

	override fun onSentPacket(event: SentPacketEvent) {
		val packet = event.packet
		if (packet is C03PacketPlayer && !mc.isIntegratedServerRunning) {
            if (aac5NofallValue.get())
                packet.onGround = true

            aac5C03List.add(packet)
            event.isCancelled = true

            if (aac5C03List.size > aac5PursePacketsValue.get())
                sendAAC5Packets()
		}
	}
}