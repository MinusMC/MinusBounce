/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.utils.render

import net.vitox.ParticleGenerator

object ParticleUtils {
    private val particleGenerator = ParticleGenerator(100)
    
    fun drawParticles(mouseX: Int, mouseY: Int) {
        particleGenerator.draw(mouseX, mouseY)
    }

    fun drawSnowFall(mouseX: Int, mouseY: Int) {
        particleGenerator.draw2(mouseX, mouseY)
    }
}
