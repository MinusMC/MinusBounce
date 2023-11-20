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

    private var verusTick = 0

    override fun onUpdate() {
        if (mc.thePlayer.isInWeb || mc.thePlayer.isInLava || mc.thePlayer.isInWater || mc.thePlayer.isOnLadder || mc.thePlayer.ridingEntity != null)
            return
        when (verusMode.get().lowercase()) {
            "hop" -> {
                if (MovementUtils.isMoving && mc.thePlayer.onGround) {
                    mc.gameSettings.keyBindJump.pressed = false
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.jump()
                        MovementUtils.strafe(0.48f)
                    }
                    MovementUtils.strafe()
                }
            }
            "hard" -> {

            }
            "yport" -> {
                if (mc.thePlayer.onGround && MovementUtils.isMoving && verusTick == 0) {
                    verusTick = 1
                    mc.thePlayer.motionY += 0.42
                } else if (verusTick == 1) {
                    MovementUtils.strafe(0.3f)
                    verusTick = 2
                } else if (verusTick == 5) {
                    verusTick = 0
                } else if (verusTick <= 5) {
                    mc.thePlayer.motionY -= 0.16
                }
            }
        }


    }
}