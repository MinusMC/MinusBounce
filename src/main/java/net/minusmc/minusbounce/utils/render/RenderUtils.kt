/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.utils.render

import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.Gui.drawModalRectWithCustomSizedTexture
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemBow
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemSword
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.ResourceLocation
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.features.module.modules.render.TargetMark
import net.minusmc.minusbounce.ui.font.Fonts
import net.minusmc.minusbounce.utils.MinecraftInstance
import net.minusmc.minusbounce.utils.block.BlockUtils
import net.minusmc.minusbounce.utils.extensions.*
import net.minusmc.minusbounce.utils.misc.MathUtils
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import kotlin.math.*


object RenderUtils : MinecraftInstance() {
    private val DISPLAY_LISTS_2D = (0..3).map {glGenLists(1)}.toTypedArray()
    private const val ANIMATION_DURATION = 500
    private var startTime = System.currentTimeMillis()
    var deltaTime = 0

    private val frustrum = Frustum()

    init {
        glNewList(DISPLAY_LISTS_2D[0], GL_COMPILE)
        quickDrawRect(-7f, 2f, -4f, 3f)
        quickDrawRect(4f, 2f, 7f, 3f)
        quickDrawRect(-7f, 0.5f, -6f, 3f)
        quickDrawRect(6f, 0.5f, 7f, 3f)
        glEndList()
        glNewList(DISPLAY_LISTS_2D[1], GL_COMPILE)
        quickDrawRect(-7f, 3f, -4f, 3.3f)
        quickDrawRect(4f, 3f, 7f, 3.3f)
        quickDrawRect(-7.3f, 0.5f, -7f, 3.3f)
        quickDrawRect(7f, 0.5f, 7.3f, 3.3f)
        glEndList()
        glNewList(DISPLAY_LISTS_2D[2], GL_COMPILE)
        quickDrawRect(4f, -20f, 7f, -19f)
        quickDrawRect(-7f, -20f, -4f, -19f)
        quickDrawRect(6f, -20f, 7f, -17.5f)
        quickDrawRect(-7f, -20f, -6f, -17.5f)
        glEndList()
        glNewList(DISPLAY_LISTS_2D[3], GL_COMPILE)
        quickDrawRect(7f, -20f, 7.3f, -17.5f)
        quickDrawRect(-7.3f, -20f, -7f, -17.5f)
        quickDrawRect(4f, -20.3f, 7.3f, -20f)
        quickDrawRect(-7.3f, -20.3f, -4f, -20f)
        glEndList()

    }

    fun isInViewFrustrum(entity: Entity) = isInViewFrustrum(entity.entityBoundingBox) || entity.ignoreFrustumCheck

    fun isInViewFrustrum(bb: AxisAlignedBB): Boolean {
        val current = mc.renderViewEntity
        frustrum.setPosition(current.posX, current.posY, current.posZ)
        return frustrum.isBoundingBoxInFrustum(bb)
    }

    /**
     * RECTAGLE
     */

    fun drawRoundedCornerRect(x: Float, y: Float, x1: Float, y1: Float, radius: Float, color: Int) {
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glDisable(GL_TEXTURE_2D)
        glDisable(GL_CULL_FACE)
        GLUtils.glColor(color)
        drawRoundedCornerRect(x, y, x1, y1, radius)
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        GLUtils.setGlState(GL_CULL_FACE, glIsEnabled(GL_CULL_FACE))
    }

    fun drawRoundedCornerRect(x: Float, y: Float, x1: Float, y1: Float, radius: Float) {
        glBegin(GL_POLYGON)
        val xRadius = ((x1 - x) * 0.5f).coerceAtMost(radius)
        val yRadius = ((y1 - y) * 0.5f).coerceAtMost(radius)
        quickPolygonCircle(x + xRadius, y + yRadius, xRadius, yRadius, 180, 270)
        quickPolygonCircle(x1 - xRadius, y + yRadius, xRadius, yRadius, 90, 180)
        quickPolygonCircle(x1 - xRadius, y1 - yRadius, xRadius, yRadius, 0, 90)
        quickPolygonCircle(x + xRadius, y1 - yRadius, xRadius, yRadius, 270, 360)
        glEnd()
    }

    fun originalRoundedRect(paramXStart: Number, paramYStart: Number, paramXEnd: Number, paramYEnd: Number, radius: Number, color: Int) {
        originalRoundedRect(paramXStart.toDouble(), paramYStart.toDouble(), paramXEnd.toDouble(), paramYEnd.toDouble(), radius.toDouble(), color)
    }

