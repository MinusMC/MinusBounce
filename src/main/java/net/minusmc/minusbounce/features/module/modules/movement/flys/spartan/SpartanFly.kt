package net.minusmc.minusbounce.features.module.modules.movement.flys.spartan

import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyMode
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyType

class SpartanFly: FlyMode("Spartan", FlyType.SPARTAN) {

	private var ticks = 0

	override fun onUpdate() {
		mc.thePlayer.motionY = 0.0
        ticks++

        if (ticks >= 12) {
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 8, mc.thePlayer.posZ, true))
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - 8, mc.thePlayer.posZ, true))
            ticks = 0
        }
	}
}