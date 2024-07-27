/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.combat

import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.UpdateEvent
import net.minusmc.minusbounce.event.SentPacketEvent
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.utils.PacketUtils
import net.minusmc.minusbounce.utils.player.RotationUtils
import net.minusmc.minusbounce.utils.timer.MSTimer
import net.minusmc.minusbounce.value.IntegerValue
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.ListValue
import net.minecraft.item.ItemBow
import net.minecraft.network.play.client.C03PacketPlayer.C05PacketPlayerLook
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing

@ModuleInfo(name = "FastBow", spacedName = "Fast Bow", description = "Turns your bow into a machine gun.", category = ModuleCategory.COMBAT)
class FastBow : Module() {

    private val modeValue = ListValue("Mode", arrayOf("Normal", "1.17"), "Normal")
    private val packetsValue = IntegerValue("Packets", 3, 3, 20)
    private val delay = IntegerValue("Delay", 0, 0, 500, "ms")


    val timer = MSTimer()

    private var packetCount = 0

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (!mc.thePlayer.isUsingItem) {
            packetCount = 0
            return
        }

        val itemStack = mc.thePlayer.inventory.getCurrentItem() ?: return

        if (itemStack.item is ItemBow) {
            if (packetCount == 0)
                PacketUtils.sendPacketNoEvent(C08PacketPlayerBlockPlacement(BlockPos.ORIGIN, 255, mc.thePlayer.currentEquippedItem, 0F, 0F, 0F))

            val rotation = RotationUtils.currentRotation ?: mc.thePlayer.rotation

            if (delay.get() == 0) {

                repeat(packetsValue.get()) {
                    when (modeValue.get().lowercase()) {
                        "normal" -> PacketUtils.sendPacketNoEvent(C05PacketPlayerLook(rotation.yaw, rotation.pitch, true))
                        "1.17" -> PacketUtils.sendPacketNoEvent(C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, rotation.yaw, rotation.pitch, true))
                    }
                }

                PacketUtils.sendPacketNoEvent(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
            } else {
                if (timer.hasTimePassed(delay.get().toLong())) {
                    packetCount++
                    when (modeValue.get().lowercase()) {
                        "normal" -> PacketUtils.sendPacketNoEvent(C05PacketPlayerLook(rotation.yaw, rotation.pitch, true))
                        "1.17" -> PacketUtils.sendPacketNoEvent(C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, rotation.yaw, rotation.pitch, true))
                    }
                    timer.reset()
                }
                if (packetCount == packetsValue.get())
                    PacketUtils.sendPacketNoEvent(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
            }
            mc.thePlayer.itemInUseCount = itemStack.maxItemUseDuration - 1
        }
    }

    @EventTarget
    fun onSentPacket(event: SentPacketEvent) {
        mc.thePlayer ?: return

        val packet = event.packet
        val itemStack = mc.thePlayer.inventory.getCurrentItem() ?: return

        if (itemStack.item !is ItemBow)
            return

        if (packet is C08PacketPlayerBlockPlacement) {
            event.isCancelled = true
            return
        }

        if (packet is C07PacketPlayerDigging && packet.status == C07PacketPlayerDigging.Action.RELEASE_USE_ITEM)
            event.isCancelled = true
    }
}