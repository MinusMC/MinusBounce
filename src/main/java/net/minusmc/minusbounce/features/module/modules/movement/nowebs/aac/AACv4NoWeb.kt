package net.minusmc.minusbounce.features.module.modules.movement.nowebs.aac

import net.minusmc.minusbounce.features.module.modules.movement.nowebs.NoWebMode
import net.minusmc.minusbounce.event.JumpEvent
import net.minusmc.minusbounce.utils.player.MovementUtils

class AACv4NoWeb: NoWebMode("AACv4") {
	override fun onUpdate() {
		mc.gameSettings.keyBindRight.pressed = false
        mc.gameSettings.keyBindBack.pressed = false
        mc.gameSettings.keyBindLeft.pressed = false

        if(mc.thePlayer.onGround){
            MovementUtils.strafe(0.25F)
        }else{
            MovementUtils.strafe(0.12F)
            mc.thePlayer.motionY = 0.0
        }
    }

    override fun onJump(event: JumpEvent) {
        event.cancelEvent()
    }
}