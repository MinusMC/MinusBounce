package net.minusmc.minusbounce.ui.client.clickgui.styles.other.liquidbounceplus.extensions

import net.minusmc.minusbounce.features.module.modules.client.ClickGUI
import net.minusmc.minusbounce.utils.render.AnimationUtils
import net.minusmc.minusbounce.utils.render.RenderUtils

fun Float.animSmooth(target: Float, speed: Float) = if (ClickGUI.fastRenderValue.get()) target else AnimationUtils.animate(target, this, speed * RenderUtils.deltaTime * 0.025F)
fun Float.animLinear(speed: Float, min: Float, max: Float) = if (ClickGUI.fastRenderValue.get()) { if (speed < 0F) min else max } else (this + speed).coerceIn(min, max)