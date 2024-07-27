package net.minusmc.minusbounce.features.module.modules.movement.flys.hypixel

import net.minusmc.minusbounce.event.MoveEvent
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyMode
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyType
import net.minusmc.minusbounce.utils.timer.TickTimer
import java.math.BigDecimal
import java.math.RoundingMode

class FreeHypixelFly: FlyMode("FreeHypixel", FlyType.HYPIXEL) {
	private val freeHypixelTimer = TickTimer()

	private var freeHypixelYaw = 0f
    private var freeHypixelPitch = 0f


	override fun onEnable() {
		super.onEnable()
		freeHypixelTimer.reset()
        mc.thePlayer.setPositionAndUpdate(mc.thePlayer.posX, mc.thePlayer.posY + 0.42, mc.thePlayer.posZ)
        freeHypixelYaw = mc.thePlayer.rotationYaw
        freeHypixelPitch = mc.thePlayer.rotationPitch
	}

	override fun onUpdate() {
		if (freeHypixelTimer.hasTimePassed(10)) {
            mc.thePlayer.capabilities.isFlying = true
            return
        }

        mc.thePlayer.rotationYaw = freeHypixelYaw
        mc.thePlayer.rotationPitch = freeHypixelPitch
        mc.thePlayer.motionX = 0.0
        mc.thePlayer.motionZ = 0.0
        mc.thePlayer.motionY = 0.0

        if (startY == BigDecimal(mc.thePlayer.posY).setScale(3, RoundingMode.HALF_DOWN).toDouble())
            freeHypixelTimer.update()
	}

	override fun onMove(event: MoveEvent) {
		if (!freeHypixelTimer.hasTimePassed(10))
            event.zero()
	}

}

