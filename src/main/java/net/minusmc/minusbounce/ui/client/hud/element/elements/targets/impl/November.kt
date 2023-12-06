package net.minusmc.minusbounce.ui.client.hud.element.elements.targets.impl

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer
import net.minusmc.minusbounce.ui.client.hud.element.Border
import net.minusmc.minusbounce.ui.client.hud.element.elements.Target
import net.minusmc.minusbounce.ui.client.hud.element.elements.targets.TargetStyle
import net.minusmc.minusbounce.ui.font.Fonts
import net.minusmc.minusbounce.utils.render.RenderUtils
import net.minusmc.minusbounce.utils.render.Stencil
import net.minusmc.minusbounce.value.BoolValue
import org.lwjgl.opengl.GL11
import java.awt.Color

class November(inst: Target) : TargetStyle("November", inst, true) {
    private val rm = BoolValue("Modern", true).displayable { targetInstance.styleValue.get().equals("ravenb4", true) }

    private val novemberRoundValue = BoolValue("RoundedBar", true)

    private var calcScaleX = 0F
    private var calcScaleY = 0F
    private var calcTranslateX = 0F
    private var calcTranslateY = 0F

    fun updateData(calcTranslateX: Float, calcTranslateY: Float, calcScaleX: Float, calcScaleY: Float) {
        this.calcTranslateX = calcTranslateX
        this.calcTranslateY = calcTranslateY
        this.calcScaleX = calcScaleX
        this.calcScaleY = calcScaleY
    }
    override fun drawTarget(entity: EntityPlayer) {
        updateAnim(entity.health)
        val name = entity.name
        val health = (entity.health / entity.maxHealth) * 100
        val tWidth = 200F
        val tHeight = 40F
        val playerInfo = mc.netHandler.getPlayerInfo(entity.uniqueID)
        val border = getBorder(entity)
        border!!.draw()


        RenderUtils.drawRect(0F, 0F, tWidth, tHeight, Color(25, 25, 25, 200).rgb)


        Fonts.font40.drawString(name, 42F, 10F, Color.WHITE.rgb)

        // Health bar
        val barWidth = 105F
        val barHeight = 10F
        val barX = 40F
        val barY = 28F
        val healthWidth = (easingHealth / entity.maxHealth) * barWidth // Calculate the actual health bar width
        val healthColor = Color(245, 0, 70).rgb // Health bar color (F50046)
        RenderUtils.drawRoundedRect(barX, barY, barX + barWidth, barY + barHeight, 6F, Color(40, 40, 40, 150).rgb)
        if (novemberRoundValue.get())
            RenderUtils.customRounded(barX, barY, barX + healthWidth, barY + barHeight, 6F, 6F, 6F, 6F, healthColor)
        else
            RenderUtils.drawRect(barX, barY, barX + healthWidth, barY + barHeight, healthColor)

        // Health percentage
        Fonts.font35.drawString("${decimalFormat4.format(health)}%", barX + healthWidth + 6F, barY + 2F, Color.WHITE.rgb)

        // Head
        if (playerInfo != null) {
            val headSize = 24F
            val headX = 4F
            val headY = (tHeight - headSize) / 2F
            Stencil.write(false)
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            RenderUtils.fastRoundedRect(headX, headY, headX + headSize, headY + headSize, 6F)
            GL11.glDisable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            Stencil.erase(true)
            drawHead(playerInfo.locationSkin, headX.toInt(), headY.toInt(), headSize.toInt(), headSize.toInt(), 1F - targetInstance.getFadeProgress())
            Stencil.dispose()
        }
    }


    override fun handleBlur(entity: EntityPlayer) {
        val tWidth = 200F
        val tHeight = 40F
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        RenderUtils.drawRect(0F, 0F, tWidth, tHeight, Color(25, 25, 25, 200).rgb)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    override fun handleShadowCut(entity: EntityPlayer) = handleBlur(entity)

    override fun handleShadow(entity: EntityPlayer) {
        val tWidth = 200F
        val tHeight = 40F
        RenderUtils.drawRect(0F, 0F, tWidth, tHeight, Color(0, 0, 0, 50).rgb)
    }

    override fun getBorder(entity: EntityPlayer?): Border? {
        return Border(0F, 0F, if (rm.get())40F + mc.fontRendererObj.getStringWidth(entity!!.displayName.formattedText) else 60F + mc.fontRendererObj.getStringWidth(entity!!.displayName.formattedText), if (rm.get()) 35F else 28F)
    }
}