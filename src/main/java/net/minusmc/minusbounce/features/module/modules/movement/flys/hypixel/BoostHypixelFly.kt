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
import net.minusmc.minusbounce.utils.MovementUtils
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

    override fun handleUpdate() {}

	override fun onEnable() {
		moveSpeed = 0.0

		if(!mc.thePlayer.onGround) return

        if (hypixelC04.get()) for (i in 0..9)
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

        if (failedStart) return

        val amplifier =
            if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) 1.2 * (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).amplifier + 1) else 1.0
        val baseSpeed = 0.29 * amplifier

        when (boostHypixelState) {
            1 -> {
            	moveSpeed = if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) 1.56 * baseSpeed else 2.034 * baseSpeed
                boostHypixelState = 2
            }
            2 -> {
            	moveSpeed *= 2.16
                boostHypixelState = 3
            }
            3 -> {
            	moveSpeed =  if (mc.thePlayer.ticksExisted % 2 == 0) lastDistance - 0.0103 * (lastDistance - baseSpeed) else lastDistance - 0.0123 * (lastDistance - baseSpeed)
                boostHypixelState = 4
            }
            else -> moveSpeed = lastDistance - lastDistance / 159.8
        }

        moveSpeed = max(moveSpeed, 0.3)

        val yaw = MovementUtils.direction
        event.x = -sin(yaw) * moveSpeed
        event.z = cos(yaw) * moveSpeed
        mc.thePlayer.motionX = event.x
        mc.thePlayer.motionZ = event.z
	}

	override fun onMotion(event: MotionEvent) {
		when (event.eventState) {
	        EventState.PRE -> {
	        	hypixelTimer.update()
	            if (hypixelTimer.hasTimePassed(2)) {
	                mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 1.0E-5, mc.thePlayer.posZ)
	                hypixelTimer.reset()
	            }
	            if(!failedStart) mc.thePlayer.motionY = 0.0
	        }
	        EventState.POST -> {
	        	val xDist = mc.thePlayer.posX - mc.thePlayer.prevPosX
	            val zDist = mc.thePlayer.posZ - mc.thePlayer.prevPosZ
                lastDistance = sqrt(xDist * xDist + zDist * zDist)
	        }
	    }
	}

	override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is C03PacketPlayer) packet.onGround = false
        if (packet is S08PacketPlayerPosLook) {
        	failedStart = true
            ClientUtils.displayChatMessage("§8[§c§lBoostHypixel-§a§lFly§8] §cSetback detected.")
        }
    }

    override fun onBlockBB(event: BlockBBEvent) {
        if (event.block is BlockAir && event.y < mc.thePlayer.posY && mc.thePlayer.inventory.getCurrentItem() == null) {
            event.boundingBox = AxisAlignedBB.fromBounds(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(), (event.x + 1).toDouble(), mc.thePlayer.posY, (event.z + 1).toDouble())
        }
    }

    override fun onJump(event: JumpEvent) {
        if (mc.thePlayer.inventory.getCurrentItem() == null) event.cancelEvent()
    }

    override fun onStep(event: StepEvent) {
        if (mc.thePlayer.inventory.getCurrentItem() == null) event.stepHeight = 0f
    }
}
