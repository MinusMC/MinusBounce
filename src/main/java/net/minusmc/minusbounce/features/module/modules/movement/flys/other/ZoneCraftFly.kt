package net.minusmc.minusbounce.features.module.modules.movement.flys.other

import net.minusmc.minusbounce.event.MoveEvent
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyType
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyMode
import net.minusmc.minusbounce.utils.*
import net.minusmc.minusbounce.value.*
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minusmc.minusbounce.event.BlockBBEvent
import net.minecraft.block.BlockAir
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minusmc.minusbounce.utils.player.RotationUtils

class ZoneCraftFly: FlyMode("ZoneCraft", FlyType.OTHER) {
    private val timerBoostValue = BoolValue("TimerBoost", false)
    
    override fun onMove(event: MoveEvent) {
        mc.timer.timerSpeed = 1f

        if (timerBoostValue.get()) {
            if (mc.thePlayer.ticksExisted % 20 < 10)
                mc.timer.timerSpeed = 1.25f
            else
                mc.timer.timerSpeed = 0.8f
        }
        
        RotationUtils.setTargetRotation(Rotation(mc.thePlayer.rotationYaw, 90f))
        mc.netHandler.networkManager.sendPacket(C08PacketPlayerBlockPlacement(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ), 1, null, 0f, 1f, 0f))

    }
    
    override fun onBlockBB(event: BlockBBEvent) {
        if (event.block is BlockAir && event.y <= startY) {
            event.boundingBox = AxisAlignedBB.fromBounds(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(), event.x + 1.0, startY, event.z + 1.0)
        }
    }
}
