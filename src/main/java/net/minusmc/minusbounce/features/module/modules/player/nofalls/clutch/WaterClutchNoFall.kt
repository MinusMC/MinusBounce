package net.minusmc.minusbounce.features.module.modules.player.nofalls.clutch

import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemBucket
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.Vec3
import net.minusmc.minusbounce.event.EventState
import net.minusmc.minusbounce.event.MotionEvent
import net.minusmc.minusbounce.features.module.modules.player.nofalls.NoFallMode
import net.minusmc.minusbounce.utils.RotationUtils
import net.minusmc.minusbounce.utils.VecRotation
import net.minusmc.minusbounce.utils.misc.NewFallingPlayer
import net.minusmc.minusbounce.utils.timer.TickTimer
import net.minusmc.minusbounce.value.FloatValue
import kotlin.math.ceil
import kotlin.math.sqrt

class WaterClutchNoFall: NoFallMode("WaterClutch") {
    private val minFallDistanceValue = FloatValue("MinMLGHeight", 5F, 2F, 50F, "m")
    
    private val mlgTimer = TickTimer()
    private var currentMlgRotation: VecRotation? = null
    private var currentMlgItemIndex = 0
    private var currentMlgBlock: BlockPos? = null

	override fun onMotion(event: MotionEvent) {
		if (event.eventState == EventState.PRE) {
            currentMlgRotation = null
            mlgTimer.update()

            if (!mlgTimer.hasTimePassed(10)) return

            if (mc.thePlayer.fallDistance > minFallDistanceValue.get()) {
                val newFallingPlayer = NewFallingPlayer(mc.thePlayer)
                val maxDist = mc.playerController.blockReachDistance + 1.5
                val collision =
                    newFallingPlayer.findCollision(ceil(1.0 / mc.thePlayer.motionY * -maxDist).toInt()) ?: return
                var ok = Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.eyeHeight, mc.thePlayer.posZ).distanceTo(Vec3(collision).addVector(0.5, 0.5, 0.5)) < mc.playerController.blockReachDistance + sqrt(0.75)
                if (mc.thePlayer.motionY < collision.y + 1 - mc.thePlayer.posY)
                    ok = true

                if (!ok) return

                var index = -1

                for (i in 36..44) {
                    val itemStack = mc.thePlayer.inventoryContainer.getSlot(i).stack

                    if (itemStack != null && (itemStack.item == Items.water_bucket || itemStack.item is ItemBlock && (itemStack.item as ItemBlock).block == Blocks.web)) {
                        index = i - 36

                        if (mc.thePlayer.inventory.currentItem == index)
                            break
                    }
                }

                if (index == -1)
                    return

                currentMlgItemIndex = index
                currentMlgBlock = collision

                if (mc.thePlayer.inventory.currentItem != index) {
                    mc.thePlayer.sendQueue.addToSendQueue(C09PacketHeldItemChange(index))
                }

                currentMlgRotation = RotationUtils.faceBlock(collision)
                currentMlgRotation!!.rotation.toPlayer(mc.thePlayer)
            }
        } else if (currentMlgRotation != null) {
            val stack = mc.thePlayer.inventory.mainInventory[currentMlgItemIndex]

            if (stack.item is ItemBucket)
                mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, stack)
            else if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, stack, currentMlgBlock, EnumFacing.UP, Vec3(0.0, 0.5, 0.0).add(Vec3(currentMlgBlock ?: return))))
                mlgTimer.reset()

            if (mc.thePlayer.inventory.currentItem != currentMlgItemIndex)
                mc.thePlayer.sendQueue.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
        }
	}
}