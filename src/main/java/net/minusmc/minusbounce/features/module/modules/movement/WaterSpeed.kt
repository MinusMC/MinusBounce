/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.minusmc.minusbounce.features.module.modules.movement

import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.features.module.Module
import net.minecraft.block.BlockLiquid
import net.minusmc.minusbounce.event.UpdateEvent
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.utils.block.BlockUtils.getBlock
import net.minusmc.minusbounce.value.FloatValue

@ModuleInfo(
    name = "WaterSpeed",
    spacedName = "Water Speed",
    description = "Allows you to swim faster.",
    category = ModuleCategory.MOVEMENT
)
class WaterSpeed : Module() {
    private val speedValue = FloatValue("Speed", 1.2f, 1.1f, 1.5f)
    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        if (mc.thePlayer.isInWater() && getBlock(mc.thePlayer.getPosition()) is BlockLiquid) {
            val speed = speedValue.get()
            mc.thePlayer.motionX *= speed
            mc.thePlayer.motionZ *= speed
        }
    }
}