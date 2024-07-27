package net.minusmc.minusbounce.features.module.modules.movement.flys.hypixel

import net.minecraft.block.BlockAir
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.AxisAlignedBB
import net.minusmc.minusbounce.event.BlockBBEvent
import net.minusmc.minusbounce.event.JumpEvent
import net.minusmc.minusbounce.event.SentPacketEvent
import net.minusmc.minusbounce.event.StepEvent
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyMode
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyType
import net.minusmc.minusbounce.utils.timer.MSTimer
import net.minusmc.minusbounce.utils.timer.TickTimer
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.value.IntegerValue

class HypixelFly: FlyMode("Hypixel", FlyType.HYPIXEL) {
    private val hypixelBoost = BoolValue("Boost", true)
    private val hypixelBoostDelay = IntegerValue("BoostDelay", 1200, 0, 2000)
    private val hypixelBoostTimer = FloatValue("BoostTimer", 1F, 0F, 5F)

    private val hypixelTimer = TickTimer()
    private val flyTimer = MSTimer()

    override fun onUpdate() {
        val boostDelay = hypixelBoostDelay.get()
        if (hypixelBoost.get() && !flyTimer.hasTimePassed(boostDelay.toLong())) {
            mc.timer.timerSpeed = 1f + hypixelBoostTimer.get() * flyTimer.hasTimeLeft(boostDelay.toLong()) / boostDelay
        }

        hypixelTimer.update()

        if (hypixelTimer.hasTimePassed(2)) {
            mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 1.0E-5, mc.thePlayer.posZ)
            hypixelTimer.reset()
        }
    }

    override fun onSentPacket(event: SentPacketEvent) {
        val packet = event.packet
        if (packet is C03PacketPlayer)
            packet.onGround = false
    }

    override fun onBlockBB(event: BlockBBEvent) {
        if (event.block is BlockAir && event.y < mc.thePlayer.posY) {
            event.boundingBox = AxisAlignedBB.fromBounds(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(), (event.x + 1).toDouble(), mc.thePlayer.posY, (event.z + 1).toDouble())
        }
    }

    override fun onJump(event: JumpEvent) {
        event.isCancelled = true
    }

    override fun onStep(event: StepEvent) {
        event.stepHeight = 0f
    }
}

