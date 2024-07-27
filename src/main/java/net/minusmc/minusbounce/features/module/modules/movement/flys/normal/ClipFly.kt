package net.minusmc.minusbounce.features.module.modules.movement.flys.normal

import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyType
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyMode
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.IntegerValue
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.event.SentPacketEvent
import net.minusmc.minusbounce.event.MoveEvent
import net.minusmc.minusbounce.utils.misc.MathUtils
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition

import kotlin.math.sin
import kotlin.math.cos

class ClipFly: FlyMode("Clip", FlyType.NORMAL) {
    private val clipDelay = IntegerValue("DelayTick", 25, 1, 50)
    private val clipH = FloatValue("Horizontal", 7.9f, 0f, 10f)
    private val clipV = FloatValue("Vertical", 1.75f, -10f, 10f)
    private val clipMotionY = FloatValue("MotionY", 0f, -2f, 2f)
    private val clipTimer = FloatValue("Timer", 1f, 0.08f, 10f)
    private val clipGroundSpoof = BoolValue("GroundSpoof", true)
    private val clipCollisionCheck = BoolValue("CollisionCheck", true)
    private val clipNoMove = BoolValue("NoMove", true)

    private fun hClip(x: Double, y: Double, z: Double) {
        val expectedX = mc.thePlayer.posX + x
        val expectedY = mc.thePlayer.posY + y
        val expectedZ = mc.thePlayer.posZ + z

        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(expectedX, expectedY, expectedZ, mc.thePlayer.onGround))
        mc.thePlayer.setPosition(expectedX, expectedY, expectedZ)
    }

    override fun onUpdate() {
        mc.thePlayer ?: return

        mc.thePlayer.motionY = clipMotionY.get().toDouble()
        mc.timer.timerSpeed = clipTimer.get()
        if (mc.thePlayer.ticksExisted % clipDelay.get() == 0) {

            val yaw = MathUtils.toRadians(mc.thePlayer.rotationYaw)
            val x = -sin(yaw) * clipH.get()
            val z = cos(yaw) * clipH.get()

            if (!clipCollisionCheck.get() || mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.entityBoundingBox.offset(x.toDouble(), clipV.get().toDouble(), z.toDouble())).isEmpty())
                hClip(x.toDouble(), clipV.get().toDouble(), z.toDouble())
        }
    }

    override fun onSentPacket(event: SentPacketEvent) {
        val packet = event.packet
        if (packet is C03PacketPlayer && clipGroundSpoof.get())
            packet.onGround = true 
    }

    override fun onMove(event: MoveEvent) {
        if (clipNoMove.get())
            event.zeroXZ()
    }
}