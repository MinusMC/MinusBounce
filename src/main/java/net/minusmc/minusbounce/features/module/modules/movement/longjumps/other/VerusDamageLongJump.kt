package net.minusmc.minusbounce.features.module.modules.movement.longjumps.other

import net.minusmc.minusbounce.features.module.modules.movement.longjumps.LongJumpMode
import net.minusmc.minusbounce.utils.player.MovementUtils
import net.minusmc.minusbounce.utils.PacketUtils
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.value.ListValue
import net.minusmc.minusbounce.event.MoveEvent
import net.minusmc.minusbounce.event.SentPacketEvent
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook

class VerusDamageLongJump: LongJumpMode("VerusDamage") {
	private val verusDmgModeValue = ListValue("DamageMode", arrayOf("Instant", "InstantC06", "Jump"), "None")
    private val verusBoostValue = FloatValue("Boost", 4.25F, 0F, 10F)
    private val verusHeightValue = FloatValue("Height", 0.42F, 0F, 10F)
    private val verusTimerValue = FloatValue("Timer", 1F, 0.05F, 10F)

    private var verusDmged = false
    private var verusJumpTimes = 0

    override fun onEnable() {
    	verusDmged = false
        verusJumpTimes = 0
        val y = mc.thePlayer.posY

        when (verusDmgModeValue.get()) {
            "instant" -> if (mc.thePlayer.onGround && mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.entityBoundingBox.offset(0.0, 4.0, 0.0)).isEmpty()) {
                PacketUtils.sendPacketNoEvent(C04PacketPlayerPosition(mc.thePlayer.posX, y + 4, mc.thePlayer.posZ, false))
                PacketUtils.sendPacketNoEvent(C04PacketPlayerPosition(mc.thePlayer.posX, y, mc.thePlayer.posZ, false))
                PacketUtils.sendPacketNoEvent(C04PacketPlayerPosition(mc.thePlayer.posX, y, mc.thePlayer.posZ, true))
                mc.thePlayer.motionX = 0.0
                mc.thePlayer.motionZ = 0.0
            }

            "instantc06" -> if (mc.thePlayer.onGround && mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.entityBoundingBox.offset(0.0, 4.0, 0.0)).isEmpty()) {
                PacketUtils.sendPacketNoEvent(C06PacketPlayerPosLook(mc.thePlayer.posX, y + 4, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, false))
                PacketUtils.sendPacketNoEvent(C06PacketPlayerPosLook(mc.thePlayer.posX, y, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, false))
                PacketUtils.sendPacketNoEvent(C06PacketPlayerPosLook(mc.thePlayer.posX, y, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, true))
                mc.thePlayer.motionX = 0.0
                mc.thePlayer.motionZ = 0.0
            }

            "jump" -> if (mc.thePlayer.onGround) {
                mc.thePlayer.jump()
                verusJumpTimes = 1
            }
        }
    }

	override fun onUpdate() {
		if (mc.thePlayer.hurtTime > 0 && !verusDmged) {
            verusDmged = true
            MovementUtils.strafe(verusBoostValue.get())
            mc.thePlayer.motionY = verusHeightValue.get().toDouble()
        }

        if (verusDmgModeValue.get().equals("jump", true) && verusJumpTimes < 5) {
            if (mc.thePlayer.onGround) {
                mc.thePlayer.jump()
                verusJumpTimes += 1
            }
            return
        }

        if (verusDmged) {
            mc.timer.timerSpeed = verusTimerValue.get()
            return
        }

        mc.thePlayer.movementInput.moveForward = 0F
        mc.thePlayer.movementInput.moveStrafe = 0F
        if (!verusDmgModeValue.get().equals("jump", true))
            mc.thePlayer.motionY = 0.0
	}

	override fun onSentPacket(event: SentPacketEvent) {
		val packet = event.packet
        if (packet is C03PacketPlayer && verusDmgModeValue.get().equals("jump", true) && verusJumpTimes < 5)
            packet.onGround = false
	}

	override fun onMove(event: MoveEvent) {
		if (!verusDmged)
            event.zeroXZ()
	}
}
