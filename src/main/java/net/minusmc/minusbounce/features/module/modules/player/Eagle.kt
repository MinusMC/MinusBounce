/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.player

import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.UpdateEvent
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minecraft.client.settings.GameSettings
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos

@ModuleInfo(name = "Eagle", description = "Makes you eagle (aka. FastBridge).", category = ModuleCategory.PLAYER)
class Eagle : Module() {

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val blockPos = BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)
        mc.gameSettings.keyBindSneak.pressed = mc.theWorld.getBlockState(blockPos).block === Blocks.air
    }

    override fun onDisable() {
        mc.thePlayer ?: return

        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindSneak))
            mc.gameSettings.keyBindSneak.pressed = false
    }
}
