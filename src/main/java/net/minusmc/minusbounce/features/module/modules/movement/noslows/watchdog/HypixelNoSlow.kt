package net.minusmc.minusbounce.features.module.modules.movement.noslows.watchdog

import net.minecraft.item.*
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.util.BlockPos
import net.minecraft.util.MovingObjectPosition
import net.minusmc.minusbounce.utils.extensions.rayTraceCustom
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.event.PostMotionEvent
import net.minusmc.minusbounce.features.module.modules.combat.KillAura
import net.minusmc.minusbounce.features.module.modules.movement.noslows.NoSlowMode
import net.minusmc.minusbounce.utils.PacketUtils
import net.minusmc.minusbounce.utils.Rotation
import net.minusmc.minusbounce.utils.player.RotationUtils

class HypixelNoSlow : NoSlowMode("Hypixel") {
    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (!mc.thePlayer.isEating) {
            if (packet is C08PacketPlayerBlockPlacement) {
                if (mc.gameSettings.keyBindUseItem.isKeyDown && mc.thePlayer.heldItem != null && (mc.thePlayer.heldItem.item is ItemFood || mc.thePlayer.heldItem.item is ItemBucketMilk || mc.thePlayer.heldItem.item is ItemPotion && !ItemPotion.isSplash(
                        mc.thePlayer.heldItem.metadata
                    ) || mc.thePlayer.heldItem.item is ItemBow)
                ) {
                    if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit === MovingObjectPosition.MovingObjectType.BLOCK && packet.position != BlockPos(
                            -1,
                            -1,
                            1
                        )
                    ) return
                    event.cancelEvent()
                    val position: MovingObjectPosition = mc.thePlayer.rayTraceCustom(
                        mc.playerController.blockReachDistance.toDouble(), mc.thePlayer.rotationYaw, 90f
                    )
                        ?: return
                    val rot = Rotation(mc.thePlayer.rotationYaw, 90f)
                    RotationUtils.setTargetRotation(rot)
                    sendUseItem(position)
                }
            }
        }
    }

    override fun onPostMotion(event: PostMotionEvent) {
        if ((mc.thePlayer.isUsingItem || MinusBounce.moduleManager[KillAura::class.java]!!.blockingStatus) && mc.thePlayer.heldItem != null && mc.thePlayer.heldItem.item is ItemSword) {
            mc.netHandler.addToSendQueue(
                C08PacketPlayerBlockPlacement(
                    mc.thePlayer.inventoryContainer.getSlot(
                        mc.thePlayer.inventory.currentItem + 36
                    ).stack
                )
            )
        }
    }
    private fun sendUseItem(mouse: MovingObjectPosition) {
        val facingX = (mouse.hitVec.xCoord - mouse.blockPos.x.toDouble()).toFloat()
        val facingY = (mouse.hitVec.yCoord - mouse.blockPos.y.toDouble()).toFloat()
        val facingZ = (mouse.hitVec.zCoord - mouse.blockPos.z.toDouble()).toFloat()
        PacketUtils.sendPacketNoEvent(
            C08PacketPlayerBlockPlacement(
                mouse.blockPos,
                mouse.sideHit.index,
                mc.thePlayer.heldItem,
                facingX,
                facingY,
                facingZ
            )
        )
    }
}
