package net.minusmc.minusbounce.features.module.modules.client

import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.value.BoolValue

@ModuleInfo(name = "Target", description = "Select target to KillAura", category = ModuleCategory.CLIENT, canEnable = false)
class Target: Module() {
    val players = BoolValue("Players", true)
    val mobs = BoolValue("Mobs", true)
    val animals = BoolValue("Animals", true)
    val villager = BoolValue("Villager", true)
    val invisible = BoolValue("Invisible", true)
    val dead = BoolValue("Dead", true)
}