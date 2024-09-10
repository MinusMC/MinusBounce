package net.minusmc.minusbounce.features.module.modules.render

import net.minecraft.entity.item.EntityEnderPearl
import net.minecraft.entity.projectile.EntityArrow
import net.minecraft.entity.projectile.EntityFireball
import net.minecraft.entity.projectile.EntityLargeFireball
import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.Render2DEvent
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.ui.font.Fonts
import net.minusmc.minusbounce.utils.render.RenderUtils
import net.minusmc.minusbounce.utils.player.RotationUtils
import net.minusmc.minusbounce.utils.misc.MathUtils
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.FloatValue
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

@ModuleInfo(name = "Indicators", description = "Indicators2", category = ModuleCategory.RENDER)
class Indicators : Module() {

    private val fireBall = BoolValue("Fireball", true)
    private val arrow = BoolValue("Arrow", true)
    private val pearl = BoolValue("Pearl", true)
    private val scaleValue = FloatValue("Scale", 0.7f, 0.65f, 1.25f)
    private val radiusValue = FloatValue("Radius", 50f, 15f, 150f)

    lateinit var displayName : String

    fun drawArrow(x: Double, y: Double, angle: Double, size: Double, degrees: Double) {
        val arrowSize = size * 2

        val maxAngle = angle + MathUtils.toRadians(degrees)
        val minAngle = angle - MathUtils.toRadians(degrees)

        val arrowX = x - arrowSize * cos(angle)
        val arrowY = y - arrowSize * sin(angle)
        
        RenderUtils.drawLine(x, y, arrowX + arrowSize * cos(maxAngle), arrowY + arrowSize * sin(maxAngle), size.toFloat())
        RenderUtils.drawLine(x, y, arrowX + arrowSize * cos(minAngle), arrowY + arrowSize * sin(minAngle), size.toFloat())
    }

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        val sc = ScaledResolution(mc)
        val scWidth = sc.scaledWidth
        val scHeight = sc.scaledHeight

        val entities = mc.theWorld.loadedEntityList.filter {
            ((it is EntityFireball || it is EntityLargeFireball) && fireBall.get()) || 
            (it is EntityArrow && arrow.get()) || 
            (it is EntityEnderPearl && pearl.get())
        }

        for (entity in entities) {
            val displayName = when (entity) {
                is EntityFireball, is EntityLargeFireball -> "Fireball"
                is EntityArrow -> "Arrow"
                is EntityEnderPearl -> "Ender Pearl"
                else -> continue
            }

            val distance = floor(mc.thePlayer.getDistanceToEntity(entity))
            val rotation = RotationUtils.getRotations(entity.posX, 0.0, entity.posZ)
            val deltaYaw = MathUtils.toRadians(rotation.yaw + 90f - mc.thePlayer.rotationYaw)

            val scale = scaleValue.get()
            val radius = radiusValue.get()
            val arrowX = scWidth / 2 + radius * sin(deltaYaw)
            val arrowY = scHeight / 2 - radius * cos(deltaYaw)
            
            val arrowAngle = atan2(arrowY - scHeight / 2, arrowX - scWidth / 2)
            drawArrow(arrowX.toDouble(), arrowY.toDouble(), arrowAngle.toDouble(), 3.0, 100.0)
            GlStateManager.color(255f, 255f, 255f, 255f)

            val image = when (entity) {
                is EntityFireball, is EntityLargeFireball -> ResourceLocation("textures/items/fireball.png")
                is EntityEnderPearl -> ResourceLocation("textures/items/ender_pearl.png")
                is EntityArrow -> ResourceLocation("textures/items/arrow.png")
                else -> continue
            }

            val imgX = scWidth / 2 + (radius - 18) * sin(deltaYaw)
            val imgY = scHeight / 2 - (radius - 18) * cos(deltaYaw)
            GlStateManager.scale(scale, scale, scale)
            RenderUtils.drawImage(image, imgX / scale - 5, imgY / scale - 5, 32, 32)
            GlStateManager.scale(1 / scale, 1 / scale, 1 / scale)

            GlStateManager.scale(scale, scale, scale)

            val textX = scWidth / 2 + (radius - 13) * sin(deltaYaw)
            val textY = scHeight / 2 - (radius - 13) * cos(deltaYaw)

            val string = "${distance}m"
            val stringWidth = Fonts.minecraftFont.getStringWidth(string)

            Fonts.minecraftFont.drawStringWithShadow(string, textX.toFloat() / scale - stringWidth / 2, textY.toFloat() / scale - 4, -1)
            GlStateManager.scale(1 / scale, 1 / scale, 1 / scale)
        }
    }
}