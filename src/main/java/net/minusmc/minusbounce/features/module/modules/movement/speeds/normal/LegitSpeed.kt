package net.minusmc.minusbounce.features.module.modules.movement.speeds.normal

import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedType
import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedMode
import net.minusmc.minusbounce.event.StrafeEvent
import net.minusmc.minusbounce.utils.Rotation
import net.minusmc.minusbounce.utils.player.RotationUtils
import net.minusmc.minusbounce.utils.player.MovementUtils
import net.minusmc.minusbounce.utils.player.MovementCorrection
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.ListValue

class LegitSpeed: SpeedMode("Legit", SpeedType.NORMAL) {

    private val exploit = ListValue("ExploitMode", arrayOf("Rotate", "Speed"), "Speed")
    private val cpuSPEED = BoolValue("SpeedUpExploit", true)
    private val jumpSpeed = BoolValue("NoJumpDelay", true)

    override fun onUpdate() {
        when (exploit.get().lowercase()) {
            "rotate" -> if (!mc.thePlayer.onGround) {
                val rotation = Rotation(mc.thePlayer.rotationYaw + 45f, mc.thePlayer.rotationPitch)
                RotationUtils.setTargetRotation(rotation, 2, 10f, 10f, MovementCorrection.Type.NORMAL)
            }
            "speed" -> MovementUtils.useDiagonalSpeed()
        }

        if (cpuSPEED.get())
            mc.timer.timerSpeed = 1.0020022128217f

        if (jumpSpeed.get())
            mc.thePlayer.jumpTicks = 0
    }

    override fun onStrafe(event: StrafeEvent) {
        if (MovementUtils.isMoving && !mc.thePlayer.isInWater) {
            if (mc.thePlayer.onGround && !mc.gameSettings.keyBindJump.pressed)
                mc.thePlayer.jump()
        }
    }
}