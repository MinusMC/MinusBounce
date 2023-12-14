package net.minusmc.minusbounce.features.module.modules.player.nofalls.aac

import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.util.BlockPos
import net.minusmc.minusbounce.features.module.modules.player.nofalls.NoFallMode
import net.minusmc.minusbounce.utils.block.BlockUtils

class AAC5014NoFall: NoFallMode("AAC 5.0.14") {
    private var aac5doFlag = false
    private var aac5Timer = 0
    private var aac5Check = false

    override fun onEnable() {
        aac5Check = false
        aac5Timer = 0
        aac5doFlag = false
    }

    override fun onDisable() {
        aac5Check = false
        aac5Timer = 0
        aac5doFlag = false
    }
    
    override fun onUpdate() {
        var offsetYs = 0.0
        aac5Check = false

        while (mc.thePlayer.motionY - 1.5 < offsetYs) {
            val blockPos = BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + offsetYs, mc.thePlayer.posZ)
            val block = BlockUtils.getBlock(blockPos)
            val axisAlignedBB = block!!.getCollisionBoundingBox(mc.theWorld, blockPos, BlockUtils.getState(blockPos))
            if (axisAlignedBB != null) {
                offsetYs = -999.9
                aac5Check = true
            }
            offsetYs -= 0.5
        }

        if (mc.thePlayer.onGround) {
            mc.thePlayer.fallDistance = -2f
            aac5Check = false
        }

        if (aac5Timer > 0)
            aac5Timer--

        if (aac5Check && mc.thePlayer.fallDistance > 2.5 && !mc.thePlayer.onGround) {
            aac5doFlag = true
            aac5Timer = 18
        } else if (aac5Timer < 2)
            aac5doFlag = false

        if (aac5doFlag)
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + if (mc.thePlayer.onGround) 0.5 else 0.42, mc.thePlayer.posZ, true))
    }
}