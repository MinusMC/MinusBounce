/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.combat

import net.minecraft.client.settings.GameSettings
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minusmc.minusbounce.event.AttackEvent
import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.UpdateEvent
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.value.IntegerValue
import net.minusmc.minusbounce.value.ListValue
import net.minusmc.minusbounce.utils.player.MovementUtils

@ModuleInfo(name = "SuperKnockback", spacedName = "Super Knockback", description = "Increases knockback dealt to other entities.", category = ModuleCategory.COMBAT)
class SuperKnockback : Module() {
    private val modeValue = ListValue("Mode", arrayOf("DoublePacket", "Packet", "LegitFast", "WTap", "STap", "SprintTap", "SneakTap", "SprintSilentTap"), "DoublePacket")
    private val hurtTimeValue = IntegerValue("HurtTime", 10, 0, 10)
    private val ticksDelay = IntegerValue("TicksDelay", 1, 1, 10)
    private var ticks = 0

    override fun onDisable() {
        ticks = 0
        mc.thePlayer.isSneaking = false
    }

    @EventTarget
    fun onAttack(event: AttackEvent) {
        val target = event.targetEntity

        if (target !is EntityLivingBase || !MovementUtils.isMoving || !mc.thePlayer.onGround)
            return

        if (target.hurtTime >= hurtTimeValue.get())
            return

        
        when (modeValue.get().lowercase()) {
            "doublepacket" -> {
                mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING))
                mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING))
                mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING))
                mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING))
                mc.thePlayer.serverSprintState = true
            }

            "packet" -> {
                mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING))
                mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING))
                mc.thePlayer.serverSprintState = true
            }

            "legitfast" -> {
                if (mc.thePlayer.isSprinting)
                    mc.thePlayer.isSprinting = false
                mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING))
                mc.thePlayer.serverSprintState = true
            }
            
            "wtap", "stap", "sneaktap", "sprintsilenttap" -> ticks = ticksDelay.get() + 2
            "sprinttap" -> mc.thePlayer.sprintState = 2
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        ticks--

        if (ticks == ticksDelay.get() + 1) {
            when (modeValue.get().lowercase()) {
                "wtap" -> mc.gameSettings.keyBindForward.pressed = false
                "stap" -> {
                    mc.gameSettings.keyBindForward.pressed = true
                    mc.gameSettings.keyBindBack.pressed = true
                }
                "sprintsilenttap" -> mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING))
                "sneaktap" -> mc.gameSettings.keyBindSneak.pressed = true
            }
        } else if (ticks == 1) {
            when (modeValue.get().lowercase()) {
                "wtap" -> mc.gameSettings.keyBindForward.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindForward)
                "stap" -> {
                    mc.gameSettings.keyBindForward.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindForward)
                    mc.gameSettings.keyBindBack.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindBack)
                }
                "sprintsilenttap" -> mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING))
                "sneaktap" -> mc.gameSettings.keyBindSneak.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)
            }
        }
    }

    override val tag: String
        get() = modeValue.get()
}
