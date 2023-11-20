/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.minusmc.minusbounce.features.module.modules.movement.speeds.verus

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.features.module.modules.movement.Speed
import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedMode
import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedType
import net.minusmc.minusbounce.utils.MovementUtils
import net.minusmc.minusbounce.value.ListValue

class VerusSpeed: SpeedMode("Verus", SpeedType.VERUS) {

    private val verusMode = ListValue("Mode", arrayOf("Hop", "Hard", "YPort"), "Hop")

    override fun onUpdate() {
        if (verusMode.get().equals("Hop", true)) {
            if (!mc.thePlayer.isInWeb && !mc.thePlayer.isInLava && !mc.thePlayer.isInWater && !mc.thePlayer.isOnLadder && mc.thePlayer.ridingEntity == null) {
                if (MovementUtils.isMoving && mc.thePlayer.onGround) {
                    mc.gameSettings.keyBindJump.pressed = false
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.jump()
                        MovementUtils.strafe(0.48f)
                    }
                    MovementUtils.strafe()
                }
            }
        }
        if (verusMode.get().equals("YPort", true)) {
            if (!mc.thePlayer.isInWeb && !mc.thePlayer.isInLava && !mc.thePlayer.isInWater && !mc.thePlayer.isOnLadder && mc.thePlayer.ridingEntity == null) {
                if (MovementUtils.isMoving && mc.thePlayer.onGround) {
                    mc.gameSettings.keyBindJump.pressed = false
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.jump()
                        mc.thePlayer.motionY = 0.30
                        MovementUtils.strafe(0.48f)
                    }
                    MovementUtils.strafe()
                }
            }
        }
    }
}