    fun originalRoundedRect(paramXStart: Double, paramYStart: Double, paramXEnd: Double, paramYEnd: Double, radius: Double, color: Int) {
        val (xStart, xEnd) = if (paramXStart > paramXEnd) paramXEnd to paramXStart else paramXStart to paramXEnd
        val (yStart, yEnd) = if (paramYStart > paramYEnd) paramYEnd to paramYStart else paramYStart to paramYEnd

        val x1 = xStart + radius
        val y1 = yStart + radius
        val x2 = xEnd - radius
        val y2 = yEnd - radius
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GLUtils.glColor(color)
        worldrenderer.begin(GL_POLYGON, DefaultVertexFormats.POSITION)
        val degree = PI / 180

        for (i in 0.0..90.0 step 1.0)
            worldrenderer.pos(x2 + sin(i * degree) * radius, y2 + cos(i * degree) * radius, 0.0).endVertex()

        for (i in 90.0..180.0 step 1.0)
            worldrenderer.pos(x2 + sin(i * degree) * radius, y1 + cos(i * degree) * radius, 0.0).endVertex()

        for (i in 180.0..270.0 step 1.0)
            worldrenderer.pos(x1 + sin(i * degree) * radius, y1 + cos(i * degree) * radius, 0.0).endVertex()

        for (i in 270.0..360.0 step 1.0)
            worldrenderer.pos(x1 + sin(i * degree) * radius, y2 + cos(i * degree) * radius, 0.0).endVertex()

        tessellator.draw()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    fun newDrawRect(left: Float, top: Float, right: Float, bottom: Float, color: Int) {
        newDrawRect(left.toDouble(), top.toDouble(), right.toDouble(), bottom.toDouble(), color)
    }

    fun newDrawRect(pLeft: Double, pTop: Double, pRight: Double, pBottom: Double, color: Int) {
        val (left, right) = if (pLeft > pRight) pRight to pLeft else pLeft to pRight
        val (top, bottom) = if (pTop > pBottom) pBottom to pTop else pTop to pBottom

        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GLUtils.glColor(color)
        worldrenderer.begin(7, DefaultVertexFormats.POSITION)
        worldrenderer.pos(left, bottom, 0.0).endVertex()
        worldrenderer.pos(right, bottom, 0.0).endVertex()
        worldrenderer.pos(right, top, 0.0).endVertex()
        worldrenderer.pos(left, top, 0.0).endVertex()
        tessellator.draw()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    fun drawRoundedRectWithWidthHeight(x: Number, y: Number, width: Number, height: Number, radius: Number, color: Int, popPush: Boolean = true) {
        drawRoundedRect(x.toDouble(), y.toDouble(), x.toDouble() + width.toDouble(), y.toDouble() + height.toDouble(), radius.toDouble(), color, popPush)
    }

    @JvmOverloads
    fun drawRoundedRect(paramXStart: Number, paramYStart: Number, paramXEnd: Number, paramYEnd: Number, radius: Number, color: Int, popPush: Boolean = true) {
        drawRoundedRect(paramXStart.toDouble(), paramYStart.toDouble(), paramXEnd.toDouble(), paramYEnd.toDouble(), radius.toDouble(), color, popPush)
    }

    fun drawRoundedRect(paramXStart: Double, paramYStart: Double, paramXEnd: Double, paramYEnd: Double, radius: Double, color: Int, popPush: Boolean = true) {
        val (xStart, xEnd) = if (paramXStart > paramXEnd) paramXEnd to paramXStart else paramXStart to paramXEnd
        val (yStart, yEnd) = if (paramYStart > paramYEnd) paramYEnd to paramYStart else paramYStart to paramYEnd

        val x1 = xStart + radius
        val y1 = yStart + radius
        val x2 = xEnd - radius
        val y2 = yEnd - radius

        if (popPush)
            glPushMatrix()

        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glLineWidth(1f)
        GLUtils.glColor(color)
        glBegin(GL_POLYGON)
        val degree = PI / 180

        for (i in 0.0..90.0 step 1.0)
            glVertex2d(x2 + sin(i * degree) * radius, y2 + cos(i * degree) * radius)

        for (i in 90.0..180.0 step 1.0)
            glVertex2d(x2 + sin(i * degree) * radius, y1 + cos(i * degree) * radius)

        for (i in 180.0..270.0 step 1.0)
            glVertex2d(x1 + sin(i * degree) * radius, y1 + cos(i * degree) * radius)

        for (i in 270.0..360.0 step 1.0)
            glVertex2d(x1 + sin(i * degree) * radius, y2 + cos(i * degree) * radius)

        glEnd()
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        if (popPush) glPopMatrix()
    }

    fun drawCustomRoundedCornerRect(paramXStart: Double, paramYStart: Double, paramXEnd: Double, paramYEnd: Double, radiusTopLeft: Double, radiusTopRight: Double, radiusBottomLeft: Double, radiusBottomRight: Double, color: Int) {
        val (xStart, xEnd) = if (paramXStart > paramXEnd) paramXEnd to paramXStart else paramXStart to paramXEnd
        val (yStart, yEnd) = if (paramYStart > paramYEnd) paramYEnd to paramYStart else paramYStart to paramYEnd

        val xTopLeft = xStart + radiusTopLeft
        val yTopLeft = yStart + radiusTopLeft
        val xTopRight = xEnd - radiusTopRight
        val yTopRight = yStart + radiusTopRight
        val xBottomRight = xEnd - radiusBottomRight
        val yBottomRight = yEnd - radiusBottomRight
        val xBottomLeft = xStart + radiusBottomLeft
        val yBottomLeft = yEnd - radiusBottomLeft

        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glLineWidth(1f)
        GLUtils.glColor(color)
        glBegin(GL_POLYGON)
        val degree = PI / 180

        if (radiusBottomRight <= 0)
            glVertex2d(xBottomLeft, yBottomLeft)
        else for (i in 0.0..90.0 step 1.0)
            glVertex2d(xBottomLeft + sin(i * degree) * radiusBottomRight, yBottomLeft + cos(i * degree) * radiusBottomRight)

        if (radiusTopRight <= 0)
            glVertex2d(xTopRight, yTopRight)
        else for (i in 90.0..180.0 step 1.0)
            glVertex2d(xTopRight + sin(i * degree) * radiusTopRight, yTopRight + cos(i * degree) * radiusTopRight)

        if (radiusTopLeft <= 0)
            glVertex2d(xTopLeft, yTopLeft)
        else for (i in 180.0..270.0 step 1.0)
            glVertex2d(xTopLeft + sin(i * degree) * radiusTopLeft, yTopLeft + cos(i * degree) * radiusTopLeft)

        if (radiusBottomLeft <= 0)
            glVertex2d(xBottomRight, yBottomRight)
        else for (i in 270.0..360.0 step 1.0)
            glVertex2d(xBottomRight + sin(i * degree) * radiusBottomLeft, yBottomRight + cos(i * degree) * radiusBottomLeft)

        glEnd()
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        glPopMatrix()
    }

    fun fastRoundedRect(paramXStart: Number, paramYStart: Number, paramXEnd: Number, paramYEnd: Number, radius: Number) {
        fastRoundedRect(paramXStart.toDouble(), paramYStart.toDouble(), paramXEnd.toDouble(), paramYEnd.toDouble(), radius.toDouble())
    }

    fun fastRoundedRect(paramXStart: Double, paramYStart: Double, paramXEnd: Double, paramYEnd: Double, radius: Double) {
        val (xStart, xEnd) = if (paramXStart > paramXEnd) paramXEnd to paramXStart else paramXStart to paramXEnd
        val (yStart, yEnd) = if (paramYStart > paramYEnd) paramYEnd to paramYStart else paramYStart to paramYEnd

        val x1 = xStart + radius
        val y1 = yStart + radius
        val x2 = xEnd - radius
        val y2 = yEnd - radius

        glEnable(GL_LINE_SMOOTH)
        glLineWidth(1f)
        glBegin(GL_POLYGON)
        val degree = Math.PI / 180

        for (i in 0.0..90.0 step 1.0)
            glVertex2d(x2 + sin(i * degree) * radius, y2 + cos(i * degree) * radius)

        for (i in 90.0..180.0 step 1.0)
            glVertex2d(x2 + sin(i * degree) * radius, y1 + cos(i * degree) * radius)

        for (i in 180.0..270.0 step 1.0)
            glVertex2d(x1 + sin(i * degree) * radius, y1 + cos(i * degree) * radius)

        for (i in 270.0..360.0 step 1.0)
            glVertex2d(x1 + sin(i * degree) * radius, y2 + cos(i * degree) * radius)


        glEnd()
        glDisable(GL_LINE_SMOOTH)
    }

    fun drawGradientRect(left: Number, top: Number, right: Number, bottom: Number, startColor: Int, endColor: Int) {
        drawGradientRect(left.toDouble(), top.toDouble(), right.toDouble(), bottom.toDouble(), startColor, endColor)
    }

    fun drawGradientRect(left: Double, top: Double, right: Double, bottom: Double, startColor: Int, endColor: Int) {
        val startAlpha = (startColor shr 24 and 255).toFloat() / 255f
        val startRed = (startColor shr 16 and 255).toFloat() / 255f
        val startGreen = (startColor shr 8 and 255).toFloat() / 255f
        val startBlue = (startColor and 255).toFloat() / 255f

        val endAlpha = (endColor shr 24 and 255).toFloat() / 255f
        val endRed = (endColor shr 16 and 255).toFloat() / 255f
        val endGreen = (endColor shr 8 and 255).toFloat() / 255f
        val endBlue = (endColor and 255).toFloat() / 255f

        GlStateManager.pushMatrix()
        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.disableAlpha()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.shadeModel(7425)
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR)
        worldrenderer.pos(right, top, 0.0).color(startRed, startGreen, startBlue, startAlpha).endVertex()
        worldrenderer.pos(left, top, 0.0).color(startRed, startGreen, startBlue, startAlpha).endVertex()
        worldrenderer.pos(left, bottom, 0.0).color(endRed, endGreen, endBlue, endAlpha).endVertex()
        worldrenderer.pos(right, bottom, 0.0).color(endRed, endGreen, endBlue, endAlpha).endVertex()
        tessellator.draw()
        GlStateManager.shadeModel(7424)
        GlStateManager.disableBlend()
        GlStateManager.enableAlpha()
        GlStateManager.enableTexture2D()
        GlStateManager.popMatrix()
    }

    fun quickDrawRect(x: Number, y: Number, x2: Number, y2: Number, color: Int) {
        quickDrawRect(x.toDouble(), y.toDouble(), x2.toDouble(), y2.toDouble(), color)
    }

    fun quickDrawRect(x: Double, y: Double, x2: Double, y2: Double, color: Int) {
        GLUtils.glColor(color)
        quickDrawRect(x, y, x2, y2)
    }

    fun quickDrawRect(x: Number, y: Number, x2: Number, y2: Number) {
        quickDrawRect(x.toDouble(), y.toDouble(), x2.toDouble(), y2.toDouble())
    }

    fun quickDrawRect(x: Double, y: Double, x2: Double, y2: Double) {
        glBegin(GL_QUADS)
        glVertex2d(x2, y)
        glVertex2d(x, y)
        glVertex2d(x, y2)
        glVertex2d(x2, y2)
        glEnd()
    }

    fun drawRect(x: Number, y: Number, x2: Number, y2: Number, color: Color) {
        drawRect(x.toDouble(), y.toDouble(), x2.toDouble(), y2.toDouble(), color.rgb)
    }

    fun drawRect(x: Number, y: Number, x2: Number, y2: Number, color: Int) {
        drawRect(x.toDouble(), y.toDouble(), x2.toDouble(), y2.toDouble(), color)
    }

    fun drawRect(x: Double, y: Double, x2: Double, y2: Double, color: Color) {
        drawRect(x, y, x2, y2, color.rgb)
    }

    fun drawRect(x: Double, y: Double, x2: Double, y2: Double, color: Int) {
        glPushMatrix()
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        GLUtils.glColor(color)
        glBegin(GL_QUADS)
        glVertex2d(x2, y)
        glVertex2d(x, y)
        glVertex2d(x, y2)
        glVertex2d(x2, y2)
        glEnd()
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        glPopMatrix()
    }

    fun drawRectWithTessellator(pLeft: Double, pTop: Double, pRight: Double, pBottom: Double, color: Int) {
        val (left, right) = if (pLeft > pRight) pRight to pLeft else pLeft to pRight
        val (top, bottom) = if (pTop > pBottom) pBottom to pTop else pTop to pBottom

        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GLUtils.glColor(color)
        worldrenderer.begin(7, DefaultVertexFormats.POSITION)
        worldrenderer.pos(left, bottom, 0.0).endVertex()
        worldrenderer.pos(right, bottom, 0.0).endVertex()
        worldrenderer.pos(right, top, 0.0).endVertex()
        worldrenderer.pos(left, top, 0.0).endVertex()
        tessellator.draw()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    fun drawBorderedRect(x: Number, y: Number, x2: Number, y2: Number, width: Number, borderColor: Int, rectColor: Int) {
        drawBorderedRect(x.toFloat(), y.toFloat(), x2.toFloat(), y2.toFloat(), width.toFloat(), borderColor, rectColor)
    }

    fun drawBorderedRect(x: Float, y: Float, x2: Float, y2: Float, width: Float, borderColor: Int, rectColor: Int) {
        drawRect(x, y, x2, y2, rectColor)
        drawBorder(x, y, x2, y2, width, borderColor)
    }

    fun drawBorder(x: Number, y: Number, x2: Number, y2: Number, width: Float, color: Int) {
        drawBorder(x.toDouble(), y.toDouble(), x2.toDouble(), y2.toDouble(), width, color)
    }

    fun drawBorder(x: Double, y: Double, x2: Double, y2: Double, width: Float, color: Int) {
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        GLUtils.glColor(color)
        glLineWidth(width)
        glBegin(GL_LINE_LOOP)
        glVertex2d(x2, y)
        glVertex2d(x, y)
        glVertex2d(x, y2)
        glVertex2d(x2, y2)
        glEnd()
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
    }

    fun drawRectBasedBorder(x: Float, y: Float, x2: Float, y2: Float, width: Float, color1: Int) {
        drawRect(x - width / 2f, y - width / 2f, x2 + width / 2f, y + width / 2f, color1)
        drawRect(x - width / 2f, y + width / 2f, x + width / 2f, y2 + width / 2f, color1)
        drawRect(x2 - width / 2f, y + width / 2f, x2 + width / 2f, y2 + width / 2f, color1)
        drawRect(x + width / 2f, y2 - width / 2f, x2 - width / 2f, y2 + width / 2f, color1)
    }

    fun drawRectBasedBorder(x: Double, y: Double, x2: Double, y2: Double, width: Double, color1: Int) {
        newDrawRect(x - width / 2f, y - width / 2f, x2 + width / 2f, y + width / 2f, color1)
        newDrawRect(x - width / 2f, y + width / 2f, x + width / 2f, y2 + width / 2f, color1)
        newDrawRect(x2 - width / 2f, y + width / 2f, x2 + width / 2f, y2 + width / 2f, color1)
        newDrawRect(x + width / 2f, y2 - width / 2f, x2 - width / 2f, y2 + width / 2f, color1)
    }

    fun quickDrawBorderedRect(x: Float, y: Float, x2: Float, y2: Float, width: Float, color1: Int, color2: Int) {
        quickDrawRect(x, y, x2, y2, color2)
        GLUtils.glColor(color1)
        glLineWidth(width)
        glBegin(GL_LINE_LOOP)
        glVertex2d(x2.toDouble(), y.toDouble())
        glVertex2d(x.toDouble(), y.toDouble())
        glVertex2d(x.toDouble(), y2.toDouble())
        glVertex2d(x2.toDouble(), y2.toDouble())
        glEnd()
    }

    fun drawRoundedGradientOutlineCorner(x: Number, y: Number, x2: Number, y2: Number, width: Float, radius: Float, color: Int, color2: Int) {
        drawRoundedGradientOutlineCorner(x.toFloat(), y.toFloat(), x2.toFloat(), y2.toFloat(), width, radius, color, color2)
    }

    fun drawRoundedGradientOutlineCorner(x: Float, y: Float, x2: Float, y2: Float, width: Float, radius: Float, color: Int, color2: Int) {
        drawRoundedGradientOutlineCorner(x, y, x2, y2, width, radius, color, color, color2, color2)
    }

    fun drawRoundedGradientOutlineCorner(x: Number, y: Number, x2: Number, y2: Number, width: Float, radius: Float, color: Int, color2: Int, color3: Int, color4: Int) {
        drawRoundedGradientOutlineCorner(x.toFloat(), y.toFloat(), x2.toFloat(), y2.toFloat(), width, radius, color, color2, color3, color4)
    }

    fun drawRoundedGradientOutlineCorner(x: Float, y: Float, x2: Float, y2: Float, width: Float, radius: Float, color: Int, color2: Int, color3: Int, color4: Int) {
        val drawnX = x * 2
        val drawnY = y * 2
        val drawnX2 = x2 * 2
        val drawnY2 = y2 * 2

        GLUtils.glColor(-1)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glShadeModel(GL_SMOOTH)
        glPushAttrib(0)
        glScaled(0.5, 0.5, 0.5)

        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        GLUtils.glColor(color)
        glEnable(GL_LINE_SMOOTH)
        glShadeModel(GL_SMOOTH)
        glLineWidth(width)
        glBegin(GL_LINE_LOOP)

        for (i in 0..90 step 3) {
            val angle = MathUtils.toRadiansDouble(i)
            glVertex2d(drawnX + radius - sin(angle) * radius, drawnY + radius - cos(angle) * radius)
        }

        GLUtils.glColor(color2)

        for (i in 90..180 step 3) {
            val angle = MathUtils.toRadiansDouble(i)
            glVertex2d(drawnX + radius - sin(angle) * radius, drawnY2 - radius - cos(angle) * radius)
        }

        GLUtils.glColor(color3)

        for (i in 0..90 step 3) {
            val angle = MathUtils.toRadiansDouble(i)
            glVertex2d(drawnX2 - radius + sin(angle) * radius, drawnY2 - radius + cos(angle) * radius)
            
        }

        GLUtils.glColor(color4)

        for (i in 0..90 step 3) {
            val angle = MathUtils.toRadiansDouble(i)
            glVertex2d(drawnX2 - radius + sin(angle) * radius, drawnY + radius + cos(angle) * radius)
        }

        glEnd()
        glLineWidth(1f)
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)
        glScaled(2.0, 2.0, 2.0)
        glPopAttrib()
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        glShadeModel(GL_FLAT)
        GLUtils.glColor(-1)
    }

    fun drawRoundedGradientRectCorner(x: Number, y: Number, x2: Number, y2: Number, radius: Float, color: Int, color2: Int) {
        drawRoundedGradientRectCorner(x.toFloat(), y.toFloat(), x2.toFloat(), y2.toFloat(), radius, color, color2)
    }

    fun drawRoundedGradientRectCorner(x: Float, y: Float, x2: Float, y2: Float, radius: Float, color: Int, color2: Int) {
        drawRoundedGradientRectCorner(x, y, x2, y2, radius, color, color, color2, color2)
    }

    fun drawRoundedGradientRectCorner(x: Number, y: Number, x2: Number, y2: Number, radius: Float, color: Int, color2: Int, color3: Int, color4: Int) {
        drawRoundedGradientRectCorner(x.toFloat(), y.toFloat(), x2.toFloat(), y2.toFloat(), radius, color, color2, color3, color4)
    }

    fun drawRoundedGradientRectCorner(x: Float, y: Float, x2: Float, y2: Float, radius: Float, color: Int, color2: Int, color3: Int, color4: Int) {
        val drawnX = x * 2
        val drawnY = y * 2
        val drawnX2 = x2 * 2
        val drawnY2 = y2 * 2

        GLUtils.glColor(-1)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glShadeModel(GL_SMOOTH)
        glPushAttrib(0)
        glScaled(0.5, 0.5, 0.5)

        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        GLUtils.glColor(color)
        glEnable(GL_LINE_SMOOTH)
        glShadeModel(GL_SMOOTH)
        glBegin(6)
        
        for (i in 0..90 step 3) {
            val angle = MathUtils.toRadiansDouble(i)
            glVertex2d(drawnX + radius - sin(angle) * radius, drawnY + radius - cos(angle) * radius)
        }

        GLUtils.glColor(color2)

        for (i in 90..180 step 3) {
            val angle = MathUtils.toRadiansDouble(i)
            glVertex2d(drawnX + radius - sin(angle) * radius, drawnY2 - radius - cos(angle) * radius)
        }

        GLUtils.glColor(color3)

        for (i in 0..90 step 3) {
            val angle = MathUtils.toRadiansDouble(i)
            glVertex2d(drawnX2 - radius + sin(angle) * radius, drawnY2 - radius + cos(angle) * radius)
            
        }

        GLUtils.glColor(color4)

        for (i in 0..90 step 3) {
            val angle = MathUtils.toRadiansDouble(i)
            glVertex2d(drawnX2 - radius + sin(angle) * radius, drawnY + radius + cos(angle) * radius)
        }

        glEnd()
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)
        glScaled(2.0, 2.0, 2.0)
        glPopAttrib()
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        glShadeModel(GL_FLAT)
        GLUtils.glColor(-1)
    }


