/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.ui.client.hud.element.elements

import net.minecraft.client.gui.GuiChat
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.features.module.modules.combat.KillAura
import net.minusmc.minusbounce.ui.client.hud.designer.GuiHudDesigner
import net.minusmc.minusbounce.ui.client.hud.element.Border
import net.minusmc.minusbounce.ui.client.hud.element.Element
import net.minusmc.minusbounce.ui.client.hud.element.ElementInfo
import net.minusmc.minusbounce.ui.font.Fonts
import net.minusmc.minusbounce.utils.misc.MathUtils
import net.minusmc.minusbounce.utils.extensions.getDistanceToEntityBox
import net.minusmc.minusbounce.utils.render.*
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.value.IntegerValue
import net.minusmc.minusbounce.value.ListValue
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.pow

/**
 * A target hud
 */

@ElementInfo(name = "Target", disableScale = true, retrieveDamage = true)
class Target : Element() {
    val backgroundAlpha = IntegerValue("Background-Alpha", 255, 0, 255)
    val radiusValue = FloatValue("Radius-Strength", 0.0f, 0.0f, 10.0f)


    override fun drawElement(): Border? {
        val kaTarget = MinusBounce.moduleManager[KillAura::class.java]!!.target

        val target = if (kaTarget != null && kaTarget is EntityPlayer && mc.thePlayer.getDistanceToEntityBox(kaTarget) <= 8.0) kaTarget
            else if (mc.currentScreen is GuiChat || mc.currentScreen is GuiHudDesigner) mc.thePlayer
            else return null

        val playerInfo = mc.netHandler.getPlayerInfo(target.uniqueID) ?: return null

        val targetHealth = MathUtils.round(target.health)
        val selfHealth = MathUtils.round(mc.thePlayer.health)
        val healthString = "$targetHealth - $selfHealth"

        val displayedName = if (target.name.length > 13) {
            target.name.substring(0, 13) + "..."
        } else target.name

        RenderUtils.drawRoundedRect(0f, 0f, 130f, 45f, radiusValue.get(), Color(0, 0, 0, backgroundAlpha.get()).rgb)
        RenderUtils.drawHead(playerInfo.locationSkin, 8, 8, 30, 30)

        Fonts.fontLexend35.drawStringWithShadow(displayedName, 45f, 8f, Color.WHITE.rgb)
        Fonts.fontLexend35.drawStringWithShadow(healthString, 45f, 17f, Color.WHITE.rgb)

        val health = target.health.coerceIn(0f, 20f)

        val barColor = when {
            health >= 18f -> Color(119, 130, 190).rgb
            health > 7 && health < 18f -> Color(252, 185, 65).rgb
            else -> Color(225, 38, 53).rgb
        }
        RenderUtils.drawRect(45F, 30F, 45f + 80f * health / 20f, 38f, barColor)
        return Border(0f, 0f, 130f, 45f)
    }
}
