/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.misc

import net.minecraft.network.play.client.C19PacketResourcePackStatus
import net.minecraft.network.play.server.S48PacketResourcePackSend
import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.ReceivedPacketEvent
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.utils.ClientUtils
import java.io.File
import java.net.URI
import java.net.URISyntaxException

@ModuleInfo(name = "PackSpoofer", spacedName = "Pack Spoofer", description = "Prevents servers from forcing you to download their resource pack.", category = ModuleCategory.MISC)
class PackSpoofer : Module() {

    @EventTarget
    fun onReceivedPacket(event: ReceivedPacketEvent) {
        val packet = event.packet

        if (packet is S48PacketResourcePackSend) {
            val url = packet.url
            val hash = packet.hash

            try {
                val scheme = URI(url).scheme
                val isLevelProtocol = "level" == scheme

                if ("http" != scheme && "https" != scheme && !isLevelProtocol)
                    throw URISyntaxException(url, "Wrong protocol")

                if (isLevelProtocol && (url.contains("..") || !url.endsWith(".zip"))) {
                    val s2 = url.substring("level://".length)
                    val file1 = File(mc.mcDataDir, "saves")
                    val file2 = File(file1, s2)

                    if (!file2.isFile() || url.contains("minusbounce", true)) {
                        mc.netHandler.addToSendQueue(C19PacketResourcePackStatus(hash, C19PacketResourcePackStatus.Action.FAILED_DOWNLOAD))
                        event.isCancelled = true
                        return
                    }
                }

                mc.netHandler.addToSendQueue(C19PacketResourcePackStatus(packet.hash,
                        C19PacketResourcePackStatus.Action.ACCEPTED))
                mc.netHandler.addToSendQueue(C19PacketResourcePackStatus(packet.hash,
                        C19PacketResourcePackStatus.Action.SUCCESSFULLY_LOADED))
            } catch (e: URISyntaxException) {
                ClientUtils.logger.error("Failed to handle resource pack", e)
                mc.netHandler.addToSendQueue(C19PacketResourcePackStatus(hash, C19PacketResourcePackStatus.Action.FAILED_DOWNLOAD))
            }

            event.isCancelled = true
        }
    }

}