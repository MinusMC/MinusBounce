/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.ui.client.clickgui

import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.util.ResourceLocation
import net.minecraft.util.StringUtils
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.features.module.modules.client.ClickGUI
import net.minusmc.minusbounce.ui.client.clickgui.styles.dropdown.elements.Element
import net.minusmc.minusbounce.utils.MinecraftInstance
import java.util.*

abstract class Panel(val name: String, var x: Int, var y: Int, val width: Int, val height: Int, var open: Boolean): MinecraftInstance() {
    var x2 = 0
    var y2 = 0
    private var scroll = 0
    var dragged = 0
        private set
    var drag = false
    var scrollbar = false
        private set
    val elements: MutableList<Element>
    var isVisible = true
    private var elementsHeight = 0f
    private var fade = 0f

    init {
        elements = ArrayList()
        setupItems()
    }

    abstract fun setupItems()
    fun drawScreen(mouseX: Int, mouseY: Int, button: Float) {
        val clickGui = MinusBounce.moduleManager[ClickGUI::class.java]!!.style
        if (clickGui !is DropDownClickGui) return
        if (!isVisible) return
        val maxElements = Objects.requireNonNull(
            MinusBounce.moduleManager.getModule(
                ClickGUI::class.java
            )
        )!!.maxElementsValue.get()

        // Drag
        if (drag) {
            val nx = x2 + mouseX
            val ny = y2 + mouseY
            if (nx > -1) x = nx
            if (ny > -1) y = ny
        }
        elementsHeight = (getElementsHeight() - 1).toFloat()
        val scrollbar = elements.size >= maxElements
        if (this.scrollbar != scrollbar) this.scrollbar = scrollbar
        clickGui.drawPanel(mouseX, mouseY, this)
        var y = y + height - 2
        var count = 0
        for (element in elements) {
            if (++count > scroll && count < scroll + (maxElements + 1) && scroll < elements.size) {
                element.setLocation(x, y)
                element.width = width
                if (y <= this.y + fade) element.drawScreen(mouseX, mouseY, button)
                y += element.height + 1
                element.isVisible = true
            } else element.isVisible = false
        }
    }

    fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (!isVisible) return false
        if (mouseButton == 1 && isHovering(mouseX, mouseY)) {
            open = !open
            mc.soundHandler.playSound(PositionedSoundRecord.create(ResourceLocation("random.bow"), 1.0f))
            return true
        }
        for (element in elements) {
            if (element.y <= y + fade && element.mouseClicked(mouseX, mouseY, mouseButton)) {
                return true
            }
        }
        return false
    }

    fun mouseReleased(mouseX: Int, mouseY: Int, state: Int): Boolean {
        if (!isVisible) return false
        drag = false
        if (!open) return false
        for (element in elements) {
            if (element.y <= y + fade && element.mouseReleased(mouseX, mouseY, state)) {
                return true
            }
        }
        return false
    }

    fun handleScroll(mouseX: Int, mouseY: Int, wheel: Int): Boolean {
        val maxElements = Objects.requireNonNull(
            MinusBounce.moduleManager.getModule(
                ClickGUI::class.java
            )
        )!!.maxElementsValue.get()
        if (mouseX >= x && mouseX <= x + 100 && mouseY >= y && mouseY <= y + 19 + elementsHeight) {
            if (wheel < 0 && scroll < elements.size - maxElements) {
                ++scroll
                if (scroll < 0) scroll = 0
            } else if (wheel > 0) {
                --scroll
                if (scroll < 0) scroll = 0
            }
            if (wheel < 0) {
                if (dragged < elements.size - maxElements) ++dragged
            } else if (wheel > 0 && dragged >= 1) {
                --dragged
            }
            return true
        }
        return false
    }

    fun updateFade(delta: Int) {
        if (open) {
            if (fade < elementsHeight) fade += 0.4f * delta
            if (fade > elementsHeight) fade = elementsHeight.toInt().toFloat()
        } else {
            if (fade > 0) fade -= 0.4f * delta
            if (fade < 0) fade = 0f
        }
    }

    fun getFade(): Int {
        return fade.toInt()
    }

    private fun getElementsHeight(): Int {
        var height = 0
        var count = 0
        for (element in elements) {
            if (count >= Objects.requireNonNull(
                    MinusBounce.moduleManager.getModule(
                        ClickGUI::class.java
                    )
                )!!.maxElementsValue.get()
            ) continue
            height += element.height + 1
            ++count
        }
        return height
    }

    fun isHovering(mouseX: Int, mouseY: Int): Boolean {
        val textWidth = mc.fontRendererObj.getStringWidth(
            StringUtils.stripControlCodes(
                name
            )
        ) - 100f
        return mouseX >= x - textWidth / 2f - 19f && mouseX <= x - textWidth / 2f + mc.fontRendererObj.getStringWidth(
            StringUtils.stripControlCodes(
                name
            )
        ) + 19f && mouseY >= y && mouseY <= y + height - if (open) 2 else 0
    }
}