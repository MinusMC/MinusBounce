package net.minusmc.minusbounce.features.module.modules.movement.nowebs.other

import net.minusmc.minusbounce.features.module.modules.movement.nowebs.NoWebMode
import net.minusmc.minusbounce.event.JumpEvent

class NegativityNoWeb: NoWebMode("Negativity") {
    override fun onUpdate() {
        if (!mc.thePlayer.isInWeb)
            return

        mc.thePlayer.jumpMovementFactor = 0.4f
        
        if (mc.thePlayer.ticksExisted % 2 == 0)
            mc.thePlayer.jumpMovementFactor = 0.53F

        if (!mc.gameSettings.keyBindSneak.isKeyDown)
            mc.thePlayer.motionY = 0.0
    }

    override fun onJump(event: JumpEvent) {
        event.isCancelled = true
    }
}