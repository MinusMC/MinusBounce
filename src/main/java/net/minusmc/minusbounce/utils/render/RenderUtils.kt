/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.utils.render

import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.Gui.drawModalRectWithCustomSizedTexture
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.*
import net.minecraft.client.renderer.GlStateManager.disableBlend
import net.minecraft.client.renderer.GlStateManager.enableTexture2D
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.projectile.EntityEgg
import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemBow
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemSword
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.MathHelper
import net.minecraft.util.ResourceLocation
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.features.module.modules.render.TargetMark
import net.minusmc.minusbounce.ui.font.Fonts
import net.minusmc.minusbounce.utils.MinecraftInstance
import net.minusmc.minusbounce.utils.block.BlockUtils
import net.minusmc.minusbounce.utils.particles.Particle
import net.minusmc.minusbounce.utils.render.ColorUtils.getColor
import net.minusmc.minusbounce.utils.render.ColorUtils.setColour
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import kotlin.math.*


object RenderUtils : MinecraftInstance() {
    private val glCapMap: MutableMap<Int, Boolean> = HashMap()
    var deltaTime = 0
    private val DISPLAY_LISTS_2D = IntArray(4)
    private var startTime: Long = 0
    private const val animationDuration = 500

    init {
        for (i in DISPLAY_LISTS_2D.indices) {
            DISPLAY_LISTS_2D[i] = GL11.glGenLists(1)
        }
        GL11.glNewList(DISPLAY_LISTS_2D[0], GL11.GL_COMPILE)
        quickDrawRect(-7f, 2f, -4f, 3f)
        quickDrawRect(4f, 2f, 7f, 3f)
        quickDrawRect(-7f, 0.5f, -6f, 3f)
        quickDrawRect(6f, 0.5f, 7f, 3f)
        GL11.glEndList()
        GL11.glNewList(DISPLAY_LISTS_2D[1], GL11.GL_COMPILE)
        quickDrawRect(-7f, 3f, -4f, 3.3f)
        quickDrawRect(4f, 3f, 7f, 3.3f)
        quickDrawRect(-7.3f, 0.5f, -7f, 3.3f)
        quickDrawRect(7f, 0.5f, 7.3f, 3.3f)
        GL11.glEndList()
        GL11.glNewList(DISPLAY_LISTS_2D[2], GL11.GL_COMPILE)
        quickDrawRect(4f, -20f, 7f, -19f)
        quickDrawRect(-7f, -20f, -4f, -19f)
        quickDrawRect(6f, -20f, 7f, -17.5f)
        quickDrawRect(-7f, -20f, -6f, -17.5f)
        GL11.glEndList()
        GL11.glNewList(DISPLAY_LISTS_2D[3], GL11.GL_COMPILE)
        quickDrawRect(7f, -20f, 7.3f, -17.5f)
        quickDrawRect(-7.3f, -20f, -7f, -17.5f)
        quickDrawRect(4f, -20.3f, 7.3f, -20f)
        quickDrawRect(-7.3f, -20.3f, -4f, -20f)
        GL11.glEndList()
    }

    fun renderParticles(particles: List<Particle>) {
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glEnable(GL_LINE_SMOOTH)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        var i = 0
        try {
            for (particle in particles) {
                i++
                val v = particle.position
                var draw = true
                val x = v.xCoord - mc.renderManager.renderPosX
                val y = v.yCoord - mc.renderManager.renderPosY
                val z = v.zCoord - mc.renderManager.renderPosZ
                val distanceFromPlayer = mc.thePlayer.getDistance(v.xCoord, v.yCoord - 1, v.zCoord)
                var quality = (distanceFromPlayer * 4 + 10).toInt()
                if (quality > 350) quality = 350
                if (!isInViewFrustrum(EntityEgg(mc.theWorld, v.xCoord, v.yCoord, v.zCoord))) draw = false
                if (i % 10 != 0 && distanceFromPlayer > 25) draw = false
                if (i % 3 == 0 && distanceFromPlayer > 15) draw = false
                if (draw) {
                    glPushMatrix()
                    glTranslated(x, y, z)
                    val scale = 0.04f
                    glScalef(-scale, -scale, -scale)
                    glRotated((-mc.renderManager.playerViewY).toDouble(), 0.0, 1.0, 0.0)
                    glRotated(
                        mc.renderManager.playerViewX.toDouble(),
                        if (mc.gameSettings.thirdPersonView === 2) -1.0 else 1.0,
                        0.0,
                        0.0
                    )
                    val c = Color(getColor(-(1 + 5 * 1.7f), 0.7f, 1f))
                    drawFilledCircleNoGL(0, 0, 0.7, c.hashCode(), quality)
                    if (distanceFromPlayer < 4) drawFilledCircleNoGL(
                        0,
                        0,
                        1.4,
                        Color(c.red, c.green, c.blue, 50).hashCode(),
                        quality
                    )
                    if (distanceFromPlayer < 20) drawFilledCircleNoGL(
                        0,
                        0,
                        2.3,
                        Color(c.red, c.green, c.blue, 30).hashCode(),
                        quality
                    )
                    glScalef(0.8f, 0.8f, 0.8f)
                    glPopMatrix()
                }
            }
        } catch (ignored: ConcurrentModificationException) {
        }
        glDisable(GL_LINE_SMOOTH)
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glColor3d(255.0, 255.0, 255.0)
    }

    fun drawFilledCircleNoGL(x: Int, y: Int, r: Double, c: Int, quality: Int) {
        val f = (c shr 24 and 0xff) / 255f
        val f1 = (c shr 16 and 0xff) / 255f
        val f2 = (c shr 8 and 0xff) / 255f
        val f3 = (c and 0xff) / 255f
        glColor4f(f1, f2, f3, f)
        glBegin(GL_TRIANGLE_FAN)
        for (i in 0..360 / quality) {
            val x2 = Math.sin(i * quality * Math.PI / 180) * r
            val y2 = Math.cos(i * quality * Math.PI / 180) * r
            glVertex2d(x + x2, y + y2)
        }
        glEnd()
    }

    private fun quickPolygonCircle(x: Float, y: Float, xRadius: Float, yRadius: Float, start: Int, end: Int) {
        var i = end
        while (i >= start) {
            glVertex2d(x + Math.sin(i * Math.PI / 180.0) * xRadius, y + Math.cos(i * Math.PI / 180.0) * yRadius)
            i -= 4
        }
    }

    fun drawRoundedCornerRect(x: Float, y: Float, x1: Float, y1: Float, radius: Float, color: Int) {
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glDisable(GL_TEXTURE_2D)
        val hasCull = glIsEnabled(GL_CULL_FACE)
        glDisable(GL_CULL_FACE)
        glColor(color)
        drawRoundedCornerRect(x, y, x1, y1, radius)
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        setGlState(GL_CULL_FACE, hasCull)
    }

    fun drawRoundedCornerRect(x: Float, y: Float, x1: Float, y1: Float, radius: Float) {
        glBegin(GL_POLYGON)
        val xRadius = Math.min((x1 - x) * 0.5, radius.toDouble()).toFloat()
        val yRadius = Math.min((y1 - y) * 0.5, radius.toDouble()).toFloat()
        quickPolygonCircle(x + xRadius, y + yRadius, xRadius, yRadius, 180, 270)
        quickPolygonCircle(x1 - xRadius, y + yRadius, xRadius, yRadius, 90, 180)
        quickPolygonCircle(x1 - xRadius, y1 - yRadius, xRadius, yRadius, 0, 90)
        quickPolygonCircle(x + xRadius, y1 - yRadius, xRadius, yRadius, 270, 360)
        glEnd()
    }

    fun color(color: Int) = color(color, ((color shr 24 and 0xFF) / 255).toFloat())

    fun color(color: Int, alpha: Float) {
        val r = (color shr 16 and 0xFF) / 255.0f
        val g = (color shr 8 and 0xFF) / 255.0f
        val b = (color and 0xFF) / 255.0f
        GlStateManager.color(r, g, b, alpha)
    }

    private val frustrum = Frustum()
    internal var zLevel = 0f

    /**
     * Draws a textured rectangle at the stored z-value. Args: x, y, u, v, width, height
     */

    fun drawSquareTriangle(cx: Float, cy: Float, dirX: Float, dirY: Float, color: Color, filled: Boolean) {
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.resetColor()
        glColor(color)
        worldrenderer.begin(if (filled) 5 else 2, DefaultVertexFormats.POSITION)
        worldrenderer.pos((cx + dirX).toDouble(), cy.toDouble(), 0.0).endVertex()
        worldrenderer.pos(cx.toDouble(), cy.toDouble(), 0.0).endVertex()
        worldrenderer.pos(cx.toDouble(), (cy + dirY).toDouble(), 0.0).endVertex()
        worldrenderer.pos((cx + dirX).toDouble(), cy.toDouble(), 0.0).endVertex()
        tessellator.draw()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.color(1f, 1f, 1f, 1f)
    }

