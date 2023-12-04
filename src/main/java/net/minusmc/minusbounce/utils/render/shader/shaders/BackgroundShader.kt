/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.utils.render.shader.shaders

import net.minecraft.client.gui.ScaledResolution
import net.minusmc.minusbounce.utils.ClientUtils
import net.minusmc.minusbounce.utils.render.RenderUtils
import net.minusmc.minusbounce.utils.render.shader.Shader
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL20

class BackgroundShader : Shader("background.frag") {
    private var time = 0f
    override fun setupUniforms() {
        setupUniform("iResolution")
        setupUniform("iTime")
    }

    override fun updateUniforms() {
        val scaledResolution = ScaledResolution(mc)
        val resolutionID = getUniform("iResolution")
        if (resolutionID > -1) GL20.glUniform2f(
            resolutionID,
            Display.getWidth().toFloat(),
            Display.getHeight().toFloat()
        )
        val timeID = getUniform("iTime")
        if (timeID > -1) GL20.glUniform1f(timeID, time)
        time += 0.005f * RenderUtils.deltaTime
    }

    companion object {
        @JvmField
        val BACKGROUND_SHADER = BackgroundShader()
        init {
            ClientUtils.displayChatMessage("background shader loaded here =====================================================")
        }
    }
}
