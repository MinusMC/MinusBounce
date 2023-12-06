package net.minusmc.minusbounce.features.module.modules.movement.longjumps.other

import net.minusmc.minusbounce.features.module.modules.movement.longjumps.LongJumpMode
import net.minusmc.minusbounce.utils.MovementUtils
import net.minusmc.minusbounce.utils.ClientUtils
import net.minusmc.minusbounce.utils.PosLookInstance
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.ListValue
import net.minusmc.minusbounce.event.MoveEvent
import net.minusmc.minusbounce.event.PacketEvent
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook
import net.minecraft.network.play.server.S08PacketPlayerPosLook


class MatrixFlagLongJump : LongJumpMode("MatrixFlag") {
	private val matrixBoostValue = FloatValue("Boost", 1.95F, 0F, 3F)
    private val matrixHeightValue = FloatValue("Height", 5F, 0F, 10F)
    private val matrixSilentValue = BoolValue("Silent", true)
    private val matrixBypassModeValue = ListValue("BypassMode", arrayOf("Motion", "Clip", "None"), "Motion")
    private val matrixDebugValue = BoolValue("Debug", true)

    private var posLookInstance = PosLookInstance()
    private var hasFell = false
    private var flagged = false

    private var lastMotX = 0.0
    private var lastMotY = 0.0
    private var lastMotZ = 0.0

    fun debug(message: String) {
        if (matrixDebugValue.get())
            ClientUtils.displayChatMessage(message)
    }

    override fun onEnable() {
        mc.thePlayer ?: return
    	hasFell = false
    	flagged = false
        posLookInstance.reset()

    	if (matrixBypassModeValue.get().equals("none", true)) {
            debug("no less flag enabled.")
            hasFell = true
            return
        }
        if (mc.thePlayer.onGround) {
            if (matrixBypassModeValue.get().equals("clip", true)) {
                mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.01, mc.thePlayer.posZ)
                debug("clipped")
            }
            if (matrixBypassModeValue.get().equals("motion", true))
                mc.thePlayer.jump()
        } else if (mc.thePlayer.fallDistance > 0f) {
            hasFell = true
            debug("falling detected")
        }
    }

	override fun onUpdateSpecial() {
		if (hasFell) {
            if (!flagged && !matrixSilentValue.get()) {
                MovementUtils.strafe(matrixBoostValue.get())
                mc.thePlayer.motionY = matrixHeightValue.get().toDouble()
                debug("triggering")
            }
        } else {
            if (matrixBypassModeValue.get().equals("motion", true)) {
                mc.thePlayer.motionX *= 0.2
                mc.thePlayer.motionZ *= 0.2
                if (mc.thePlayer.fallDistance > 0) {
                    hasFell = true
                    debug("activated")
                }
            }
            if (matrixBypassModeValue.get().equals("clip", true) && mc.thePlayer.motionY < 0F) {
                hasFell = true
                debug("activated")
            }
        }
        return
	}

	override fun onPacket(event: PacketEvent) {
		val packet = event.packet
		if (packet is C03PacketPlayer) {
			if (packet is C06PacketPlayerPosLook && posLookInstance.equalFlag(packet)) {
	            posLookInstance.reset()
	            mc.thePlayer.motionX = lastMotX
	            mc.thePlayer.motionY = lastMotY
	            mc.thePlayer.motionZ = lastMotZ
	            debug("should be launched by now")
	        } else if (matrixSilentValue.get()) {
	            if (hasFell && !flagged) {
                    if (packet.isMoving) {
	                    debug("modifying packet: rotate false, onGround false, moving enabled, x, y, z set to expected speed")
	                    packet.onGround = false
	                    val yaw = if (packet.rotating) packet.yaw else mc.thePlayer.rotationYaw
	                    val xz = MovementUtils.getXZDist(matrixBoostValue.get(), yaw)
	                    lastMotX = xz[0]
	                    lastMotY = matrixHeightValue.get().toDouble()
	                    lastMotZ = xz[1]
	                    packet.x += lastMotX
	                    packet.y += lastMotY
	                    packet.z += lastMotZ
	                }
	            }
	        }
		}

        if (packet is S08PacketPlayerPosLook && hasFell) {
            debug("flag check started")
            flagged = true
            posLookInstance.set(packet)
            if (!matrixSilentValue.get()) {
                debug("data saved")
                lastMotX = mc.thePlayer.motionX
                lastMotY = mc.thePlayer.motionY
                lastMotZ = mc.thePlayer.motionZ
            }
        }
	}

	override fun onMove(event: MoveEvent) {
		if (matrixSilentValue.get() && hasFell && !flagged) event.cancelEvent()
	}
}
