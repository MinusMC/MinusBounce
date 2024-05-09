/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.combat

import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.AttackEvent
import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.StrafeEvent
import net.minusmc.minusbounce.event.UpdateEvent
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.utils.extensions.getDistanceToEntityBox
import net.minusmc.minusbounce.utils.timer.MSTimer
import net.minusmc.minusbounce.utils.timer.TickTimer
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.value.IntegerValue
import net.minusmc.minusbounce.value.ListValue

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

        if (target !is EntityLivingBase)
            return

        if (target.hurtTime > hurtTimeValue.get())
            return

        when (modeValue.get().lowercase()) {
            "doublepacket" -> {
                if (mc.thePlayer.isSprinting)
                    mc.thePlayer.isSprinting = true
                mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING))
                mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING))
                mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING))
                mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING))
                mc.thePlayer.serverSprintState = true
            }
            "packet" -> {
                if(mc.thePlayer.isSprinting)
                    mc.thePlayer.isSprinting = true

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
            "wtap", "stap", "sprinttap", "sneaktap", "sprintsilenttap" -> ticks = ticksDelay.get() + 2
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        ticks--

        if (ticks == ticksDelay.get() + 1) {
            when (modeValue.get().lowercase()) {
                "wtap" -> mc.gameSettings.keyBindForward.pressed = false
                "stap" -> mc.gameSettings.keyBindBack.pressed = true
                "sprinttap" -> mc.thePlayer.isSprinting = false
                "sprintsilenttap" -> mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING))
                "sneaktap" -> mc.gameSettings.keyBindSneak.pressed = true
            }
        } else if (ticks == 1) {
            when (modeValue.get().lowercase()) {
                "wtap" -> mc.gameSettings.keyBindForward.pressed = true
                "stap" -> mc.gameSettings.keyBindBack.pressed = false
                "sprinttap" -> mc.thePlayer.isSprinting = true
                "sprintsilenttap" -> mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING))
                "sneaktap" -> mc.gameSettings.keyBindSneak.pressed = false
            }
        }
    }

    val canSprint: Boolean
        get() = !modeValue.get().equals("sprinttap", true) || ticks <= 1 || ticks >= ticksDelay.get() + 2

    override val tag: String
        get() = modeValue.get()
}
