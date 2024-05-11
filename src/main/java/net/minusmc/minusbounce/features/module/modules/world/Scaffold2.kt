package net.minusmc.minusbounce.features.module.modules.world

import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.PreUpdateEvent
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo

@ModuleInfo(name = "Scaffold2", description = "Goofy ah new scaffold.", category = ModuleCategory.WORLD)
class Scaffold2: Module() {

    override fun onEnable() {

    }

    override fun onDisable() {

    }

    @EventTarget
    fun onPreUpdate(event: PreUpdateEvent) {

    }

}