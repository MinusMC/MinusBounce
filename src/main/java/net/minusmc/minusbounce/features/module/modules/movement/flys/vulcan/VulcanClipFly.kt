package net.minusmc.minusbounce.features.module.modules.movement.flys.vulcan

import net.minusmc.minusbounce.event.PreMotionEvent
import net.minusmc.minusbounce.event.ReceivedPacketEvent
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyMode
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyType
import net.minusmc.minusbounce.utils.misc.MathUtils
import net.minusmc.minusbounce.value.BoolValue
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import kotlin.math.cos
import kotlin.math.sin

class VulcanClipFly: FlyMode("VulcanClip", FlyType.VULCAN) {
    private val canClipValue = BoolValue("CanClip", true)

    private var waitFlag = false
    private var canGlide = false
    private var ticks = 0

    override fun onEnable() {
		super.onEnable()
        if (mc.thePlayer.onGround && canClipValue.get()) {
            clip(0f, -0.1f)
            waitFlag = true
            canGlide = false
            ticks = 0
            mc.timer.timerSpeed = 0.1f
        } else {
            waitFlag = false
            canGlide = true
        }
    }

    override fun onPreMotion(event: PreMotionEvent) {
        if (!canGlide)
            return

        mc.timer.timerSpeed = 1f
        mc.thePlayer.motionY = -if (ticks % 2 == 0) 0.17 else 0.1

        if (ticks == 0)
            mc.thePlayer.motionY = -0.07

        ticks++
    }

    override fun onReceivedPacket(event: ReceivedPacketEvent) {
        val packet = event.packet
        if (packet is S08PacketPlayerPosLook && waitFlag) {
            waitFlag = false
            mc.thePlayer.setPosition(packet.x, packet.y, packet.z)
            mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, false))
            event.isCancelled = true
            mc.thePlayer.jump()
            clip(0.127318f, 0f)
            clip(3.425559f, 3.7f)
            clip(3.14285f, 3.54f)
            clip(2.88522f, 3.4f)
            canGlide = true
        }
    }

    private fun clip(dist: Float, y: Float) {
        val yaw = MathUtils.toRadians(mc.thePlayer.rotationYaw)
        val x = -sin(yaw) * dist
        val z = cos(yaw) * dist
        mc.thePlayer.setPosition(mc.thePlayer.posX + x, mc.thePlayer.posY + y, mc.thePlayer.posZ + z)
        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false))
    }
}