package net.minusmc.minusbounce.features.module.modules.player.antivoids.normal

import net.minusmc.minusbounce.features.module.modules.player.antivoids.AntiVoidMode
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.util.BlockPos
import net.minecraft.block.BlockAir
import net.minusmc.minusbounce.utils.block.BlockUtils


class TPBackAntiVoid: AntiVoidMode("TPBack") {
    private var tried = false

    private var posX = 0.0
    private var posY = 0.0
    private var posZ = 0.0

    override fun onEnable() {
        tried = false
    }

    override fun onUpdate() {
        if (mc.thePlayer.onGround && BlockUtils.getBlock(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)) !is BlockAir) {
            posX = mc.thePlayer.prevPosX
            posY = mc.thePlayer.prevPosY
            posZ = mc.thePlayer.prevPosZ
        }
        if (!antivoid.voidOnlyValue.get() || isVoid) {
            if (mc.thePlayer.fallDistance > antivoid.maxFallDistValue.get() && !tried) {
                mc.thePlayer.setPositionAndUpdate(posX, posY, posZ)
                mc.thePlayer.fallDistance = 0F
                mc.thePlayer.motionX = 0.0
                mc.thePlayer.motionY = 0.0
                mc.thePlayer.motionZ = 0.0
                tried = true
            }
        }
    }
}