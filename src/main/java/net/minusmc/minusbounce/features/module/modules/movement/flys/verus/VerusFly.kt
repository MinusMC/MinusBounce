package net.minusmc.minusbounce.features.module.modules.movement.flys.verus

import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyType
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyMode
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.ListValue
import net.minusmc.minusbounce.value.IntegerValue
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.utils.timer.TickTimer
import net.minusmc.minusbounce.utils.PacketUtils
import net.minusmc.minusbounce.utils.MovementUtils
import net.minusmc.minusbounce.utils.render.RenderUtils
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.event.MoveEvent
import net.minusmc.minusbounce.event.BlockBBEvent
import net.minecraft.block.BlockAir
import net.minecraft.util.AxisAlignedBB
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook

import java.awt.Color

class VerusFly: FlyMode("Verus", FlyType.VERUS) {

    private val verusTimer = TickTimer()
    private var verusJumpTimes: Int = 0
    private var boostTicks: Int = 0
    private var dmgCooldown: Int = 0
    private var verusDmged = false
    private var shouldActiveDmg = false

    private val verusDmgModeValue = ListValue("DamageMode", arrayOf("None", "Instant", "InstantC06", "Jump"), "None")
    private val verusBoostModeValue = ListValue("BoostMode", arrayOf("Static", "Gradual"), "Gradual") {
        !verusDmgModeValue.get().equals("none", true)
    }
    private val verusReDamageValue = BoolValue("ReDamage", true) {
        !verusDmgModeValue.get().equals("none", true) && !verusDmgModeValue.get().equals("jump", true)
    }
    private val verusReDmgTickValue = IntegerValue("ReDamage-Ticks", 20, 0, 300) {
        !verusDmgModeValue.get().equals("none", true) && !verusDmgModeValue.get()
            .equals("jump", true) && verusReDamageValue.get()
    }
    private val verusVisualValue = BoolValue("VisualPos", false)
    private val verusVisualHeightValue = FloatValue("VisualHeight", 0.42F, 0F, 1F) { verusVisualValue.get() }
    private val verusSpeedValue = FloatValue("Speed", 5F, 0F, 10F) { !verusDmgModeValue.get().equals("none", true) }
    private val verusTimerValue = FloatValue("Timer", 1F, 0.1F, 10F) { !verusDmgModeValue.get().equals("none", true) }
    private val verusDmgTickValue = IntegerValue("Ticks", 20, 0, 300) { !verusDmgModeValue.get().equals("none", true) }
    private val verusSpoofGround = BoolValue("SpoofGround", false)

    override fun handleUpdate() {}

    override fun onEnable() {
        verusTimer.reset()
        verusJumpTimes = 0
        verusDmged = false

        val y = mc.thePlayer.posY
        when (verusDmgModeValue.get()) {
            "Instant" -> {
                if (mc.thePlayer.onGround && mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.entityBoundingBox.offset(0.0, 4.0, 0.0).expand(0.0, 0.0, 0.0)).isEmpty()) {
                    PacketUtils.sendPacketNoEvent(C04PacketPlayerPosition(mc.thePlayer.posX, y + 4, mc.thePlayer.posZ, false))
                    PacketUtils.sendPacketNoEvent(C04PacketPlayerPosition(mc.thePlayer.posX, y, mc.thePlayer.posZ, false))
                    PacketUtils.sendPacketNoEvent(C04PacketPlayerPosition(mc.thePlayer.posX, y, mc.thePlayer.posZ, true))
                    mc.thePlayer.motionX = 0.0
                    mc.thePlayer.motionZ = 0.0
                    if (verusReDamageValue.get()) dmgCooldown = verusReDmgTickValue.get()
                }
            }
            "InstantC06" -> {
                if (mc.thePlayer.onGround && mc.theWorld.getCollidingBoundingBoxes(
                        mc.thePlayer,
                        mc.thePlayer.entityBoundingBox.offset(0.0, 4.0, 0.0).expand(0.0, 0.0, 0.0)
                    ).isEmpty()
                ) {
                    PacketUtils.sendPacketNoEvent(C06PacketPlayerPosLook(mc.thePlayer.posX, y + 4, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, false))
                    PacketUtils.sendPacketNoEvent(C06PacketPlayerPosLook(mc.thePlayer.posX, y, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, false))
                    PacketUtils.sendPacketNoEvent(C06PacketPlayerPosLook(mc.thePlayer.posX, y, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, true))
                    mc.thePlayer.motionX = 0.0
                    mc.thePlayer.motionZ = 0.0
                    if (verusReDamageValue.get()) dmgCooldown = verusReDmgTickValue.get()
                }
            }
            "Jump" -> {
                if (mc.thePlayer.onGround) {
                    mc.thePlayer.jump()
                    verusJumpTimes = 1
                }
            }
            else -> verusDmged = true
        }

