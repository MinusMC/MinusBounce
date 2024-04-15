package net.minusmc.minusbounce.features.module.modules.world.scaffold.tower

import net.minusmc.minusbounce.features.module.modules.world.scaffold.TowerScaffold
import net.minusmc.minusbounce.event.PreMotionEvent
import net.minusmc.minusbounce.utils.player.MovementUtils

class VerusTower: TowerScaffold("Verus") {
    private var verusState = 0
    private var verusJumped = false

    override fun onPreMotion(event: PreMotionEvent) {
        if (mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.entityBoundingBox.offset(0.0, -0.01, 0.0)).isNotEmpty() && mc.thePlayer.onGround && mc.thePlayer.isCollidedVertically) {
            verusState = 0
            verusJumped = true
        }
        if (verusJumped) {
            MovementUtils.strafe()
            when (verusState) {
                0 -> {
                    fakeJump()
                    mc.thePlayer.motionY = 0.41999998688697815
                }
                3 -> {
                    event.onGround = true
                    mc.thePlayer.motionY = 0.0
                }
            }
            verusState++
            verusJumped = false
        }
        verusJumped = true
    }

    override fun onPostMotion() {
        if (!scaffold.towerStatus) {
            verusState = 0
        }
    }
}