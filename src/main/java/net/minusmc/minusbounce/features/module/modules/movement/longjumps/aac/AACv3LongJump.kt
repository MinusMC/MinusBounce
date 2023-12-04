package net.minusmc.minusbounce.features.module.modules.movement.longjumps.aac

import net.minusmc.minusbounce.features.module.modules.movement.longjumps.LongJumpMode
import net.minusmc.minusbounce.event.JumpEvent
import net.minecraft.util.EnumFacing

class AACv3LongJump : LongJumpMode("AACv3") {
    private var teleported = false

	override fun onUpdate() {
        if (mc.thePlayer.fallDistance > 0.5f && !teleported) {
            val horizontalFacing = mc.thePlayer.horizontalFacing
            var x = 0.0
            var z = 0.0
            when (horizontalFacing) {
                EnumFacing.NORTH -> z = -3.0
                EnumFacing.EAST -> x = 3.0
                EnumFacing.SOUTH -> z = 3.0
                EnumFacing.WEST -> x = -3.0
                else -> return
            }
            mc.thePlayer.setPosition(mc.thePlayer.posX + x, mc.thePlayer.posY, mc.thePlayer.posZ + z)
            teleported = true
        }
	}

    override fun onJump(event: JumpEvent) {
        teleported = false
    }
}
