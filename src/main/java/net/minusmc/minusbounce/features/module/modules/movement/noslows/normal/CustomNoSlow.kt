package net.minusmc.minusbounce.features.module.modules.movement.noslows.normal

import net.minusmc.minusbounce.features.module.modules.movement.noslows.NoSlowMode
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.IntegerValue
import net.minusmc.minusbounce.event.MotionEvent

class CustomNoSlow : NoSlowMode("Custom") {
	private val customRelease = BoolValue("ReleasePacket", false)
	private val customPlace = BoolValue("PlacePacket", false)
	private val customOnGround = BoolValue("OnGround", false)
	private val customDelayValue = IntegerValue("Delay", 60, 0, 1000, "ms")

	override fun onMotion(event: MotionEvent) {
		sendPacket(event, customRelease.get(), customPlace.get(), customDelayValue.get() > 0, customDelayValue.get().toLong(), customOnGround.get())
	}
}