package net.minusmc.minusbounce.utils.player

import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.utils.MinecraftInstance
import net.minusmc.minusbounce.utils.PacketUtils
import net.minusmc.minusbounce.utils.Rotation
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.network.Packet
import net.minecraft.network.handshake.client.C00Handshake
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S40PacketDisconnect
import net.minecraft.network.status.client.C00PacketServerQuery
import net.minecraft.network.status.client.C01PacketPing
import net.minecraft.util.Vec3

/**
 * @author CCBluex
 */

object BlinkUtils: MinecraftInstance() {
    val sentPackets = mutableListOf<Packet<*>>()
    val receivedPackets = mutableListOf<Packet<*>>()
    val positions = mutableListOf<Vec3>()

    private var fakePlayer: EntityOtherPlayerMP? = null

    val packetsSize: Int
        get() = sentPackets.size + receivedPackets.size

    val isBlinking: Boolean
        get() = packetsSize > 0


    fun blink(event: SentPacketEvent, sent: Boolean = true, receive: Boolean = true) {
        mc.thePlayer ?: return

        if (mc.thePlayer.isDead || event.isCancelled)
            return

        val packet = event.packet

        if (packet is C00Handshake || packet is C00PacketServerQuery || packet is C01PacketPing)
            return

        if (sent && !receive) {
            event.isCancelled = true

            synchronized(sentPackets) {
                sentPackets += packet
            }

            if (packet is C03PacketPlayer && packet.isMoving)
                synchronized(positions) {
                    positions += Vec3(packet.x, packet.y, packet.z)
                }
        }

        if (!sent && receive)
            synchronized(sentPackets) {
                while (sentPackets.size > 0) {
                    val packet = sentPackets.removeFirst()
                    PacketUtils.sendPacketNoEvent(packet)
                }
            }

        if (sent && receive) {
            event.isCancelled = true

            synchronized(sentPackets) {
                sentPackets += packet
            }

            if (packet is C03PacketPlayer && packet.isMoving) {
                synchronized(positions) {
                    positions += Vec3(packet.x, packet.y, packet.z)
                }

                if (packet.rotating)
                    RotationUtils.serverRotation = Rotation(packet.yaw, packet.pitch)
            }
        }

        if (!sent && !receive)
            unblink()
    }

    fun blink(event: ReceivedPacketEvent, sent: Boolean = true, receive: Boolean = true) {
        mc.thePlayer ?: return

        if (mc.thePlayer.isDead || event.isCancelled)
            return

        val packet = event.packet

        if (packet is S02PacketChat || packet is S40PacketDisconnect)
            return


        if (sent && receive)
            synchronized(receivedPackets) {
                while (receivedPackets.size > 0) {
                    val packet = receivedPackets.removeFirst()
                    PacketUtils.processPacket(packet)
                }
            }

        if (!sent && receive && mc.thePlayer.ticksExisted > 10) {
            event.isCancelled = true
            synchronized(receivedPackets) {
                receivedPackets += packet
            }
        }

        if (sent && receive && mc.thePlayer.ticksExisted > 10) {
            event.isCancelled = true

            synchronized(receivedPackets) {
                receivedPackets += packet
            }
        }

        if (!sent && !receive)
            unblink()
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        event.worldClient ?: run {
            sentPackets.clear()
            receivedPackets.clear()
            positions.clear()
        }
    }


    fun unblink() {
        mc.theWorld ?: return

        synchronized(receivedPackets) {
            while (receivedPackets.size > 0) {
                val packet = receivedPackets.removeFirst()
                PacketUtils.processPacket(packet)
            }
        }

        synchronized(sentPackets) {
            while (receivedPackets.size > 0) {
                val packet = receivedPackets.removeFirst()
                PacketUtils.sendPacketNoEvent(packet)
            }
        }

        sentPackets.clear()
        receivedPackets.clear()
        positions.clear()

        // Remove fake player
        fakePlayer?.let {
            mc.theWorld.removeEntityFromWorld(it.entityId)
            fakePlayer = null
        }
    }

    fun addFakePlayer() {
        mc.thePlayer ?: return

        val faker = EntityOtherPlayerMP(mc.theWorld, mc.thePlayer.gameProfile)

        faker.rotationYawHead = mc.thePlayer.rotationYawHead
        faker.renderYawOffset = mc.thePlayer.renderYawOffset
        faker.copyLocationAndAnglesFrom(mc.thePlayer)
        faker.rotationYawHead = mc.thePlayer.rotationYawHead
        faker.inventory = mc.thePlayer.inventory
        mc.theWorld.addEntityToWorld(-1337, faker)

        fakePlayer = faker
    }
}