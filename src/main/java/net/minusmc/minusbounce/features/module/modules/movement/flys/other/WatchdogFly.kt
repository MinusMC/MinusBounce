package net.minusmc.minusbounce.features.module.modules.movement.flys.other

import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyType
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyMode
import net.minusmc.minusbounce.utils.player.MovementUtils
import net.minusmc.minusbounce.utils.player.RotationUtils
import net.minusmc.minusbounce.utils.PacketUtils
import net.minusmc.minusbounce.utils.PlayerUtils
import net.minusmc.minusbounce.event.PreMotionEvent
import net.minusmc.minusbounce.event.PostMotionEvent
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.event.JumpEvent
import net.minusmc.minusbounce.event.StepEvent
import net.minusmc.minusbounce.event.MoveEvent
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.ui.client.hud.element.elements.Notification

import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.network.play.server.S08PacketPlayerPosLook


class WatchdogFly: FlyMode("Watchdog", FlyType.OTHER) {
	
    private var wdState: Int = 0
    private var expectItemStack: Int = 0
	

    override fun handleUpdate() {}

	override fun onEnable() {
		expectItemStack = PlayerUtils.getSlimeSlot()
        if (expectItemStack == -1) {
            MinusBounce.hud.addNotification(Notification("Fly", "The fly requires slime blocks to be activated properly."))
            return
        }

        if (mc.thePlayer.onGround) {
            mc.thePlayer.jump()
            wdState = 1
        }
        return
	}

	override fun onPreMotion(event: PreMotionEvent) {
        val current = mc.thePlayer.inventory.currentItem
        if (wdState == 1 && mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.entityBoundingBox.offset(0.0, -1.0, 0.0).expand(0.0, 0.0, 0.0)).isEmpty()) {
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
                MovementUtils.strafe(MovementUtils.getBaseMoveSpeed().toFloat() * 0.938f)
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
        if (wdState == 2) {
            if (mc.playerController.onPlayerRightClick(
                mc.thePlayer, mc.theWorld, 
                mc.thePlayer.inventoryContainer.getSlot(expectItemStack).stack, 
                BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 2, mc.thePlayer.posZ), 
                EnumFacing.UP, 
                RotationUtils.getVectorForRotation(RotationUtils.getRotations(mc.thePlayer.posX, mc.thePlayer.posZ, mc.thePlayer.posY - 1))))
                mc.netHandler.addToSendQueue(C0APacketAnimation())
            wdState = 3
        }
    }

	override fun onPacket(event: PacketEvent) {
		val packet = event.packet
		if (packet is C09PacketHeldItemChange && wdState < 4) event.cancelEvent()
		if (packet is S08PacketPlayerPosLook) {
			if (wdState == 3) {
				wdState = 4
				if (fly.fakeDmgValue.get() && mc.thePlayer != null)
                    mc.thePlayer.handleStatusUpdate(2.toByte())
			}
		}
	}

	override fun onMove(event: MoveEvent) {
		if (wdState < 4) event.zeroXZ()
	}

	override fun onJump(event: JumpEvent) {
		if (wdState >= 1) event.cancelEvent()
	}

	override fun onStep(event: StepEvent) {
		event.stepHeight = 0f
	}
}