        if (verusVisualValue.get()) mc.thePlayer.setPosition(mc.thePlayer.posX, y + verusVisualHeightValue.get(), mc.thePlayer.posZ)
        shouldActiveDmg = dmgCooldown > 0
    }

    override fun resetMotion() {
        if (fly.resetMotionValue.get() && boostTicks > 0) {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
    }

    override fun onUpdate() {
        mc.thePlayer.capabilities.isFlying = false
        mc.thePlayer.motionX = 0.0 
        mc.thePlayer.motionZ = 0.0
        if (!verusDmgModeValue.get().equals("Jump", true) || shouldActiveDmg || verusDmged)
            mc.thePlayer.motionY = 0.0

        if (verusDmgModeValue.get().equals("Jump", true) && verusJumpTimes < 5) {
            if (mc.thePlayer.onGround) {
                mc.thePlayer.jump()
                verusJumpTimes += 1
            }
            return
        }

        if (shouldActiveDmg) {
            if (dmgCooldown > 0) 
                dmgCooldown--
            else if (verusDmged) {
                verusDmged = false
                val y = mc.thePlayer.posY
                when (verusDmgModeValue.get()) {
                    "Instant" -> {
                        if (mc.thePlayer.onGround && mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.entityBoundingBox.offset(0.0, 4.0, 0.0).expand(0.0, 0.0, 0.0)).isEmpty()) {
                            PacketUtils.sendPacketNoEvent(C04PacketPlayerPosition(mc.thePlayer.posX, y + 4, mc.thePlayer.posZ, false))
                            PacketUtils.sendPacketNoEvent(C04PacketPlayerPosition(mc.thePlayer.posX, y, mc.thePlayer.posZ, false))
                            PacketUtils.sendPacketNoEvent(C04PacketPlayerPosition(mc.thePlayer.posX, y, mc.thePlayer.posZ, true))
                            mc.thePlayer.motionX = 0.0
                            mc.thePlayer.motionZ = 0.0
                        }
                    }
                    "InstantC06" -> {
                        if (mc.thePlayer.onGround && mc.theWorld.getCollidingBoundingBoxes(
                                mc.thePlayer,
                                mc.thePlayer.entityBoundingBox.offset(0.0, 4.0, 0.0).expand(0.0, 0.0, 0.0)
                            ).isEmpty()
                        ) {
                            PacketUtils.sendPacketNoEvent(C06PacketPlayerPosLook(mc.thePlayer.posX, y + 4, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, false))
                            PacketUtils.sendPacketNoEvent(C06PacketPlayerPosLook(mc.thePlayer.posX, y, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, false))
                            PacketUtils.sendPacketNoEvent(C06PacketPlayerPosLook(mc.thePlayer.posX, y, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, true))
                            mc.thePlayer.motionX = 0.0
                            mc.thePlayer.motionZ = 0.0
                        }
                    }
                }
                dmgCooldown = verusReDmgTickValue.get()
            }
        }

        if (!verusDmged && mc.thePlayer.hurtTime > 0) {
            verusDmged = true
            boostTicks = verusDmgTickValue.get()
        }

        if (boostTicks > 0) {
            mc.timer.timerSpeed = verusTimerValue.get()

            val motion = if (verusBoostModeValue.get().equals("static", true)) verusSpeedValue.get()
            else (boostTicks / verusDmgTickValue.get()) * verusSpeedValue.get()
            boostTicks--

            MovementUtils.strafe(motion)
        } else if (verusDmged) {
            mc.timer.timerSpeed = 1f
            MovementUtils.strafe(MovementUtils.baseMoveSpeed.toFloat() * 0.6f)
        } else {
            mc.thePlayer.movementInput.moveForward = 0f
            mc.thePlayer.movementInput.moveStrafe = 0f
        }
    }

    override fun onRender2D() {
        val scaledRes = ScaledResolution(mc)
        if (boostTicks > 0) {
            val width = (verusDmgTickValue.get() - boostTicks) / verusDmgTickValue.get() * 60f
            RenderUtils.drawRect(scaledRes.scaledWidth / 2F - 31F, scaledRes.scaledHeight / 2F + 14F, scaledRes.scaledWidth / 2F + 31F, scaledRes.scaledHeight / 2F + 18F, Color(160, 0, 0).rgb)
            RenderUtils.drawRect(scaledRes.scaledWidth / 2F - 30F, scaledRes.scaledHeight / 2F + 15F, scaledRes.scaledWidth / 2F - 30F + width, scaledRes.scaledHeight / 2F + 17F, Color(255, 255, 255).rgb)
        }
        if (shouldActiveDmg) {
            val width = (verusReDmgTickValue.get() - dmgCooldown) / verusReDmgTickValue.get() * 60f
            RenderUtils.drawRect(scaledRes.scaledWidth / 2F - 31F, scaledRes.scaledHeight / 2F + 14F + 10F, scaledRes.scaledWidth / 2F + 31F, scaledRes.scaledHeight / 2F + 18F + 10F, Color(160, 0, 0).rgb)
            RenderUtils.drawRect(scaledRes.scaledWidth / 2F - 30F, scaledRes.scaledHeight / 2F + 15F + 10F, scaledRes.scaledWidth / 2F - 30F + width, scaledRes.scaledHeight / 2F + 17F + 10F, Color(255, 31, 31).rgb)
        }
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is C03PacketPlayer) {
            if (verusSpoofGround.get() && verusDmged) packet.onGround = true
            if (verusDmgModeValue.get().equals("Jump", true) && verusJumpTimes < 5) packet.onGround = false
        }
    }

    override fun onMove(event: MoveEvent) {
        if (!verusDmged)
            if (verusDmgModeValue.get().equals("Jump", true))
                event.zeroXZ()
            else
                event.cancelEvent()
    }

    override fun onBlockBB(event: BlockBBEvent) {
        if (event.block is BlockAir && (verusDmgModeValue.get().equals("none", true) || verusDmged) && event.y < mc.thePlayer.posY) {
            event.boundingBox = AxisAlignedBB.fromBounds(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(), (event.x + 1).toDouble(), mc.thePlayer.posY, (event.z + 1).toDouble())
        }
    }

}