package net.minusmc.minusbounce.features.module.modules.movement.flys.other

import net.minecraft.network.play.client.C03PacketPlayer
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyMode
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyType
import net.minusmc.minusbounce.utils.player.MovementUtils
import net.minusmc.minusbounce.value.FloatValue

class SkyCaveBadFly: FlyMode("SkyCaveBad", FlyType.OTHER) {
	private val speedValue = FloatValue("Speed", 1f, 0.1f, 9f)

    override fun onUpdate() {
        if (mc.thePlayer.fallDistance >= 3.8f) {
            mc.thePlayer.sendQueue.addToSendQueue(C03PacketPlayer(true))
            mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY - mc.thePlayer.motionY, mc.thePlayer.posZ)
            mc.thePlayer.motionY = 0.8
            
            MovementUtils.strafe(speedValue.get())

            mc.thePlayer.fallDistance = 0f
        }
    }

}