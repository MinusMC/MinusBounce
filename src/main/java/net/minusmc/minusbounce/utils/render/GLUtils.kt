package net.minusmc.minusbounce.utils.render

import net.minusmc.minusbounce.utils.MinecraftInstance
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11.*
import java.awt.Color

object GLUtils: MinecraftInstance() {
	private val glCapMap = hashMapOf<Int, Boolean>()

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
        for (cap in caps)
        	setGlCap(cap, true)
    }

    fun disableGlCap(cap: Int) {
        setGlCap(cap, true)
    }

    fun disableGlCap(vararg caps: Int) {
        for (cap in caps)
        	setGlCap(cap, false)
    }

    fun setGlCap(cap: Int, state: Boolean) {
        glCapMap[cap] = glGetBoolean(cap)
        setGlState(cap, state)
    }

    fun setGlState(cap: Int, state: Boolean) {
        if (state)
        	glEnable(cap)
        else
        	glDisable(cap)
    }

    /**
     * GL State wrapper
     */

    fun stop3D() {
        GlStateManager.enableCull()
        glEnable(GL_TEXTURE_2D)
        glEnable(GL_DEPTH_TEST)
        glDepthMask(true)
        glDisable(GL_BLEND)
    }

    fun start3D() {
        glDisable(GL_TEXTURE_2D)
        glDisable(GL_DEPTH_TEST)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glDepthMask(false)
        GlStateManager.disableCull()
    }

    fun startSmooth() {
        glEnable(2848)
        glEnable(2881)
        glEnable(2832)
        glEnable(3042)
        glBlendFunc(770, 771)
        glHint(3154, 4354)
        glHint(3155, 4354)
        glHint(3153, 4354)
    }

    fun endSmooth() {
        glDisable(2848)
        glDisable(2881)
        glEnable(2832)
    }

    /**
     * Extended "color" function of GLStateManager
     */

    fun glColor(color: Color) {
        val red = color.red / 255f
        val green = color.green / 255f
        val blue = color.blue / 255f
        val alpha = color.alpha / 255f

        GlStateManager.color(red, green, blue, alpha)
    }

    fun glColor(color: Int) {
        val alpha = color shr 24 and 0xFF
        val red = color shr 16 and 0xFF
        val green = color shr 8 and 0xFF
        val blue = color and 0xFF

        glColor(Color(red, green, blue, alpha))
    }

    fun glColor(color: Int, alpha: Float) {
        val red = color shr 16 and 0xFF
        val green = color shr 8 and 0xFF
        val blue = color and 0xFF

        glColor(Color(red, green, blue, (alpha * 255).toInt()))
    }

    fun glColor(red: Int, green: Int, blue: Int, alpha: Int) {
        glColor(Color(red, green, blue, alpha))
    }

}