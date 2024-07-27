package net.minusmc.minusbounce.features.module.modules.movement.flys.verus

import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyType
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyMode
import net.minusmc.minusbounce.utils.player.MovementUtils
import net.minusmc.minusbounce.event.MoveEvent
import net.minusmc.minusbounce.event.BlockBBEvent
import net.minecraft.block.BlockAir
import net.minecraft.util.AxisAlignedBB

class VerusLowHopFly: FlyMode("VerusLowHop", FlyType.VERUS) {
	override fun onMove(event: MoveEvent) {

        if (mc.thePlayer.isInWeb || mc.thePlayer.isInLava || mc.thePlayer.isInWater || mc.thePlayer.isOnLadder || mc.gameSettings.keyBindJump.isKeyDown || mc.thePlayer.ridingEntity != null)
            return
            
		if (MovementUtils.isMoving) {
            mc.gameSettings.keyBindJump.pressed = false
            if (mc.thePlayer.onGround) {
                mc.thePlayer.jump()
                mc.thePlayer.motionY = 0.0
                MovementUtils.strafe(0.61f)
                event.y = 0.41999998688698
            }
            MovementUtils.strafe()
        }
	}

	override fun onBlockBB(event: BlockBBEvent) {
		if (event.block is BlockAir)
			event.boundingBox = AxisAlignedBB(-2.0, -1.0, -2.0, 2.0, 1.0, 2.0).offset(event.x.toDouble(), event.y.toDouble(), event.z.toDouble())
	}

}