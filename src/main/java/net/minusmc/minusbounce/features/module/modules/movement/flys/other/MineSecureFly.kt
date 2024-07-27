package net.minusmc.minusbounce.features.module.modules.movement.flys.other

import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyType
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyMode
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.utils.timer.MSTimer
import net.minusmc.minusbounce.utils.player.MovementUtils
import net.minusmc.minusbounce.utils.misc.MathUtils
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import kotlin.math.sin
import kotlin.math.cos

class MineSecureFly: FlyMode("MineSecure", FlyType.OTHER) {
    private val speedValue = FloatValue("Speed", 2f, 0f, 5f)
	private val mineSecureVClipTimer = MSTimer()

    override fun onUpdate() {
        mc.thePlayer.capabilities.isFlying = false

        if (!mc.gameSettings.keyBindSneak.isKeyDown)
            mc.thePlayer.motionY = -0.01

        mc.thePlayer.motionX = 0.0
        mc.thePlayer.motionZ = 0.0
        MovementUtils.strafe(speedValue.get())

        if (mineSecureVClipTimer.hasTimePassed(150) && mc.gameSettings.keyBindJump.isKeyDown) {
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 5, mc.thePlayer.posZ, false))
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(0.5, -1000.0, 0.5, false))
            val yaw = MathUtils.toRadians(mc.thePlayer.rotationYaw)
            val x = -sin(yaw) * 0.4
            val z = cos(yaw) * 0.4
            mc.thePlayer.setPosition(mc.thePlayer.posX + x, mc.thePlayer.posY, mc.thePlayer.posZ + z)
            mineSecureVClipTimer.reset()
        }
    }
}