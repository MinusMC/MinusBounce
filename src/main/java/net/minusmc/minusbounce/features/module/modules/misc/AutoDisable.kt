/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.misc

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.ui.client.hud.element.elements.Notification
import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.WorldEvent
import net.minusmc.minusbounce.event.ReceivedPacketEvent
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minecraft.network.play.server.S08PacketPlayerPosLook

@ModuleInfo(name = "AutoDisable", spacedName = "Auto Disable", description = "Automatically disable modules for you on flag or world respawn.", category = ModuleCategory.MISC, array = false)
class AutoDisable : Module() {
    
    @EventTarget
    fun onReceivedPacket(event: ReceivedPacketEvent) {
        if (event.packet is S08PacketPlayerPosLook)
            disableModules(DisableEvent.FLAG)
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        disableModules(DisableEvent.WORLD_CHANGE)
    }

    fun disableModules(enumDisable: DisableEvent) {
        var moduleNames = 0
        MinusBounce.moduleManager.modules.filter { it.autoDisables.contains(enumDisable) && it.state }.forEach { it.toggle(); moduleNames++ }

        if (moduleNames <= 0) return

        val flagMessage = when (enumDisable) {
            DisableEvent.FLAG -> "unexpected teleport"
            DisableEvent.WORLD_CHANGE -> "world change"
            else -> "game ended"
        }

        MinusBounce.hud.addNotification(Notification("AutoDisable", "Disabled $moduleNames ${if (moduleNames > 1) "modules" else "module"} due to $flagMessage.", Notification.Type.INFO, 1000L))
    }

    companion object {
        fun handleGameEnd() {
            val autoDisableModule = MinusBounce.moduleManager[AutoDisable::class.java]!!
            autoDisableModule.disableModules(DisableEvent.GAME_END)
        }
    }

    enum class DisableEvent {
        FLAG,
        WORLD_CHANGE,
        GAME_END
    }
}
