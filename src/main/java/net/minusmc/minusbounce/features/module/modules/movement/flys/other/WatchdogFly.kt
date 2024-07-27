package net.minusmc.minusbounce.features.module.modules.movement.flys.other

import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyType
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyMode
import net.minusmc.minusbounce.utils.player.MovementUtils
import net.minusmc.minusbounce.utils.player.RotationUtils
import net.minusmc.minusbounce.utils.PacketUtils
import net.minusmc.minusbounce.utils.PlayerUtils
import net.minusmc.minusbounce.event.PreMotionEvent
import net.minusmc.minusbounce.event.PostMotionEvent
import net.minusmc.minusbounce.event.SentPacketEvent
import net.minusmc.minusbounce.event.ReceivedPacketEvent
import net.minusmc.minusbounce.event.JumpEvent
import net.minusmc.minusbounce.event.StepEvent
import net.minusmc.minusbounce.event.MoveEvent
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.ui.client.hud.element.elements.Notification

import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.network.play.server.S08PacketPlayerPosLook


class WatchdogFly: FlyMode("Watchdog", FlyType.OTHER) {
    private val fakeDamageWhenFlag = BoolValue("FakeDamageWhenFlag", true)

    private var wdState = 0
    private var expectItemStack = 0

	override fun onEnable() {
		super.onEnable()
		expectItemStack = PlayerUtils.getSlimeSlot()
        if (expectItemStack == -1) {
            MinusBounce.hud.addNotification(Notification("Fly", "The fly requires slime blocks to be activated properly."))
            return
        }

        if (mc.thePlayer.onGround) {
            mc.thePlayer.jump()
            wdState = 1
        }
	}

	override fun onPreMotion(event: PreMotionEvent) {
        val current = mc.thePlayer.inventory.currentItem
        if (wdState == 1 && mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.entityBoundingBox.offset(0.0, -1.0, 0.0)).isEmpty()) {
            PacketUtils.sendPacketNoEvent(C09PacketHeldItemChange(expectItemStack))
            wdState = 2
        }

        mc.timer.timerSpeed = 1f

        if (wdState == 3 && expectItemStack != -1) {
            PacketUtils.sendPacketNoEvent(C09PacketHeldItemChange(current))
            expectItemStack = -1
        }

        if (wdState == 4) {
            if (MovementUtils.isMoving)
                MovementUtils.strafe(0.938f * MovementUtils.baseMoveSpeed.toFloat())
            else
                MovementUtils.strafe(0f)

            mc.thePlayer.motionY = -0.0015
        } else if (wdState < 3) {
            val rot = RotationUtils.getRotations(mc.thePlayer.posX, mc.thePlayer.posZ, mc.thePlayer.posY - 1)
            RotationUtils.setTargetRotation(rot)
            event.yaw = rot.yaw
            event.pitch = rot.pitch
        } else
            event.y -= 0.08
	}

    override fun onPostMotion(event: PostMotionEvent) {
        if (wdState != 2)
            return

        val stack = mc.thePlayer.inventoryContainer.getSlot(expectItemStack).stack
        val blockPos = BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 2, mc.thePlayer.posZ)
        val rotation = RotationUtils.getRotations(mc.thePlayer.posX, mc.thePlayer.posZ, mc.thePlayer.posY - 1)
        val vec = RotationUtils.getVectorForRotation(rotation)

        if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, stack, blockPos, EnumFacing.UP, vec))
            mc.netHandler.addToSendQueue(C0APacketAnimation())

        wdState = 3
    }

	override fun onSentPacket(event: SentPacketEvent) {
		val packet = event.packet

		if (packet is C09PacketHeldItemChange && wdState < 4)
            event.isCancelled = true
	}

    override fun onReceivedPacket(event: ReceivedPacketEvent) {
        val packet = event.packet

        if (packet is S08PacketPlayerPosLook && wdState == 3) {
            wdState = 4

            if (fakeDamageWhenFlag.get())
                mc.thePlayer.handleStatusUpdate(2.toByte())
        }
    }

	override fun onMove(event: MoveEvent) {
		if (wdState < 4)
            event.zeroXZ()
	}

	override fun onJump(event: JumpEvent) {
		if (wdState >= 1)
            event.isCancelled = true
	}

	override fun onStep(event: StepEvent) {
		event.stepHeight = 0f
	}
}