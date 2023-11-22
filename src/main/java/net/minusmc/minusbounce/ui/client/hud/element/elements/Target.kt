/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.ui.client.hud.element.elements

import net.minecraft.client.gui.GuiChat
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.ui.client.hud.designer.GuiHudDesigner
import net.minusmc.minusbounce.ui.client.hud.element.Border
import net.minusmc.minusbounce.ui.client.hud.element.Element
import net.minusmc.minusbounce.ui.client.hud.element.ElementInfo
import net.minusmc.minusbounce.ui.client.hud.element.elements.targets.TargetStyle
import net.minusmc.minusbounce.ui.client.hud.element.elements.targets.impl.Chill
import net.minusmc.minusbounce.ui.client.hud.element.elements.targets.impl.Rice
import net.minusmc.minusbounce.utils.ClassUtils
import net.minusmc.minusbounce.utils.extensions.getDistanceToEntityBox
import net.minusmc.minusbounce.utils.render.*
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.value.IntegerValue
import net.minusmc.minusbounce.value.ListValue
import org.lwjgl.opengl.GL11
import java.awt.Color

/**
 * A target hud
 */
@ElementInfo(name = "Target", disableScale = true, retrieveDamage = true)
class Target : Element() {
    private val styles = ClassUtils.resolvePackage("${this.javaClass.`package`.name}.targets", TargetStyle::class.java)
        .map { it.getConstructor(Target::class.java).newInstance(this)}
        .sortedBy { it.name }

    val style: TargetStyle
        get() = styles.find { it.name.equals(styleValue.get(), true) } ?: throw NullPointerException()

    val styleValue = ListValue("Style", styles.map { it.name }.toTypedArray(), "LiquidBounce")

    // Global variables
    private val blurValue = BoolValue("Blur", false)
    private val blurStrength = FloatValue("Blur-Strength", 1F, 0.01F, 40F) { blurValue.get() }

    private val shadowValue = BoolValue("Shadow", false)
    private val shadowStrength = FloatValue("Shadow-Strength", 1F, 0.01F, 40F) { shadowValue.get() }
    val shadowColorMode = ListValue("Shadow-Color", arrayOf("Background", "Custom", "Bar"), "Background") { shadowValue.get() }

    val shadowColorRedValue = IntegerValue("Shadow-Red", 0, 0, 255) {
        shadowValue.get() && shadowColorMode.get().equals("custom", true)
    }
    val shadowColorGreenValue = IntegerValue("Shadow-Green", 111, 0, 255) {
        shadowValue.get() && shadowColorMode.get().equals("custom", true)
    }
    val shadowColorBlueValue = IntegerValue("Shadow-Blue", 255, 0, 255) {
        shadowValue.get() && shadowColorMode.get().equals("custom", true)
    }

    private val fadeValue = BoolValue("FadeAnim", false)
    val fadeSpeed = FloatValue("Fade-Speed", 1F, 0F, 5F) { fadeValue.get() }

    val noAnimValue = BoolValue("No-Animation", false)
    val globalAnimSpeed = FloatValue("Global-AnimSpeed", 3F, 1F, 9F) { !noAnimValue.get() }

    private val showWithChatOpen = BoolValue("Show-ChatOpen", true)
    private val resetBar = BoolValue("ResetBarWhenHiding", false)

    val colorModeValue = ListValue("Color", arrayOf("Custom", "Rainbow", "Sky", "Slowly", "Fade", "Health"), "Custom")
    val redValue = IntegerValue("Red", 252, 0, 255)
    val greenValue = IntegerValue("Green", 96, 0, 255)
    val blueValue = IntegerValue("Blue", 66, 0, 255)
    val saturationValue = FloatValue("Saturation", 1F, 0F, 1F)
    val brightnessValue = FloatValue("Brightness", 1F, 0F, 1F)
    val waveSecondValue = IntegerValue("Seconds", 2, 1, 10)
    private val bgRedValue = IntegerValue("Background-Red", 0, 0, 255)
    private val bgGreenValue = IntegerValue("Background-Green", 0, 0, 255)
    private val bgBlueValue = IntegerValue("Background-Blue", 0, 0, 255)
    val bgAlphaValue = IntegerValue("Background-Alpha", 160, 0, 255)

    private var mainTarget: EntityPlayer? = null
    var animProgress = 0F

    var barColor = Color(-1)
    var bgColor = Color(-1)

//    init {
//        styles.map {
//            style.values.forEach {it.name = "${style.name}-${it.name}"}
//        }
//    }

