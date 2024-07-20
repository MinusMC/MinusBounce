/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.movement.nowebs.other

import net.minusmc.minusbounce.event.MoveInputEvent
import net.minusmc.minusbounce.features.module.modules.movement.nowebs.NoWebMode

class IntaveNoWeb: NoWebMode("Intave") {
    private var wasInWeb = false
    private var counter = 0

    override fun onUpdate() {
        if (mc.thePlayer.isInWeb) {
            mc.thePlayer.onGround = false

            if (!mc.thePlayer.isCollidedVertically)
                mc.thePlayer.jumpMovementFactor = 0.8f
            else if (mc.thePlayer.movementInput.moveStrafe == 0f && mc.gameSettings.keyBindForward.isKeyDown)
                mc.thePlayer.jumpMovementFactor = 0.74f
            else {
                mc.thePlayer.onGround = true
                mc.thePlayer.jumpMovementFactor = 0.2f
            }

            wasInWeb = mc.thePlayer.isInWeb
        }

        if (mc.thePlayer.jumpMovementFactor > 0.03f && wasInWeb) {
            wasInWeb = mc.thePlayer.isInWeb
            mc.thePlayer.jumpMovementFactor = 0.02f
        }
    }

    override fun onMoveInput(event: MoveInputEvent) {
        if (mc.thePlayer.isCollidedVertically) {
            if (mc.thePlayer.isInWeb && event.strafe == 0.0f && mc.gameSettings.keyBindForward.isKeyDown)
                event.forward = if (++counter % 5 == 0) 0.0f else 1.0f
        } else if (mc.thePlayer.isInWeb) {
            if (mc.thePlayer.isSprinting)
                event.forward = 0f

            event.sneak = true
            event.forward = event.forward.coerceIn(-0.3f, 0.3f)
            event.strafe = if (event.forward == 0f) event.strafe.coerceIn(-0.3f, 0.3f) else 0f
        }
    }

}