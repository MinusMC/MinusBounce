/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.special

import io.netty.buffer.Unpooled
import net.minecraft.network.PacketBuffer
import net.minecraft.network.play.client.C17PacketCustomPayload
import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.Listenable
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.utils.MinecraftInstance

import kotlin.jvm.JvmField
class AntiForge : MinecraftInstance(), Listenable {
    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (enabled && !mc.isIntegratedServerRunning) {
            try {
                if (blockProxyPacket && packet.javaClass.getName() == "net.minecraftforge.fml.common.network.internal.FMLProxyPacket") event.isCancelled = true
                if (blockPayloadPackets && packet is C17PacketCustomPayload) {
                    if (!packet.channelName.startsWith("MC|")) event.isCancelled = true else if (packet.channelName.equals(
                            "MC|Brand",
                            ignoreCase = true
                        )
                    ) packet.data =
                        PacketBuffer(Unpooled.buffer()).writeString("vanilla")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun handleEvents(): Boolean {
        return true
    }

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