package net.minusmc.minusbounce.features.module.modules.render

import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.Render3DEvent
import net.minusmc.minusbounce.event.UpdateEvent
import net.minusmc.minusbounce.event.WorldEvent
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.utils.render.HealthParticle
import net.minusmc.minusbounce.utils.misc.MathUtils
import net.minusmc.minusbounce.utils.misc.RandomUtils
import net.minusmc.minusbounce.utils.EntityUtils
import net.minusmc.minusbounce.value.*
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.math.BigDecimal
import java.util.*
import kotlin.math.abs

@ModuleInfo(name = "DamageParticle", spacedName = "Damage Particle", description = "Allows you to see targets damage.", category = ModuleCategory.RENDER)
class DamageParticle : Module() {
    private val ticksShowParticleValue = IntegerValue("TicksShowParticle", 20, 10, 50)
    private val sizeValue = FloatValue("Size", 0.03f, 0.01f, 0.07f)

    private val colorModeValue = ListValue("ColorMode", arrayOf("ColorCode", "Custom"), "ColorCode")
    private val red = IntegerValue("Red", 255, 0, 255) { colorModeValue.get().equals("custom", true) }
    private val green = IntegerValue("Green", 255, 0, 255) { colorModeValue.get().equals("custom", true) }
    private val blue = IntegerValue("Blue", 255, 0, 255) { colorModeValue.get().equals("custom", true) }

    private val lastHealthMap = hashMapOf<Int, Float>()
    private val particles = mutableListOf<HealthParticle>()

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        synchronized(particles) {
            for (entity in mc.theWorld.loadedEntityList) {
                if (entity is EntityLivingBase && EntityUtils.isSelected(entity, false)) {

                    val lastHealth = lastHealthMap[entity.entityId] ?: entity.maxHealth
                    lastHealthMap[entity.entityId] = entity.health

                    if (lastHealth == entity.health)
                        continue

                    val colorPrefix = if (!colorModeValue.get().equals("custom", true)) 
                        (if (lastHealth > entity.health) "§c" else "§a") 
                    else 
                        (if (lastHealth > entity.health) "-" else "+")

                    particles.add(HealthParticle(
                        "$colorPrefix${MathUtils.round(abs(lastHealth - entity.health))}",
                        entity.posX - RandomUtils.nextDouble(0.0, 0.5),
                        MathUtils.interpolate(entity.entityBoundingBox.maxY, entity.entityBoundingBox.minY, 0.5),
                        entity.posZ - RandomUtils.nextDouble(0.0, 0.5)
                    ))
                }
            }


            particles.removeIf { it.ticks++ > ticksShowParticleValue.get() }
        }
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        val size = sizeValue.get()

        synchronized(particles) {
            for (particle in particles) {
                val x = particle.posX - mc.renderManager.renderPosX
                val y = particle.posY - mc.renderManager.renderPosY
                val z = particle.posZ - mc.renderManager.renderPosZ

                GlStateManager.pushMatrix()
                GlStateManager.enablePolygonOffset()
                GlStateManager.doPolygonOffset(1f, -1500000f)
                GlStateManager.translate(x, y, z)
                GlStateManager.rotate(-mc.renderManager.playerViewY, 0f, 1f, 0f)

                GlStateManager.rotate(mc.renderManager.playerViewX, if (mc.gameSettings.thirdPersonView == 2) -1f else 1f, 0f, 0f)
                GlStateManager.scale(-size, -size, size)
                GL11.glDepthMask(false)
                mc.fontRendererObj.drawStringWithShadow(particle.health, -mc.fontRendererObj.getStringWidth(particle.health) / 2f, -mc.fontRendererObj.FONT_HEIGHT - 1f, if (colorModeValue.get().equals("custom", true)) Color(red.get(), green.get(), blue.get()).rgb else 0)
                GL11.glColor4f(187f, 255f, 255f, 1f)
                GL11.glDepthMask(true)
                GlStateManager.doPolygonOffset(1.0f, 1500000f)
                GlStateManager.disablePolygonOffset()
                GlStateManager.popMatrix()
            }
        }
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        particles.clear()
        lastHealthMap.clear()
    }
}