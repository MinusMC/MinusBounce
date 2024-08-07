package net.minusmc.minusbounce.features.module.modules.movement.flys.other

import net.minusmc.minusbounce.event.ReceivedPacketEvent
import net.minusmc.minusbounce.event.PreMotionEvent
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyType
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyMode
import net.minusmc.minusbounce.utils.misc.MathUtils
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.server.S12PacketEntityVelocity

import kotlin.math.*

class IntaveFly: FlyMode("Intave", FlyType.OTHER) {
    private var serverPosX = 0.0
    private var serverPosY = 0.0
    private var serverPosZ = 0.0
    private var teleported = false

    override fun onEnable() {
		super.onEnable()
        serverPosX = mc.thePlayer.posX
        serverPosY = mc.thePlayer.posY
        serverPosZ = mc.thePlayer.posZ
        teleported = false
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f
    }

    override fun onPreMotion(event: PreMotionEvent) {
        mc.thePlayer ?: return

        if (teleported) {
            mc.timer.timerSpeed = 0.3f
            return
        }

        val yaw = MathUtils.toRadians(mc.thePlayer.rotationYaw)

        if (mc.thePlayer.ticksExisted % 3 == 0) {
            mc.netHandler.addToSendQueue(C03PacketPlayer(mc.thePlayer.onGround))
            mc.thePlayer.setPosition(serverPosX, serverPosY, serverPosZ)
        }

        event.y += -1.1 + if (mc.thePlayer.ticksExisted % 3 == 0) 0.42f else 0f
        event.x += sin(yaw) * 6.0
        event.z += -cos(yaw) * 6.0
    }

    override fun onReceivedPacket(event: ReceivedPacketEvent) {
        val packet = event.packet

        if (packet is S08PacketPlayerPosLook && !teleported)
            event.isCancelled = true

        if (packet is S12PacketEntityVelocity && packet.entityID == mc.thePlayer.entityId && packet.motionY > 4000.0)
            teleported = true
    }
}
