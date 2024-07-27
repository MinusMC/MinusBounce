package net.minusmc.minusbounce.features.module.modules.movement.flys.ncp

import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyMode
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyType
import net.minusmc.minusbounce.utils.misc.MathUtils
import net.minusmc.minusbounce.value.FloatValue
import kotlin.math.cos
import kotlin.math.sin

class NCPPacketFly: FlyMode("NCPPacket", FlyType.NCP) {
    private val timerValue = FloatValue("Timer", 1.1f, 1.0f, 1.3f)
    private val speedValue = FloatValue("Speed", 0.28f, 0.27f, 0.29f)

    override fun onUpdate() {
        val yaw = MathUtils.toRadians(mc.thePlayer.rotationYaw)
        val x = -sin(yaw) * speedValue.get()
        val z = cos(yaw) * speedValue.get()
        
        mc.thePlayer.motionX = 0.0
        mc.thePlayer.motionY = 0.0
        mc.thePlayer.motionZ = 0.0

        mc.timer.timerSpeed = timerValue.get()
        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX + x, mc.thePlayer.motionY , mc.thePlayer.motionZ + z, false))
        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX + x, mc.thePlayer.motionY - 490, mc.thePlayer.motionZ + z, true))
        mc.thePlayer.posX += x
        mc.thePlayer.posZ += z
    }
}