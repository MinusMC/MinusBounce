/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.special

import io.netty.buffer.Unpooled
import net.minecraft.network.PacketBuffer
import net.minecraft.network.play.client.C17PacketCustomPayload
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.utils.MinecraftInstance

import kotlin.jvm.JvmField

class AntiForge : MinecraftInstance(), Listenable {

    @EventTarget
    fun onSentPacket(event: SentPacketEvent) {
        val packet = event.packet

        if (!enabled || mc.isIntegratedServerRunning)
            return

        if (blockPayloadPackets && packet is C17PacketCustomPayload) {
            if (!packet.channelName.startsWith("MC|"))
                event.isCancelled = true 
            else if (packet.channelName.equals("MC|Brand", true)) 
                packet.data = PacketBuffer(Unpooled.buffer()).writeString("vanilla")
        }
    }

    @EventTarget
    fun onReceivedPacket(event: ReceivedPacketEvent) {
        val packet = event.packet

        if (!enabled || mc.isIntegratedServerRunning)
            return

        if (blockProxyPacket && packet.javaClass.name == "net.minecraftforge.fml.common.network.internal.FMLProxyPacket")
            event.isCancelled = true
    }

    override fun handleEvents() = true

    companion object {
        @JvmField
        var enabled = true
        @JvmField
        var blockFML = true
        @JvmField
        var blockProxyPacket = true
        @JvmField
        var blockPayloadPackets = true
    }
}