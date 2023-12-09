package net.minusmc.minusbounce.features.module.modules.misc

import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.util.MathHelper
import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.MotionEvent
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.event.Render2DEvent
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.ui.font.Fonts
import net.minusmc.minusbounce.utils.render.AnimationUtils
import net.minusmc.minusbounce.utils.render.RenderUtils
import net.minusmc.minusbounce.utils.render.Stencil
import net.minusmc.minusbounce.utils.timer.MSTimer
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.IntegerValue
import net.minusmc.minusbounce.value.ListValue
import net.minusmc.minusbounce.value.TextValue
import java.awt.Color
import java.text.DecimalFormat

@ModuleInfo(name = "AutoHypixel", spacedName = "Auto Hypixel", description = "Auto Join Hypixel.", category = ModuleCategory.MISC)
class AutoHypixel: Module() {

    private val delayValue = IntegerValue("Delay", 0, 0, 5000, "ms")
    private val autoGGValue = BoolValue("Auto-GG", true)
    private val ggMessageValue = TextValue("GG-Message", "GG") { autoGGValue.get() }
    private val checkValue = BoolValue("CheckGameMode", true)
    private val antiSnipeValue = BoolValue("AntiSnipe", true)
    private val renderValue = BoolValue("Render", true)
    private val modeValue = ListValue("Mode", arrayOf("Solo", "Teams", "Ranked", "Mega"), "Solo")
    private val soloTeamsValue = ListValue("Solo/Teams-Mode", arrayOf("Normal", "Insane", "Insane")) {
        modeValue.get().equals("solo", true) || modeValue.get().equals("teams", true)
    }
    private val megaValue = ListValue("Mega-Mode", arrayOf("Normal", "Doubles"), "Normal") {
        modeValue.get().equals("mega", true)
    }

    private val timer = MSTimer()

    private val gameMode = "NONE"
    private var shouldChangeGame = false

    private val dFormat = DecimalFormat("0.0")

    private var posY = -20F

    private val strings = arrayOf(
            "1st Killer -",
            "1st Place -",
            "died! Want to play again? Click here!",
            "won! Want to play again? Click here!",
            "- Damage Dealt -",
            "1st -",
            "Winning Team -",
            "Winners:",
            "Winner:",
            "Winning Team:",
            " win the game!",
            "1st Place:",
            "Last team standing!",
            "Winner #1 (",
            "Top Survivors",
            "Winners -")

    override fun onEnable() {
        shouldChangeGame = false
        timer.reset()
    }

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        if (checkValue.get() && !gameMode.lowercase().contains("skywars"))
            return

        val sc = ScaledResolution(mc)
        val middleX = sc.scaledWidth / 2F
        val detail =
            "Next game in " + dFormat.format(timer.hasTimeLeft(delayValue.get().toLong()).toFloat() / 1000F) + "s..."
        val middleWidth = Fonts.font40.getStringWidth(detail) / 2F
        val strength =
            MathHelper.clamp_float(timer.hasTimeLeft(delayValue.get().toLong()).toFloat() / delayValue.get(), 0F, 1F)
        val wid = strength * (5F + middleWidth) * 2F

        posY = AnimationUtils.animate((if (shouldChangeGame) 10F else -20F), posY, 0.25F * 0.05F * RenderUtils.deltaTime)
        if (!renderValue.get() || posY < -15)
            return

        Stencil.write(true)
        RenderUtils.drawRoundedRect(middleX - 5F - middleWidth, posY, middleX + 5F + middleWidth, posY + 15F, 3F, (0xA0000000).toInt())
        Stencil.erase(true)
        RenderUtils.drawRect(
            middleX - 5F - middleWidth,
            posY,
            middleX - 5F - middleWidth + wid,
            posY + 15F,
            Color(0.4F, 0.8F, 0.4F, 0.35F).rgb
        )
        Stencil.dispose()

        GlStateManager.resetColor()
        Fonts.fontSFUI40.drawString(detail, middleX - middleWidth - 1F, posY + 4F, -1)
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if ((!checkValue.get() || gameMode.lowercase().contains("skywars")) && shouldChangeGame && timer.hasTimePassed(delayValue.get().toLong())) {
            val mode = modeValue.get().lowercase()
            val mode2 = if (mode.equals("ranked", true)) "_normal" else if (mode.equals("mega", true)) "_${megaValue.get().lowercase()}" else "_${soloTeamsValue.get().lowercase()}" 
            mc.thePlayer.sendChatMessage("/play ${mode}${mode2}")
            shouldChangeGame = false
        }
        if (!shouldChangeGame) timer.reset()
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is S02PacketChat) {
            val text = packet.chatComponent.unformattedText ?: return
            if (antiSnipeValue.get() && text.contains("Sending you to")) {
                event.cancelEvent()
                return
            }

            for (s in strings)
                if (text.contains(s)) {
                    if (autoGGValue.get() && text.contains(strings[3])) mc.thePlayer.sendChatMessage(ggMessageValue.get())
                    shouldChangeGame = true
                    break
                }
        }
    }

}