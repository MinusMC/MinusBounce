package net.minusmc.minusbounce.features.module.modules.movement.flys.spartan

import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyType
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyMode
import net.minusmc.minusbounce.utils.player.MovementUtils
import net.minusmc.minusbounce.value.FloatValue
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition


class BugSpartanFly: FlyMode("BugSpartan", FlyType.SPARTAN) {
	private val vanillaSpeedValue = FloatValue("Speed", 2F, 0F, 5F)

	override fun onEnable() {
		super.onEnable()
		val x = mc.thePlayer.posX
        val y = mc.thePlayer.posY
        val z = mc.thePlayer.posZ

		for (i in 0..64) {
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.049f, z, false))
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y, z, false))
        }
        
        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.1f, z, true))
        mc.thePlayer.motionX *= 0.1f
        mc.thePlayer.motionZ *= 0.1f
        mc.thePlayer.swingItem()
	}

	override fun onUpdate() {
        mc.thePlayer.capabilities.isFlying = false

        mc.thePlayer.motionY = 0.0
        mc.thePlayer.motionX = 0.0
        mc.thePlayer.motionZ = 0.0

        if (mc.gameSettings.keyBindJump.isKeyDown)
            mc.thePlayer.motionY += vanillaSpeedValue.get()

        if (mc.gameSettings.keyBindSneak.isKeyDown)
            mc.thePlayer.motionY -= vanillaSpeedValue.get()

        MovementUtils.strafe(vanillaSpeedValue.get())
	}
}

