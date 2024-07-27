package net.minusmc.minusbounce.features.module.modules.combat.velocitys.grim

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.ReceivedPacketEvent
import net.minusmc.minusbounce.event.EntityDamageEvent
import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minusmc.minusbounce.utils.PacketUtils
import net.minusmc.minusbounce.value.ListValue
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S27PacketExplosion
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook
import net.minecraft.util.EnumFacing
import net.minecraft.util.BlockPos


class GrimC07Velocity : VelocityMode("GrimC07") {
    private var packetPayloadValue = ListValue("PacketPayload", arrayOf("C03", "C06"), "C03")
    private var canCancel = false
    private var canSpoof = false

    override fun onEnable() {
        canCancel = false
        canSpoof = false
    }

    override fun onUpdate() {
        if (!canSpoof)
            return

        val pos = BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)

        when (packetPayloadValue.get().lowercase()) {
            "c03" -> PacketUtils.sendPacketNoEvent(C03PacketPlayer(mc.thePlayer.onGround))
            "c06" -> PacketUtils.sendPacketNoEvent(C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, mc.thePlayer.onGround))
        }

        PacketUtils.sendPacketNoEvent(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, pos, mc.thePlayer.horizontalFacing.opposite))
        canSpoof = false
    }

    override fun onEntityDamage(event: EntityDamageEvent) {
        if (event.damagedEntity == mc.thePlayer)
            canCancel = true
    }

    override fun onReceivedPacket(event: ReceivedPacketEvent) {
        val packet = event.packet

        if (!canCancel)
            return

        if (packet is S12PacketEntityVelocity && packet.entityID == mc.thePlayer.entityId) {
            canSpoof = true
            canCancel = false
            event.isCancelled = true
        }

        if (packet is S27PacketExplosion) {
            canSpoof = true
            canCancel = false
            event.isCancelled = true
        }
    }
}