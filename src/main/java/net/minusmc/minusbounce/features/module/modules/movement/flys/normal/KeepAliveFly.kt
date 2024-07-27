package net.minusmc.minusbounce.features.module.modules.movement.flys.normal

import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyType
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyMode
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.utils.player.MovementUtils
import net.minecraft.network.play.client.C00PacketKeepAlive

class KeepAliveFly: FlyMode("KeepAlive", FlyType.NORMAL) {
	private val vanillaSpeedValue = FloatValue("Speed", 2f, 0f, 5f)

    override fun onUpdate() {
        mc.netHandler.addToSendQueue(C00PacketKeepAlive())
        mc.thePlayer.capabilities.isFlying = false

        mc.thePlayer.motionY = 0.0
        mc.thePlayer.motionX = 0.0
        mc.thePlayer.motionZ = 0.0

        if (mc.gameSettings.keyBindJump.isKeyDown)
            mc.thePlayer.motionY += vanillaSpeedValue.get().toDouble()

        if (mc.gameSettings.keyBindSneak.isKeyDown)
            mc.thePlayer.motionY -= vanillaSpeedValue.get().toDouble()
            
        MovementUtils.strafe(vanillaSpeedValue.get())
    }
}
