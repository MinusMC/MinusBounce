/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.movement

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.StrafeEvent
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.features.module.modules.world.Scaffold


@ModuleInfo(name = "Sprint", description = "Automatically sprints all the time.", category = ModuleCategory.MOVEMENT)
class Sprint : Module() {

    @EventTarget(priority = -5)
    fun onStrafe(event: StrafeEvent) {
        val scaffold = MinusBounce.moduleManager[Scaffold::class.java] ?: return
        if (mc.thePlayer.sprintState != 2 && !scaffold.state) {
            mc.gameSettings.keyBindSprint.pressed = true
        }
    }

    override fun onDisable() {
        mc.thePlayer.isSprinting = false
        mc.gameSettings.keyBindSprint.pressed = false
    }

}