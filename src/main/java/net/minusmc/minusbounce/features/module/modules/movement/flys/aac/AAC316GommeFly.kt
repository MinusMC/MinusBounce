package net.minusmc.minusbounce.features.module.modules.movement.flys.aac

import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyType
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyMode

import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition

class AAC316GommeFly: FlyMode("AAC3.1.6-Gomme", FlyType.AAC) {
	private var aac3delay = 0
	private var noFlag = false

    
	override fun onDisable() {
		noFlag = false
	}

	override fun onUpdate() {
		mc.thePlayer.capabilities.isFlying = true

        if (aac3delay == 2) {
            mc.thePlayer.motionY += 0.05
        } else if (aac3delay > 2) {
            mc.thePlayer.motionY -= 0.05
            aac3delay = 0
        }

        aac3delay++

        if (!noFlag)
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.onGround))
        
        if (mc.thePlayer.posY <= 0)
            noFlag = true
	}
}