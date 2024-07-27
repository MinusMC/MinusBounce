package net.minusmc.minusbounce.features.module.modules.movement.longjumps.other

import net.minecraft.network.play.client.C03PacketPlayer.C05PacketPlayerLook
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.util.BlockPos
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.MoveEvent
import net.minusmc.minusbounce.features.module.modules.movement.longjumps.LongJumpMode
import net.minusmc.minusbounce.ui.client.hud.element.elements.Notification
import net.minusmc.minusbounce.utils.player.MovementUtils
import net.minusmc.minusbounce.utils.PlayerUtils
import net.minusmc.minusbounce.value.FloatValue

class PearlLongJump: LongJumpMode("Pearl") {
	private val pearlBoostValue = FloatValue("Boost", 4.25F, 0F, 10F)
    private val pearlHeightValue = FloatValue("Height", 0.42F, 0F, 10F)
    private val pearlTimerValue = FloatValue("Timer", 1F, 0.05F, 10F)

    private var pearlState = 0

    override fun onEnable() {
    	pearlState = 0
    }

	override fun onUpdate() {
		val enderPearlSlot = PlayerUtils.getPearlSlot()
        if (pearlState == 0) {
            if (enderPearlSlot == -1) {
                MinusBounce.hud.addNotification(Notification("LongJump", "You don't have any ender pearl!", Notification.Type.ERROR))
                pearlState = -1
                longjump.state = false
                return                    
            }

            if (mc.thePlayer.inventory.currentItem != enderPearlSlot)
                mc.netHandler.addToSendQueue(C09PacketHeldItemChange(enderPearlSlot))

            val itemStack = mc.thePlayer.inventoryContainer.getSlot(enderPearlSlot + 36).stack

            mc.thePlayer.sendQueue.addToSendQueue(C05PacketPlayerLook(mc.thePlayer.rotationYaw, 90f, mc.thePlayer.onGround))
            mc.thePlayer.sendQueue.addToSendQueue(C08PacketPlayerBlockPlacement(BlockPos(-1.0, -1.0, -1.0), 255, itemStack, 0f, 0f, 0f))
            
            if (enderPearlSlot != mc.thePlayer.inventory.currentItem)
                mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))                    

            pearlState = 1                    
        }

        if (pearlState == 1 && mc.thePlayer.hurtTime > 0) {
            pearlState = 2
            MovementUtils.strafe(pearlBoostValue.get())
            mc.thePlayer.motionY = pearlHeightValue.get().toDouble()
        }

        if (pearlState == 2) 
            mc.timer.timerSpeed = pearlTimerValue.get()
	}

	override fun onMove(event: MoveEvent) {
		if (pearlState != 2)
            event.isCancelled = true
	}
}
