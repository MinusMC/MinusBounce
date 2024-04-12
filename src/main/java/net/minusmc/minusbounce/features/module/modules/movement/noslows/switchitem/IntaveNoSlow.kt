package net.minusmc.minusbounce.features.module.modules.movement.noslows.switchitem

import net.minecraft.item.*
import net.minecraft.network.Packet
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minusmc.minusbounce.event.PostMotionEvent
import net.minusmc.minusbounce.event.PreMotionEvent
import net.minusmc.minusbounce.features.module.modules.movement.noslows.NoSlowMode
/**
* @Author : longathelstan
* @Date : 12/4/2024 
**/
class IntaveNoSlow : NoSlowMode("Intave") {
    override fun onPreMotion(event: PreMotionEvent) {
        if (shouldSkip()) return
        mc.thePlayer.sendQueue.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
        handleItemUsage()
    }

    override fun onPostMotion(event: PostMotionEvent) {
        if (shouldSkip()) return
        mc.thePlayer.inventory.getCurrentItem()?.let {
            mc.thePlayer.sendQueue.addToSendQueue(C08PacketPlayerBlockPlacement(it))
        }
    }

    override fun onUpdate() {
        if (shouldSkip()) return
        handleItemUsage()
    }

    private fun handleItemUsage() {
        mc.thePlayer.getCurrentEquippedItem()?.item?.let { item ->
            if (item is ItemSword || item is ItemBow || isConsumable(item)) switchItem()
        }
    }

    private fun isConsumable(item: Item?): Boolean = when (item) {
        is ItemFood, is ItemBucketMilk, is ItemPotion -> true
        else -> false
    }

    private fun shouldSkip(): Boolean = mc.thePlayer.getCurrentEquippedItem()?.let {
        !mc.thePlayer.isUsingItem() || (mc.thePlayer.moveForward == 0.0f && mc.thePlayer.moveStrafing == 0.0f)
    } ?: true

    private fun switchItem() {
        mc.thePlayer.inventory.currentItem.let { currentItem ->
            val slotIDtoSwitch = if (currentItem == 7) currentItem - 2 else currentItem + 2
            sendPacket(C09PacketHeldItemChange(slotIDtoSwitch))
            sendPacket(C09PacketHeldItemChange(currentItem))
        }
    }

    private fun sendPacket(packet: Packet<*>) = mc.thePlayer.sendQueue.addToSendQueue(packet)
}