    fun drawTexturedModalRect(x: Int, y: Int, textureX: Int, textureY: Int, width: Int, height: Int, zLevel: Float) {
        val f = 0.00390625f
        val f1 = 0.00390625f
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX)
        worldrenderer.pos((x + 0).toDouble(), (y + height).toDouble(), zLevel.toDouble())
            .tex(((textureX + 0).toFloat() * f).toDouble(), ((textureY + height).toFloat() * f1).toDouble()).endVertex()
        worldrenderer.pos((x + width).toDouble(), (y + height).toDouble(), zLevel.toDouble())
            .tex(((textureX + width).toFloat() * f).toDouble(), ((textureY + height).toFloat() * f1).toDouble())
            .endVertex()
        worldrenderer.pos((x + width).toDouble(), (y + 0).toDouble(), zLevel.toDouble())
            .tex(((textureX + width).toFloat() * f).toDouble(), ((textureY + 0).toFloat() * f1).toDouble()).endVertex()
        worldrenderer.pos((x + 0).toDouble(), (y + 0).toDouble(), zLevel.toDouble())
            .tex(((textureX + 0).toFloat() * f).toDouble(), ((textureY + 0).toFloat() * f1).toDouble()).endVertex()
        tessellator.draw()
    }

    fun drawGradientSidewaysH(left: Double, top: Double, right: Double, bottom: Double, col1: Int, col2: Int) {
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glShadeModel(GL11.GL_SMOOTH)
        quickDrawGradientSidewaysH(left, top, right, bottom, col1, col2)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GL11.glShadeModel(GL11.GL_FLAT)
    }

    fun quickDrawGradientSidewaysH(left: Double, top: Double, right: Double, bottom: Double, col1: Int, col2: Int) {
        GL11.glBegin(GL11.GL_QUADS)
        glColor(col1)
        GL11.glVertex2d(left, top)
        GL11.glVertex2d(left, bottom)
        glColor(col2)
        GL11.glVertex2d(right, bottom)
        GL11.glVertex2d(right, top)
        GL11.glEnd()
    }

    fun drawGradientSidewaysV(left: Double, top: Double, right: Double, bottom: Double, col1: Int, col2: Int) {
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glShadeModel(GL11.GL_SMOOTH)
        quickDrawGradientSidewaysV(left, top, right, bottom, col1, col2)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GL11.glShadeModel(GL11.GL_FLAT)
    }

    fun quickDrawGradientSidewaysV(left: Double, top: Double, right: Double, bottom: Double, col1: Int, col2: Int) {
        GL11.glBegin(GL11.GL_QUADS)
        glColor(col1)
        GL11.glVertex2d(right, top)
        GL11.glVertex2d(left, top)
        glColor(col2)
        GL11.glVertex2d(left, bottom) // TODO: Fix this, this may have been a mistake
        GL11.glVertex2d(right, bottom)
        GL11.glEnd()
    }

    fun drawHead(skin: ResourceLocation?, x: Int, y: Int, width: Int, height: Int, color: Int) {
        val f3 = (color shr 24 and 255).toFloat() / 255.0f
        val f = (color shr 16 and 255).toFloat() / 255.0f
        val f1 = (color shr 8 and 255).toFloat() / 255.0f
        val f2 = (color and 255).toFloat() / 255.0f
        GlStateManager.color(f, f1, f2, f3)
        mc.textureManager.bindTexture(skin)
        RenderUtils.drawScaledCustomSizeModalRect(
            x, y, 8f, 8f, 8, 8, width, height,
            64f, 64f
        )
        RenderUtils.drawScaledCustomSizeModalRect(
            x, y, 40f, 8f, 8, 8, width, height,
            64f, 64f
        )
    }

    fun isInViewFrustrum(entity: Entity): Boolean {
        return isInViewFrustrum(entity.entityBoundingBox) || entity.ignoreFrustumCheck
    }

    private fun isInViewFrustrum(bb: AxisAlignedBB): Boolean {
        val current = mc.renderViewEntity
        frustrum.setPosition(current.posX, current.posY, current.posZ)
        return frustrum.isBoundingBoxInFrustum(bb)
    }

    fun interpolate(current: Float, old: Float, scale: Float): Float {
        return old + (current - old) * scale
    }

    fun interpolate(current: Double, old: Double, scale: Double): Double {
        return old + (current - old) * scale
    }

    fun SkyRainbow(var2: Int, st: Float, bright: Float): Int {
        var v1 = ceil((System.currentTimeMillis() + (var2 * 109).toLong()).toDouble()) / 5
        return Color.getHSBColor(if ((360.0.also { v1 %= it } / 360.0).toFloat()
                .toDouble() < 0.5) -(v1 / 360.0).toFloat() else (v1 / 360.0).toFloat(), st, bright).rgb
    }

    fun skyRainbow(var2: Int, st: Float, bright: Float): Color {
        var v1 = ceil((System.currentTimeMillis() + (var2 * 109).toLong()).toDouble()) / 5
        return Color.getHSBColor(if ((360.0.also { v1 %= it } / 360.0).toFloat()
                .toDouble() < 0.5) -(v1 / 360.0).toFloat() else (v1 / 360.0).toFloat(), st, bright)
    }

    fun getRainbowOpaque(
        seconds: Int,
        saturation: Float,
        brightness: Float,
        index: Int
    ): Int {
        val hue =
            (System.currentTimeMillis() + index) % (seconds * 1000) / (seconds * 1000).toFloat()
        return Color.HSBtoRGB(hue, saturation, brightness)
    }

    fun getNormalRainbow(delay: Int, sat: Float, brg: Float): Int {
        var rainbowState = ceil((System.currentTimeMillis() + delay) / 20.0)
        rainbowState %= 360.0
        return Color.getHSBColor((rainbowState / 360.0f).toFloat(), sat, brg).rgb
    }

    fun startSmooth() {
        GL11.glEnable(2848)
        GL11.glEnable(2881)
        GL11.glEnable(2832)
        GL11.glEnable(3042)
        GL11.glBlendFunc(770, 771)
        GL11.glHint(3154, 4354)
        GL11.glHint(3155, 4354)
        GL11.glHint(3153, 4354)
    }

    fun endSmooth() {
        GL11.glDisable(2848)
        GL11.glDisable(2881)
        GL11.glEnable(2832)
    }

    fun drawExhiRect(x: Float, y: Float, x2: Float, y2: Float) {
        drawRect(x - 3.5f, y - 3.5f, x2 + 3.5f, y2 + 3.5f, Color.black.rgb)
        drawRect(x - 3f, y - 3f, x2 + 3f, y2 + 3f, Color(50, 50, 50).rgb)
        //drawBorder(x - 1.5F, y - 1.5F, x2 + 1.5F, y2 + 1.5F, 2.5F, new Color(26, 26, 26).getRGB());
        drawRect(x - 2.5f, y - 2.5f, x2 + 2.5f, y2 + 2.5f, Color(26, 26, 26).rgb)
        drawRect(x - 0.5f, y - 0.5f, x2 + 0.5f, y2 + 0.5f, Color(50, 50, 50).rgb)
        drawRect(x, y, x2, y2, Color(18, 18, 18).rgb)
    }

    fun drawExhiRect(x: Float, y: Float, x2: Float, y2: Float, alpha: Float) {
        drawRect(x - 3.5f, y - 3.5f, x2 + 3.5f, y2 + 3.5f, Color(0f, 0f, 0f, alpha).rgb)
        drawRect(x - 3f, y - 3f, x2 + 3f, y2 + 3f, Color(50f / 255f, 50f / 255f, 50f / 255f, alpha).rgb)
        drawRect(x - 2.5f, y - 2.5f, x2 + 2.5f, y2 + 2.5f, Color(26f / 255f, 26f / 255f, 26f / 255f, alpha).rgb)
        drawRect(x - 0.5f, y - 0.5f, x2 + 0.5f, y2 + 0.5f, Color(50f / 255f, 50f / 255f, 50f / 255f, alpha).rgb)
        drawRect(x, y, x2, y2, Color(18f / 255f, 18 / 255f, 18f / 255f, alpha).rgb)
    }

    fun drawMosswareRect(
        x: Float, y: Float, x2: Float, y2: Float, width: Float,
        color1: Int, color2: Int
    ) {
        drawRect(x, y, x2, y2, color2)
        drawBorder(x, y, x2, y2, width, color1)
    }

    fun originalRoundedRect(
        paramXStart: Float,
        paramYStart: Float,
        paramXEnd: Float,
        paramYEnd: Float,
        radius: Float,
        color: Int
    ) {
        var paramXStart = paramXStart
        var paramYStart = paramYStart
        var paramXEnd = paramXEnd
        var paramYEnd = paramYEnd
        val alpha = (color shr 24 and 0xFF) / 255.0f
        val red = (color shr 16 and 0xFF) / 255.0f
        val green = (color shr 8 and 0xFF) / 255.0f
        val blue = (color and 0xFF) / 255.0f
        var z = 0f
        if (paramXStart > paramXEnd) {
            z = paramXStart
            paramXStart = paramXEnd
            paramXEnd = z
        }
        if (paramYStart > paramYEnd) {
            z = paramYStart
            paramYStart = paramYEnd
            paramYEnd = z
        }
        val x1 = (paramXStart + radius).toDouble()
        val y1 = (paramYStart + radius).toDouble()
        val x2 = (paramXEnd - radius).toDouble()
        val y2 = (paramYEnd - radius).toDouble()
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.color(red, green, blue, alpha)
        worldrenderer.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION)
        val degree = Math.PI / 180
        run {
            var i = 0.0
            while (i <= 90) {
                worldrenderer.pos(
                    x2 + sin(i * degree) * radius,
                    y2 + cos(i * degree) * radius,
                    0.0
                ).endVertex()
                i += 1.0
            }
        }
        run {
            var i = 90.0
            while (i <= 180) {
                worldrenderer.pos(
                    x2 + sin(i * degree) * radius,
                    y1 + cos(i * degree) * radius,
                    0.0
                ).endVertex()
                i += 1.0
            }
        }
        run {
            var i = 180.0
            while (i <= 270) {
                worldrenderer.pos(
                    x1 + sin(i * degree) * radius,
                    y1 + cos(i * degree) * radius,
                    0.0
                ).endVertex()
                i += 1.0
            }
        }
        var i = 270.0
        while (i <= 360) {
            worldrenderer.pos(x1 + sin(i * degree) * radius, y2 + cos(i * degree) * radius, 0.0).endVertex()
            i += 1.0
        }
        tessellator.draw()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    fun newDrawRect(left: Float, top: Float, right: Float, bottom: Float, color: Int) {
        var left = left
        var top = top
        var right = right
        var bottom = bottom
        if (left < right) {
            val i = left
            left = right
            right = i
        }
        if (top < bottom) {
            val j = top
            top = bottom
            bottom = j
        }
        val f3 = (color shr 24 and 255).toFloat() / 255.0f
        val f = (color shr 16 and 255).toFloat() / 255.0f
        val f1 = (color shr 8 and 255).toFloat() / 255.0f
        val f2 = (color and 255).toFloat() / 255.0f
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.color(f, f1, f2, f3)
        worldrenderer.begin(7, DefaultVertexFormats.POSITION)
        worldrenderer.pos(left.toDouble(), bottom.toDouble(), 0.0).endVertex()
        worldrenderer.pos(right.toDouble(), bottom.toDouble(), 0.0).endVertex()
        worldrenderer.pos(right.toDouble(), top.toDouble(), 0.0).endVertex()
        worldrenderer.pos(left.toDouble(), top.toDouble(), 0.0).endVertex()
        tessellator.draw()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    fun whatRoundedRect(
        paramXStart: Float,
        paramYStart: Float,
        paramXEnd: Float,
        paramYEnd: Float,
        color: Int,
        radius: Float
    ) {
        var paramXStart = paramXStart
        var paramYStart = paramYStart
        var paramXEnd = paramXEnd
        var paramYEnd = paramYEnd
        val alpharect = (color shr 24 and 0xFF) / 255.0f
        val redrect = (color shr 16 and 0xFF) / 255.0f
        val greenrect = (color shr 8 and 0xFF) / 255.0f
        val bluerect = (color and 0xFF) / 255.0f
        var z = 0f
        if (paramXStart > paramXEnd) {
            z = paramXStart
            paramXStart = paramXEnd
            paramXEnd = z
        }
        if (paramYStart > paramYEnd) {
            z = paramYStart
            paramYStart = paramYEnd
            paramYEnd = z
        }
        val x1 = (paramXStart + radius).toDouble()
        val y1 = (paramYStart + radius).toDouble()
        val x2 = (paramXEnd - radius).toDouble()
        val y2 = (paramYEnd - radius).toDouble()
        GL11.glPushMatrix()
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glLineWidth(1f)
        glColor(color)
        GL11.glBegin(GL11.GL_POLYGON)
        val degree = Math.PI / 180
        run {
            var i = 0.0
            while (i <= 90) {
                GL11.glVertex2d(x2 + sin(i * degree) * radius, y2 + cos(i * degree) * radius)
                i += 1.0
            }
        }
        run {
            var i = 90.0
            while (i <= 180) {
                GL11.glVertex2d(x2 + sin(i * degree) * radius, y1 + cos(i * degree) * radius)
                i += 1.0
            }
        }
        run {
            var i = 180.0
            while (i <= 270) {
                GL11.glVertex2d(x1 + sin(i * degree) * radius, y1 + cos(i * degree) * radius)
                i += 1.0
            }
        }
        var i = 270.0
        while (i <= 360) {
            GL11.glVertex2d(x1 + sin(i * degree) * radius, y2 + cos(i * degree) * radius)
            i += 1.0
        }
        GL11.glEnd()
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GL11.glPopMatrix()
    }

    fun newDrawRect(left: Double, top: Double, right: Double, bottom: Double, color: Int) {
        var left = left
        var top = top
        var right = right
        var bottom = bottom
        if (left < right) {
            val i = left
            left = right
            right = i
        }
        if (top < bottom) {
            val j = top
            top = bottom
            bottom = j
        }
        val f3 = (color shr 24 and 255).toFloat() / 255.0f
        val f = (color shr 16 and 255).toFloat() / 255.0f
        val f1 = (color shr 8 and 255).toFloat() / 255.0f
        val f2 = (color and 255).toFloat() / 255.0f
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.color(f, f1, f2, f3)
        worldrenderer.begin(7, DefaultVertexFormats.POSITION)
        worldrenderer.pos(left, bottom, 0.0).endVertex()
        worldrenderer.pos(right, bottom, 0.0).endVertex()
        worldrenderer.pos(right, top, 0.0).endVertex()
        worldrenderer.pos(left, top, 0.0).endVertex()
        tessellator.draw()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    @JvmOverloads
    fun drawRoundedRect(
        paramXStart: Float,
        paramYStart: Float,
        paramXEnd: Float,
        paramYEnd: Float,
        radius: Float,
        color: Int,
        popPush: Boolean = true
    ) {
        var paramXStart = paramXStart
        var paramYStart = paramYStart
        var paramXEnd = paramXEnd
        var paramYEnd = paramYEnd
        val alpha = (color shr 24 and 0xFF) / 255.0f
        val red = (color shr 16 and 0xFF) / 255.0f
        val green = (color shr 8 and 0xFF) / 255.0f
        val blue = (color and 0xFF) / 255.0f
        var z = 0f
        if (paramXStart > paramXEnd) {
            z = paramXStart
            paramXStart = paramXEnd
            paramXEnd = z
        }
        if (paramYStart > paramYEnd) {
            z = paramYStart
            paramYStart = paramYEnd
            paramYEnd = z
        }
        val x1 = (paramXStart + radius).toDouble()
        val y1 = (paramYStart + radius).toDouble()
        val x2 = (paramXEnd - radius).toDouble()
        val y2 = (paramYEnd - radius).toDouble()
        if (popPush) GL11.glPushMatrix()
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glLineWidth(1f)
        GL11.glColor4f(red, green, blue, alpha)
        GL11.glBegin(GL11.GL_POLYGON)
        val degree = Math.PI / 180
        run {
            var i = 0.0
            while (i <= 90) {
                GL11.glVertex2d(x2 + sin(i * degree) * radius, y2 + cos(i * degree) * radius)
                i += 1.0
            }
        }
        run {
            var i = 90.0
            while (i <= 180) {
                GL11.glVertex2d(x2 + sin(i * degree) * radius, y1 + cos(i * degree) * radius)
                i += 1.0
            }
        }
        run {
            var i = 180.0
            while (i <= 270) {
                GL11.glVertex2d(x1 + sin(i * degree) * radius, y1 + cos(i * degree) * radius)
                i += 1.0
            }
        }
        var i = 270.0
        while (i <= 360) {
            GL11.glVertex2d(x1 + sin(i * degree) * radius, y2 + cos(i * degree) * radius)
            i += 1.0
        }
        GL11.glEnd()
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        if (popPush) GL11.glPopMatrix()
    }

    fun drawScaledCustomSizeModalRect(
        x: Int,
        y: Int,
        u: Float,
        v: Float,
        uWidth: Int,
        vHeight: Int,
        width: Int,
        height: Int,
        tileWidth: Float,
        tileHeight: Float
    ) {
        val f = 1.0f / tileWidth
        val f1 = 1.0f / tileHeight
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        worldrenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX)
        worldrenderer.pos(x.toDouble(), (y + height).toDouble(), 0.0)
            .tex((u * f).toDouble(), ((v + vHeight.toFloat()) * f1).toDouble()).endVertex()
        worldrenderer.pos((x + width).toDouble(), (y + height).toDouble(), 0.0)
            .tex(((u + uWidth.toFloat()) * f).toDouble(), ((v + vHeight.toFloat()) * f1).toDouble()).endVertex()
        worldrenderer.pos((x + width).toDouble(), y.toDouble(), 0.0)
            .tex(((u + uWidth.toFloat()) * f).toDouble(), (v * f1).toDouble()).endVertex()
        worldrenderer.pos(x.toDouble(), y.toDouble(), 0.0).tex((u * f).toDouble(), (v * f1).toDouble()).endVertex()
        tessellator.draw()
    }

    // rTL = radius top left, rTR = radius top right, rBR = radius bottom right, rBL = radius bottom left
    fun customRounded(
        paramXStart: Float,
        paramYStart: Float,
        paramXEnd: Float,
        paramYEnd: Float,
        rTL: Float,
        rTR: Float,
        rBR: Float,
        rBL: Float,
        color: Int
    ) {
        var paramXStart = paramXStart
        var paramYStart = paramYStart
        var paramXEnd = paramXEnd
        var paramYEnd = paramYEnd
        val alpha = (color shr 24 and 0xFF) / 255.0f
        val red = (color shr 16 and 0xFF) / 255.0f
        val green = (color shr 8 and 0xFF) / 255.0f
        val blue = (color and 0xFF) / 255.0f
        var z = 0f
        if (paramXStart > paramXEnd) {
            z = paramXStart
            paramXStart = paramXEnd
            paramXEnd = z
        }
        if (paramYStart > paramYEnd) {
            z = paramYStart
            paramYStart = paramYEnd
            paramYEnd = z
        }
        val xTL = (paramXStart + rTL).toDouble()
        val yTL = (paramYStart + rTL).toDouble()
        val xTR = (paramXEnd - rTR).toDouble()
        val yTR = (paramYStart + rTR).toDouble()
        val xBR = (paramXEnd - rBR).toDouble()
        val yBR = (paramYEnd - rBR).toDouble()
        val xBL = (paramXStart + rBL).toDouble()
        val yBL = (paramYEnd - rBL).toDouble()
        GL11.glPushMatrix()
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glLineWidth(1f)
        GL11.glColor4f(red, green, blue, alpha)
        GL11.glBegin(GL11.GL_POLYGON)
        val degree = Math.PI / 180
        if (rBR <= 0) GL11.glVertex2d(xBR, yBR) else {
            var i = 0.0
            while (i <= 90) {
                GL11.glVertex2d(xBR + sin(i * degree) * rBR, yBR + cos(i * degree) * rBR)
                i += 1.0
            }
        }
        if (rTR <= 0) GL11.glVertex2d(xTR, yTR) else {
            var i = 90.0
            while (i <= 180) {
                GL11.glVertex2d(xTR + sin(i * degree) * rTR, yTR + cos(i * degree) * rTR)
                i += 1.0
            }
        }
        if (rTL <= 0) GL11.glVertex2d(xTL, yTL) else {
            var i = 180.0
            while (i <= 270) {
                GL11.glVertex2d(xTL + sin(i * degree) * rTL, yTL + cos(i * degree) * rTL)
                i += 1.0
            }
        }
        if (rBL <= 0) GL11.glVertex2d(xBL, yBL) else {
            var i = 270.0
            while (i <= 360) {
                GL11.glVertex2d(xBL + sin(i * degree) * rBL, yBL + cos(i * degree) * rBL)
                i += 1.0
            }
        }
        GL11.glEnd()
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GL11.glPopMatrix()
    }

    fun fastRoundedRect(paramXStart: Float, paramYStart: Float, paramXEnd: Float, paramYEnd: Float, radius: Float) {
        var paramXStart = paramXStart
        var paramYStart = paramYStart
        var paramXEnd = paramXEnd
        var paramYEnd = paramYEnd
        var z = 0f
        if (paramXStart > paramXEnd) {
            z = paramXStart
            paramXStart = paramXEnd
            paramXEnd = z
        }
        if (paramYStart > paramYEnd) {
            z = paramYStart
            paramYStart = paramYEnd
            paramYEnd = z
        }
        val x1 = (paramXStart + radius).toDouble()
        val y1 = (paramYStart + radius).toDouble()
        val x2 = (paramXEnd - radius).toDouble()
        val y2 = (paramYEnd - radius).toDouble()
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glLineWidth(1f)
        GL11.glBegin(GL11.GL_POLYGON)
        val degree = Math.PI / 180
        run {
            var i = 0.0
            while (i <= 90) {
                GL11.glVertex2d(x2 + sin(i * degree) * radius, y2 + cos(i * degree) * radius)
                i += 1.0
            }
        }
        run {
            var i = 90.0
            while (i <= 180) {
                GL11.glVertex2d(x2 + sin(i * degree) * radius, y1 + cos(i * degree) * radius)
                i += 1.0
            }
        }
        run {
            var i = 180.0
            while (i <= 270) {
                GL11.glVertex2d(x1 + sin(i * degree) * radius, y1 + cos(i * degree) * radius)
                i += 1.0
            }
        }
        var i = 270.0
        while (i <= 360) {
            GL11.glVertex2d(x1 + sin(i * degree) * radius, y2 + cos(i * degree) * radius)
            i += 1.0
        }
        GL11.glEnd()
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
    }

    fun drawTriAngle(cx: Float, cy: Float, r: Float, n: Float, color: Color, polygon: Boolean) {
        var cx = cx
        var cy = cy
        var r = r
        cx *= 2.0.toFloat()
        cy *= 2.0.toFloat()
        val b = 6.2831852 / n
        val p = cos(b)
        val s = sin(b)
        r *= 2.0.toFloat()
        var x = r.toDouble()
        var y = 0.0
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        GL11.glLineWidth(1f)
        enableGlCap(GL11.GL_LINE_SMOOTH)
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.resetColor()
        glColor(color)
        GlStateManager.scale(0.5f, 0.5f, 0.5f)
        worldrenderer.begin(if (polygon) GL11.GL_POLYGON else 2, DefaultVertexFormats.POSITION)
        var ii = 0
        while (ii < n) {
            worldrenderer.pos(x + cx, y + cy, 0.0).endVertex()
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

    fun drawGradientSideways(left: Double, top: Double, right: Double, bottom: Double, col1: Int, col2: Int) {
        val f = (col1 shr 24 and 0xFF) / 255.0f
        val f2 = (col1 shr 16 and 0xFF) / 255.0f
        val f3 = (col1 shr 8 and 0xFF) / 255.0f
        val f4 = (col1 and 0xFF) / 255.0f
        val f5 = (col2 shr 24 and 0xFF) / 255.0f
        val f6 = (col2 shr 16 and 0xFF) / 255.0f
        val f7 = (col2 shr 8 and 0xFF) / 255.0f
        val f8 = (col2 and 0xFF) / 255.0f
        GL11.glEnable(3042)
        GL11.glDisable(3553)
        GL11.glBlendFunc(770, 771)
        GL11.glEnable(2848)
        GL11.glShadeModel(7425)
        GL11.glPushMatrix()
        GL11.glBegin(7)
        GL11.glColor4f(f2, f3, f4, f)
        GL11.glVertex2d(left, top)
        GL11.glVertex2d(left, bottom)
        GL11.glColor4f(f6, f7, f8, f5)
        GL11.glVertex2d(right, bottom)
        GL11.glVertex2d(right, top)
        GL11.glEnd()
        GL11.glPopMatrix()
        GL11.glEnable(3553)
        GL11.glDisable(3042)
        GL11.glDisable(2848)
        GL11.glShadeModel(7424)
    }

    fun drawGradientRect(left: Int, top: Int, right: Int, bottom: Int, startColor: Int, endColor: Int) {
        val f = (startColor shr 24 and 255).toFloat() / 255.0f
        val f1 = (startColor shr 16 and 255).toFloat() / 255.0f
        val f2 = (startColor shr 8 and 255).toFloat() / 255.0f
        val f3 = (startColor and 255).toFloat() / 255.0f
        val f4 = (endColor shr 24 and 255).toFloat() / 255.0f
        val f5 = (endColor shr 16 and 255).toFloat() / 255.0f
        val f6 = (endColor shr 8 and 255).toFloat() / 255.0f
        val f7 = (endColor and 255).toFloat() / 255.0f
        GlStateManager.pushMatrix()
        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.disableAlpha()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.shadeModel(7425)
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR)
        worldrenderer.pos(right.toDouble(), top.toDouble(), zLevel.toDouble()).color(f1, f2, f3, f).endVertex()
        worldrenderer.pos(left.toDouble(), top.toDouble(), zLevel.toDouble()).color(f1, f2, f3, f).endVertex()
        worldrenderer.pos(left.toDouble(), bottom.toDouble(), zLevel.toDouble()).color(f5, f6, f7, f4).endVertex()
        worldrenderer.pos(right.toDouble(), bottom.toDouble(), zLevel.toDouble()).color(f5, f6, f7, f4).endVertex()
        tessellator.draw()
        GlStateManager.shadeModel(7424)
        GlStateManager.disableBlend()
        GlStateManager.enableAlpha()
        GlStateManager.enableTexture2D()
        GlStateManager.popMatrix()
    }

    fun drawGradientSideways(left: Float, top: Float, right: Float, bottom: Float, col1: Int, col2: Int) {
        val f = (col1 shr 24 and 0xFF) / 255.0f
        val f2 = (col1 shr 16 and 0xFF) / 255.0f
        val f3 = (col1 shr 8 and 0xFF) / 255.0f
        val f4 = (col1 and 0xFF) / 255.0f
        val f5 = (col2 shr 24 and 0xFF) / 255.0f
        val f6 = (col2 shr 16 and 0xFF) / 255.0f
        val f7 = (col2 shr 8 and 0xFF) / 255.0f
        val f8 = (col2 and 0xFF) / 255.0f
        GL11.glEnable(3042)
        GL11.glDisable(3553)
        GL11.glBlendFunc(770, 771)
        GL11.glEnable(2848)
        GL11.glShadeModel(7425)
        GL11.glPushMatrix()
        GL11.glBegin(7)
        GL11.glColor4f(f2, f3, f4, f)
        GL11.glVertex2f(left, top)
        GL11.glVertex2f(left, bottom)
        GL11.glColor4f(f6, f7, f8, f5)
        GL11.glVertex2f(right, bottom)
        GL11.glVertex2f(right, top)
        GL11.glEnd()
        GL11.glPopMatrix()
        GL11.glEnable(3553)
        GL11.glDisable(3042)
        GL11.glDisable(2848)
        GL11.glShadeModel(7424)
    }

    fun drawBlockBox(blockPos: BlockPos, color: Color, outline: Boolean) {
        val renderManager = mc.renderManager
        val timer = mc.timer
        val x = blockPos.x - renderManager.renderPosX
        val y = blockPos.y - renderManager.renderPosY
        val z = blockPos.z - renderManager.renderPosZ
        var axisAlignedBB = AxisAlignedBB(x, y, z, x + 1.0, y + 1, z + 1.0)
        val block = BlockUtils.getBlock(blockPos)
        if (block != null) {
            val player: EntityPlayer = mc.thePlayer
            val posX = player.lastTickPosX + (player.posX - player.lastTickPosX) * timer.renderPartialTicks.toDouble()
            val posY = player.lastTickPosY + (player.posY - player.lastTickPosY) * timer.renderPartialTicks.toDouble()
            val posZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * timer.renderPartialTicks.toDouble()
            axisAlignedBB = block.getSelectedBoundingBox(mc.theWorld, blockPos)
                .expand(0.0020000000949949026, 0.0020000000949949026, 0.0020000000949949026)
                .offset(-posX, -posY, -posZ)
        }
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        enableGlCap(GL11.GL_BLEND)
        disableGlCap(GL11.GL_TEXTURE_2D, GL11.GL_DEPTH_TEST)
        GL11.glDepthMask(false)
        glColor(color.red, color.green, color.blue, if (color.alpha != 255) color.alpha else if (outline) 26 else 35)
        drawFilledBox(axisAlignedBB)
        if (outline) {
            GL11.glLineWidth(1f)
            enableGlCap(GL11.GL_LINE_SMOOTH)
            glColor(color)
            drawSelectionBoundingBox(axisAlignedBB)
        }
        GlStateManager.resetColor()
        GL11.glDepthMask(true)
        resetCaps()
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

    fun drawTexturedRect(x: Float, y: Float, width: Float, height: Float, image: String) {
        glPushMatrix()
        val enableBlend = glIsEnabled(GL_BLEND)
        val disableAlpha = !glIsEnabled(GL_ALPHA_TEST)
        if (!enableBlend) glEnable(GL_BLEND)
        if (!disableAlpha) glDisable(GL_ALPHA_TEST)
        mc.textureManager.bindTexture(ResourceLocation("liquidbounce+/ui/$image.png"))
        GlStateManager.color(1f, 1f, 1f, 1f)
        drawModalRectWithCustomSizedTexture(x.toInt(), y.toInt(), 0f, 0f, width.toInt(), height.toInt(), width, height)
        if (!enableBlend) glDisable(GL_BLEND)
        if (!disableAlpha) glEnable(GL_ALPHA_TEST)
        glPopMatrix()
    }

    fun drawSelectionBoundingBox(boundingBox: AxisAlignedBB) {
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        worldrenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION)

        // Lower Rectangle
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex()
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex()
        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex()
        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex()
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex()

        // Upper Rectangle
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex()
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex()
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex()

        // Upper Rectangle
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex()
        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex()
        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex()
        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex()
        tessellator.draw()
    }

    fun drawEntityBox(entity: Entity, color: Color, outline: Boolean) {
        val renderManager = mc.renderManager
        val timer = mc.timer
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        enableGlCap(GL11.GL_BLEND)
        disableGlCap(GL11.GL_TEXTURE_2D, GL11.GL_DEPTH_TEST)
        GL11.glDepthMask(false)
        val x = (entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * timer.renderPartialTicks
                - renderManager.renderPosX)
        val y = (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * timer.renderPartialTicks
                - renderManager.renderPosY)
        val z = (entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * timer.renderPartialTicks
                - renderManager.renderPosZ)
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
            GL11.glLineWidth(1f)
            enableGlCap(GL11.GL_LINE_SMOOTH)
            glColor(color.red, color.green, color.blue, 95)
            drawSelectionBoundingBox(axisAlignedBB)
        }
        glColor(color.red, color.green, color.blue, if (outline) 26 else 35)
        drawFilledBox(axisAlignedBB)
        GlStateManager.resetColor()
        GL11.glDepthMask(true)
        resetCaps()
    }

    fun drawAxisAlignedBB(axisAlignedBB: AxisAlignedBB, color: Color) {
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glLineWidth(2f)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glDepthMask(false)
        glColor(color)
        drawFilledBox(axisAlignedBB)
        GlStateManager.resetColor()
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glDepthMask(true)
        GL11.glDisable(GL11.GL_BLEND)
    }

    fun drawPlatform(y: Double, color: Color, size: Double) {
        val renderManager = mc.renderManager
        val renderY = y - renderManager.renderPosY
        drawAxisAlignedBB(AxisAlignedBB(size, renderY + 0.02, size, -size, renderY, -size), color)
    }

    fun drawPlatform(entity: Entity, color: Color) {
        val renderManager = mc.renderManager
        val timer = mc.timer
        val targetMark = MinusBounce.moduleManager.getModule(TargetMark::class.java) ?: return
        val x = (entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * timer.renderPartialTicks
                - renderManager.renderPosX)
        val y = (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * timer.renderPartialTicks
                - renderManager.renderPosY)
        val z = (entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * timer.renderPartialTicks
                - renderManager.renderPosZ)
        val axisAlignedBB = entity.entityBoundingBox
            .offset(-entity.posX, -entity.posY, -entity.posZ)
            .offset(x, y - targetMark.moveMarkValue.get(), z)
        drawAxisAlignedBB(
            AxisAlignedBB(
                axisAlignedBB.minX,
                axisAlignedBB.maxY + 0.2,
                axisAlignedBB.minZ,
                axisAlignedBB.maxX,
                axisAlignedBB.maxY + 0.26,
                axisAlignedBB.maxZ
            ),
            color
        )
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
        val rendermanager = mc.renderManager
        rendermanager.setPlayerViewY(180f)
        rendermanager.isRenderShadow = false
        rendermanager.renderEntityWithPosYaw(entity, 0.0, 0.0, 0.0, 0f, 1f)
        rendermanager.isRenderShadow = true
        GlStateManager.popMatrix()
        RenderHelper.disableStandardItemLighting()
        GlStateManager.disableRescaleNormal()
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit)
        GlStateManager.disableTexture2D()
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit)
    }

    fun drawEntityOnScreen(posX: Int, posY: Int, scale: Int, entity: EntityLivingBase?) {
        drawEntityOnScreen(posX.toDouble(), posY.toDouble(), scale.toFloat(), entity)
    }

    fun quickDrawRect(x: Float, y: Float, x2: Float, y2: Float) {
        GL11.glBegin(GL11.GL_QUADS)
        GL11.glVertex2d(x2.toDouble(), y.toDouble())
        GL11.glVertex2d(x.toDouble(), y.toDouble())
        GL11.glVertex2d(x.toDouble(), y2.toDouble())
        GL11.glVertex2d(x2.toDouble(), y2.toDouble())
        GL11.glEnd()
    }

    fun drawRect(x: Int, y: Int, x2: Int, y2: Int, color: Int) {
        drawRect(x.toFloat(), y.toFloat(), x2.toFloat(), y2.toFloat(), color)
    }

    fun drawRect(x: Float, y: Float, x2: Float, y2: Float, color: Int) {
        glPushMatrix()
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glColor(color)
        glBegin(GL_QUADS)
        glVertex2d(x2.toDouble(), y.toDouble())
        glVertex2d(x.toDouble(), y.toDouble())
        glVertex2d(x.toDouble(), y2.toDouble())
        glVertex2d(x2.toDouble(), y2.toDouble())
        glEnd()
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        glPopMatrix()
    }

    fun drawRect(left: Double, top: Double, right: Double, bottom: Double, color: Int) {
        var left = left
        var top = top
        var right = right
        var bottom = bottom
        if (left < right) {
            val i = left
            left = right
            right = i
        }
        if (top < bottom) {
            val j = top
            top = bottom
            bottom = j
        }
        val f3 = (color shr 24 and 255).toFloat() / 255.0f
        val f = (color shr 16 and 255).toFloat() / 255.0f
        val f1 = (color shr 8 and 255).toFloat() / 255.0f
        val f2 = (color and 255).toFloat() / 255.0f
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.color(f, f1, f2, f3)
        worldrenderer.begin(7, DefaultVertexFormats.POSITION)
        worldrenderer.pos(left, bottom, 0.0).endVertex()
        worldrenderer.pos(right, bottom, 0.0).endVertex()
        worldrenderer.pos(right, top, 0.0).endVertex()
        worldrenderer.pos(left, top, 0.0).endVertex()
        tessellator.draw()
        enableTexture2D()
        disableBlend()
    }

    fun drawRect(rect: net.minusmc.minusbounce.utils.geom.Rectangle, color: Int) {
        drawRect(rect.x, rect.y, rect.x + rect.width, rect.y + rect.height, color)
    }

    fun drawRect(rect: net.minusmc.minusbounce.utils.geom.Rectangle, color: Color) {
        drawRect(rect.x, rect.y, rect.x + rect.width, rect.y + rect.height, color.rgb)
    }

    /**
     * Like [.drawRect], but without setup
     */
    fun quickDrawRect(x: Float, y: Float, x2: Float, y2: Float, color: Int) {
        glColor(color)
        GL11.glBegin(GL11.GL_QUADS)
        GL11.glVertex2d(x2.toDouble(), y.toDouble())
        GL11.glVertex2d(x.toDouble(), y.toDouble())
        GL11.glVertex2d(x.toDouble(), y2.toDouble())
        GL11.glVertex2d(x2.toDouble(), y2.toDouble())
        GL11.glEnd()
    }

    fun drawRect(x: Float, y: Float, x2: Float, y2: Float, color: Color) {
        drawRect(x, y, x2, y2, color.rgb)
    }

    fun drawBorderedRect(
        x: Float, y: Float, x2: Float, y2: Float, width: Float,
        color1: Int, color2: Int
    ) {
        drawRect(x, y, x2, y2, color2)
        drawBorder(x, y, x2, y2, width, color1)
    }

    fun drawBorder(x: Float, y: Float, x2: Float, y2: Float, width: Float, color1: Int) {
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        glColor(color1)
        GL11.glLineWidth(width)
        GL11.glBegin(GL11.GL_LINE_LOOP)
        GL11.glVertex2d(x2.toDouble(), y.toDouble())
        GL11.glVertex2d(x.toDouble(), y.toDouble())
        GL11.glVertex2d(x.toDouble(), y2.toDouble())
        GL11.glVertex2d(x2.toDouble(), y2.toDouble())
        GL11.glEnd()
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
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
        glColor(color1)
        GL11.glLineWidth(width)
        GL11.glBegin(GL11.GL_LINE_LOOP)
        GL11.glVertex2d(x2.toDouble(), y.toDouble())
        GL11.glVertex2d(x.toDouble(), y.toDouble())
        GL11.glVertex2d(x.toDouble(), y2.toDouble())
        GL11.glVertex2d(x2.toDouble(), y2.toDouble())
        GL11.glEnd()
    }

    fun drawLoadingCircle(x: Float, y: Float) {
        for (i in 0..3) {
            val rot = (System.nanoTime() / 5000000 * i % 360).toInt()
            drawCircle(x, y, (i * 10).toFloat(), rot - 180, rot)
        }
    }

    fun drawCircle(x: Float, y: Float, radius: Float, lineWidth: Float, start: Int, end: Int, color: Color) {
        glColor(color)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
        glColor(color)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glLineWidth(lineWidth)
        GL11.glBegin(GL11.GL_LINE_STRIP)
        var i = end.toFloat()
        while (i >= start) {
            GL11.glVertex2f(
                (x + cos(i * Math.PI / 180) * (radius * 1.001f)).toFloat(),
                (y + sin(i * Math.PI / 180) * (radius * 1.001f)).toFloat()
            )
            i -= 360 / 90.0f
        }
        GL11.glEnd()
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    fun drawCircle(x: Float, y: Float, radius: Float, lineWidth: Float, start: Int, end: Int) {
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
        glColor(Color.WHITE)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glLineWidth(lineWidth)
        GL11.glBegin(GL11.GL_LINE_STRIP)
        var i = end.toFloat()
        while (i >= start) {
            GL11.glVertex2f(
                (x + cos(i * Math.PI / 180) * (radius * 1.001f)).toFloat(),
                (y + sin(i * Math.PI / 180) * (radius * 1.001f)).toFloat()
            )
            i -= 360 / 90.0f
        }
        GL11.glEnd()
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    fun drawCircle(x: Float, y: Float, radius: Float, start: Int, end: Int) {
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
        glColor(Color.WHITE)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glLineWidth(2f)
        GL11.glBegin(GL11.GL_LINE_STRIP)
        var i = end.toFloat()
        while (i >= start) {
            GL11.glVertex2f(
                (x + cos(i * Math.PI / 180) * (radius * 1.001f)).toFloat(),
                (y + sin(i * Math.PI / 180) * (radius * 1.001f)).toFloat()
            )
            i -= 360 / 90.0f
        }
        GL11.glEnd()
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    fun drawGradientCircle(x: Float, y: Float, radius: Float, start: Int, end: Int, color1: Color, color2: Color) {
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glLineWidth(2f)
        GL11.glBegin(GL11.GL_LINE_STRIP)
        var i = end.toFloat()
        while (i >= start) {
            var c = ColorUtils.getGradientOffset(color1, color2, 1.0, (abs(System.currentTimeMillis() / 360.0 + (i * 34 / 360) * 56 / 100) / 10).toInt()).rgb
            val f2 = (c shr 24 and 255).toFloat() / 255.0f
            val f22 = (c shr 16 and 255).toFloat() / 255.0f
            val f3 = (c shr 8 and 255).toFloat() / 255.0f
            val f4 = (c and 255).toFloat() / 255.0f
            GlStateManager.color(f22, f3, f4, f2)
            GL11.glVertex2f(
                (x + Math.cos(i * Math.PI / 180) * (radius * 1.001f)).toFloat(),
                (y + Math.sin(i * Math.PI / 180) * (radius * 1.001f)).toFloat()
            )
            i -= 360f / 90.0f
        }
        GL11.glEnd()
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    fun drawFilledCircle(xx: Int, yy: Int, radius: Float, color: Color) {
        val sections = 50
        val dAngle = 2 * Math.PI / sections
        var x: Float
        var y: Float
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glBegin(GL11.GL_TRIANGLE_FAN)
        for (i in 0 until sections) {
            x = (radius * sin(i * dAngle)).toFloat()
            y = (radius * cos(i * dAngle)).toFloat()
            GL11.glColor4f(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
            GL11.glVertex2f(xx + x, yy + y)
        }
        GlStateManager.color(0f, 0f, 0f)
        GL11.glEnd()
        GL11.glPopAttrib()
    }

    fun drawFilledCircle(xx: Float, yy: Float, radius: Float, color: Color) {
        val sections = 50
        val dAngle = 2 * Math.PI / sections
        var x: Float
        var y: Float
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glBegin(GL11.GL_TRIANGLE_FAN)
        for (i in 0 until sections) {
            x = (radius * sin(i * dAngle)).toFloat()
            y = (radius * cos(i * dAngle)).toFloat()
            GL11.glColor4f(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
            GL11.glVertex2f(xx + x, yy + y)
        }
        GlStateManager.color(0f, 0f, 0f)
        GL11.glEnd()
        GL11.glPopAttrib()
    }

    fun drawImage(image: ResourceLocation?, x: Int, y: Int, width: Int, height: Int) {
        glDisable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glDepthMask(false)
        OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        mc.textureManager.bindTexture(image)
        drawModalRectWithCustomSizedTexture(x.toInt(),
            y.toInt(), 0f, 0f, width.toInt(), height.toInt(), width.toFloat(), height.toFloat())
        glDepthMask(true)
        glDisable(GL_BLEND)
        glEnable(GL_DEPTH_TEST)
    }

    fun drawImage(image: ResourceLocation?, x: Int, y: Int, width: Int, height: Int, alpha: Float) {
        glDisable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glDepthMask(false)
        OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
        glColor4f(1.0f, 1.0f, 1.0f, alpha)
        mc.textureManager.bindTexture(image)
        drawModalRectWithCustomSizedTexture(x, y, 0f, 0f, width, height, width.toFloat(), height.toFloat())
        glDepthMask(true)
        glDisable(GL_BLEND)
        glEnable(GL_DEPTH_TEST)
    }

    fun drawImagee(image: ResourceLocation?, x: Double, y: Double, width: Double, height: Double) {
        glDisable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glDepthMask(false)
        OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        mc.textureManager.bindTexture(image)
        drawModalRectWithCustomSizedTexture(x.toInt(),
            y.toInt(), 0f, 0f, width.toInt(), height.toInt(), width.toFloat(), height.toFloat())
        glDepthMask(true)
        glDisable(GL_BLEND)
        glEnable(GL_DEPTH_TEST)
    }

    fun drawImagee(image: ResourceLocation?, x: Int, y: Int, width: Int, height: Int, alpha: Float) {
        glDisable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glDepthMask(false)
        OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
        glColor4f(1.0f, 1.0f, 1.0f, alpha)
        mc.textureManager.bindTexture(image)
        drawModalRectWithCustomSizedTexture(x, y, 0f, 0f, width, height, width.toFloat(), height.toFloat())
        glDepthMask(true)
        glDisable(GL_BLEND)
        glEnable(GL_DEPTH_TEST)
    }

    fun drawImage2(image: ResourceLocation?, x: Float, y: Float, width: Int, height: Int) {
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glDepthMask(false)
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        GL11.glTranslatef(x, y, x)
        mc.textureManager.bindTexture(image)
        Gui.drawModalRectWithCustomSizedTexture(0, 0, 0f, 0f, width, height, width.toFloat(), height.toFloat())
        GL11.glTranslatef(-x, -y, -x)
        GL11.glDepthMask(true)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
    }

    fun drawImage3(
        image: ResourceLocation?,
        x: Float,
        y: Float,
        width: Int,
        height: Int,
        r: Float,
        g: Float,
        b: Float,
        al: Float
    ) {
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glDepthMask(false)
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
        GL11.glColor4f(r, g, b, al)
        GL11.glTranslatef(x, y, x)
        mc.textureManager.bindTexture(image)
        Gui.drawModalRectWithCustomSizedTexture(0, 0, 0f, 0f, width, height, width.toFloat(), height.toFloat())
        GL11.glTranslatef(-x, -y, -x)
        GL11.glDepthMask(true)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
    }

    fun drawExhiEnchants(stack: ItemStack, x: Float, y: Float) {
        var y = y
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
                drawExhiOutlined(
                    prot.toString() + "",
                    drawExhiOutlined("P", x, y, 0.35f, darkBorder, -1, true),
                    y,
                    0.35f,
                    getBorderColor(prot),
                    getMainColor(prot),
                    true
                )
                y += 4f
            }
            if (unb > 0) {
                drawExhiOutlined(
                    unb.toString() + "",
                    drawExhiOutlined("U", x, y, 0.35f, darkBorder, -1, true),
                    y,
                    0.35f,
                    getBorderColor(unb),
                    getMainColor(unb),
                    true
                )
                y += 4f
            }
            if (thorn > 0) {
                drawExhiOutlined(
                    thorn.toString() + "",
                    drawExhiOutlined("T", x, y, 0.35f, darkBorder, -1, true),
                    y,
                    0.35f,
                    getBorderColor(thorn),
                    getMainColor(thorn),
                    true
                )
                y += 4f
            }
        }
        if (stack.item is ItemBow) {
            val power = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, stack)
            val punch = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, stack)
            val flame = EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, stack)
            val unb = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack)
            if (power > 0) {
                drawExhiOutlined(
                    power.toString() + "",
                    drawExhiOutlined("Pow", x, y, 0.35f, darkBorder, -1, true),
                    y,
                    0.35f,
                    getBorderColor(power),
                    getMainColor(power),
                    true
                )
                y += 4f
            }
            if (punch > 0) {
                drawExhiOutlined(
                    punch.toString() + "",
                    drawExhiOutlined("Pun", x, y, 0.35f, darkBorder, -1, true),
                    y,
                    0.35f,
                    getBorderColor(punch),
                    getMainColor(punch),
                    true
                )
                y += 4f
            }
            if (flame > 0) {
                drawExhiOutlined(
                    flame.toString() + "",
                    drawExhiOutlined("F", x, y, 0.35f, darkBorder, -1, true),
                    y,
                    0.35f,
                    getBorderColor(flame),
                    getMainColor(flame),
                    true
                )
                y += 4f
            }
            if (unb > 0) {
                drawExhiOutlined(
                    unb.toString() + "",
                    drawExhiOutlined("U", x, y, 0.35f, darkBorder, -1, true),
                    y,
                    0.35f,
                    getBorderColor(unb),
                    getMainColor(unb),
                    true
                )
                y += 4f
            }
        }
        if (stack.item is ItemSword) {
            val sharp = EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack)
            val kb = EnchantmentHelper.getEnchantmentLevel(Enchantment.knockback.effectId, stack)
            val fire = EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack)
            val unb = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack)
            if (sharp > 0) {
                drawExhiOutlined(
                    sharp.toString() + "",
                    drawExhiOutlined("S", x, y, 0.35f, darkBorder, -1, true),
                    y,
                    0.35f,
                    getBorderColor(sharp),
                    getMainColor(sharp),
                    true
                )
                y += 4f
            }
            if (kb > 0) {
                drawExhiOutlined(
                    kb.toString() + "",
                    drawExhiOutlined("K", x, y, 0.35f, darkBorder, -1, true),
                    y,
                    0.35f,
                    getBorderColor(kb),
                    getMainColor(kb),
                    true
                )
                y += 4f
            }
            if (fire > 0) {
                drawExhiOutlined(
                    fire.toString() + "",
                    drawExhiOutlined("F", x, y, 0.35f, darkBorder, -1, true),
                    y,
                    0.35f,
                    getBorderColor(fire),
                    getMainColor(fire),
                    true
                )
                y += 4f
            }
            if (unb > 0) {
                drawExhiOutlined(
                    unb.toString() + "",
                    drawExhiOutlined("U", x, y, 0.35f, darkBorder, -1, true),
                    y,
                    0.35f,
                    getBorderColor(unb),
                    getMainColor(unb),
                    true
                )
                y += 4f
            }
        }
        GlStateManager.enableDepth()
        RenderHelper.enableGUIStandardItemLighting()
    }

    private fun drawExhiOutlined(
        text: String,
        x: Float,
        y: Float,
        borderWidth: Float,
        borderColor: Int,
        mainColor: Int,
        drawText: Boolean
    ): Float {
        Fonts.fontTahomaSmall!!.drawString(text, x, y - borderWidth, borderColor)
        Fonts.fontTahomaSmall!!.drawString(text, x, y + borderWidth, borderColor)
        Fonts.fontTahomaSmall!!.drawString(text, x - borderWidth, y, borderColor)
        Fonts.fontTahomaSmall!!.drawString(text, x + borderWidth, y, borderColor)
        if (drawText) Fonts.fontTahomaSmall!!.drawString(text, x, y, mainColor)
        return x + Fonts.fontTahomaSmall!!.getWidth(text) - 2f
    }

    private fun getMainColor(level: Int): Int {
        return if (level == 4) -0x560000 else -1
    }

    private fun getBorderColor(level: Int): Int {
        if (level == 2) return 0x7055FF55
        if (level == 3) return 0x7000AAAA
        if (level == 4) return 0x70AA0000
        return if (level >= 5) 0x70FFAA00 else 0x70FFFFFF
    }

    fun glColor(red: Int, green: Int, blue: Int, alpha: Int) {
        GlStateManager.color(red / 255f, green / 255f, blue / 255f, alpha / 255f)
    }

    fun glColor(color: Color) {
        val red = color.red / 255f
        val green = color.green / 255f
        val blue = color.blue / 255f
        val alpha = color.alpha / 255f
        GlStateManager.color(red, green, blue, alpha)
    }

    fun glColor(color: Color, alpha: Float) {
        val red = color.red / 255f
        val green = color.green / 255f
        val blue = color.blue / 255f
        GlStateManager.color(red, green, blue, alpha / 255f)
    }

    fun glColor(color: Int) {
        val alpha = (color shr 24 and 0xFF) / 255f
        val red = (color shr 16 and 0xFF) / 255f
        val green = (color shr 8 and 0xFF) / 255f
        val blue = (color and 0xFF) / 255f
        GlStateManager.color(red, green, blue, alpha)
    }

    fun draw2D(entity: EntityLivingBase, posX: Double, posY: Double, posZ: Double, color: Int, backgroundColor: Int) {
        GlStateManager.pushMatrix()
        GlStateManager.translate(posX, posY, posZ)
        GlStateManager.rotate(-mc.renderManager.playerViewY, 0f, 1f, 0f)
        GlStateManager.scale(-0.1, -0.1, 0.1)
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GlStateManager.depthMask(true)
        glColor(color)
        GL11.glCallList(DISPLAY_LISTS_2D[0])
        glColor(backgroundColor)
        GL11.glCallList(DISPLAY_LISTS_2D[1])
        GlStateManager.translate(0.0, 21 + -(entity.entityBoundingBox.maxY - entity.entityBoundingBox.minY) * 12, 0.0)
        glColor(color)
        GL11.glCallList(DISPLAY_LISTS_2D[2])
        glColor(backgroundColor)
        GL11.glCallList(DISPLAY_LISTS_2D[3])

        // Stop render
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_BLEND)
        GlStateManager.popMatrix()
    }

    fun draw2D(blockPos: BlockPos, color: Int, backgroundColor: Int) {
        val renderManager = mc.renderManager
        val posX = blockPos.x + 0.5 - renderManager.renderPosX
        val posY = blockPos.y - renderManager.renderPosY
        val posZ = blockPos.z + 0.5 - renderManager.renderPosZ
        GlStateManager.pushMatrix()
        GlStateManager.translate(posX, posY, posZ)
        GlStateManager.rotate(-mc.renderManager.playerViewY, 0f, 1f, 0f)
        GlStateManager.scale(-0.1, -0.1, 0.1)
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GlStateManager.depthMask(true)
        glColor(color)
        GL11.glCallList(DISPLAY_LISTS_2D[0])
        glColor(backgroundColor)
        GL11.glCallList(DISPLAY_LISTS_2D[1])
        GlStateManager.translate(0f, 9f, 0f)
        glColor(color)
        GL11.glCallList(DISPLAY_LISTS_2D[2])
        glColor(backgroundColor)
        GL11.glCallList(DISPLAY_LISTS_2D[3])

        // Stop render
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_BLEND)
        GlStateManager.popMatrix()
    }

    fun renderNameTag(string: String?, x: Double, y: Double, z: Double) {
        val renderManager = mc.renderManager
        GL11.glPushMatrix()
        GL11.glTranslated(x - renderManager.renderPosX, y - renderManager.renderPosY, z - renderManager.renderPosZ)
        GL11.glNormal3f(0f, 1f, 0f)
        GL11.glRotatef(-mc.renderManager.playerViewY, 0f, 1f, 0f)
        GL11.glRotatef(mc.renderManager.playerViewX, 1f, 0f, 0f)
        GL11.glScalef(-0.05f, -0.05f, 0.05f)
        setGlCap(GL11.GL_LIGHTING, false)
        setGlCap(GL11.GL_DEPTH_TEST, false)
        setGlCap(GL11.GL_BLEND, true)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        val width = Fonts.font35!!.getStringWidth(string!!) / 2
        Gui.drawRect(-width - 1, -1, width + 1, Fonts.font35!!.FONT_HEIGHT, Int.MIN_VALUE)
        Fonts.font35!!.drawString(string, -width.toFloat(), 1.5f, Color.WHITE.rgb, true)
        resetCaps()
        GL11.glColor4f(1f, 1f, 1f, 1f)
        GL11.glPopMatrix()
    }

    fun drawLine(x: Float, y: Float, x1: Float, y1: Float, width: Float) {
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glLineWidth(width)
        GL11.glBegin(GL11.GL_LINES)
        GL11.glVertex2f(x, y)
        GL11.glVertex2f(x1, y1)
        GL11.glEnd()
        GL11.glEnable(GL11.GL_TEXTURE_2D)
    }

    fun drawLimitedCircle(lx: Float, ly: Float, x2: Float, y2: Float, xx: Int, yy: Int, radius: Float, color: Color) {
        val sections = 50
        val dAngle = 2 * Math.PI / sections
        var x: Float
        var y: Float
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glBegin(GL11.GL_TRIANGLE_FAN)
        for (i in 0 until sections) {
            x = (radius * sin(i * dAngle)).toFloat()
            y = (radius * cos(i * dAngle)).toFloat()
            GL11.glColor4f(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
            GL11.glVertex2f(
                min(x2.toDouble(), max((xx + x).toDouble(), lx.toDouble())).toFloat(),
                min(y2.toDouble(), max((yy + y).toDouble(), ly.toDouble())).toFloat()
            )
        }
        GlStateManager.color(0f, 0f, 0f)
        GL11.glEnd()
        GL11.glPopAttrib()
    }

    fun drawLine(x: Double, y: Double, x1: Double, y1: Double, width: Float) {
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glLineWidth(width)
        GL11.glBegin(GL11.GL_LINES)
        GL11.glVertex2d(x, y)
        GL11.glVertex2d(x1, y1)
        GL11.glEnd()
        GL11.glEnable(GL11.GL_TEXTURE_2D)
    }

    fun makeScissorBox(x: Float, y: Float, x2: Float, y2: Float) {
        val scaledResolution = ScaledResolution(mc)
        val factor = scaledResolution.scaleFactor
        GL11.glScissor(
            (x * factor).toInt(),
            ((scaledResolution.scaledHeight - y2) * factor).toInt(),
            ((x2 - x) * factor).toInt(),
            ((y2 - y) * factor).toInt()
        )
    }
    fun otherDrawOutlinedBoundingBox(yaw: Float, x: Double, y: Double, z: Double, width: Double, height: Double) {
        var width = width * 1.5
        var yaw = (MathHelper.wrapAngleTo180_float(yaw) + 45.0).toFloat()

        var yaw1: Float
        var yaw2: Float
        var yaw3: Float
        var yaw4: Float

        if (yaw < 0.0) {
            yaw1 = 360.0F - abs(yaw)
        } else {
            yaw1 = yaw.toFloat()
        }
        yaw1 *= -1.0F
        yaw1 = (yaw1 * 0.017453292519943295).toFloat()
        yaw += 90.0F

        if (yaw < 0.0) {
            yaw2 = 0.0F
            yaw2 += 360.0F - Math.abs(yaw)
        } else {
            yaw2 = yaw.toFloat()
        }
        yaw2 *= -1.0F
        yaw2 = (yaw2 * 0.017453292519943295).toFloat()

        yaw += 90.0F

        if (yaw < 0.0) {
            yaw3 = 0.0F
            yaw3 += 360.0F - Math.abs(yaw)
        } else {
            yaw3 = yaw.toFloat()
        }

        yaw3 *= -1.0F
        yaw3 = (yaw3 * 0.017453292519943295).toFloat()

        yaw += 90.0F

        if (yaw < 0.0) {
            yaw4 = 0.0F
            yaw4 += 360.0F - Math.abs(yaw)
        } else {
            yaw4 = yaw.toFloat()
        }
        yaw4 *= -1.0F
        yaw4 = (yaw4 * 0.017453292519943295).toFloat()

        val x1 = (sin(yaw1) * width + x).toFloat()
        val z1 = (cos(yaw1) * width + z).toFloat()
        val x2 = (sin(yaw2) * width + x).toFloat()
        val z2 = (cos(yaw2) * width + z).toFloat()
        val x3 = (sin(yaw3) * width + x).toFloat()
        val z3 = (cos(yaw3) * width + z).toFloat()
        val x4 = (sin(yaw4) * width + x).toFloat()
        val z4 = (cos(yaw4) * width + z).toFloat()
        val y2 = (y + height).toFloat()

        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        worldrenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION)
        worldrenderer.pos(x1.toDouble(), y, z1.toDouble()).endVertex()
        worldrenderer.pos(x1.toDouble(), y2.toDouble(), z1.toDouble()).endVertex()
        worldrenderer.pos(x2.toDouble(), y2.toDouble(), z2.toDouble()).endVertex()
        worldrenderer.pos(x2.toDouble(), y, z2.toDouble()).endVertex()
        worldrenderer.pos(x1.toDouble(), y, z1.toDouble()).endVertex()
        worldrenderer.pos(x4.toDouble(), y, z4.toDouble()).endVertex()
        worldrenderer.pos(x3.toDouble(), y, z3.toDouble()).endVertex()
        worldrenderer.pos(x3.toDouble(), y2.toDouble(), z3.toDouble()).endVertex()
        worldrenderer.pos(x4.toDouble(), y2.toDouble(), z4.toDouble()).endVertex()
        worldrenderer.pos(x4.toDouble(), y, z4.toDouble()).endVertex()
        worldrenderer.pos(x4.toDouble(), y2.toDouble(), z4.toDouble()).endVertex()
        worldrenderer.pos(x3.toDouble(), y2.toDouble(), z3.toDouble()).endVertex()
        worldrenderer.pos(x2.toDouble(), y2.toDouble(), z2.toDouble()).endVertex()
        worldrenderer.pos(x2.toDouble(), y, z2.toDouble()).endVertex()
        worldrenderer.pos(x3.toDouble(), y, z3.toDouble()).endVertex()
        worldrenderer.pos(x4.toDouble(), y, z4.toDouble()).endVertex()
        worldrenderer.pos(x4.toDouble(), y2.toDouble(), z4.toDouble()).endVertex()
        worldrenderer.pos(x1.toDouble(), y2.toDouble(), z1.toDouble()).endVertex()
        worldrenderer.pos(x1.toDouble(), y, z1.toDouble()).endVertex()
        tessellator.draw()
    }
    fun otherDrawBoundingBox(yaw: Float, x: Double, y: Double, z: Double, width: Double, height: Double) {
        var width = width * 1.5
        var yaw = MathHelper.wrapAngleTo180_float(yaw) + 45.0f
        var yaw1: Float
        var yaw2: Float
        var yaw3: Float
        var yaw4: Float

        if (yaw < 0.0f) {
            yaw1 = 0.0f
            yaw1 += 360.0f - abs(yaw)
        } else {
            yaw1 = yaw
        }

        yaw1 *= -1.0f
        yaw1 = (yaw1 * (PI / 180.0)).toFloat()

        yaw += 90.0f

        if (yaw < 0.0f) {
            yaw2 = 0.0f
            yaw2 += 360.0f - abs(yaw)
        } else {
            yaw2 = yaw
        }

        yaw2 *= -1.0f
        yaw2 = (yaw2 * (PI / 180.0)).toFloat()

        yaw += 90.0f

        if (yaw < 0.0f) {
            yaw3 = 0.0f
            yaw3 += 360.0f - abs(yaw)
        } else {
            yaw3 = yaw
        }

        yaw3 *= -1.0f
        yaw3 = (yaw3 * (PI / 180.0)).toFloat()

        yaw += 90.0f

        if (yaw < 0.0f) {
            yaw4 = 0.0f
            yaw4 += 360.0f - abs(yaw)
        } else {
            yaw4 = yaw
        }

        yaw4 *= -1.0f
        yaw4 = (yaw4 * (PI / 180.0)).toFloat()

        val x1 = (sin(yaw1) * width + x).toFloat()
        val z1 = (cos(yaw1) * width + z).toFloat()
        val x2 = (sin(yaw2) * width + x).toFloat()
        val z2 = (cos(yaw2) * width + z).toFloat()
        val x3 = (sin(yaw3) * width + x).toFloat()
        val z3 = (cos(yaw3) * width + z).toFloat()
        val x4 = (sin(yaw4) * width + x).toFloat()
        val z4 = (cos(yaw4) * width + z).toFloat()

        val y1 = y.toFloat()
        val y2 = (y + height).toFloat()

        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.getWorldRenderer()
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
        worldRenderer.pos(x1.toDouble(), y1.toDouble(), z1.toDouble()).endVertex()
        worldRenderer.pos(x1.toDouble(), y2.toDouble(), z1.toDouble()).endVertex()
        worldRenderer.pos(x2.toDouble(), y2.toDouble(), z2.toDouble()).endVertex()
        worldRenderer.pos(x2.toDouble(), y1.toDouble(), z2.toDouble()).endVertex()
        worldRenderer.pos(x2.toDouble(), y1.toDouble(), z2.toDouble()).endVertex()
        worldRenderer.pos(x2.toDouble(), y2.toDouble(), z2.toDouble()).endVertex()
        worldRenderer.pos(x3.toDouble(), y2.toDouble(), z3.toDouble()).endVertex()
        worldRenderer.pos(x3.toDouble(), y1.toDouble(), z3.toDouble()).endVertex()
        worldRenderer.pos(x3.toDouble(), y1.toDouble(), z3.toDouble()).endVertex()
        worldRenderer.pos(x3.toDouble(), y2.toDouble(), z3.toDouble()).endVertex()
        worldRenderer.pos(x4.toDouble(), y2.toDouble(), z4.toDouble()).endVertex()
        worldRenderer.pos(x4.toDouble(), y1.toDouble(), z4.toDouble()).endVertex()
        worldRenderer.pos(x4.toDouble(), y1.toDouble(), z4.toDouble()).endVertex()
        worldRenderer.pos(x4.toDouble(), y2.toDouble(), z4.toDouble()).endVertex()
        worldRenderer.pos(x1.toDouble(), y2.toDouble(), z1.toDouble()).endVertex()
        worldRenderer.pos(x1.toDouble(), y1.toDouble(), z1.toDouble()).endVertex()
        worldRenderer.pos(x1.toDouble(), y1.toDouble(), z1.toDouble()).endVertex()
        worldRenderer.pos(x2.toDouble(), y1.toDouble(), z2.toDouble()).endVertex()
        worldRenderer.pos(x3.toDouble(), y1.toDouble(), z3.toDouble()).endVertex()
        worldRenderer.pos(x4.toDouble(), y1.toDouble(), z4.toDouble()).endVertex()
        worldRenderer.pos(x1.toDouble(), y2.toDouble(), z1.toDouble()).endVertex()
        worldRenderer.pos(x2.toDouble(), y2.toDouble(), z2.toDouble()).endVertex()
        worldRenderer.pos(x3.toDouble(), y2.toDouble(), z3.toDouble()).endVertex()
        worldRenderer.pos(x4.toDouble(), y2.toDouble(), z4.toDouble()).endVertex()
        tessellator.draw()
    }

    fun drawRoundedGradientOutlineCorner(
        x: Float,
        y: Float,
        x1: Float,
        y1: Float,
        width: Float,
        radius: Float,
        color: Int,
        color2: Int,
        color3: Int,
        color4: Int
    ) {
        var x = x
        var y = y
        var x1 = x1
        var y1 = y1
        ColorUtils.setColour(-1)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glShadeModel(GL_SMOOTH)
        glPushAttrib(0)
        glScaled(0.5, 0.5, 0.5)
        x *= 2.0.toFloat()
        y *= 2.0.toFloat()
        x1 *= 2.0.toFloat()
        y1 *= 2.0.toFloat()
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        ColorUtils.setColour(color)
        glEnable(GL_LINE_SMOOTH)
        glShadeModel(GL_SMOOTH)
        glLineWidth(width)
        glBegin(GL_LINE_LOOP)
        var i: Int
        i = 0
        while (i <= 90) {
            glVertex2d(
                x + radius + sin(i * Math.PI / 180.0) * radius * -1.0,
                y + radius + cos(i * Math.PI / 180.0) * radius * -1.0
            )
            i += 3
        }
        ColorUtils.setColour(color2)
        i = 90
        while (i <= 180) {
            glVertex2d(
                x + radius + sin(i * Math.PI / 180.0) * radius * -1.0,
                y1 - radius + cos(i * Math.PI / 180.0) * radius * -1.0
            )
            i += 3
        }
        ColorUtils.setColour(color3)
        i = 0
        while (i <= 90) {
            glVertex2d(x1 - radius + sin(i * Math.PI / 180.0) * radius, y1 - radius + cos(i * Math.PI / 180.0) * radius)
            i += 3
        }
        ColorUtils.setColour(color4)
        i = 90
        while (i <= 180) {
            glVertex2d(
                x1 - radius + sin(i * Math.PI / 180.0) * radius,
                y + radius + cos(i * Math.PI / 180.0) * radius
            )
            i += 3
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
        ColorUtils.setColour(-1)
    }

    fun drawRoundedGradientOutlineCorner(
        x: Float,
        y: Float,
        x1: Float,
        y1: Float,
        width: Float,
        radius: Float,
        color: Int,
        color2: Int
    ) {
        var x = x
        var y = y
        var x1 = x1
        var y1 = y1
        ColorUtils.setColour(-1)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glShadeModel(GL_SMOOTH)
        glPushAttrib(0)
        glScaled(0.5, 0.5, 0.5)
        x *= 2.0f
        y *= 2.0f
        x1 *= 2.0f
        y1 *= 2.0f
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        ColorUtils.setColour(color)
        glEnable(GL_LINE_SMOOTH)
        glShadeModel(GL_SMOOTH)
        glLineWidth(width)
        glBegin(GL_LINE_LOOP)
        var i: Int
        i = 0
        while (i <= 90) {
            glVertex2d(
                x + radius + sin(i * Math.PI / 180.0) * radius * -1.0,
                y + radius + cos(i * Math.PI / 180.0) * radius * -1.0
            )
            i += 3
        }
        ColorUtils.setColour(color)
        i = 90
        while (i <= 180) {
            glVertex2d(
                x + radius + sin(i * Math.PI / 180.0) * radius * -1.0,
                y1 - radius + cos(i * Math.PI / 180.0) * radius * -1.0
            )
            i += 3
        }
        ColorUtils.setColour(color2)
        i = 0
        while (i <= 90) {
            glVertex2d(x1 - radius + sin(i * Math.PI / 180.0) * radius, y1 - radius + cos(i * Math.PI / 180.0) * radius)
            i += 3
        }
        ColorUtils.setColour(color2)
        i = 90
        while (i <= 180) {
            glVertex2d(
                x1 - radius + sin(i * Math.PI / 180.0) * radius,
                y + radius + cos(i * Math.PI / 180.0) * radius
            )
            i += 3
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
        ColorUtils.setColour(-1)
    }

    fun drawRoundedGradientRectCorner(
        x: Float,
        y: Float,
        x1: Float,
        y1: Float,
        radius: Float,
        color: Int,
        color2: Int,
        color3: Int,
        color4: Int
    ) {
        var x = x
        var y = y
        var x1 = x1
        var y1 = y1
        setColour(-1)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glShadeModel(GL_SMOOTH)
        glPushAttrib(0)
        glScaled(0.5, 0.5, 0.5)
        x *= 2.0.toFloat()
        y *= 2.0.toFloat()
        x1 *= 2.0.toFloat()
        y1 *= 2.0.toFloat()
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        setColour(color)
        glEnable(GL_LINE_SMOOTH)
        glShadeModel(GL_SMOOTH)
        glBegin(6)
        var i: Int
        i = 0
        while (i <= 90) {
            glVertex2d(
                x + radius + sin(i * Math.PI / 180.0) * radius * -1.0,
                y + radius + cos(i * Math.PI / 180.0) * radius * -1.0
            )
            i += 3
        }
        setColour(color2)
        i = 90
        while (i <= 180) {
            glVertex2d(
                x + radius + sin(i * Math.PI / 180.0) * radius * -1.0,
                y1 - radius + cos(i * Math.PI / 180.0) * radius * -1.0
            )
            i += 3
        }
        setColour(color3)
        i = 0
        while (i <= 90) {
            glVertex2d(x1 - radius + sin(i * Math.PI / 180.0) * radius, y1 - radius + cos(i * Math.PI / 180.0) * radius)
            i += 3
        }
        setColour(color4)
        i = 90
        while (i <= 180) {
            glVertex2d(
                x1 - radius + sin(i * Math.PI / 180.0) * radius,
                y + radius + cos(i * Math.PI / 180.0) * radius
            )
            i += 3
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
        setColour(-1)
    }

    fun drawAnimatedGradient(left: Double, top: Double, right: Double, bottom: Double, col1: Int, col2: Int) {
        val currentTime = System.currentTimeMillis()
        if (startTime.toInt() === 0) {
            startTime = currentTime
        }
        val elapsedTime: Long = currentTime - startTime
        val progress: Float = (elapsedTime % animationDuration) as Float / animationDuration
        val color1: Int
        val color2: Int
        if ((elapsedTime / animationDuration % 2).toInt() === 0) {
            color1 = interpolateColors(col1, col2, progress)
            color2 = interpolateColors(col2, col1, progress)
        } else {
            color1 = interpolateColors(col2, col1, progress)
            color2 = interpolateColors(col1, col2, progress)
        }
        drawGradientSideways(left, top, right, bottom, color1, color2)
        if (elapsedTime >= 2 * animationDuration) {
            startTime = currentTime
        }
    }

    fun interpolateColors(color1: Int, color2: Int, progress: Float): Int {
        val alpha = ((1.0 - progress) * (color1 ushr 24) + progress * (color2 ushr 24)).toInt()
        val red = ((1.0 - progress) * (color1 shr 16 and 0xFF) + progress * (color2 shr 16 and 0xFF)).toInt()
        val green = ((1.0 - progress) * (color1 shr 8 and 0xFF) + progress * (color2 shr 8 and 0xFF)).toInt()
        val blue = ((1.0 - progress) * (color1 and 0xFF) + progress * (color2 and 0xFF)).toInt()
        return alpha shl 24 or (red shl 16) or (green shl 8) or blue
    }

    fun drawRoundedGradientRectCorner(
        x: Float,
        y: Float,
        x1: Float,
        y1: Float,
        radius: Float,
        color: Int,
        color2: Int
    ) {
        var x = x
        var y = y
        var x1 = x1
        var y1 = y1
        setColour(-1)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glShadeModel(GL_SMOOTH)
        glPushAttrib(0)
        glScaled(0.5, 0.5, 0.5)
        x *= 2.0.toFloat()
        y *= 2.0.toFloat()
        x1 *= 2.0.toFloat()
        y1 *= 2.0.toFloat()
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        setColour(color)
        glEnable(GL_LINE_SMOOTH)
        glShadeModel(GL_SMOOTH)
        glBegin(6)
        var i: Int
        i = 0
        while (i <= 90) {
            glVertex2d(
                x + radius + sin(i * Math.PI / 180.0) * radius * -1.0,
                y + radius + cos(i * Math.PI / 180.0) * radius * -1.0
            )
            i += 3
        }
        setColour(color)
        i = 90
        while (i <= 180) {
            glVertex2d(
                x + radius + sin(i * Math.PI / 180.0) * radius * -1.0,
                y1 - radius + cos(i * Math.PI / 180.0) * radius * -1.0
            )
            i += 3
        }
        setColour(color2)
        i = 0
        while (i <= 90) {
            glVertex2d(x1 - radius + sin(i * Math.PI / 180.0) * radius, y1 - radius + cos(i * Math.PI / 180.0) * radius)
            i += 3
        }
        setColour(color2)
        i = 90
        while (i <= 180) {
            glVertex2d(
                x1 - radius + sin(i * Math.PI / 180.0) * radius,
                y + radius + cos(i * Math.PI / 180.0) * radius
            )
            i += 3
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
        setColour(-1)
    }
    /**
     * @param x : X pos
     * @param y : Y pos
     * @param x1 : X2 pos
     * @param y1 : Y2 pos
     * @param width : width of line;
     * @param radius : round of edges;
     * @param color : color;
     * @param color2 : color2;
     * @param color3 : color3;
     * @param color4 : color4;
     */

    /**
     * GL CAP MANAGER
     *
     * TODO: Remove gl cap manager and replace by something better
     */
    fun resetCaps() {
        glCapMap.forEach(this::setGlState)
    }

    fun enableGlCap(cap: Int) {
        setGlCap(cap, true)
    }

    fun enableGlCap(vararg caps: Int) {
        for (cap in caps) setGlCap(cap, true)
    }

    fun disableGlCap(cap: Int) {
        setGlCap(cap, true)
    }

    fun disableGlCap(vararg caps: Int) {
        for (cap in caps) setGlCap(cap, false)
    }

    fun setGlCap(cap: Int, state: Boolean) {
        glCapMap[cap] = GL11.glGetBoolean(cap)
        setGlState(cap, state)
    }

    fun setGlState(cap: Int, state: Boolean) {
        if (state) GL11.glEnable(cap) else GL11.glDisable(cap)
    }

}