    override fun drawElement(): Border? {
        val mainStyle = style

        val kaTarget = MinusBounce.combatManager.target

        val actualTarget = if (kaTarget != null && kaTarget is EntityPlayer && mc.thePlayer.getDistanceToEntityBox(kaTarget) <= 8.0) kaTarget 
                            else if ((mc.currentScreen is GuiChat && showWithChatOpen.get()) || mc.currentScreen is GuiHudDesigner) mc.thePlayer 
                            else null

        val preBarColor = when (colorModeValue.get()) {
            "Rainbow" -> Color(RenderUtils.getRainbowOpaque(waveSecondValue.get(), saturationValue.get(), brightnessValue.get(), 0))
            "Custom" -> Color(redValue.get(), greenValue.get(), blueValue.get())
            "Sky" -> RenderUtils.skyRainbow(0, saturationValue.get(), brightnessValue.get())
            "Fade" -> ColorUtils.fade(Color(redValue.get(), greenValue.get(), blueValue.get()), 0, 100)
            "Health" -> if (actualTarget != null) BlendUtils.getHealthColor(actualTarget.health, actualTarget.maxHealth) else Color.green
            else -> ColorUtils.LiquidSlowly(System.nanoTime(), 0, saturationValue.get(), brightnessValue.get())!!
        }

        val preBgColor = Color(bgRedValue.get(), bgGreenValue.get(), bgBlueValue.get(), bgAlphaValue.get())

        if (fadeValue.get())
            animProgress += (0.0075F * fadeSpeed.get() * RenderUtils.deltaTime * if (actualTarget != null) -1F else 1F)
        else animProgress = 0F

        animProgress = animProgress.coerceIn(0F, 1F)

        barColor = ColorUtils.reAlpha(preBarColor, preBarColor.alpha / 255F * (1F - animProgress))
        bgColor = ColorUtils.reAlpha(preBgColor, preBgColor.alpha / 255F * (1F - animProgress))

        if (actualTarget != null || !fadeValue.get())
            mainTarget = actualTarget
        else if (animProgress >= 1F)
            mainTarget = null

        val returnBorder = mainStyle.getBorder(mainTarget) ?: return null
        val borderWidth = returnBorder.x2 - returnBorder.x
        val borderHeight = returnBorder.y2 - returnBorder.y

        if (mainTarget == null) {
            if (resetBar.get()) 
                mainStyle.easingHealth = 0F
            if (mainStyle is Rice)
                mainStyle.particleList.clear()
            return returnBorder
        }
        val convertTarget = mainTarget!!
        
        val calcScaleX = animProgress * (4F / (borderWidth / 2F))
        val calcScaleY = animProgress * (4F / (borderHeight / 2F))
        val calcTranslateX = borderWidth / 2F * calcScaleX
        val calcTranslateY = borderHeight / 2F * calcScaleY

        if (shadowValue.get() && mainStyle.shaderSupport) {
            GL11.glTranslated(-renderX, -renderY, 0.0)
            GL11.glPushMatrix()

            ShadowUtils.shadow(shadowStrength.get(), {
                GL11.glPushMatrix()
                GL11.glTranslated(renderX, renderY, 0.0)
                if (fadeValue.get()) {
                    GL11.glTranslatef(calcTranslateX, calcTranslateY, 0F)
                    GL11.glScalef(1F - calcScaleX, 1F - calcScaleY, 1F - calcScaleX)
                }
                mainStyle.handleShadow(convertTarget)
                GL11.glPopMatrix()
            }, {
                GL11.glPushMatrix()
                GL11.glTranslated(renderX, renderY, 0.0)
                if (fadeValue.get()) {
                    GL11.glTranslatef(calcTranslateX, calcTranslateY, 0F)
                    GL11.glScalef(1F - calcScaleX, 1F - calcScaleY, 1F - calcScaleX)
                }
                mainStyle.handleShadowCut(convertTarget)
                GL11.glPopMatrix()
            })

            GL11.glPopMatrix()
            GL11.glTranslated(renderX, renderY, 0.0)
        }

        if (blurValue.get() && mainStyle.shaderSupport) {
            val floatX = renderX.toFloat()
            val floatY = renderY.toFloat()

            GL11.glTranslated(-renderX, -renderY, 0.0)
            GL11.glPushMatrix()
            BlurUtils.blur(floatX + returnBorder.x, floatY + returnBorder.y, floatX + returnBorder.x2, floatY + returnBorder.y2, blurStrength.get() * (1F - animProgress), false) {
                GL11.glPushMatrix()
                GL11.glTranslated(renderX, renderY, 0.0)
                if (fadeValue.get()) {
                    GL11.glTranslatef(calcTranslateX, calcTranslateY, 0F)
                    GL11.glScalef(1F - calcScaleX, 1F - calcScaleY, 1F - calcScaleX)
                }
                mainStyle.handleBlur(convertTarget)
                GL11.glPopMatrix()
            }
            GL11.glPopMatrix()
            GL11.glTranslated(renderX, renderY, 0.0)
        }

        if (fadeValue.get()) {
            GL11.glPushMatrix()
            GL11.glTranslatef(calcTranslateX, calcTranslateY, 0F)
            GL11.glScalef(1F - calcScaleX, 1F - calcScaleY, 1F - calcScaleX)
        }
        
        if (mainStyle is Chill)
            mainStyle.updateData(renderX.toFloat() + calcTranslateX, renderY.toFloat() + calcTranslateY, calcScaleX, calcScaleY)
        mainStyle.drawTarget(convertTarget)

        if (fadeValue.get())
            GL11.glPopMatrix()

        GlStateManager.resetColor()
        return returnBorder
    }

    override fun handleDamage(ent: EntityPlayer) {
        if (mainTarget != null && ent == mainTarget)
            style.handleDamage(ent)
    }

    fun getFadeProgress() = animProgress

    override val values = super.values.toMutableList().also {
        styles.map {
            style -> style.values.forEach { value ->
                val displayableFunction = value.displayableFunction
                it.add(value.displayable { displayableFunction.invoke() && styleValue.get() == style.name })
            }
        }
    }

}
