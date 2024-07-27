package net.minusmc.minusbounce.features.module.modules.movement.flys.aac

import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyType
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyMode
import net.minusmc.minusbounce.value.FloatValue
import org.lwjgl.input.Keyboard

class AAC3313Fly: FlyMode("AAC3.3.13", FlyType.AAC) {
	private val aacMotion2 = FloatValue("Motion", 10f, 0.1f, 10f)
	private var wasDead = false

	override fun onDisable() {
		wasDead = false
	}

	override fun onUpdate() {
		if (mc.thePlayer.isDead)
            wasDead = true

        if (wasDead || mc.thePlayer.onGround) {
            wasDead = false

            mc.thePlayer.motionY = aacMotion2.get().toDouble()
            mc.thePlayer.onGround = false
        }

        mc.timer.timerSpeed = 1f

        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
            mc.timer.timerSpeed = 0.2f
            mc.rightClickDelayTimer = 0
        }
	}
}