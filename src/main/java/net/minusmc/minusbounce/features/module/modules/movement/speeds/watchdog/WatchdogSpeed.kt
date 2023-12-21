package net.minusmc.minusbounce.features.module.modules.movement.speeds.watchdog

import net.minusmc.minusbounce.event.MoveEvent
import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedMode
import net.minusmc.minusbounce.utils.MovementUtils
import net.minecraft.potion.Potion
import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedType

class WatchdogSpeed: SpeedMode("Watchdog", SpeedType.WATCHDOG) {

    override fun onUpdate() {
        if (mc.thePlayer.onGround && MovementUtils.isMoving) {
            if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                MovementUtils.strafe(0.5f)
            } else {
                MovementUtils.strafe(0.44f)
            }
            mc.thePlayer.motionY = MovementUtils.getJumpBoostModifier(0.42F, true)
        }
    }

    override fun onMove(event: MoveEvent) {

    }
}