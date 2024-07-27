/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.world

import net.minecraft.network.play.server.S03PacketTimeUpdate
import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.ReceivedPacketEvent
import net.minusmc.minusbounce.event.UpdateEvent
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.value.IntegerValue
import net.minusmc.minusbounce.value.ListValue

@ModuleInfo(name = "Ambience", description = "Change your world time and weather client-side.", category = ModuleCategory.WORLD)
class Ambience : Module() {
    private val timeModeValue = ListValue("Time", arrayOf("Static", "Cycle"), "Static")
    private val cycleSpeedValue = IntegerValue("CycleSpeed", 1, -24, 24) { timeModeValue.get().equals("cycle", true) }
    private val staticTimeValue = IntegerValue("StaticTime", 18000, 0, 24000) {
        timeModeValue.get().equals("static", true)
    }
    private val weatherModeValue = ListValue("Weather", arrayOf("Clear", "Rain", "NoModification"), "Clear")
    private val rainStrengthValue = FloatValue("RainStrength", 0.1F, 0.01F, 1F) {
        weatherModeValue.get().equals("rain", true)
    }
    private val tagValue = ListValue("Tag", arrayOf("TimeOnly", "Simplified", "Detailed", "None"), "TimeOnly")

    private var timeCycle = 0L

    override fun onEnable() {
        timeCycle = 0L
    }

    @EventTarget
    fun onReceivedPacket(event: ReceivedPacketEvent) {
        if (event.packet is S03PacketTimeUpdate)
            event.isCancelled = true
    }
    
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (timeModeValue.get().equals("static", true))
            mc.theWorld.worldTime = staticTimeValue.get().toLong()
        else {
            mc.theWorld.worldTime = timeCycle
            timeCycle += (cycleSpeedValue.get() * 10).toLong()

            if (timeCycle > 24000L) timeCycle = 0L
            if (timeCycle < 0L) timeCycle = 24000L
        }

        if (!weatherModeValue.get().equals("nomodification", true))
            mc.theWorld.setRainStrength(if (weatherModeValue.get().equals("clear", true)) 0F else rainStrengthValue.get())
    }

    override val tag: String?
        get() = when (tagValue.get().lowercase()) {
            "timeonly" -> if (timeModeValue.get().equals("static", true)) staticTimeValue.get().toString() else timeCycle.toString()
            "simplified" -> "${if (timeModeValue.get().equals("static", true)) staticTimeValue.get().toString() else timeCycle.toString()}, ${weatherModeValue.get()}"
            "detailed" -> "Time: ${
                if (timeModeValue.get().equals("static", true)) staticTimeValue.get()
                    .toString() else "Cycle, $timeCycle"
            }, Weather: ${weatherModeValue.get()}"
            else -> null
        }
}