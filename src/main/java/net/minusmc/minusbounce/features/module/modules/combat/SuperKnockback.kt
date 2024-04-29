/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.combat

import net.minecraft.client.settings.GameSettings
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.client.C0BPacketEntityAction
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
    private val hurtTimeValue = IntegerValue("HurtTime", 10, 0, 10)
    private val modeValue = ListValue("Mode", arrayOf("ExtraPacket", "Legit", "LegitFast", "Silent", "WTap", "Packet", "Zitter"), "ExtraPacket")
    //custom useless mode :V
    private val delay = IntegerValue("Delay", 0, 0, 500, "ms")

    private val more = BoolValue("More", false)
    private val moreMode = ListValue("More-mode", arrayOf("Release", "CancelMovement"), "Release") {more.get()}
    private val minDistance = FloatValue("Min", 2.3F, 0F, 4F, "m") {more.get()}
    private val maxDistance = FloatValue("Max", 4.0F, 3F, 7F, "m") {more.get()}
    private val keepTick = IntegerValue("Keep", 10, 0, 40, "tick") {more.get()}
    private val restTick = IntegerValue("Rest", 4, 0, 40, "tick") {more.get()}
    private val onlyForward = BoolValue("OnlyForward", true) {more.get()}
    private val onlyNoHurt = BoolValue("OnlyNoHurt", true) {more.get()}

    val timer = MSTimer()
    private val tick = TickTimer()
    private val zitterTimer = MSTimer()
    
    private var ticks = 0
    private var isHit = false
    var target: EntityPlayer? = null
    private val binds = arrayOf(
        mc.gameSettings.keyBindForward,
        mc.gameSettings.keyBindBack,
        mc.gameSettings.keyBindRight,
        mc.gameSettings.keyBindLeft
    )
    private var zitterDirection = false

    override fun onEnable() {
        isHit = false
    }
    @EventTarget
    // I added since LB only have one SuperKnockback mode.ik there is superkb script that better than this
    fun onAttack(event: AttackEvent) {
        if (more.get()) target = if (event.targetEntity is EntityPlayer) event.targetEntity else return
        if (event.targetEntity is EntityLivingBase) {
            if (event.targetEntity.hurtTime > hurtTimeValue.get() || !timer.hasTimePassed(delay.get().toLong()))
                return
            when (modeValue.get().lowercase()) {
                "extrapacket" -> {
                    if (mc.thePlayer.isSprinting)
                        mc.thePlayer.isSprinting = true
                    mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING))

                    mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING))
                    mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING))
                    mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING))
                    mc.thePlayer.serverSprintState = true
                }
                "silent" -> ticks = 1
                "legit", "wtap" -> ticks = 2
                "legitfast" -> {
                    if (mc.thePlayer.hurtTime === 10) {
                        if (mc.thePlayer.isSprinting()) {
                            mc.thePlayer.isSprinting = false
                        }
                        mc.getNetHandler().addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING))
                        mc.thePlayer.serverSprintState = true
                    }
                }
                "packet" -> {
                    if(mc.thePlayer.isSprinting)
                        mc.thePlayer.isSprinting = true
                    mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING))
                    mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING))
                    mc.thePlayer.serverSprintState = true
                }
                "zitter" -> {
                    if (mc.thePlayer.hurtTime == 10 && mc.thePlayer.onGround) {
                        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindRight)) mc.gameSettings.keyBindRight.pressed = false
                        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindLeft)) mc.gameSettings.keyBindLeft.pressed = false
                        if (zitterTimer.hasTimePassed(100)) {
                            zitterDirection = !zitterDirection
                            mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING))
                            mc.thePlayer.serverSprintState = true
                            zitterTimer.reset()
                        }
                        if (zitterDirection) {
                            mc.gameSettings.keyBindRight.pressed = true
                            mc.gameSettings.keyBindLeft.pressed = false
                        } else {
                            mc.gameSettings.keyBindRight.pressed = false
                            mc.gameSettings.keyBindLeft.pressed = true
                        }
                    }
                }
            }
            timer.reset()
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        when (modeValue.get().lowercase()) {
            "legit" -> if (ticks == 2) {
                mc.gameSettings.keyBindForward.pressed = false
                ticks = 1
            } else if (ticks == 1) {
                mc.gameSettings.keyBindForward.pressed = true
                ticks = 0
            }
            "wtap" -> if (ticks == 2) {
                mc.thePlayer.isSprinting = false
                ticks = 1
            } else if (ticks == 1) {
                mc.thePlayer.isSprinting = true
                ticks = 0
            }
            "slient" -> if (ticks == 1) {
                mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING))
                ticks = 2
            } else if (ticks == 2) {
                mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING))
                ticks = 0
            }
        }
        if (more.get()) {
            if (target == null) return
            if (onlyNoHurt.get() && mc.thePlayer.hurtTime > 0) return
            if (tick.hasTimePassed(keepTick.get() + restTick.get())) tick.reset()
            tick.update()
            val distance = mc.thePlayer.getDistanceToEntityBox(target!!)
            if (target!!.isDead || distance >= maxDistance.get()) {
                target = null
                for (bind in binds) bind.pressed = GameSettings.isKeyDown(bind)
                return
            }
            if (moreMode.get().equals("Release")) {
                if (distance <= minDistance.get() && !tick.hasTimePassed(keepTick.get())) {
                    if (onlyForward.get()) mc.gameSettings.keyBindForward.pressed = false
                    else for (bind in binds) bind.pressed = false
                } else {
                    for (bind in binds) bind.pressed = GameSettings.isKeyDown(bind)
                }
            }
        }
    }

    @EventTarget
    fun onStrafe(event: StrafeEvent) {
        if (moreMode.get().equals("CancelMovement")) {
            target?.let {
                if (mc.thePlayer.getDistanceToEntityBox(it) <= minDistance.get() && !tick.hasTimePassed(keepTick.get())) {
                    if (!onlyForward.get() || event.forward > 0F) {
                        event.cancelEvent()
                    }
                }
            }
        }
    }
    override val tag: String
        get() = modeValue.get()
}