    /**
     * OTHER RECTAGLE
     */

    fun drawExhiRect(x: Float, y: Float, x2: Float, y2: Float) {
        drawExhiRect(x, y, x2, y2, 1f)
    }

    fun drawExhiRect(x: Float, y: Float, x2: Float, y2: Float, alpha: Float) {
        val alphaInt = (alpha * 255).toInt()

        drawRect(x - 3.5f, y - 3.5f, x2 + 3.5f, y2 + 3.5f, Color(0, 0, 0, alphaInt).rgb)
        drawRect(x - 3f, y - 3f, x2 + 3f, y2 + 3f, Color(50, 50, 50, alphaInt).rgb)
        drawRect(x - 2.5f, y - 2.5f, x2 + 2.5f, y2 + 2.5f, Color(26, 26, 26, alphaInt).rgb)
        drawRect(x - 0.5f, y - 0.5f, x2 + 0.5f, y2 + 0.5f, Color(50, 50, 50, alphaInt).rgb)
        drawRect(x, y, x2, y2, Color(18, 18, 18, alphaInt).rgb)
    }

    fun drawMosswareRect(x: Float, y: Float, x2: Float, y2: Float, width: Float, color1: Int, color2: Int) {
        drawRect(x, y, x2, y2, color2)
        drawBorder(x, y, x2, y2, width, color1)
    }


