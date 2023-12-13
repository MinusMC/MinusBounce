/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.minusmc.minusbounce.features.module

import net.minecraft.util.ResourceLocation
import java.awt.Color

enum class ModuleCategory(var displayName: String, val color: Int) {

    COMBAT("Combat", Color(0xe84c3c).rgb),
    PLAYER("Player", Color(0x8c43ac).rgb),
    MOVEMENT("Movement", Color(0x28ba66).rgb),
    RENDER("Render", Color(0x3700ce).rgb),
    CLIENT("Client", Color(160, 55, 63).rgb),
    WORLD("World", Color(0xcadf6f).rgb),
    EXPLOIT("Exploit", Color(51, 152, 217).rgb),
    MISC("Misc", Color(0x105748).rgb),
    SCRIPT("Script", Color(196, 224, 249).rgb);

    var icon: ResourceLocation = ResourceLocation("minusbounce/categories/${displayName.lowercase()}.png")
}