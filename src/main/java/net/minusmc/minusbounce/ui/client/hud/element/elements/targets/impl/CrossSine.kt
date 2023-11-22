package net.minusmc.minusbounce.ui.client.hud.element.elements.targets.impl

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer
import net.minusmc.minusbounce.features.module.modules.client.HUD
import net.minusmc.minusbounce.ui.client.hud.element.Border
import net.minusmc.minusbounce.ui.client.hud.element.elements.Target
import net.minusmc.minusbounce.ui.client.hud.element.elements.targets.TargetStyle
import net.minusmc.minusbounce.ui.font.Fonts
import net.minusmc.minusbounce.utils.extensions.skin
import net.minusmc.minusbounce.utils.render.RenderUtils
import net.minusmc.minusbounce.value.BoolValue

import java.awt.Color

class CrossSine(inst: Target) : TargetStyle("CrossSine", inst, true) {
    val numberValue = BoolValue("Show Number", false).displayable { targetInstance.styleValue.equals("Normal") }
    val percentValue = BoolValue("Percent", false).displayable { targetInstance.styleValue.equals("Normal") && numberValue.get() }

    override fun drawTarget(entity: EntityPlayer) {
        val fonts = Fonts.fontTenacityBold40
        val leaght = fonts.getStringWidth(entity.name)
        updateAnim(entity.health)
        RenderUtils.drawRoundedRect(0F, 0F, 42F + leaght, 23F, 0F, Color(32, 32, 32, fadeAlpha(255)).rgb)

        RenderUtils.drawAnimatedGradient(
            0.0, 0.0, (42.0 + leaght) * (easingHealth / entity.maxHealth), 1.0,
            HUD.getColorWithAlpha(0, fadeAlpha(255)).rgb,
            HUD.getColorWithAlpha(1, fadeAlpha(255)).rgb
        )

        if (numberValue.get()) {
            GlStateManager.enableBlend()
            fonts.drawStringFade((if (percentValue.get()) decimalFormat3.format((easingHealth / entity.maxHealth) * 100) + "%" else "${decimalFormat3.format(easingHealth)}❤"), (42F + leaght) * (easingHealth / entity.maxHealth), -8F, HUD.getColorWithAlpha(1,fadeAlpha(255)))
            GlStateManager.disableAlpha()
            GlStateManager.disableBlend()
        }
        GlStateManager.enableBlend()
        RenderUtils.drawHead(entity.skin, 2, 3, 18, 18, Color(255,255,255,fadeAlpha(255)).rgb)
        fonts.drawString(entity.name, 28F, 7F, Color(255, 255, 255, fadeAlpha(255)).rgb)
        GlStateManager.disableAlpha()
        GlStateManager.disableBlend()
    }

    override fun getBorder(entity: EntityPlayer?): Border? {
        return Border(0F, 0F, 42F + Fonts.fontTenacityBold40.getStringWidth(entity!!.name), 23F)
    }
}
