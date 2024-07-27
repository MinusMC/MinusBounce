package net.minusmc.minusbounce.features.module.modules.movement.flys.other

import net.minusmc.minusbounce.event.JumpEvent
import net.minusmc.minusbounce.event.PreMotionEvent
import net.minusmc.minusbounce.event.PostMotionEvent
import net.minusmc.minusbounce.event.StepEvent
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyMode
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyType
import net.minusmc.minusbounce.utils.player.MovementUtils
import net.minusmc.minusbounce.utils.misc.MathUtils

class FunCraftFly: FlyMode("FunCraft", FlyType.OTHER) {
	private var moveSpeed = 0.0

    override fun onEnable() {
		super.onEnable()
        if (mc.thePlayer.onGround && !mc.gameSettings.keyBindJump.pressed)
            mc.thePlayer.jump()

        moveSpeed = 1.0
    }

    override fun onPostMotion(event: PostMotionEvent) {
        if (!MovementUtils.isMoving)
            moveSpeed = 0.25

        if (moveSpeed > 0.25)
            moveSpeed -= moveSpeed / 159.0
    }

    override fun onPreMotion(event: PreMotionEvent) {
        event.onGround = true

        if (!MovementUtils.isMoving)
            moveSpeed = 0.25

        if (moveSpeed > 0.25)
            moveSpeed -= moveSpeed / 159.0
        
        mc.thePlayer.capabilities.isFlying = false
        mc.thePlayer.motionY = 0.0
        mc.thePlayer.motionX = 0.0
        mc.thePlayer.motionZ = 0.0

        MovementUtils.strafe(moveSpeed.toFloat())
        mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY - 8e-6, mc.thePlayer.posZ)
    }

    override fun onJump(event: JumpEvent) {
        if (moveSpeed > 0)
            event.isCancelled = true
    }

    override fun onStep(event: StepEvent) {
        event.stepHeight = 0f
    }
}