/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.special

import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.Listenable
import net.minusmc.minusbounce.event.SentPacketEvent
import net.minusmc.minusbounce.utils.MinecraftInstance
import net.minecraft.network.EnumConnectionState
import net.minecraft.network.handshake.client.C00Handshake
import java.util.*

class BungeeCordSpoof : MinecraftInstance(), Listenable {
    
    @EventTarget
    fun onSentPacket(event: SentPacketEvent) {
        val packet = event.packet
        if (packet is C00Handshake && enabled && packet.requestedState == EnumConnectionState.LOGIN)
            packet.ip = "${packet.ip}\u0000${getRandomIpPart()}.${getRandomIpPart()}.${getRandomIpPart()}.${getRandomIpPart()}\u0000${UUIDSpoofer.getUUID()}"
    }

    private fun getRandomIpPart() = RANDOM.nextInt(256).toString()

    override fun handleEvents() = true

    companion object {
        private val RANDOM = Random()

        @JvmField
        var enabled = false
    }
}
