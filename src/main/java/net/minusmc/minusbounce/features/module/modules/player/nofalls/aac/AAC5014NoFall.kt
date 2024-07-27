package net.minusmc.minusbounce.features.module.modules.player.nofalls.aac

import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.util.BlockPos
import net.minusmc.minusbounce.features.module.modules.player.nofalls.NoFallMode
import net.minusmc.minusbounce.utils.block.BlockUtils

class AAC5014NoFall: NoFallMode("AAC5.0.14") {
    private var doFlag = false
    private var ticks = 0
    private var check = false

    override fun onEnable() {
        check = false
        ticks = 0
        doFlag = false
    }

    override fun onDisable() {
        check = false
        ticks = 0
        doFlag = false
    }
    
    override fun onUpdate() {
        var offsetY = 0.0
        check = false

        while (mc.thePlayer.motionY - 1.5 < offsetY) {
            val blockPos = BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + offsetY, mc.thePlayer.posZ)
            val block = BlockUtils.getBlock(blockPos)

            val boundingBox = block?.getCollisionBoundingBox(mc.theWorld, blockPos, BlockUtils.getState(blockPos))
            if (boundingBox != null) {
                check = true
                break
            }

            offsetY -= 0.5
        }

        if (mc.thePlayer.onGround) {
            mc.thePlayer.fallDistance = -2f
            check = false
        }

        if (ticks > 0)
            ticks--

        if (check && mc.thePlayer.fallDistance > 2.5 && !mc.thePlayer.onGround) {
            doFlag = true
            ticks = 18
        } else if (ticks < 2)
            doFlag = false

        if (doFlag)
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + if (mc.thePlayer.onGround) 0.5 else 0.42, mc.thePlayer.posZ, true))
    }
}