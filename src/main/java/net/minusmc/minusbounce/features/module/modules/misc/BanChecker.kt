/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 *
 * This code belongs to WYSI-Foundation. Please give credits when using this in your repository.
 */
package net.minusmc.minusbounce.features.module.modules.misc

import com.google.gson.JsonParser
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.UpdateEvent
import net.minusmc.minusbounce.event.WorldEvent
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.ui.client.hud.element.elements.Notification
import net.minusmc.minusbounce.utils.ClientUtils
import net.minusmc.minusbounce.utils.misc.HttpUtils.get
import net.minusmc.minusbounce.utils.timer.MSTimer
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.IntegerValue
import kotlin.concurrent.thread

@ModuleInfo(name = "BanChecker", spacedName = "Ban Checker", description = "Checks for ban on Hypixel every minute and alert you if there is any.", category = ModuleCategory.MISC)
class BanChecker : Module() {
    private val notify = BoolValue("Notify", true)
    private val nofifyWhenNoBan = BoolValue("NotifyWhenNoBan", false)
    private val notifyStaff = BoolValue("NotifyStaff", true)
    private val notifyWatchdog = BoolValue("NotifyWatchdog", true)
    private val onlyOnHypixel = BoolValue("NotifyOnlyOnHypixel", true)
    private val alertTime = IntegerValue("AlertTime", 5, 1, 50, "s")

    var staffLastMin = 0
    var watchdogLastMin = 0
    private var lastStaffTotal = -1
    private var working = false
    private var timer = MSTimer()

    private fun reset() {
        working = false
        timer.time = -1
        staffLastMin = 0
        watchdogLastMin = 0
        lastStaffTotal = -1
    }

    private fun getThread(): Thread {
        return thread(start = false, isDaemon = true, name = "BanCheckerThread") {
            working = true

            try {
                val apiContent = get("https://api.plancke.io/hypixel/v1/punishmentStats")
                val jsonObject = JsonParser().parse(apiContent).getAsJsonObject()
                if (jsonObject["success"].asBoolean && jsonObject.has("record")) {
                    val objectAPI = jsonObject["record"].getAsJsonObject()
                    watchdogLastMin = objectAPI["watchdog_lastMinute"].asInt
                    var staffBanTotal = objectAPI["staff_total"].asInt
                    if (staffBanTotal < lastStaffTotal)
                        staffBanTotal = lastStaffTotal
                    if (lastStaffTotal == -1)
                        lastStaffTotal = staffBanTotal
                    else {
                        staffLastMin = staffBanTotal - lastStaffTotal
                        lastStaffTotal = staffBanTotal
                    }

                    tag = ((if (notifyStaff.get()) staffLastMin else 0)
                            + (if (notifyWatchdog.get()) watchdogLastMin else 0)).toString()

                    if (mc.thePlayer != null && notify.get()) {
                        if (notifyStaff.get() && !(onlyOnHypixel.get() && !isOnHypixel))
                            if (staffLastMin > 0)
                                MinusBounce.hud.addNotification(Notification("Staffs banned $staffLastMin players in the last minute!", Notification.Type.WARNING, alertTime.get() * 500L))
                            else if (nofifyWhenNoBan.get())
                                MinusBounce.hud.addNotification(Notification("Staffs didn't ban any player in the last minute.", Notification.Type.SUCCESS, alertTime.get() * 500L))

                        if (notifyWatchdog.get() && !(onlyOnHypixel.get() && !isOnHypixel))
                            if (watchdogLastMin > 0)
                                MinusBounce.hud.addNotification(Notification("Watchdog banned $watchdogLastMin players in the last minute!", Notification.Type.WARNING, alertTime.get() * 500L))
                            else if (nofifyWhenNoBan.get())
                                MinusBounce.hud.addNotification(Notification("Watchdog didn't ban any player in the last minute.", Notification.Type.SUCCESS, alertTime.get() * 500L))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (notify.get() && mc.thePlayer != null)
                    chat("BanChecker error")
            }

            working = false
        }

    }

    override fun onEnable() {
        reset()
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        reset()
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (!timer.hasTimePassed(60_000L) || working)
            return

        timer.reset()

        getThread().start()
    }

    private val isOnHypixel: Boolean
        get() = !mc.isIntegratedServerRunning && mc.currentServerData.serverIP.contains("hypixel.net")
    override var tag = "Idle..."
}