package net.minusmc.minusbounce.features.module.modules.movement.flys.other

import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyType
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyMode
import net.minusmc.minusbounce.event.SentPacketEvent
import net.minusmc.minusbounce.event.BlockBBEvent
import net.minusmc.minusbounce.event.JumpEvent
import net.minusmc.minusbounce.event.StepEvent
import net.minecraft.util.AxisAlignedBB
import net.minecraft.block.BlockAir
import net.minecraft.network.play.client.C03PacketPlayer


class RewinsideFly: FlyMode("Rewinside", FlyType.OTHER) {
    
    override fun onSentPacket(event: SentPacketEvent) {
        val packet = event.packet
        if (packet is C03PacketPlayer)
            packet.onGround = true
    }

    override fun onBlockBB(event: BlockBBEvent) {
        if (event.block is BlockAir && event.y < mc.thePlayer.posY)
            event.boundingBox = AxisAlignedBB.fromBounds(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(), (event.x + 1).toDouble(), mc.thePlayer.posY, (event.z + 1).toDouble())
    }

    override fun onJump(event: JumpEvent) {
        event.isCancelled = true
    }

    override fun onStep(event: StepEvent) {
        event.stepHeight = 0f
    }

}