/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.render

import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.MathHelper
import net.minecraft.util.ResourceLocation
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.Render2DEvent
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.ui.font.Fonts
import net.minusmc.minusbounce.utils.render.RenderUtils
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.IntegerValue
import org.lwjgl.opengl.GL11
import java.util.stream.Collectors

@ModuleInfo(name = "JelloArraylist", description = "dit me tlz", category = ModuleCategory.RENDER)
class JelloArraylist : Module() {
    private var wtf = ResourceLocation("minusbounce/arraylistshadow.png")
    var modules: List<Module> = java.util.ArrayList()
    private val useTrueFont = BoolValue("Use-TrueFont", true)
    private val animateSpeed = IntegerValue("Animate-Speed", 5, 1, 20)
    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        updateElements(event.partialTicks) //fps async
        renderArraylist()
    }

    private fun updateElements(partialTicks: Float) {
        modules = MinusBounce.moduleManager.modules
            .stream()
            .filter { mod: Module ->
                mod.array && !mod.name.equals(
                    "JelloArraylist",
                    ignoreCase = true
                )
            }
            .sorted(ModComparator())
            .collect(
                Collectors.toCollection { ArrayList() }
            )
        val tick = 1f - partialTicks
        for (module in modules) {
            module.animation += (if (module.state) animateSpeed.get() else -animateSpeed.get()) * tick
            module.animation = MathHelper.clamp_float(module.animation, 0f, 20f)
        }
    }

    private fun renderArraylist() {
        val sr = ScaledResolution(mc)
        var yStart = 1f
        for (module in modules) {
            if (module.animation <= 0f) continue
            val xStart = (sr.scaledWidth - Fonts.fontSFUI40.getStringWidth(module.name) - 5).toFloat()
            GlStateManager.pushMatrix()
            GlStateManager.disableAlpha()
            RenderUtils.drawImage3(
                wtf,
                xStart - 8 - 2 - 1,
                yStart + 2 - 2.5f - 1.5f - 1.5f - 1.5f - 6 - 1,
                (Fonts.fontSFUI40.getStringWidth(module.name) * 1 + 20 + 10),
                (18.5 + 6 + 12 + 2).toInt(),
                1f,
                1f,
                1f,
                module.animation / 20f * 0.7f
            )
            GlStateManager.enableAlpha()
            GlStateManager.popMatrix()
            yStart += (7.5f + 5.25f) * (module.animation / 20f)
        }
        yStart = 1f
        for (module in modules) {
            if (module.animation <= 0f) continue
            val xStart = (sr.scaledWidth - Fonts.fontSFUI40.getStringWidth(module.name) - 5).toFloat()
            GlStateManager.pushMatrix()
            GL11.glColor4f(1f, 1f, 1f, module.animation / 20f * 0.7f)
            //GlStateManager.resetColor();
            if (useTrueFont.get()) {
                GlStateManager.disableAlpha()
            }
            Fonts.fontSFUI40.drawString(module.name, xStart, yStart + 7.5f, -1)
            if (useTrueFont.get()) {
                GlStateManager.enableAlpha()
            }
            GlStateManager.popMatrix()
            yStart += (7.5f + 5.25f) * (module.animation / 20f)
        }
        GlStateManager.resetColor()
    }

    internal inner class ModComparator : Comparator<Module> {
        override fun compare(e1: Module, e2: Module): Int {
            return if (Fonts.fontSFUI40.getStringWidth(e1.name) < Fonts.fontSFUI40.getStringWidth(e2.name)) 1 else -1
        }
    }
}
