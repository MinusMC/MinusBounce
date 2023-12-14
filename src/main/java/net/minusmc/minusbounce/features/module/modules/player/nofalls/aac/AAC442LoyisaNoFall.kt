package net.minusmc.minusbounce.features.module.modules.player.nofalls.aac

import net.minusmc.minusbounce.features.module.modules.player.nofalls.NoFallMode
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minusmc.minusbounce.utils.timer.MSTimer
import net.minusmc.minusbounce.event.PacketEvent

class AAC442LoyisaNoFall: NoFallMode("AAC 4.4.2 Loyisa") {
    private var isDmgFalling = false
    private var modifiedTimer = false
    private var matrixFlagWait = 0
    private var aac4FlagCount = 0
    private val aac4FlagCooldown = MSTimer()

    override fun onEnable() {
        isDmgFalling = false
        aac4FlagCount = 0
    }

    override fun onDisable() {
        isDmgFalling = false
        aac4FlagCount = 0
    }

	override fun onUpdate() {
        if (modifiedTimer) {
            mc.timer.timerSpeed = 1.0F
            modifiedTimer = false
        }
        
        if (matrixFlagWait > 0) {
            matrixFlagWait--
            if (matrixFlagWait == 0)
                mc.timer.timerSpeed = 1F
        }

		if (mc.thePlayer.fallDistance > 3)
            isDmgFalling = true

        if (aac4FlagCount >= 3 || aac4FlagCooldown.hasTimePassed(1500L)) return
        if (!aac4FlagCooldown.hasTimePassed(1500L) && (mc.thePlayer.onGround || mc.thePlayer.fallDistance < 0.5)) {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
            mc.thePlayer.onGround = false
            mc.thePlayer.jumpMovementFactor = 0.0f
        }
	}

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is S08PacketPlayerPosLook) {
            aac4FlagCount++
            if(matrixFlagWait > 0) {
                aac4FlagCooldown.reset()
                aac4FlagCount = 1
                event.cancelEvent()
            }
        }

        if (isDmgFalling && packet is C03PacketPlayer && packet.onGround && mc.thePlayer.onGround) {
            matrixFlagWait = 2
            isDmgFalling = false
            event.cancelEvent()
            mc.thePlayer.onGround = false
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(packet.x, packet.y - 256, packet.z, false))
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(packet.x, -10.0, packet.z, true))
            mc.timer.timerSpeed = 0.18f
            modifiedTimer = true
        }
    }
}