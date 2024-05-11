package net.minusmc.minusbounce.features.module.modules.movement.speeds.blocksmc

import net.minecraft.potion.Potion
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedMode
import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedType
import net.minusmc.minusbounce.utils.player.MovementUtils


class BlocksMCLowHopSpeed: SpeedMode("BlocksMCLowHop", SpeedType.BLOCKSMC) {

    override fun onUpdate() {
        if (MovementUtils.isMoving) {
            if (mc.thePlayer.hurtTime > 0 && MinusBounce.combatManager.target != null) {
                MovementUtils.strafe(0.45995554F)
                if (mc.thePlayer.onGround) {
                    mc.thePlayer.jump()
                    mc.thePlayer.motionY = 0.22
                } else if (mc.thePlayer.fallDistance < -0.2) {
                    mc.thePlayer.motionY = -0.1
                }
            } else {
                if (mc.thePlayer.onGround) {
                    mc.thePlayer.jump()
                    mc.thePlayer.motionY = 0.41
                }
                if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                    MovementUtils.strafe(0.37f)
                } else {
                    MovementUtils.strafe(MovementUtils.speed)
                }
            }
        } else {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
    }
}