package net.minusmc.minusbounce.features.module.modules.player.nofalls.aac

import net.minusmc.minusbounce.features.module.modules.player.nofalls.NoFallMode
import net.minusmc.minusbounce.event.EventState
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.event.MotionEvent
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.AxisAlignedBB

class AAC4xNoFall: NoFallMode("AAC 4.x") {
    private var aac4Fakelag = false
    private var aac4FlagCount = 0
    private var packetModify = false

    private val aac4Packets = mutableListOf<C03PacketPlayer>()

    override fun onEnable() {
        aac4FlagCount = 0
        aac4Fakelag = false
        packetModify = false
        aac4Packets.clear()
    }

    override fun onDisable() {
        aac4FlagCount = 0
        aac4Fakelag = false
        packetModify = false
        aac4Packets.clear()
    }

    override fun onMotion(event: MotionEvent) {
        if (event.eventState == EventState.PRE) {
            if (!inVoid()) {
                if (aac4Fakelag) {
                    aac4Fakelag = false
                    if (aac4Packets.size > 0) {
                        for (packet in aac4Packets) mc.thePlayer.sendQueue.addToSendQueue(packet)
                        aac4Packets.clear()
                    }
                }
                return
            }
            if (mc.thePlayer.onGround && aac4Fakelag) {
                aac4Fakelag = false
                if (aac4Packets.size > 0) {
                    for (packet in aac4Packets) mc.thePlayer.sendQueue.addToSendQueue(packet)
                    aac4Packets.clear()
                }
                return
            }
            if (mc.thePlayer.fallDistance > 2.5 && aac4Fakelag) {
                packetModify = true
                mc.thePlayer.fallDistance = 0f
            }
            if (inAir(4.0, 1.0)) {
                return
            }
            if (!aac4Fakelag) 
                aac4Fakelag = true
        } 
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (aac4Fakelag && packet is C03PacketPlayer) {
            event.cancelEvent()
            if (packetModify) {
                packet.onGround = true
                packetModify = false
            }
            aac4Packets.add(packet)
        }
    }

    private fun inVoid(): Boolean {
        if (mc.thePlayer.posY < 0) return false
        var off = 0
        while (off < mc.thePlayer.posY + 2) {
            val bb = AxisAlignedBB(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.posX, off.toDouble(), mc.thePlayer.posZ)
            if (mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isNotEmpty()) {
                return true
            }
            off += 2
        }
        return false
    }

    private fun inAir(height: Double, plus: Double): Boolean {
        if (mc.thePlayer.posY < 0) return false
        var off = 0
        while (off < height) {
            val bb = AxisAlignedBB(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.posX, mc.thePlayer.posY - off, mc.thePlayer.posZ)
            if (mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isNotEmpty())
                return true

            off += plus.toInt()
        }
        return false
    }
}