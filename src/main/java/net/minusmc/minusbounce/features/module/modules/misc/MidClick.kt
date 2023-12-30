/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.minusmc.minusbounce.features.module.modules.misc

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.Render2DEvent
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.utils.ClientUtils
import net.minusmc.minusbounce.utils.render.ColorUtils
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.input.Mouse

@ModuleInfo(name = "MidClick", spacedName = "Mid Click", description = "Allows you to add a player as a friend by middle clicking them.", category = ModuleCategory.CLIENT)
class MidClick : Module() {
    private var wasDown = false

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        if (mc.currentScreen != null) return

        if (wasDown && Mouse.isButtonDown(2)) {
            println("Clicked middle mouse button.")
            val entity = mc.objectMouseOver.entityHit
            if (entity != null && entity is EntityPlayer) {
                val playerName = ColorUtils.stripColor(entity.name) ?: return
                val friendsConfig = MinusBounce.fileManager.friendsConfig

                if (friendsConfig.isFriend(playerName)) {
                    friendsConfig.removeFriend(playerName)
                    MinusBounce.fileManager.saveConfig(friendsConfig)
                    ClientUtils.displayChatMessage("§a§l$playerName§c was removed from your friends.")
                } else {
                    friendsConfig.addFriend(playerName)
                    MinusBounce.fileManager.saveConfig(friendsConfig)
                    ClientUtils.displayChatMessage("§a§l$playerName§c was added to your friends.")
                }
            } else
                ClientUtils.displayChatMessage("§c§lError: §aYou need to select a player.")
        }
    }
}