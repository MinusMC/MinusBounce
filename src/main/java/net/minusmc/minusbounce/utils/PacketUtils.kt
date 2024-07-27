/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.utils

import net.minecraft.network.INetHandler
import net.minecraft.network.NetworkManager
import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayServer
import net.minecraft.network.play.INetHandlerPlayClient
import net.minecraft.network.play.server.*
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.utils.timer.MSTimer

object PacketUtils : MinecraftInstance(), Listenable {
    val packetList = arrayListOf<Packet<*>>()
    var inBound = 0
    var outBound = 0
    var avgInBound = 0
    var avgOutBound = 0

    private val packetTimer = MSTimer()
    private val wdTimer = MSTimer()

    @EventTarget
    fun onSentPacket(event: SentPacketEvent) {
        outBound++
    }

    @EventTarget
    fun onReceivedPacket(event: ReceivedPacketEvent) {
        inBound++
    }

    fun sendPacketNoEvent(packet: Packet<*>) {
        packetList.add(packet)

        val netManager = mc.netHandler?.networkManager ?: return
        if (netManager.isChannelOpen) {
            netManager.flushOutboundQueue()
            netManager.dispatchPacket(packet, null)
        } else {
            netManager.readWriteLock.writeLock().lock()
            try {
                netManager.outboundPacketsQueue += NetworkManager.InboundHandlerTuplePacketListener(packet, null)
            } finally {
                netManager.readWriteLock.writeLock().unlock()
            }
        }
    }

    fun receivePacketNoEvent(packet: Packet<INetHandlerPlayServer>) {
        val netManager = mc.netHandler?.networkManager ?: return

        if (netManager.channel.isOpen) {
            try {
                packet.processPacket(netManager.packetListener as INetHandlerPlayServer)
            } catch (_: Exception) {}
        }
    }

    fun processPacket(packet: Packet<*>) {
        val netManager = mc.netHandler?.networkManager ?: return

        try {
            (packet as Packet<INetHandler>).processPacket(netManager.netHandler)
        } catch (_: Exception) {}
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        packetList.clear()
    }

    @EventTarget
    fun onTick(event: TickEvent?) {
        if (packetTimer.hasTimePassed(1000L)) {
            avgInBound = inBound
            avgOutBound = outBound
            outBound = 0
            inBound = 0
            packetTimer.reset()
        }
    }

    private fun isInventoryAction(action: Short): Boolean = action in 1..99

    override fun handleEvents() = true
}
