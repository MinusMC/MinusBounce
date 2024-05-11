/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.ui.client.clickgui.dropdown.elements

import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.util.ResourceLocation
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.modules.client.ClickGUI
import net.minusmc.minusbounce.ui.client.clickgui.dropdown.DropDownClickGui
import org.lwjgl.input.Mouse

class ModuleElement(val module: Module) : ButtonElement(null) {
    var isShowSettings = false
    var settingsWidth = 0f
    private var wasPressed = false

    init {
        displayName = module.name
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, button: Float) {
        MinusBounce.clickGui.drawModuleElement(mouseX, mouseY, this)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (mouseButton == 0 && isHovering(mouseX, mouseY) && isVisible) {
            module.toggle()
            return true
        }
        if (mouseButton == 1 && isHovering(mouseX, mouseY) && isVisible) {
            isShowSettings = !isShowSettings
            mc.soundHandler.playSound(PositionedSoundRecord.create(ResourceLocation("gui.button.press"), 1.0f))
            return true
        }
        return false
    }

    fun isntPressed(): Boolean = !wasPressed

    fun updatePressed() {
        wasPressed = Mouse.isButtonDown(0)
    }
}