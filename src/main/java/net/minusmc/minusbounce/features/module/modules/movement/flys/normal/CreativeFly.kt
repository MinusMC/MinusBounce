package net.minusmc.minusbounce.features.module.modules.movement.flys.normal

import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyType
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyMode
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.utils.timer.MSTimer
import net.minusmc.minusbounce.event.SentPacketEvent
import net.minecraft.util.AxisAlignedBB
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition


class CreativeFly: FlyMode("Creative", FlyType.NORMAL) {
    private val vanillaKickBypassValue = BoolValue("KickBypass", false)
    private val groundSpoofValue = BoolValue("GroundSpoof", false)

    private val groundTimer = MSTimer()

    private fun calculateGround(): Double {
        val boundingBox = mc.thePlayer.entityBoundingBox ?: return 0.0
        var blockHeight = 1.0
        var ground = mc.thePlayer.posY

        while (ground > 0.0) {
            val customBox = AxisAlignedBB(boundingBox.maxX, ground + blockHeight, boundingBox.maxZ, boundingBox.minX, ground, boundingBox.minZ)
            
            if (mc.theWorld.checkBlockCollision(customBox)) {
                if (blockHeight <= 0.05)
                    return ground + blockHeight

                ground += blockHeight
                blockHeight = 0.05
            }

            ground -= blockHeight
        }
        
        return 0.0
    }

    private fun handleVanillaKickBypass() {
        if (!vanillaKickBypassValue.get() || !groundTimer.hasTimePassed(1000))
            return

        val ground = calculateGround()
        var posY = mc.thePlayer.posY

        while (posY > ground) {
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, posY, mc.thePlayer.posZ, true))
            posY -= 8.0
        }

        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, ground, mc.thePlayer.posZ, true))
        posY = ground

        while (posY < mc.thePlayer.posY) {
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, posY, mc.thePlayer.posZ, true))
            posY += 8.0
        }

        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
        groundTimer.reset()
    }


    override fun onUpdate() {
        mc.thePlayer.capabilities.isFlying = true
        handleVanillaKickBypass()
    }

    override fun onSentPacket(event: SentPacketEvent) {
        val packet = event.packet
        if (packet is C03PacketPlayer && groundSpoofValue.get())
            packet.onGround = true
    }

    override fun onRender3D() {}
}