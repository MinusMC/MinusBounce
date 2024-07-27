package net.minusmc.minusbounce.features.module.modules.player.nofalls.aac

import net.minusmc.minusbounce.features.module.modules.player.nofalls.NoFallMode
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minusmc.minusbounce.utils.timer.MSTimer
import net.minusmc.minusbounce.event.*

class AAC442LoyisaNoFall: NoFallMode("AAC4.4.2Loyisa") {
    private var isDmgFalling = false
    private var modifiedTimer = false
    private var flagWaitTicks = 0
    private var flagCount = 0
    private val flagTimer = MSTimer()

    override fun onEnable() {
        isDmgFalling = false
        flagCount = 0
    }

    override fun onDisable() {
        isDmgFalling = false
        flagCount = 0
    }

	override fun onUpdate() {
        if (modifiedTimer) {
            mc.timer.timerSpeed = 1.0F
            modifiedTimer = false
        }
        
        if (flagWaitTicks > 0) {
            flagWaitTicks--

            if (flagWaitTicks == 0)
                mc.timer.timerSpeed = 1F
        }

		if (mc.thePlayer.fallDistance > 3)
            isDmgFalling = true

        if (flagCount >= 3 || flagTimer.hasTimePassed(1500L))
            return

        if (!flagTimer.hasTimePassed(1500L) && (mc.thePlayer.onGround || mc.thePlayer.fallDistance < 0.5)) {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
            mc.thePlayer.onGround = false
            mc.thePlayer.jumpMovementFactor = 0.0f
        }
	}

    override fun onReceivedPacket(event: ReceivedPacketEvent) {
        val packet = event.packet

        if (packet is S08PacketPlayerPosLook) {
            flagCount++
            if(flagWaitTicks > 0) {
                flagTimer.reset()
                flagCount = 1
                event.isCancelled = true
            }
        }

    }

    override fun onSentPacket(event: SentPacketEvent) {
        val packet = event.packet

        if (isDmgFalling && packet is C03PacketPlayer && packet.onGround && mc.thePlayer.onGround) {
            flagWaitTicks = 2
            isDmgFalling = false
            event.isCancelled = true
            mc.thePlayer.onGround = false
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(packet.x, packet.y - 256, packet.z, false))
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(packet.x, -10.0, packet.z, true))
            mc.timer.timerSpeed = 0.18f
            modifiedTimer = true
        }
    }
}