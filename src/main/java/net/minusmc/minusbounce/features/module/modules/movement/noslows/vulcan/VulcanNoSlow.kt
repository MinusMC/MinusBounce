package net.minusmc.minusbounce.features.module.modules.movement.noslows.other

import net.minusmc.minusbounce.features.module.modules.movement.noslows.NoSlowMode
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.utils.PacketUtils
import net.minusmc.minusbounce.utils.timer.MSTimer
import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayServer
import net.minecraft.network.play.client.*
import net.minecraft.util.BlockPos
import java.util.LinkedList

class VulcanNoSlow : NoSlowMode("Vulcan") {

    private val timer = MSTimer()
    private var packetBuf = LinkedList<Packet<INetHandlerPlayServer>>()
    private var waitC03 = false
    private var nextTemp = false
    private var lastBlockingStat = false

    override fun onDisable() {
        waitC03 = false
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
                if (waitC03 && packet is C03PacketPlayer) {
                    waitC03 = false
                    return
                }
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
                waitC03 = true
                msTimer.reset()
            }
        }
    }
}