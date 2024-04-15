package net.minusmc.minusbounce.features.module.modules.movement.nowebs.other

import net.minusmc.minusbounce.features.module.modules.movement.nowebs.NoWebMode

class IntaveNoWeb: NoWebMode("Intave") {
    override fun onUpdate() {
        if (mc.thePlayer.movementInput.moveStrafe == 0.0F && mc.gameSettings.keyBindForward.isKeyDown && mc.thePlayer.isCollidedVertically) {
            mc.thePlayer.jumpMovementFactor = 0.74F
        } else {
            mc.thePlayer.jumpMovementFactor = 0.2F
            mc.thePlayer.onGround = true
        }
    }
}