    /**
     * TRIANGLE
     */

    fun drawSquareTriangle(cx: Double, cy: Double, dirX: Double, dirY: Double, color: Color, filled: Boolean) {
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.resetColor()
        GLUtils.glColor(color)
        worldrenderer.begin(if (filled) 5 else 2, DefaultVertexFormats.POSITION)
        worldrenderer.pos(cx + dirX, cy, 0.0).endVertex()
        worldrenderer.pos(cx, cy, 0.0).endVertex()
        worldrenderer.pos(cx, cy + dirY, 0.0).endVertex()
        worldrenderer.pos(cx + dirX, cy, 0.0).endVertex()
        tessellator.draw()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.color(1f, 1f, 1f, 1f)
    }

    fun drawTriangle(cx: Float, cy: Float, radius: Float, n: Float, color: Color, polygon: Boolean) {
        var cornerX = cx * 2
        var cornerY = cy * 2
        var r = radius * 2
        val b = PI * 2 / n
        val p = cos(b)
        val s = sin(b)
        var x = r.toDouble()
        var y = 0.0
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        glLineWidth(1f)
        GLUtils.enableGlCap(GL_LINE_SMOOTH)
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.resetColor()
        GLUtils.glColor(color)
        GlStateManager.scale(0.5f, 0.5f, 0.5f)
        worldrenderer.begin(if (polygon) GL_POLYGON else 2, DefaultVertexFormats.POSITION)
        var ii = 0
        while (ii < n) {
            worldrenderer.pos(x + cornerX, y + cornerY, 0.0).endVertex()
            val t = x
            x = p * x - s * y
            y = s * t + p * y
            ii++
        }
        tessellator.draw()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.scale(2f, 2f, 2f)
        GlStateManager.color(1f, 1f, 1f, 1f)
    }

    /**
     * CIRCLE
     */

    fun drawFilledCircleNoGL(x: Int, y: Int, radius: Double, color: Color, quality: Int) {
        val red = color.red / 255f
        val green = color.green / 255f
        val blue = color.blue / 255f
        val alpha = color.alpha / 255f

        glColor4f(red, green, blue, alpha)
        glBegin(GL_TRIANGLE_FAN)

        for (i in 0 until 360 / quality) {
            val angle = MathUtils.toRadiansFloat(i * quality)

            val x2 = sin(angle) * radius
            val y2 = cos(angle) * radius
            glVertex2d(x + x2, y + y2)
        }
        glEnd()
    }

    fun drawFilledCircleNoGL(x: Int, y: Int, radius: Double, color: Int, quality: Int) {
        val alpha = color shr 24 and 0xFF
        val red = color shr 16 and 0xFF
        val green = color shr 8 and 0xFF
        val blue = color and 0xFF

        drawFilledCircleNoGL(x, y, radius, Color(red, green, blue, alpha), quality)
    }

    fun quickPolygonCircle(x: Float, y: Float, xRadius: Float, yRadius: Float, start: Int, end: Int) {
        for (i in end until start step 4) {
            val angle = MathUtils.toRadiansDouble(i)
            glVertex2d(x + sin(angle) * xRadius, y + cos(angle) * yRadius)
        }
    }

    fun drawLoadingCircle(x: Float, y: Float) {
        for (i in 0..3) {
            val rot = (System.nanoTime() / 5000000 * i % 360).toInt()
            drawCircle(x, y, 10f * i, rot - 180, rot)
        }
    }

    fun drawCircle(x: Float, y: Float, radius: Float, start: Int, end: Int) {
        drawCircle(x, y, radius, 2f, start, end, Color.WHITE)
    }

    fun drawCircle(x: Float, y: Float, radius: Float, lineWidth: Float, start: Int, end: Int) {
        drawCircle(x, y, radius, lineWidth, start, end, Color.WHITE)
    }

