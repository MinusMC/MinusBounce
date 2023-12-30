package net.minusmc.minusbounce.features.module.modules.movement.noslows.normal

import net.minusmc.minusbounce.features.module.modules.movement.noslows.NoSlowMode
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.IntegerValue
import net.minusmc.minusbounce.event.PreMotionEvent
import net.minusmc.minusbounce.event.PostMotionEvent

class CustomNoSlow : NoSlowMode("Custom") {
	private val customRelease = BoolValue("ReleasePacket", false)
	private val customPlace = BoolValue("PlacePacket", false)
	private val customOnGround = BoolValue("OnGround", false)
	private val customDelayValue = IntegerValue("Delay", 60, 0, 1000, "ms")

	override fun onPreMotion(event: PreMotionEvent) {
		if (customRelease.get())
			sendC07(customDelayValue.get() > 0, customDelayValue.get().toLong(), customOnGround.get())
	}

	override fun onPostMotion(event: PostMotionEvent) {
		if (customPlace.get())
			sendC08(customDelayValue.get() > 0, customDelayValue.get().toLong(), customOnGround.get())
	}
}