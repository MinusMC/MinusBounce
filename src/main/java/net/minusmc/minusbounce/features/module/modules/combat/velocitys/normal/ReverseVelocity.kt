package net.minusmc.minusbounce.features.module.modules.combat.velocitys.normal

import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minusmc.minusbounce.event.ReceivedPacketEvent
import net.minusmc.minusbounce.utils.player.MovementUtils
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.utils.timer.MSTimer

class ReverseVelocity : VelocityMode("Reverse") {
    private val reverseStrengthValue = FloatValue("Strength", 1f, 0.1f, 1f, "x")
    private var velocityInput = false
    private val velocityTimer = MSTimer()
    
    override fun onUpdate() {
        if (!velocityInput)
            return

        if (!mc.thePlayer.onGround)
            MovementUtils.strafe(MovementUtils.speed * reverseStrengthValue.get())
        else if (velocityTimer.hasTimePassed(80L))
            velocityInput = false
    }

    override fun onReceivedPacket(event: ReceivedPacketEvent) {
        if (event.packet is S12PacketEntityVelocity) {
            velocityInput = true
            velocityTimer.reset()
        }
    }
}