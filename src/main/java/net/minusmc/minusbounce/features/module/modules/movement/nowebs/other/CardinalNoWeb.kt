package net.minusmc.minusbounce.features.module.modules.movement.nowebs.other

import net.minusmc.minusbounce.features.module.modules.movement.nowebs.NoWebMode
import net.minusmc.minusbounce.utils.player.MovementUtils

class CardinalNoWeb: NoWebMode("Cardinal") {
    override fun onUpdate() {
        if (!mc.thePlayer.isInWeb)
            return

        val strafeMultiplier = if (mc.thePlayer.onGround) 0.262f else 0.366f
        MovementUtils.strafe(strafeMultiplier)
    }
}