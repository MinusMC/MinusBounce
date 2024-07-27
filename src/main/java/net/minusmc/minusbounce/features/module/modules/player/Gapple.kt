/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.player

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.ui.client.hud.element.elements.Notification
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.value.IntegerValue
import net.minusmc.minusbounce.value.ListValue
import net.minecraft.init.Items
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook
import net.minusmc.minusbounce.utils.*
import net.minusmc.minusbounce.utils.timer.MSTimer

@ModuleInfo(name = "Gapple", description = "Eat Gapples.", category = ModuleCategory.PLAYER)
class Gapple : Module() {
    private val modeValue = ListValue("Mode", arrayOf("Auto", "Once", "Head"), "Once")
    private val healthValue = FloatValue("Health", 10F, 1F, 20F)
    private val delayValue = IntegerValue("Delay", 150, 0, 1000, "ms")
    private val noAbsorption = BoolValue("NoAbsorption", true)
    private val grim = BoolValue("Grim", true)
    private val timer = MSTimer()


    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        when (modeValue.get().lowercase()) {
            "once" -> {
                doEat(true)
                state = false
            }
            "auto" -> {
                if (!timer.hasTimePassed(delayValue.get()))
                    return

                if (mc.thePlayer.health <= healthValue.get()){
                    doEat(false)
                    timer.reset()
                }
            }
            "head" -> {
                if (!timer.hasTimePassed(delayValue.get()))
                    return

                if (mc.thePlayer.health > healthValue.get())
                    return

                val headInHotbar = InventoryUtils.findItem(36, 45, Items.skull)
                if (headInHotbar != -1) {
                    mc.netHandler.addToSendQueue(C09PacketHeldItemChange(headInHotbar - 36))
                    mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.heldItem))
                    mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                    timer.reset()
                }
            }
        }
    }

    private fun doEat(warn: Boolean) {
        if (noAbsorption.get() && !warn && mc.thePlayer.absorptionAmount > 0)
            return

        val gappleInHotbar = InventoryUtils.findItem(36, 45, Items.golden_apple)
        if (gappleInHotbar != -1) {
            mc.netHandler.addToSendQueue(C09PacketHeldItemChange(gappleInHotbar - 36))
            mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.heldItem))
            repeat(35) {
                if (grim.get()) 
                    PacketUtils.sendPacketNoEvent(C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, mc.thePlayer.onGround))
                else
                    PacketUtils.sendPacketNoEvent(C03PacketPlayer(mc.thePlayer.onGround))

            }
            PacketUtils.sendPacketNoEvent(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
        } else if (warn)
            MinusBounce.hud.addNotification(Notification("Gapple", "No Gapple were found in hotbar.", Notification.Type.ERROR))
    }

    override val tag: String
        get() = modeValue.get()
}
