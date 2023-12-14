/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.minusmc.minusbounce.features.module

import net.minecraft.util.ResourceLocation
import java.awt.Color

enum class ModuleCategory(var displayName: String) {
    COMBAT("Combat"),
    PLAYER("Player"),
    MOVEMENT("Movement"),
    RENDER("Render"),
    CLIENT("Client"),
    WORLD("World"),
    EXPLOIT("Exploit"),
    MISC("Misc"),
    SCRIPT("Script");
}