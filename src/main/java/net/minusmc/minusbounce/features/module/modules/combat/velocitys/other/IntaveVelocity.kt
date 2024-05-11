package net.minusmc.minusbounce.features.module.modules.combat.velocitys.other

import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minusmc.minusbounce.value.BoolValue

class IntaveVelocity : VelocityMode("Intave") {
    private val jump = BoolValue("Jump", true)

    private var counter = 0

    override fun onUpdate() {
        counter++
        if (jump.get() && mc.thePlayer.hurtTime == 2 && mc.thePlayer.onGround && counter % 2 == 0)
            mc.thePlayer.movementInput.jump = true
    }
}