package net.minusmc.minusbounce.features.module.modules.movement.flys.spartan

import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyType
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyMode
import net.minusmc.minusbounce.utils.player.MovementUtils
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition


class Spartan2Fly: FlyMode("Spartan2", FlyType.SPARTAN) {
	override fun onUpdate() {
		MovementUtils.strafe(0.264f)
        if (mc.thePlayer.ticksExisted % 8 == 0)
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 10, mc.thePlayer.posZ, true))
	}
}