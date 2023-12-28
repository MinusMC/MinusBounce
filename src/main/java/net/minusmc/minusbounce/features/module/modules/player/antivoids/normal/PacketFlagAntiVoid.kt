package net.minusmc.minusbounce.features.module.modules.player.antivoids.normal

import net.minusmc.minusbounce.features.module.modules.player.antivoids.AntiVoidMode
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition


class PacketFlagAntiVoid: AntiVoidMode("PacketFlag") {
    private var tried = false

    override fun onEnable() {
        tried = false
    }

    override fun onUpdate() {
        if (!antivoid.voidOnlyValue.get() || isVoid) {
            if (mc.thePlayer.fallDistance > antivoid.maxFallDistValue.get() && !tried) {
                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX + 1, mc.thePlayer.posY + 1, mc.thePlayer.posZ + 1, false))
                tried = true
            }
        }
    }
}