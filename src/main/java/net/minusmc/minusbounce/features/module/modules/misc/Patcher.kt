package net.minusmc.minusbounce.features.module.modules.misc

import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.value.BoolValue

@ModuleInfo(name = "Patcher", description = "Fix some bug in 1.8.9.", category = ModuleCategory.MISC, canEnable = false)
object Patcher: Module() {
    val noHitDelay = BoolValue("NoHitDelay", false)
    val jumpPatch = BoolValue("JumpFix", true)
    val chatPosition = BoolValue("ChatPosition1.12", true)
    val silentNPESP = BoolValue("SilentNPE-SpawnPlayer", true)
}
