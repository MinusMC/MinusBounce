/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.minusmc.minusbounce.features.module.modules.render

import net.minecraft.entity.EntityLivingBase
import net.minusmc.minusbounce.event.AttackEvent
import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.PostMotionEvent
import net.minusmc.minusbounce.event.Render3DEvent
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.utils.render.Particle
import net.minusmc.minusbounce.utils.render.ParticleUtils
import net.minusmc.minusbounce.utils.timer.MSTimer
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.IntegerValue

@ModuleInfo(name = "Particles", description = "Particles", category = ModuleCategory.RENDER)
class Particles : Module() {
    private val amountValue = IntegerValue("Amount", 10, 1, 20)
    private val physicsValue = BoolValue("Physics", true)

    private val particles = mutableListOf<Particle>()
    private var target: EntityLivingBase? = null
    private val timer = MSTimer()

    @EventTarget
    fun onAttack(event: AttackEvent) {
        target = event.targetEntity as? EntityLivingBase
    }

    @EventTarget
    fun onPostMotion(event: PostMotionEvent) {
        val target = this.target ?: return

        if (target.hurtTime >= 9 && mc.thePlayer.getDistance(target.posX, target.posY, target.posZ) < 10) {
            repeat(amountValue.get()) {
                if (particles.size > 100)
                    particles.removeFirst()

                particles.add(Particle(target.posX, target.posY, target.posZ))
            }

            this.target = null
        }
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (particles.isEmpty())
            return

        for (i in 0..(timer.reachedTime / 1e11).toInt()) {
            if (physicsValue.get())
                particles.forEach(Particle::update)
            else 
                particles.forEach(Particle::updateWithoutPhysics)
        }

        particles.removeIf { 
            mc.thePlayer.getDistanceSq(it.position.xCoord, it.position.yCoord, it.position.zCoord ) > 100
        }

        timer.reset()
        ParticleUtils.renderParticles(particles)
    }
}