/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.player

import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.MoveEvent
import net.minusmc.minusbounce.event.UpdateEvent
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.utils.timer.MSTimer
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.value.IntegerValue
import net.minusmc.minusbounce.value.ListValue
import net.minecraft.item.ItemBucketMilk
import net.minecraft.item.ItemFood
import net.minecraft.item.ItemPotion
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook
import net.minusmc.minusbounce.utils.*

@ModuleInfo(name = "FastUse", spacedName = "Fast Use", description = "Allows you to use items faster.", category = ModuleCategory.PLAYER)
class FastUse : Module() {

    private val modeValue = ListValue("Mode", arrayOf("Instant", "OldGrim", "BetterGrim", "NewGrim", "NCP", "Matrix", "AAC", "CustomDelay", "DelayedInstant", "AACv4_2", "Minemora"), "NCP")

    private val instantDurationDelay = IntegerValue("InstantDurationDelay", 14, 0, 35) {modeValue.get().equals("DelayedInstant")}

    private val delayValue = IntegerValue("CustomDelay", 0, 0, 300) { modeValue.get().equals("customdelay", true) }
    private val customSpeedValue = IntegerValue("CustomSpeed", 2, 0, 35, " packet") {
        modeValue.get().equals("customdelay", true)
    }
    private val customTimer = FloatValue("CustomTimer", 1.1f, 0.5f, 2f, "x") {
        modeValue.get().equals("customdelay", true)
    }

    private val noMoveValue = BoolValue("NoMove", false)

    private val customDelayTimer = MSTimer()
    private var usedTimer = false
    
    private fun send(int: Int) {
        repeat(int) {
            mc.netHandler.addToSendQueue(C03PacketPlayer(mc.thePlayer.onGround))
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (usedTimer) {
            mc.timer.timerSpeed = 1F
            usedTimer = false
        }

        if (!mc.thePlayer.isUsingItem) {
            customDelayTimer.reset()
            return
        }

        val usingItem = mc.thePlayer.itemInUse.item

        if (usingItem is ItemFood || usingItem is ItemBucketMilk || usingItem is ItemPotion) {
            when (modeValue.get().lowercase()) {
                "matrix" -> {
                    mc.timer.timerSpeed = 0.5f
                    usedTimer = true
                }

                "instant" -> {
                    send(32)
                    mc.playerController.onStoppedUsingItem(mc.thePlayer)
                }

                "ncp" -> if (mc.thePlayer.itemInUseDuration > 14) {
                    send(20)
                    mc.playerController.onStoppedUsingItem(mc.thePlayer)
                }

                "aac" -> {
                    mc.timer.timerSpeed = 1.1F
                    usedTimer = true
                }

                "grim" -> {
                    mc.timer.timerSpeed = 0.3F
                    usedTimer = true
                    send(34)
                }

                "newgrim" -> {
                    repeat(5) {
                        PacketUtils.sendPacketNoEvent(C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, mc.thePlayer.onGround))
                    }
                }

                "oldgrim" -> {
                    repeat(5) {
                        PacketUtils.sendPacketNoEvent(C03PacketPlayer(mc.thePlayer.onGround))
                    }
                }

                "customdelay" -> {
                    mc.timer.timerSpeed = customTimer.get()
                    usedTimer = true

                    if (!customDelayTimer.hasTimePassed(delayValue.get().toLong())) return

                    send(customSpeedValue.get())
                    customDelayTimer.reset()
                }

                "delayedinstant" -> if (mc.thePlayer.itemInUseDuration > instantDurationDelay.get()) {
                    repeat(36 - mc.thePlayer.itemInUseDuration) {
                        mc.netHandler.addToSendQueue(C03PacketPlayer(mc.thePlayer.onGround))
                    }

                    mc.playerController.onStoppedUsingItem(mc.thePlayer)
                }

                "aacv4_2" -> {
                    mc.timer.timerSpeed = 0.49F
                    usedTimer = true
                    if (mc.thePlayer.itemInUseDuration > 13) {
                        send(23)
                        mc.playerController.onStoppedUsingItem(mc.thePlayer)
                    }
                }
                "minemora" -> {
                    mc.timer.timerSpeed = 0.5F
                    usedTimer = true
                    if (mc.thePlayer.ticksExisted % 2 == 0) send(2)
                }
            }
        }
    }

    @EventTarget
    fun onMove(event: MoveEvent?) {
        event ?: return

        if (!state || !mc.thePlayer.isUsingItem || !noMoveValue.get()) return
        val usingItem = mc.thePlayer.itemInUse.item
        if (usingItem is ItemFood || usingItem is ItemBucketMilk || usingItem is ItemPotion)
            event.zero()
    }

    override fun onDisable() {
        if (usedTimer) {
            mc.timer.timerSpeed = 1F
            usedTimer = false
        }
    }

    override val tag: String
        get() = modeValue.get()
}
