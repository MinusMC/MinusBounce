/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.client

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.KeyEvent
import net.minusmc.minusbounce.event.TickEvent
import net.minusmc.minusbounce.event.Render2DEvent
import net.minusmc.minusbounce.event.UpdateEvent
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.ui.client.hud.designer.GuiHudDesigner
import net.minusmc.minusbounce.ui.font.Fonts
import net.minusmc.minusbounce.utils.render.AnimationUtils
import net.minusmc.minusbounce.utils.render.RenderUtils
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.value.FontValue
import net.minusmc.minusbounce.value.IntegerValue
import net.minusmc.minusbounce.value.ListValue
import net.minusmc.minusbounce.value.TextValue

/*
TASK:
- More button from pvp client
Lunar, Badlion, ...
 */
@ModuleInfo(name = "HUD", description = "Toggles visibility of the HUD.", category = ModuleCategory.CLIENT, array = false)
class HUD : Module() {
    val tabHead = BoolValue("Tab-HeadOverlay", true)
    val animHotbarValue = BoolValue("AnimatedHotbar", true)
    val blackHotbarValue = BoolValue("BlackHotbar", true)
    val inventoryParticle = BoolValue("InventoryParticle", false)

    // Chat
    val fontChatValue = BoolValue("FontChat", false)
    val cmdBorderValue = BoolValue("CommandChatBorder", true)
    val fontType = FontValue("Font", Fonts.font40) { fontChatValue.get() }
    val chatRectValue = BoolValue("ChatRect", true)
    val chatCombineValue = BoolValue("ChatCombine", true)
    val chatAnimationValue = BoolValue("ChatAnimation", true)
    val chatAnimationSpeedValue = FloatValue("Chat-AnimationSpeed", 0.1F, 0.01F, 0.1F)

    private val toggleMessageValue = BoolValue("DisplayToggleMessage", false)
    private val toggleSoundValue = ListValue("ToggleSound", arrayOf("None", "Default", "Custom"), "Default")
    private val toggleVolumeValue = IntegerValue("ToggleVolume", 100, 0, 100) {
        toggleSoundValue.get().equals("custom", true)
    }
    val guiButtonStyle = ListValue("Button-Style", arrayOf("Vanilla", "Rounded", "LiquidBounce", "LiquidBounce+"), "Vanilla")

    val containerBackground = BoolValue("Container-Background", false)
    val containerButton = ListValue("Container-Button", arrayOf("TopLeft", "TopRight", "Off"), "TopLeft")
    val invEffectOffset = BoolValue("InvEffect-Offset", false)
    val domainValue = TextValue("Scoreboard-Domain", ".hud scoreboard-domain <your domain here>")

    private var hotBarX = 0F

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        if (mc.currentScreen is GuiHudDesigner) return
        MinusBounce.hud.render(false)
    }

    @EventTarget(ignoreCondition = true)
    fun onTick(event: TickEvent) {
        if (MinusBounce.moduleManager.shouldNotify != toggleMessageValue.get())
            MinusBounce.moduleManager.shouldNotify = toggleMessageValue.get()

        if (MinusBounce.moduleManager.toggleSoundMode != toggleSoundValue.values.indexOf(toggleSoundValue.get()))
            MinusBounce.moduleManager.toggleSoundMode = toggleSoundValue.values.indexOf(toggleSoundValue.get())

        if (MinusBounce.moduleManager.toggleVolume != toggleVolumeValue.get().toFloat())
            MinusBounce.moduleManager.toggleVolume = toggleVolumeValue.get().toFloat()
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        MinusBounce.hud.update()
    }

    @EventTarget
    fun onKey(event: KeyEvent) {
        MinusBounce.hud.handleKey('a', event.key)
    }

    fun getAnimPos(pos: Float): Float {
        hotBarX = if (state && animHotbarValue.get()) AnimationUtils.animate(
            pos,
            hotBarX,
            0.02F * RenderUtils.deltaTime.toFloat()
        )
        else pos

        return hotBarX
    }

    init {
        state = true
    }
}