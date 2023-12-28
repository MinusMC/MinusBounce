package net.minusmc.minusbounce.features.module.modules.player.antivoids.other

import net.minusmc.minusbounce.features.module.modules.player.antivoids.AntiVoidMode
import net.minusmc.minusbounce.event.PacketEvent
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S08PacketPlayerPosLook


class OldCubeCraftAntiVoid: AntiVoidMode("OldCubeCraft") {
    private var canSpoof = false
    private var tried = false
    private var flagged = false
    private var lastRecY = 0.0

    override fun onEnable() {
        canSpoof = false
        flagged = false
        tried = false
    }

    override fun onWorld() {
        if (lastRecY == 0.0)
            lastRecY = mc.thePlayer.posY
    }

    override fun onUpdate() {
        canSpoof = false
        if (!antivoid.voidOnlyValue.get() || isVoid) {
            if (mc.thePlayer.fallDistance > antivoid.maxFallDistValue.get() && mc.thePlayer.posY < lastRecY + 0.01 && mc.thePlayer.motionY <= 0 && !mc.thePlayer.onGround && !flagged) {
                mc.thePlayer.motionY = 0.0
                mc.thePlayer.motionZ = 0.0
                mc.thePlayer.motionX = 0.0
                mc.thePlayer.jumpMovementFactor = 0.00f
                canSpoof = true
                if (!tried) {
                    tried = true
                    mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, (32000.0).toDouble(), mc.thePlayer.posZ, false))
                }
            }
        }
        lastRecY = mc.thePlayer.posY
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (canSpoof && (packet is C03PacketPlayer)) {
            if (packet.y < 1145.141919810) event.cancelEvent()
        }
        if (canSpoof && (packet is S08PacketPlayerPosLook)) {
            flagged = true
        }
    }
}