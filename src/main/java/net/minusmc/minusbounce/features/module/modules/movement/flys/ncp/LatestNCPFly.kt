package net.minusmc.minusbounce.features.module.modules.movement.flys.ncp

import net.minusmc.minusbounce.utils.PacketUtils
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyMode
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyType
import net.minusmc.minusbounce.utils.player.MovementUtils
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.value.BoolValue

import net.minecraft.network.play.client.C03PacketPlayer

/**
 * @author shxp3
 */

class LatestNCPFly: FlyMode("LatestNCP", FlyType.NCP) {
    private val teleportValue = BoolValue("Teleport", false)
    private val timerValue = BoolValue("Timer", true)
    private val speedIncreaseValue = FloatValue("SpeedIncrease", 0f, 0f, 1.5f)
    private var started = false
    private var notUnder = false
    private var clipped = false

    private var offGroundTicks = 0

    override fun onEnable() {
		super.onEnable()
        notUnder = false
        started = false
        clipped = false
    }

    override fun onUpdate() {
        if (mc.thePlayer.onGround)
            offGroundTicks = 0
        else offGroundTicks++

        if (timerValue.get()) {
            if (!mc.thePlayer.onGround)
                mc.timer.timerSpeed = 0.4f
            else
                mc.timer.timerSpeed = 1.0F
        }

        val boundingBox = mc.thePlayer.entityBoundingBox.offset(0.0, 1.0, 0.0)

        if (mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, boundingBox).isEmpty() || started) {
            if (offGroundTicks == 0 && notUnder && clipped) {
                started = true
                MovementUtils.strafe(speedIncreaseValue.get() + 9.5f)
                mc.thePlayer.motionY = 0.42
                notUnder = false
            }

            if (offGroundTicks == 1 && started)
                MovementUtils.strafe(speedIncreaseValue.get() + 8f) 
        } else {
            notUnder = true

            if (clipped)
                return

            clipped = true

            if (teleportValue.get()) {
                PacketUtils.sendPacketNoEvent(C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, false))
                PacketUtils.sendPacketNoEvent(C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY - 0.1, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, false))
                PacketUtils.sendPacketNoEvent(C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, false))
            }
        }

        MovementUtils.strafe()

    }

}