    fun drawCircle(x: Float, y: Float, radius: Float, lineWidth: Float, start: Int, end: Int, color: Color) {
        GLUtils.glColor(color)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
        GLUtils.glColor(color)
        glEnable(GL_LINE_SMOOTH)
        glLineWidth(lineWidth)
        glBegin(GL_LINE_STRIP)

        for (i in end until start step 4) {
            val angle = MathUtils.toRadiansFloat(i)
            glVertex2f(x + cos(angle) * radius * 1.001f, y + sin(angle) * radius * 1.001f)
        }

        glEnd()
        glDisable(GL_LINE_SMOOTH)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    fun drawGradientCircle(x: Float, y: Float, radius: Float, start: Int, end: Int, startColor: Color, endColor: Color) {
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
        glEnable(GL_LINE_SMOOTH)
        glLineWidth(2f)
        glBegin(GL_LINE_STRIP)

        for (i in end until start step 4) {
            val color = ColorUtils.getGradientOffset(startColor, endColor, 1.0, (abs(System.currentTimeMillis() / 360.0 + (i * 34 / 360) * 56 / 100) / 10).toInt()).rgb
            GLUtils.glColor(color)

            val angle = MathUtils.toRadiansFloat(i)
            glVertex2f(x + cos(angle) * radius * 1.001f, y + sin(angle) * radius * 1.001f)

        }
        glEnd()
        glDisable(GL_LINE_SMOOTH)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    fun drawFilledCircle(x: Number, y: Number, radius: Float, color: Color) {
        drawFilledCircle(x.toFloat(), y.toFloat(), radius, color)
    }

    fun drawFilledCircle(x: Float, y: Float, radius: Float, color: Color) {
        val angleUnit = PI.toFloat() / 25 // 2 * PI / 50
        glPushAttrib(GL_ENABLE_BIT)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glBegin(GL_TRIANGLE_FAN)
        for (i in 0 until 50) {
            GLUtils.glColor(color)
            glVertex2f(x + sin(i * angleUnit) * radius, y + cos(i * angleUnit) * radius)
        }
        GlStateManager.color(0f, 0f, 0f)
        glEnd()
        glPopAttrib()
    }

    fun drawLimitedCircle(minX: Double, minY: Double, x: Int, y: Int, x2: Double, y2: Double, radius: Float, color: Color) {
        val dAngle = PI / 25
        glPushAttrib(GL_ENABLE_BIT)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glBegin(GL_TRIANGLE_FAN)
        GLUtils.glColor(color)

        for (i in 0 until 50) {
            val angle = MathUtils.toRadians(i * dAngle)
            val drawnX = (x + angle * radius).coerceIn(minX, x2).toFloat()
            val drawnY = (y + angle * radius).coerceIn(minY, y2).toFloat()
            glVertex2f(drawnX, drawnY)
        }
        GlStateManager.color(0f, 0f, 0f)
        glEnd()
        glPopAttrib()
    }

    /**
     * GRADIENT
     */

    fun drawGradientSidewaysHorizontal(left: Double, top: Double, right: Double, bottom: Double, startColor: Int, endColor: Int) {
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glShadeModel(GL_SMOOTH)
        quickDrawGradientSidewaysHorizontal(left, top, right, bottom, startColor, endColor)
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        glShadeModel(GL_FLAT)
    }

    fun quickDrawGradientSidewaysHorizontal(left: Double, top: Double, right: Double, bottom: Double, startColor: Int, endColor: Int) {
        glBegin(GL_QUADS)
        GLUtils.glColor(startColor)
        glVertex2d(left, top)
        glVertex2d(left, bottom)
        GLUtils.glColor(endColor)
        glVertex2d(right, bottom)
        glVertex2d(right, top)
        glEnd()
    }

    fun drawGradientSidewaysVertical(left: Double, top: Double, right: Double, bottom: Double, startColor: Int, endColor: Int) {
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glShadeModel(GL_SMOOTH)
        quickDrawGradientSidewaysVertical(left, top, right, bottom, startColor, endColor)
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        glShadeModel(GL_FLAT)
    }

    fun quickDrawGradientSidewaysVertical(left: Double, top: Double, right: Double, bottom: Double, startColor: Int, endColor: Int) {
        glBegin(GL_QUADS)
        GLUtils.glColor(startColor)
        glVertex2d(right, top)
        glVertex2d(left, top)
        GLUtils.glColor(endColor)
        glVertex2d(left, bottom)
        glVertex2d(right, bottom)
        glEnd()
    }

    fun drawGradientSideways(left: Number, top: Number, right: Number, bottom: Number, startColor: Int, endColor: Int) {
        drawGradientSideways(left.toDouble(), top.toDouble(), right.toDouble(), bottom.toDouble(), startColor, endColor)
    }

    fun drawGradientSideways(left: Double, top: Double, right: Double, bottom: Double, startColor: Int, endColor: Int) {
        glEnable(3042)
        glDisable(3553)
        glBlendFunc(770, 771)
        glEnable(2848)
        glShadeModel(7425)
        glPushMatrix()
        glBegin(7)
        GLUtils.glColor(startColor)
        glVertex2d(left, top)
        glVertex2d(left, bottom)
        GLUtils.glColor(endColor)
        glVertex2d(right, bottom)
        glVertex2d(right, top)
        glEnd()
        glPopMatrix()
        glEnable(3553)
        glDisable(3042)
        glDisable(2848)
        glShadeModel(7424)
    }

    fun drawAnimatedGradient(left: Double, top: Double, right: Double, bottom: Double, color1: Int, color2: Int) {
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - startTime
        val moduloTime = elapsedTime % ANIMATION_DURATION
        val progress = moduloTime.toFloat() / ANIMATION_DURATION
        val leftColor = ColorUtils.interpolateColorWithProgress(color1, color2, progress)
        val rightColor = ColorUtils.interpolateColorWithProgress(color2, color1, progress)

        val (startColor, endColor) = if (elapsedTime.toInt() / ANIMATION_DURATION % 2 == 0) leftColor to rightColor else rightColor to leftColor

        drawGradientSideways(left, top, right, bottom, startColor, endColor)
        if (elapsedTime >= 2 * ANIMATION_DURATION)
            startTime = currentTime
    }

    /**
     * TEXTURE, MODAL
     */

    fun drawTexturedModalRect(x: Number, y: Number, textureX: Number, textureY: Number, width: Int, height: Int, zLevel: Number) {
        drawTexturedModalRect(x.toDouble(), y.toDouble(), textureX.toDouble(), textureY.toDouble(), width, height, zLevel.toDouble())
    }

    fun drawTexturedModalRect(x: Double, y: Double, textureX: Double, textureY: Double, width: Int, height: Int, zLevel: Double) {
        val f = 0.00390625
        val f1 = 0.00390625
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX)
        worldRenderer.pos(x, y + height, zLevel).tex(textureX * f, (textureY + height) * f1).endVertex()
        worldRenderer.pos(x + width, y + height, zLevel).tex((textureX + width) * f, (textureY + height) * f1).endVertex()
        worldRenderer.pos(x + width, y, zLevel).tex((textureX + width) * f, textureY * f1).endVertex()
        worldRenderer.pos(x, y, zLevel).tex(textureX * f, textureY * f1).endVertex()
        tessellator.draw()
    }

    fun drawScaledCustomSizeModalRect(x: Int, y: Int, u: Float, v: Float, uWidth: Int, vHeight: Int, width: Int, height: Int, tileWidth: Float, tileHeight: Float) {
        drawScaledCustomSizeModalRect(x.toDouble(), y.toDouble(), u.toDouble(), v.toDouble(), uWidth, vHeight, width, height, tileWidth, tileHeight)
    }

    fun drawScaledCustomSizeModalRect(x: Double, y: Double, u: Double, v: Double, uWidth: Int, vHeight: Int, width: Int, height: Int, tileWidth: Float, tileHeight: Float) {
        val f = 1.0f / tileWidth
        val f1 = 1.0f / tileHeight
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        worldrenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX)
        worldrenderer.pos(x, y + height, 0.0).tex(u * f, (v + vHeight) * f1).endVertex()
        worldrenderer.pos(x + width, y + height, 0.0).tex((u + uWidth) * f, (v + vHeight) * f1).endVertex()
        worldrenderer.pos(x + width, y, 0.0).tex((u + uWidth) * f, v * f1).endVertex()
        worldrenderer.pos(x, y, 0.0).tex(u * f, v * f1).endVertex()
        tessellator.draw()
    }

    fun drawTexturedRect(x: Float, y: Float, width: Float, height: Float, image: String) {
        glPushMatrix()

        val enableBlend = glIsEnabled(GL_BLEND)
        val disableAlpha = !glIsEnabled(GL_ALPHA_TEST)
        if (!enableBlend)
            glEnable(GL_BLEND)

        if (!disableAlpha)
            glDisable(GL_ALPHA_TEST)

        mc.textureManager.bindTexture(ResourceLocation("liquidbounce+/ui/$image.png"))
        GlStateManager.color(1f, 1f, 1f, 1f)
        drawModalRectWithCustomSizedTexture(x.toInt(), y.toInt(), 0f, 0f, width.toInt(), height.toInt(), width, height)
        
        if (!enableBlend)
            glDisable(GL_BLEND)

        if (!disableAlpha) 
            glEnable(GL_ALPHA_TEST)
        glPopMatrix()
    }

