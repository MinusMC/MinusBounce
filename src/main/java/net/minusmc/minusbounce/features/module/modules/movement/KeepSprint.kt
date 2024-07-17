/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.movement

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.KnockbackEvent
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo

@ModuleInfo(name = "KeepSprint", spacedName = "Keep Sprint", description = "Keep you sprint. Hypixel auto ban.", category = ModuleCategory.MOVEMENT)
class KeepSprint: Module() {
    @EventTarget(priority = 5)
    fun onKnockback(event: KnockbackEvent) {
        event.isCancelled = true
    }
}