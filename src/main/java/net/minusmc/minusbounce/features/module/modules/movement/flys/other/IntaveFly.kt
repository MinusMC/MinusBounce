package net.minusmc.minusbounce.features.module.modules.movement.flys.other

import net.minusmc.minusbounce.event.EventState
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.event.MotionEvent
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyType
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyMode
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.util.MathHelper

class IntaveFly: FlyMode("Intave", FlyType.OTHER) {
    private var serverPosX = 0.0
    private var serverPosY = 0.0
    private var serverPosZ = 0.0
    private var teleported = false

    override fun onEnable() {
        serverPosX = mc.thePlayer.posX
        serverPosY = mc.thePlayer.posY
        serverPosZ = mc.thePlayer.posZ
        teleported = false
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f
    }

    override fun onMotion(event: MotionEvent) {
        if (event.eventState == EventState.POST) return
        val thePlayer = mc.thePlayer ?: return
        if (!teleported) {
            val yaw = thePlayer.rotationYaw * 3.1415927f / 180f
            val speed = 6.0
            if (mc.thePlayer.ticksExisted % 3 == 0) {
                mc.netHandler.addToSendQueue(C03PacketPlayer(mc.thePlayer.onGround))
                mc.thePlayer.setPosition(serverPosX, serverPosY, serverPosZ)
            }
            event.y += -1.1 + if (mc.thePlayer.ticksExisted % 3 == 0) 0.42f else 0f
            event.x += MathHelper.sin(yaw) * speed
            event.z += - MathHelper.cos(yaw) * speed
        } else {
            mc.timer.timerSpeed = 0.3f
        }
    }

    override fun onPacket(event: PacketEvent) {
        val packet= event.packet

        if (packet is S08PacketPlayerPosLook && !teleported) {
            event.cancelEvent()
        } else if (packet is S12PacketEntityVelocity) {
            if (packet.entityID == mc.thePlayer.entityId && packet.motionY / 8000.0 > 0.5) {
                teleported = true
            }
        }
    }
}
