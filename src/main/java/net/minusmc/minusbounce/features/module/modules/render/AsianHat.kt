/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.render

import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.Render3DEvent
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.utils.render.GLUtils
import net.minusmc.minusbounce.utils.misc.MathUtils
import net.minusmc.minusbounce.utils.EntityUtils
import net.minusmc.minusbounce.value.*
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.GL11
import kotlin.math.cos
import kotlin.math.sin

@ModuleInfo(name = "AsianHat", spacedName = "Asian Hat", description = "Asian hat.", category = ModuleCategory.RENDER)
class AsianHat : Module() {
    private val heightValue = FloatValue("Height", 0.3f, 0.1f, 0.7f)
    private val radiusValue = FloatValue("Radius", 0.7f, 0.3f, 1.5f)
    private val yPosValue = FloatValue("YPos", 0f, -1f, 1f)
    private val rotateSpeedValue = FloatValue("RotateSpeed", 2f, 0f, 5f)
    private val drawThePlayerValue = BoolValue("DrawThePlayer", true)
    private val onlyThirdPersonValue = BoolValue("OnlyThirdPerson", true) { drawThePlayerValue.get() }
    private val drawTargetsValue = BoolValue("DrawTargets", true)
    private val colorRedValue = IntegerValue("Red", 255, 0, 255)
    private val colorGreenValue = IntegerValue("Green", 179, 0, 255)
    private val colorBlueValue = IntegerValue("Blue", 72, 0, 255)
    private val colorAlphaValue = IntegerValue("Alpha", 255, 0, 255)

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (drawThePlayerValue.get() && !(onlyThirdPersonValue.get() && mc.gameSettings.thirdPersonView == 0))
            drawHat(mc.thePlayer)

        if (drawTargetsValue.get()) for (entity in mc.theWorld.loadedEntityList) {
            if (entity != mc.thePlayer && entity is EntityLivingBase && EntityUtils.isSelected(entity, false))
                drawHat(entity)
        }
    }

    private fun drawHat(entity: EntityLivingBase) {
        GL11.glPushMatrix()
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glDepthMask(false)
        GL11.glDisable(GL11.GL_CULL_FACE)

        GLUtils.glColor(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get(), colorAlphaValue.get())

        val x = MathUtils.interpolate(entity.posX, entity.lastTickPosX, mc.timer.renderPartialTicks) - mc.renderManager.renderPosX
        val y = MathUtils.interpolate(entity.posY, entity.lastTickPosY, mc.timer.renderPartialTicks) - mc.renderManager.renderPosY + entity.height + yPosValue.get()
        val z = MathUtils.interpolate(entity.posZ, entity.lastTickPosZ, mc.timer.renderPartialTicks) - mc.renderManager.renderPosZ
        GL11.glTranslated(x, y, z)

        GL11.glRotatef((entity.ticksExisted + mc.timer.renderPartialTicks) * rotateSpeedValue.get(), 0f, 1f, 0f)

        GL11.glBegin(GL11.GL_TRIANGLE_FAN)
        GL11.glVertex3f(0f, heightValue.get(), 0f)

        for (i in 0..360 step 5) {
            val angle = MathUtils.toRadiansDouble(i)

            val xAngle = cos(angle) * radiusValue.get()
            val zAngle = sin(angle) * radiusValue.get()

            GL11.glVertex3d(xAngle, 0.0, zAngle)
        }
        GL11.glVertex3f(0f, heightValue.get(), 0f)
        GL11.glEnd()

        GL11.glEnable(GL11.GL_CULL_FACE)
        GlStateManager.resetColor()
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glDepthMask(true)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glPopMatrix()
    }
}