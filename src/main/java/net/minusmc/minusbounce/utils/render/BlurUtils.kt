/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.utils.render

import com.google.gson.JsonSyntaxException
import net.minusmc.minusbounce.utils.ClientUtils
import net.minusmc.minusbounce.utils.MinecraftInstance
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.*
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.shader.Framebuffer
import net.minecraft.client.shader.ShaderGroup
import net.minecraft.util.ResourceLocation

import java.io.IOException
import org.lwjgl.opengl.GL11

object BlurUtils : MinecraftInstance() {

    private val shaderGroup = ShaderGroup(mc.textureManager, mc.resourceManager, mc.framebuffer, ResourceLocation("shaders/post/blurArea.json"))
    private val framebuffer = shaderGroup.mainFramebuffer
    private val frbuffer = shaderGroup.getFramebufferRaw("result")

    private var lastFactor = 0.0
    private var lastWidth = 0.0
    private var lastHeight = 0.0
    private var lastWeight = 0.0

    private var lastX = 0F
    private var lastY = 0F
    private var lastW = 0F
    private var lastH = 0F

    private var lastStrength = 5F

    private fun setupFramebuffers() {
        try {
            shaderGroup.createBindFramebuffers(mc.displayWidth, mc.displayHeight)
        } catch (e : Exception) {
            ClientUtils.logger.error("Exception caught while setting up shader group", e)
        }
    }

    private fun setValues(strength: Float, x: Float, y: Float, w: Float, h: Float, width: Float, height: Float, force: Boolean = false) {
        if (!force && strength == lastStrength && lastX == x && lastY == y && lastW == w && lastH == h) 
            return

        lastStrength = strength
        lastX = x
        lastY = y
        lastW = w
        lastH = h

        shaderGroup.listShaders.take(2).forEach {
            it.shaderManager.getShaderUniform("Radius").set(strength)
            it.shaderManager.getShaderUniform("BlurXY")[x] = height - y - h
            it.shaderManager.getShaderUniform("BlurCoord")[w] = h
        }
    }

    @JvmStatic
    fun blur(posX: Float, posY: Float, posXEnd: Float, posYEnd: Float, blurStrength: Float, displayClipMask: Boolean, triggerMethod: () -> Unit) {
        if (!OpenGlHelper.isFramebufferEnabled())
            return

        val (x, x2) = if (posX > posXEnd) posXEnd to posX else posX to posXEnd
        val (y, y2) = if (posY > posYEnd) posYEnd to posY else posY to posYEnd

        val sc = ScaledResolution(mc)
        val scaleFactor = sc.scaleFactor.toDouble()
        val width = sc.scaledWidth.toDouble()
        val height = sc.scaledHeight.toDouble()

        if (sizeHasChanged(scaleFactor, width, height)) {
            setupFramebuffers()
            setValues(blurStrength, x, y, x2 - x, y2 - y, width.toFloat(), height.toFloat(), true)
        }

        lastFactor = scaleFactor
        lastWidth = width
        lastHeight = height

        setValues(blurStrength, x, y, x2 - x, y2 - y, width.toFloat(), height.toFloat())

        framebuffer.bindFramebuffer(true)
        shaderGroup.loadShaderGroup(mc.timer.renderPartialTicks)
        mc.framebuffer.bindFramebuffer(true)

        Stencil.write(displayClipMask)
        triggerMethod()

        Stencil.erase(true)
        GlStateManager.enableBlend()
        GlStateManager.blendFunc(770, 771)
        GlStateManager.pushMatrix()
        GlStateManager.colorMask(true, true, true, false)
        GlStateManager.disableDepth()
        GlStateManager.depthMask(false)
        GlStateManager.enableTexture2D()
        GlStateManager.disableLighting()
        GlStateManager.disableAlpha()
        frbuffer.bindFramebufferTexture()
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F)
        val f2 = frbuffer.framebufferWidth.toDouble() / frbuffer.framebufferTextureWidth
        val f3 = frbuffer.framebufferHeight.toDouble() / frbuffer.framebufferTextureHeight
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.getWorldRenderer()
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR)
        worldrenderer.pos(0.0, height, 0.0).tex(0.0, 0.0).color(255, 255, 255, 255).endVertex()
        worldrenderer.pos(width, height, 0.0).tex(f2, 0.0).color(255, 255, 255, 255).endVertex()
        worldrenderer.pos(width, 0.0, 0.0).tex(f2, f3).color(255, 255, 255, 255).endVertex()
        worldrenderer.pos(0.0, 0.0, 0.0).tex(0.0, f3).color(255, 255, 255, 255).endVertex()
        tessellator.draw()
        frbuffer.unbindFramebufferTexture()
        GlStateManager.enableDepth()
        GlStateManager.depthMask(true)
        GlStateManager.colorMask(true, true, true, true)
        GlStateManager.popMatrix()
        GlStateManager.disableBlend()

        Stencil.dispose()
        GlStateManager.enableAlpha()
    }

    @JvmStatic
    fun blurArea(x: Float, y: Float, x2: Float, y2: Float, blurStrength: Float) = blur(x, y, x2, y2, blurStrength, false) { 
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        RenderUtils.quickDrawRect(x, y, x2, y2)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    @JvmStatic
    fun blurAreaRounded(x: Float, y: Float, x2: Float, y2: Float, rad: Float, blurStrength: Float) = blur(x, y, x2, y2, blurStrength, false) { 
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        RenderUtils.fastRoundedRect(x, y, x2, y2, rad)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    fun sizeHasChanged(scaleFactor: Double, width: Double, height: Double) = lastFactor != scaleFactor || lastWidth != width || lastHeight != height
}


