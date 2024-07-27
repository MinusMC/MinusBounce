package net.minusmc.minusbounce.features.module.modules.combat.velocitys.intave

import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minusmc.minusbounce.value.BoolValue
import net.minecraft.client.settings.GameSettings

class OldIntaveVelocity : VelocityMode("OldIntave") {
    private var counter = 0

    override fun onUpdate() {
        if (mc.thePlayer.hurtTime == 9) {
            if (counter++ % 2 == 0 && mc.thePlayer.onGround && mc.thePlayer.isSprinting && mc.currentScreen == null) {
                mc.thePlayer.movementInput.jump = true
                counter = 0
            }
        } else mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
    }
}