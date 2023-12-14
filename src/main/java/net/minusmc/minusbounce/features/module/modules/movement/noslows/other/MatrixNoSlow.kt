package net.minusmc.minusbounce.features.module.modules.movement.noslows.matrix

import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayServer
import net.minecraft.network.play.client.*
import net.minecraft.util.BlockPos
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.features.module.modules.movement.noslows.NoSlowMode
import net.minusmc.minusbounce.utils.PacketUtils
import java.util.*

class MatrixNoSlow : NoSlowMode("Matrix") {

    private var packetBuf = LinkedList<Packet<INetHandlerPlayServer>>()
    private var nextTemp = false
    private var lastBlockingStat = false

    override fun onDisable() {
        nextTemp = false
        packetBuf.clear()
        msTimer.reset()
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (nextTemp) {
            if ((packet is C07PacketPlayerDigging || packet is C08PacketPlayerBlockPlacement) && noslow.isBlocking)
                event.cancelEvent()
            else if (packet is C03PacketPlayer || packet is C0APacketAnimation || packet is C0BPacketEntityAction || packet is C02PacketUseEntity || packet is C07PacketPlayerDigging || packet is C08PacketPlayerBlockPlacement) {
                packetBuf.add(packet as Packet<INetHandlerPlayServer>)
                event.cancelEvent()
            }
        }
    }

    override fun onUpdate() {
        if (lastBlockingStat || noslow.isBlocking) {
            if (msTimer.hasTimePassed(230) && nextTemp) {
                nextTemp = false
                if (packetBuf.isNotEmpty()) {
                    var canAttack = false
                    for (packet in packetBuf) {
                        if (packet is C03PacketPlayer) canAttack = true
                        if (!((packet is C02PacketUseEntity || packet is C0APacketAnimation) && !canAttack)) PacketUtils.sendPacketNoEvent(packet)
                    }
                    packetBuf.clear()
                }
            }
            if (!nextTemp) {
                lastBlockingStat = noslow.isBlocking
                if (!noslow.isBlocking) return
                PacketUtils.sendPacketNoEvent(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, mc.thePlayer.inventory.getCurrentItem(), 0f, 0f, 0f))
                nextTemp = true
                msTimer.reset()
            }
        }
    }
}