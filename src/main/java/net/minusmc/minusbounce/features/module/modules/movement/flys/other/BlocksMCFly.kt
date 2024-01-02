package net.minusmc.minusbounce.features.module.modules.movement.flys.other

import net.minusmc.minusbounce.event.JumpEvent
import net.minusmc.minusbounce.event.MotionEvent
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyMode
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyType
import net.minusmc.minusbounce.utils.MovementUtils
import net.minusmc.minusbounce.value.FloatValue

class BlocksMCFly: FlyMode("BlocksMC", FlyType.OTHER) {

    private val timerSpeedValue = FloatValue("TimerSpeed", 0.1F,0.1F,1.0F)
    private var c = false
    override fun onEnable() {
        c = true
    }


    override fun onUpdate() {
        mc.thePlayer.motionY += 0.025
        if (MovementUtils.isMoving) {
            if (mc.thePlayer.onGround) {
                mc.thePlayer.jump()
            }
            if (!mc.thePlayer.onGround) mc.timer.timerSpeed = timerSpeedValue.get()
        }
        if (!mc.thePlayer.onGround) {
            MovementUtils.strafe(MovementUtils.speed * if (c) 55.80269f else 1f)
            if (c) c = false
        }
    }

    override fun onMotion(event: MotionEvent) {
        mc.thePlayer.posY -= mc.thePlayer.posY - mc.thePlayer.lastTickPosY
        mc.thePlayer.lastTickPosY -= mc.thePlayer.posY - mc.thePlayer.lastTickPosY
    }
    override fun onJump(event: JumpEvent) {
        c = true
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1.0F
    }

}