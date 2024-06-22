/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.utils

import net.minecraft.client.gui.GuiMultiplayer
import net.minecraft.client.multiplayer.GuiConnecting
import net.minecraft.client.multiplayer.ServerData
import net.minusmc.minusbounce.ui.client.GuiMainMenu

object ServerUtils : MinecraftInstance() {
    var serverData: ServerData? = null
    
    @JvmStatic
    fun connectToLastServer() {
        mc.displayGuiScreen(GuiConnecting(GuiMultiplayer(GuiMainMenu()), mc, serverData ?: return))
    }

    val remoteIp: String
        get() {
            if (mc.theWorld == null) return "Undefined"
            var serverIp = "Singleplayer"
            if (mc.theWorld.isRemote) {
                val serverData = mc.currentServerData
                if (serverData != null) serverIp = serverData.serverIP
            }
            return serverIp
        }
}