    fun drawHead(skin: ResourceLocation, x: Int, y: Int, width: Int, height: Int) {
        glDisable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glDepthMask(false)
        OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
        glColor4f(1f, 1f, 1f, 1f)
        mc.textureManager.bindTexture(skin)
        Gui.drawScaledCustomSizeModalRect(x, y, 8F, 8F, 8, 8, width, height, 64F, 64F)
        glDepthMask(true)
        glDisable(GL_BLEND)
        glEnable(GL_DEPTH_TEST)
    }

    fun drawEntityOnScreen(posX: Int, posY: Int, scale: Int, entity: EntityLivingBase?) {
        drawEntityOnScreen(posX.toDouble(), posY.toDouble(), scale.toFloat(), entity)
    }

    fun drawEntityOnScreen(posX: Double, posY: Double, scale: Float, entity: EntityLivingBase?) {
        GlStateManager.pushMatrix()
        GlStateManager.enableColorMaterial()
        GlStateManager.translate(posX, posY, 50.0)
        GlStateManager.scale(-scale, scale, scale)
        GlStateManager.rotate(180f, 0f, 0f, 1f)
        GlStateManager.rotate(135f, 0f, 1f, 0f)
        RenderHelper.enableStandardItemLighting()
        GlStateManager.rotate(-135f, 0f, 1f, 0f)
        GlStateManager.translate(0.0, 0.0, 0.0)
        mc.renderManager.setPlayerViewY(180f)
        mc.renderManager.isRenderShadow = false
        mc.renderManager.renderEntityWithPosYaw(entity, 0.0, 0.0, 0.0, 0f, 1f)
        mc.renderManager.isRenderShadow = true
        GlStateManager.popMatrix()
        RenderHelper.disableStandardItemLighting()
        GlStateManager.disableRescaleNormal()
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit)
        GlStateManager.disableTexture2D()
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit)
    }

    /**
     * BOUNDING BOX, 3D
     */


    fun drawBlockBox(blockPos: BlockPos, color: Color, outline: Boolean) {
        val x = blockPos.x - mc.renderManager.renderPosX
        val y = blockPos.y - mc.renderManager.renderPosY
        val z = blockPos.z - mc.renderManager.renderPosZ
        val block = BlockUtils.getBlock(blockPos)

        val axisAlignedBB = if (block != null) {
            val posX = MathUtils.interpolate(mc.thePlayer.posX, mc.thePlayer.lastTickPosX, mc.timer.renderPartialTicks)
            val posY = MathUtils.interpolate(mc.thePlayer.posY, mc.thePlayer.lastTickPosY, mc.timer.renderPartialTicks)
            val posZ = MathUtils.interpolate(mc.thePlayer.posZ, mc.thePlayer.lastTickPosZ, mc.timer.renderPartialTicks)

            block.getSelectedBoundingBox(mc.theWorld, blockPos)
                .expand(0.0020000000949949026).offset(-posX, -posY, -posZ)
        } else AxisAlignedBB(x, y, z, x + 1.0, y + 1, z + 1.0)

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        GLUtils.enableGlCap(GL_BLEND)
        GLUtils.disableGlCap(GL_TEXTURE_2D, GL_DEPTH_TEST)
        glDepthMask(false)
        GLUtils.glColor(color.red, color.green, color.blue, if (color.alpha != 255) color.alpha else if (outline) 26 else 35)
        drawFilledBox(axisAlignedBB)

        if (outline) {
            glLineWidth(1f)
            GLUtils.enableGlCap(GL_LINE_SMOOTH)
            GLUtils.glColor(color)
            drawSelectionBoundingBox(axisAlignedBB)
        }

        GlStateManager.resetColor()
        glDepthMask(true)
        GLUtils.resetCaps()
    }

    fun drawSelectionBoundingBox(boundingBox: AxisAlignedBB) {
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        worldrenderer.begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION)

        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex()
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex()
        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex()
        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex()
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex()

        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex()
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex()
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex()

        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex()
        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex()
        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex()
        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex()

        tessellator.draw()
    }

    fun drawEntityBox(entity: Entity, color: Color, outline: Boolean) {
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        GLUtils.enableGlCap(GL_BLEND)
        GLUtils.disableGlCap(GL_TEXTURE_2D, GL_DEPTH_TEST)
        glDepthMask(false)

        val x = MathUtils.interpolate(entity.posX, entity.lastTickPosX, mc.timer.renderPartialTicks) - mc.renderManager.renderPosX
        val y = MathUtils.interpolate(entity.posY, entity.lastTickPosY, mc.timer.renderPartialTicks) - mc.renderManager.renderPosX
        val z = MathUtils.interpolate(entity.posZ, entity.lastTickPosZ, mc.timer.renderPartialTicks) - mc.renderManager.renderPosX

        val entityBox = entity.entityBoundingBox
        val axisAlignedBB = AxisAlignedBB(
            entityBox.minX - entity.posX + x - 0.05,
            entityBox.minY - entity.posY + y,
            entityBox.minZ - entity.posZ + z - 0.05,
            entityBox.maxX - entity.posX + x + 0.05,
            entityBox.maxY - entity.posY + y + 0.15,
            entityBox.maxZ - entity.posZ + z + 0.05
        )

        if (outline) {
            glLineWidth(1f)
            GLUtils.enableGlCap(GL_LINE_SMOOTH)
            GLUtils.glColor(color.red, color.green, color.blue, 95)
            drawSelectionBoundingBox(axisAlignedBB)
        }

        GLUtils.glColor(color.red, color.green, color.blue, if (outline) 26 else 35)
        drawFilledBox(axisAlignedBB)
        GlStateManager.resetColor()
        glDepthMask(true)
        GLUtils.resetCaps()
    }

    fun drawAxisAlignedBB(axisAlignedBB: AxisAlignedBB, color: Color) {
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_BLEND)
        glLineWidth(2f)
        glDisable(GL_TEXTURE_2D)
        glDisable(GL_DEPTH_TEST)
        glDepthMask(false)
        GLUtils.glColor(color)
        drawFilledBox(axisAlignedBB)
        GlStateManager.resetColor()
        glEnable(GL_TEXTURE_2D)
        glEnable(GL_DEPTH_TEST)
        glDepthMask(true)
        glDisable(GL_BLEND)
    }

    fun drawPlatform(y: Double, color: Color, size: Double) {
        val renderY = y - mc.renderManager.renderPosY
        drawAxisAlignedBB(AxisAlignedBB(size, renderY + 0.02, size, -size, renderY, -size), color)
    }

    fun drawPlatform(entity: Entity, color: Color, moveMarkY: Float) {
        val x = MathUtils.interpolate(entity.posX, entity.lastTickPosX, mc.timer.renderPartialTicks) - mc.renderManager.renderPosX
        val y = MathUtils.interpolate(entity.posY, entity.lastTickPosY, mc.timer.renderPartialTicks) - mc.renderManager.renderPosX
        val z = MathUtils.interpolate(entity.posZ, entity.lastTickPosZ, mc.timer.renderPartialTicks) - mc.renderManager.renderPosX

        val axisAlignedBB = entity.entityBoundingBox
            .offset(-entity.posX, -entity.posY, -entity.posZ)
            .offset(x, y - moveMarkY, z)
        drawAxisAlignedBB(AxisAlignedBB(axisAlignedBB.minX, axisAlignedBB.maxY + 0.2, axisAlignedBB.minZ, axisAlignedBB.maxX, axisAlignedBB.maxY + 0.26, axisAlignedBB.maxZ), color)
    }

    fun drawFilledBox(axisAlignedBB: AxisAlignedBB) {
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer
        worldRenderer.begin(7, DefaultVertexFormats.POSITION)
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        tessellator.draw()
    }

    fun draw2D(entity: EntityLivingBase, posX: Double, posY: Double, posZ: Double, color: Int, backgroundColor: Int) {
        GlStateManager.pushMatrix()
        GlStateManager.translate(posX, posY, posZ)
        GlStateManager.rotate(-mc.renderManager.playerViewY, 0f, 1f, 0f)
        GlStateManager.scale(-0.1, -0.1, 0.1)
        glDisable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        GlStateManager.depthMask(true)
        GLUtils.glColor(color)
        glCallList(DISPLAY_LISTS_2D[0])
        GLUtils.glColor(backgroundColor)
        glCallList(DISPLAY_LISTS_2D[1])
        GlStateManager.translate(0.0, 21 - (entity.entityBoundingBox.maxY - entity.entityBoundingBox.minY) * 12, 0.0)
        GLUtils.glColor(color)
        glCallList(DISPLAY_LISTS_2D[2])
        GLUtils.glColor(backgroundColor)
        glCallList(DISPLAY_LISTS_2D[3])

        glEnable(GL_DEPTH_TEST)
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        GlStateManager.popMatrix()
    }

    fun draw2D(blockPos: BlockPos, color: Int, backgroundColor: Int) {
        val posX = blockPos.x - mc.renderManager.renderPosX + 0.5
        val posY = blockPos.y - mc.renderManager.renderPosY
        val posZ = blockPos.z - mc.renderManager.renderPosZ + 0.5
        GlStateManager.pushMatrix()
        GlStateManager.translate(posX, posY, posZ)
        GlStateManager.rotate(-mc.renderManager.playerViewY, 0f, 1f, 0f)
        GlStateManager.scale(-0.1, -0.1, 0.1)
        glDisable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        GlStateManager.depthMask(true)
        GLUtils.glColor(color)
        glCallList(DISPLAY_LISTS_2D[0])
        GLUtils.glColor(backgroundColor)
        glCallList(DISPLAY_LISTS_2D[1])
        GlStateManager.translate(0f, 9f, 0f)
        GLUtils.glColor(color)
        glCallList(DISPLAY_LISTS_2D[2])
        GLUtils.glColor(backgroundColor)
        glCallList(DISPLAY_LISTS_2D[3])

        glEnable(GL_DEPTH_TEST)
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        GlStateManager.popMatrix()
    }

    fun renderNameTag(string: String, x: Double, y: Double, z: Double) {
        val posX = x - mc.renderManager.renderPosX
        val posY = y - mc.renderManager.renderPosY
        val posZ = z - mc.renderManager.renderPosZ

        glPushMatrix()
        glTranslated(posX, posY, posZ)
        glNormal3f(0f, 1f, 0f)
        glRotatef(-mc.renderManager.playerViewY, 0f, 1f, 0f)
        glRotatef(mc.renderManager.playerViewX, 1f, 0f, 0f)
        glScalef(-0.05f, -0.05f, 0.05f)
        GLUtils.setGlCap(GL_LIGHTING, false)
        GLUtils.setGlCap(GL_DEPTH_TEST, false)
        GLUtils.setGlCap(GL_BLEND, true)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        val width = Fonts.font35.getStringWidth(string) / 2
        Gui.drawRect(-width - 1, -1, width + 1, Fonts.font35.FONT_HEIGHT, Int.MIN_VALUE)
        Fonts.font35.drawString(string, -width.toFloat(), 1.5f, Color.WHITE.rgb, true)
        GLUtils.resetCaps()
        glColor4f(1f, 1f, 1f, 1f)
        glPopMatrix()
    }

    fun otherDrawOutlinedBoundingBox(pYaw: Float, x: Double, y: Double, z: Double, pWidth: Double, height: Double) {
        val width = pWidth * 1.5
        var yaw = MathUtils.wrapAngleTo180(pYaw) + 45f

        val yawRender1 = if (yaw < 0.0) MathUtils.toRadians(abs(yaw) - 360f) else MathUtils.toRadians(yaw)
        yaw += 90.0F

        val yawRender2 = if (yaw < 0.0) MathUtils.toRadians(abs(yaw) - 360f) else MathUtils.toRadians(yaw)
        yaw += 90.0F

        val yawRender3 = if (yaw < 0.0) MathUtils.toRadians(abs(yaw) - 360f) else MathUtils.toRadians(yaw)
        yaw += 90.0F

        val yawRender4 = if (yaw < 0.0) MathUtils.toRadians(abs(yaw) - 360f) else MathUtils.toRadians(yaw)

        val x1 = sin(yawRender1) * width + x
        val z1 = cos(yawRender1) * width + z
        val x2 = sin(yawRender2) * width + x
        val z2 = cos(yawRender2) * width + z
        val x3 = sin(yawRender3) * width + x
        val z3 = cos(yawRender3) * width + z
        val x4 = sin(yawRender4) * width + x
        val z4 = cos(yawRender4) * width + z

        val y2 = y + height

        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        worldrenderer.begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION)
        worldrenderer.pos(x1, y, z1).endVertex()
        worldrenderer.pos(x1, y2, z1).endVertex()
        worldrenderer.pos(x2, y2, z2).endVertex()
        worldrenderer.pos(x2, y, z2).endVertex()
        worldrenderer.pos(x1, y, z1).endVertex()
        worldrenderer.pos(x4, y, z4).endVertex()
        worldrenderer.pos(x3, y, z3).endVertex()
        worldrenderer.pos(x3, y2, z3).endVertex()
        worldrenderer.pos(x4, y2, z4).endVertex()
        worldrenderer.pos(x4, y, z4).endVertex()
        worldrenderer.pos(x4, y2, z4).endVertex()
        worldrenderer.pos(x3, y2, z3).endVertex()
        worldrenderer.pos(x2, y2, z2).endVertex()
        worldrenderer.pos(x2, y, z2).endVertex()
        worldrenderer.pos(x3, y, z3).endVertex()
        worldrenderer.pos(x4, y, z4).endVertex()
        worldrenderer.pos(x4, y2, z4).endVertex()
        worldrenderer.pos(x1, y2, z1).endVertex()
        worldrenderer.pos(x1, y, z1).endVertex()
        tessellator.draw()
    }

    fun renderHitbox(bb: AxisAlignedBB, type: Int) {
        glBegin(type)

        glVertex3d(bb.minX, bb.minY, bb.maxZ)
        glVertex3d(bb.maxX, bb.minY, bb.maxZ)
        glVertex3d(bb.maxX, bb.minY, bb.minZ)
        glVertex3d(bb.minX, bb.minY, bb.minZ)

        glEnd()

        glBegin(type)

        glVertex3d(bb.minX, bb.maxY, bb.maxZ)
        glVertex3d(bb.maxX, bb.maxY, bb.maxZ)
        glVertex3d(bb.maxX, bb.maxY, bb.minZ)
        glVertex3d(bb.minX, bb.maxY, bb.minZ)

        glEnd()

        glBegin(type)

        glVertex3d(bb.minX, bb.minY, bb.minZ)
        glVertex3d(bb.minX, bb.minY, bb.maxZ)
        glVertex3d(bb.minX, bb.maxY, bb.maxZ)
        glVertex3d(bb.minX, bb.maxY, bb.minZ)

        glEnd()
        glBegin(type)

        glVertex3d(bb.maxX, bb.minY, bb.minZ)
        glVertex3d(bb.maxX, bb.minY, bb.maxZ)
        glVertex3d(bb.maxX, bb.maxY, bb.maxZ)
        glVertex3d(bb.maxX, bb.maxY, bb.minZ)

        glEnd()
        glBegin(type)
        glVertex3d(bb.minX, bb.minY, bb.minZ)
        glVertex3d(bb.maxX, bb.minY, bb.minZ)
        glVertex3d(bb.maxX, bb.maxY, bb.minZ)
        glVertex3d(bb.minX, bb.maxY, bb.minZ)

        glEnd()
        glBegin(type)
        glVertex3d(bb.minX, bb.minY, bb.maxZ)
        glVertex3d(bb.maxX, bb.minY, bb.maxZ)
        glVertex3d(bb.maxX, bb.maxY, bb.maxZ)
        glVertex3d(bb.minX, bb.maxY, bb.maxZ)

        glEnd()
    }

    /**
     * IMAGE
     */

    fun drawImage(image: ResourceLocation?, x: Number, y: Number, width: Number, height: Number) {
        drawImage(image, x.toInt(), y.toInt(), width.toInt(), height.toInt())
    }

    fun drawImage(image: ResourceLocation?, x: Int, y: Int, width: Int, height: Int) {
        drawImage(image, x, y, width, height, Color.WHITE)
    }

    fun drawImage(image: ResourceLocation?, x: Int, y: Int, width: Int, height: Int, alpha: Float) {
        drawImage(image, x, y, width, height, Color(255, 255, 255, (alpha * 255).toInt()))
    }

    fun drawImage(image: ResourceLocation?, x: Int, y: Int, width: Int, height: Int, color: Color) {
        glDisable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glDepthMask(false)
        OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
        GLUtils.glColor(color)
        mc.textureManager.bindTexture(image)
        drawModalRectWithCustomSizedTexture(x, y, 0f, 0f, width, height, width.toFloat(), height.toFloat())
        glDepthMask(true)
        glDisable(GL_BLEND)
        glEnable(GL_DEPTH_TEST)
    }

    /**
     * OTHERS
     */

    fun drawLine(x: Float, y: Float, x1: Float, y1: Float, width: Float) {
        glDisable(GL_TEXTURE_2D)
        glLineWidth(width)
        glBegin(GL_LINES)
        glVertex2f(x, y)
        glVertex2f(x1, y1)
        glEnd()
        glEnable(GL_TEXTURE_2D)
    }

    fun drawLine(x: Double, y: Double, x1: Double, y1: Double, width: Float) {
        glDisable(GL_TEXTURE_2D)
        glLineWidth(width)
        glBegin(GL_LINES)
        glVertex2d(x, y)
        glVertex2d(x1, y1)
        glEnd()
        glEnable(GL_TEXTURE_2D)
    }

    fun drawShadow(x: Float, y: Float, width: Float, height: Float) {
        drawTexturedRect(x - 9, y - 9, 9f, 9f, "paneltopleft")
        drawTexturedRect(x - 9, y + height, 9f, 9f, "panelbottomleft")
        drawTexturedRect(x + width, y + height, 9f, 9f, "panelbottomright")
        drawTexturedRect(x + width, y - 9, 9f, 9f, "paneltopright")
        drawTexturedRect(x - 9, y, 9f, height, "panelleft")
        drawTexturedRect(x + width, y, 9f, height, "panelright")
        drawTexturedRect(x, y - 9, width, 9f, "paneltop")
        drawTexturedRect(x, y + height, width, 9f, "panelbottom")
    }

    fun drawExhiEnchants(stack: ItemStack, x: Int, y: Int) {
        drawExhiEnchants(stack, x.toFloat(), y.toFloat())
    }

    fun drawExhiEnchants(stack: ItemStack, x: Float, y: Float) {
        var yHeight = y
        RenderHelper.disableStandardItemLighting()
        GlStateManager.disableDepth()
        GlStateManager.disableBlend()
        GlStateManager.resetColor()
        val darkBorder = -0x1000000

        if (stack.item is ItemArmor) {
            val prot = EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, stack)
            val unb = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack)
            val thorn = EnchantmentHelper.getEnchantmentLevel(Enchantment.thorns.effectId, stack)
            if (prot > 0) {
                drawExhiOutlined(prot.toString(), drawExhiOutlined("P", x, yHeight, 0.35f, darkBorder, -1, true), yHeight, 0.35f, getBorderColor(prot).rgb, getMainColor(prot), true)
                yHeight += 4f
            }
            if (unb > 0) {
                drawExhiOutlined(unb.toString(), drawExhiOutlined("U", x, yHeight, 0.35f, darkBorder, -1, true), yHeight, 0.35f, getBorderColor(unb).rgb, getMainColor(unb), true)
                yHeight += 4f
            }
            if (thorn > 0) {
                drawExhiOutlined(thorn.toString(), drawExhiOutlined("T", x, yHeight, 0.35f, darkBorder, -1, true), yHeight, 0.35f, getBorderColor(thorn).rgb, getMainColor(thorn), true)
                yHeight += 4f
            }
        }

        if (stack.item is ItemBow) {
            val power = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, stack)
            val punch = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, stack)
            val flame = EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, stack)
            val unb = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack)
            if (power > 0) {
                drawExhiOutlined(power.toString(), drawExhiOutlined("Pow", x, yHeight, 0.35f, darkBorder, -1, true), yHeight, 0.35f, getBorderColor(power).rgb, getMainColor(power), true)
                yHeight += 4f
            }
            if (punch > 0) {
                drawExhiOutlined(punch.toString(), drawExhiOutlined("Pun", x, yHeight, 0.35f, darkBorder, -1, true), yHeight, 0.35f, getBorderColor(punch).rgb, getMainColor(punch), true)
                yHeight += 4f
            }
            if (flame > 0) {
                drawExhiOutlined(flame.toString(), drawExhiOutlined("F", x, yHeight, 0.35f, darkBorder, -1, true), yHeight, 0.35f, getBorderColor(flame).rgb, getMainColor(flame), true)
                yHeight += 4f
            }
            if (unb > 0) {
                drawExhiOutlined(unb.toString(), drawExhiOutlined("U", x, yHeight, 0.35f, darkBorder, -1, true), yHeight, 0.35f, getBorderColor(unb).rgb, getMainColor(unb), true)
                yHeight += 4f
            }
        }
        
        if (stack.item is ItemSword) {
            val sharp = EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack)
            val kb = EnchantmentHelper.getEnchantmentLevel(Enchantment.knockback.effectId, stack)
            val fire = EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack)
            val unb = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack)
            if (sharp > 0) {
                drawExhiOutlined(sharp.toString(), drawExhiOutlined("S", x, yHeight, 0.35f, darkBorder, -1, true), yHeight, 0.35f, getBorderColor(sharp).rgb, getMainColor(sharp), true)
                yHeight += 4f
            }
            if (kb > 0) {
                drawExhiOutlined(kb.toString(), drawExhiOutlined("K", x, yHeight, 0.35f, darkBorder, -1, true), yHeight, 0.35f, getBorderColor(kb).rgb, getMainColor(kb), true)
                yHeight += 4f
            }
            if (fire > 0) {
                drawExhiOutlined(fire.toString(), drawExhiOutlined("F", x, yHeight, 0.35f, darkBorder, -1, true), yHeight, 0.35f, getBorderColor(fire).rgb, getMainColor(fire), true)
                yHeight += 4f
            }
            if (unb > 0) {
                drawExhiOutlined(unb.toString(), drawExhiOutlined("U", x, yHeight, 0.35f, darkBorder, -1, true), yHeight, 0.35f, getBorderColor(unb).rgb, getMainColor(unb), true)
                yHeight += 4f
            }
        }
        
        GlStateManager.enableDepth()
        RenderHelper.enableGUIStandardItemLighting()
    }

    fun drawExhiOutlined(text: String, x: Float, y: Float, borderWidth: Float, borderColor: Int, mainColor: Int, drawText: Boolean): Float {
        Fonts.fontTahomaSmall.drawString(text, x, y - borderWidth, borderColor)
        Fonts.fontTahomaSmall.drawString(text, x, y + borderWidth, borderColor)
        Fonts.fontTahomaSmall.drawString(text, x - borderWidth, y, borderColor)
        Fonts.fontTahomaSmall.drawString(text, x + borderWidth, y, borderColor)
        if (drawText)
            Fonts.fontTahomaSmall.drawString(text, x, y, mainColor)

        return x + Fonts.fontTahomaSmall.getWidth(text) - 2F;
    }

    fun getMainColor(level: Int) = if (level == 4) -0x560000 else -1

    fun getBorderColor(level: Int) = when (level) {
        2 -> Color(85, 255, 85, 112)
        3 -> Color(0, 170, 170, 112)
        4 -> Color(170, 0, 0, 112)
        else -> if (level >= 5) Color(255, 170, 0, 112) else Color(255, 255, 255, 112)
    }

    fun makeScissorBox(x: Float, y: Float, x2: Float, y2: Float) {
        val scaledResolution = ScaledResolution(mc)
        val scaledHeight = scaledResolution.scaledHeight
        val factor = scaledResolution.scaleFactor
        glScissor(
            (x * factor).toInt(),
            ((scaledHeight - y2) * factor).toInt(),
            ((x2 - x) * factor).toInt(),
            ((y2 - y) * factor).toInt()
        )
    }
}