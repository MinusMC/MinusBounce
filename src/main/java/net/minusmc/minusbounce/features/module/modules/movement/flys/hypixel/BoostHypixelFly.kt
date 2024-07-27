package net.minusmc.minusbounce.features.module.modules.movement.flys.hypixel

import net.minecraft.block.BlockAir
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.potion.Potion
import net.minecraft.util.AxisAlignedBB
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyMode
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyType
import net.minusmc.minusbounce.utils.ClientUtils
import net.minusmc.minusbounce.utils.player.MovementUtils
import net.minusmc.minusbounce.utils.timer.TickTimer
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.ListValue
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

class BoostHypixelFly: FlyMode("BoostHypixel", FlyType.HYPIXEL) {
	private val hypixelBoostMode = ListValue("Mode", arrayOf("Default", "MorePackets", "NCP"), "Default")
    private val hypixelVisualY = BoolValue("VisualY", true)
    private val hypixelC04 = BoolValue("MoreC04s", false)

	private var boostHypixelState = 1
	private var lastDistance = 0.0
	private var failedStart = false
	private var moveSpeed = 0.0

    private val hypixelTimer = TickTimer()


	override fun onEnable() {
		super.onEnable()
		moveSpeed = 0.0

		if(!mc.thePlayer.onGround) return

        if (hypixelC04.get())
            for (i in 0..9)
                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
        
        if (hypixelBoostMode.get().equals("ncp", true)) {
            for (i in 0..64) {
                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.049, mc.thePlayer.posZ, false))
                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false))
            }
        } else {
            var fallDistance = if (hypixelBoostMode.get().equals("morepackets", true)) 3.4025 else 3.0125
            while (fallDistance > 0) {
                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.0624986421, mc.thePlayer.posZ, false))
                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.0625, mc.thePlayer.posZ, false))
                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.0624986421, mc.thePlayer.posZ, false))
                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.0000013579, mc.thePlayer.posZ, false))
                fallDistance -= 0.0624986421
            }
        }
        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
        
        if (hypixelVisualY.get()) {
            mc.thePlayer.jump()
            mc.thePlayer.posY += 0.42f
        }
        
        boostHypixelState = 1
        moveSpeed = 0.1
        lastDistance = 0.0
        failedStart = false
	}

	override fun onMove(event: MoveEvent) {
		if (!MovementUtils.isMoving) {
            event.x = 0.0
            event.z = 0.0
            return
        }

        if (failedStart)
            return
        
        val baseSpeed = 0.29 * MovementUtils.speedEffect
        when (boostHypixelState) {
            1 -> {

                if (mc.thePlayer.isPotionActive(Potion.moveSpeed))
                    moveSpeed = 1.56 * baseSpeed
                else
                    moveSpeed = 2.034 * baseSpeed

                boostHypixelState = 2
            }
            2 -> {
            	moveSpeed *= 2.16
                boostHypixelState = 3
            }
            3 -> {

                if (mc.thePlayer.ticksExisted % 2 == 0)
                    moveSpeed = lastDistance - 0.0103 * (lastDistance - baseSpeed)
                else
                    moveSpeed = lastDistance - 0.0123 * (lastDistance - baseSpeed)

                boostHypixelState = 4
            }
            else -> moveSpeed = lastDistance - lastDistance / 159.8
        }

        moveSpeed = max(moveSpeed, 0.3)

        val yaw = MovementUtils.directionToRadian

        event.x = -sin(yaw) * moveSpeed
        event.z = cos(yaw) * moveSpeed

        mc.thePlayer.motionX = event.x
        mc.thePlayer.motionZ = event.z
	}

	override fun onPreMotion(event: PreMotionEvent) {
        hypixelTimer.update()

        if (hypixelTimer.hasTimePassed(2)) {
            mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 1.0E-5, mc.thePlayer.posZ)
            hypixelTimer.reset()
        }

        if (!failedStart)
            mc.thePlayer.motionY = 0.0
	}

    override fun onPostMotion(event: PostMotionEvent) {
        val xDist = mc.thePlayer.posX - mc.thePlayer.prevPosX
        val zDist = mc.thePlayer.posZ - mc.thePlayer.prevPosZ
        lastDistance = sqrt(xDist * xDist + zDist * zDist)
    }

	override fun onSentPacket(event: SentPacketEvent) {
        val packet = event.packet

        if (packet is C03PacketPlayer)
            packet.onGround = false
    }

    override fun onReceivedPacket(event: ReceivedPacketEvent) {
        val packet = event.packet

        if (packet is S08PacketPlayerPosLook) {
            failedStart = true
            ClientUtils.displayChatMessage("§8[§c§lBoostHypixel-§a§lFly§8] §cSetback detected.")
        }
    }

    override fun onBlockBB(event: BlockBBEvent) {
        if (event.block is BlockAir && event.y < mc.thePlayer.posY && mc.thePlayer.inventory.getCurrentItem() == null)
            event.boundingBox = AxisAlignedBB.fromBounds(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(), (event.x + 1).toDouble(), mc.thePlayer.posY, (event.z + 1).toDouble())
    }

    override fun onJump(event: JumpEvent) {
        mc.thePlayer.inventory.getCurrentItem() ?: run {
            event.isCancelled = true
        }
    }

    override fun onStep(event: StepEvent) {
        mc.thePlayer.inventory.getCurrentItem() ?: run {
            event.stepHeight = 0f
        }
    }
}
