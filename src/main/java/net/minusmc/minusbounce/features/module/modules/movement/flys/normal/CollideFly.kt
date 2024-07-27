package net.minusmc.minusbounce.features.module.modules.movement.flys.normal

import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyType
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyMode
import net.minusmc.minusbounce.event.BlockBBEvent
import net.minecraft.block.BlockAir
import net.minecraft.util.AxisAlignedBB

class CollideFly: FlyMode("Collide", FlyType.NORMAL) {
    
    override fun onBlockBB(event: BlockBBEvent) {
        if (event.block is BlockAir && !mc.thePlayer.isSneaking)
            event.boundingBox = AxisAlignedBB(-2.0, -1.0, -2.0, 2.0, 1.0, 2.0).offset(event.x.toDouble(), event.y.toDouble(), event.z.toDouble())
    }
}