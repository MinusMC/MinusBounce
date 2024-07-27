package net.minusmc.minusbounce.features.module.modules.movement.flys.aac

import net.minecraft.network.play.client.C03PacketPlayer
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyMode
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyType
import net.minusmc.minusbounce.utils.player.MovementUtils
import net.minusmc.minusbounce.utils.render.RenderUtils
import net.minusmc.minusbounce.value.FloatValue
import java.awt.Color

class AAC1910Fly: FlyMode("AAC1.9.10", FlyType.AAC) {
	private val aacSpeedValue = FloatValue("Speed", 0.3f, 0f, 1f)
	private var aacJump = 0.0
	
	override fun onEnable() {
		super.onEnable()
		aacJump = -3.8
	}

	override fun onUpdate() {
		if (mc.gameSettings.keyBindJump.isKeyDown)
            aacJump += 0.2

        if (mc.gameSettings.keyBindSneak.isKeyDown)
            aacJump -= 0.2

        if (startY + aacJump > mc.thePlayer.posY) {
            mc.netHandler.addToSendQueue(C03PacketPlayer(true))
            mc.thePlayer.motionY = 0.8
            MovementUtils.strafe(aacSpeedValue.get())
        }

        MovementUtils.strafe()
	}
	override fun onRender3D() {
		RenderUtils.drawPlatform(startY + aacJump, Color(0, 0, 255, 90), 1.0)
	}
}