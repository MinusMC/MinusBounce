/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.utils

import net.minecraft.client.gui.GuiDownloadTerrain
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.multiplayer.GuiConnecting
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.utils.timer.MSTimer
import net.minusmc.minusbounce.utils.misc.StringUtils

object SessionUtils : MinecraftInstance(), Listenable {
    private val sessionTimer = MSTimer()
    private val worldTimer = MSTimer()
    var lastSessionTime = 0L
    var backupSessionTime = 0L
    var lastWorldTime = 0L
    private var requireDelay = false
    private var lastScreen: GuiScreen? = null

    fun handleConnection() {
        backupSessionTime = 0L
        requireDelay = true
        lastSessionTime = System.currentTimeMillis() - sessionTimer.time
        if (lastSessionTime < 0L) lastSessionTime = 0L
        sessionTimer.reset()
    }

    fun handleReconnection() {
        if (requireDelay) sessionTimer.time = System.currentTimeMillis() - backupSessionTime
    }

    val formatSessionTime: String
        get() {
            val deltaTime = System.currentTimeMillis() - sessionTimer.time
            if (deltaTime < 0L) sessionTimer.reset()
            return StringUtils.getFormatTime(System.currentTimeMillis() - sessionTimer.time)
        }

    val formatLastSessionTime: String
        get() {
            if (lastSessionTime < 0L) lastSessionTime = 0L
            return StringUtils.getFormatTime(lastSessionTime)
        }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        lastWorldTime = System.currentTimeMillis() - worldTimer.time
        worldTimer.reset()
        if (event.worldClient == null) {
            backupSessionTime = System.currentTimeMillis() - sessionTimer.time
            requireDelay = true
        } else
            requireDelay = false
    }

    @EventTarget
    fun onSession(event: SessionEvent?) {
        handleConnection()
    }

    @EventTarget
    fun onScreen(event: ScreenEvent) {
        if (event.guiScreen == null && lastScreen != null && (lastScreen is GuiDownloadTerrain || lastScreen is GuiConnecting)) handleReconnection()
        lastScreen = event.guiScreen
    }

    override fun handleEvents() = true
}
