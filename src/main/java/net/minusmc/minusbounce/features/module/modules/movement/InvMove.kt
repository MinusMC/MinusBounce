/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.movement

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.features.module.modules.movement.Speed
import net.minusmc.minusbounce.event.ClickWindowEvent
import net.minusmc.minusbounce.event.EventState
import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.MotionEvent
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.event.UpdateEvent
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.utils.MovementUtils
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.ListValue
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.gui.GuiIngameMenu
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C16PacketClientStatus
import net.minecraft.client.settings.GameSettings

@ModuleInfo(name = "InvMove", spacedName = "Inv Move", description = "Allows you to walk while an inventory is opened.", category = ModuleCategory.MOVEMENT)
class InvMove : Module() {

    val modeValue = ListValue("Mode", arrayOf("Vanilla", "Silent", "Blink"), "Vanilla")
    private val sprintModeValue = ListValue("InvSprint", arrayOf("AACAP", "Stop", "Keep"), "Keep")
    private val noDetectableValue = BoolValue("NoDetectable", false)
    private val noMoveClicksValue = BoolValue("NoMoveClicks", false)

    private val playerPackets = mutableListOf<C03PacketPlayer>()

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val speedModule = MinusBounce.moduleManager.getModule(Speed::class.java) as Speed
        if (mc.currentScreen !is GuiChat && mc.currentScreen !is GuiIngameMenu && (!noDetectableValue.get() || mc.currentScreen !is GuiContainer)) {
            mc.gameSettings.keyBindForward.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindForward)
            mc.gameSettings.keyBindBack.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindBack)
            mc.gameSettings.keyBindRight.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindRight)
            mc.gameSettings.keyBindLeft.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindLeft)
            if (!speedModule.state || !speedModule.mode.modeName.equals("Legit", true)) 
                mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
            mc.gameSettings.keyBindSprint.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindSprint)

            if (sprintModeValue.get().equals("stop", true))
                mc.thePlayer.isSprinting = false
        }
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (event.eventState == EventState.PRE && playerPackets.size > 0 && (mc.currentScreen == null || mc.currentScreen is GuiChat || mc.currentScreen is GuiIngameMenu || (noDetectableValue.get() && mc.currentScreen is GuiContainer))) {
            playerPackets.forEach { mc.netHandler.addToSendQueue(it) }
            playerPackets.clear()
        }
    }

    @EventTarget
    fun onClick(event: ClickWindowEvent) {
        if (noMoveClicksValue.get() && MovementUtils.isMoving)
            event.cancelEvent()
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        when (modeValue.get().lowercase()) {
            "silent" -> if (packet is C16PacketClientStatus && packet.status == C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT) event.cancelEvent()
            "blink" -> if (mc.currentScreen != null && mc.currentScreen !is GuiChat && mc.currentScreen !is GuiIngameMenu && (!noDetectableValue.get() || mc.currentScreen !is GuiContainer) && packet is C03PacketPlayer) {
                event.cancelEvent()
                playerPackets.add(packet)
            }
        }
    }

    override fun onDisable() {
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindForward) || mc.currentScreen != null)
            mc.gameSettings.keyBindForward.pressed = false
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindBack) || mc.currentScreen != null)
            mc.gameSettings.keyBindBack.pressed = false
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindRight) || mc.currentScreen != null)
            mc.gameSettings.keyBindRight.pressed = false
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindLeft) || mc.currentScreen != null)
            mc.gameSettings.keyBindLeft.pressed = false
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindJump) || mc.currentScreen != null)
            mc.gameSettings.keyBindJump.pressed = false
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindSprint) || mc.currentScreen != null)
            mc.gameSettings.keyBindSprint.pressed = false
    }

    fun isAACAP(): Boolean = sprintModeValue.get().equals("aacap", true) && mc.currentScreen != null && mc.currentScreen !is GuiChat && mc.currentScreen !is GuiIngameMenu && (!noDetectableValue.get() || mc.currentScreen !is GuiContainer)

    override val tag: String
        get() = modeValue.get()
}
