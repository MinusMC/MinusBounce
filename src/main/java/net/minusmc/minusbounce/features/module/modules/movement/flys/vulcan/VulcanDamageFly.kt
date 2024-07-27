package net.minusmc.minusbounce.features.module.modules.movement.flys.vulcan

import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook
import net.minecraft.network.play.client.C0FPacketConfirmTransaction
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minusmc.minusbounce.event.ReceivedPacketEvent
import net.minusmc.minusbounce.event.SentPacketEvent
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyMode
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyType
import net.minusmc.minusbounce.utils.player.MovementUtils
import net.minusmc.minusbounce.utils.misc.MathUtils
import net.minusmc.minusbounce.utils.PacketUtils
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.value.ListValue
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class VulcanDamageFly: FlyMode("VulcanDamage", FlyType.VULCAN) {
    private val bypassMode = ListValue("BypassMode", arrayOf("Damage", "SelfDamage", "InstantDamage", "Flag"), "InstantDamage")
    private val flyMode = ListValue("FlyMode", arrayOf("Timer", "CancelMove", "Clip"), "CancelMove")
    private val flyHSpeedValue = FloatValue("Horizontal", 1.0f, 0.5f, 2.5f) { flyMode.get().equals("CancelMove", true) }
    private val flyVSpeedValue = FloatValue("Vertical", 0.42f, 0.42f, 2.5f) { flyMode.get().equals("CancelMove", true) }
    private val flyDistanceValue = FloatValue("Distance", 10.0f, 6.0f, 10.0f)
    private val autoDisableValue = BoolValue("AutoDisable", true)
    private val flyTimerValue = FloatValue("Timer", 0.05f, 0.05f, 0.25f) { flyMode.get().equals("Timer", true) }
    private var waitFlag = false
    private var isStarted = false
    private var isDamaged = false
    private var dmgJumpCount = 0
    private var flyTicks = 0
    
    private var lastSentX = 0.0
    private var lastSentY = 0.0
    private var lastSentZ = 0.0
    
    private var lastTickX = 0.0
    private var lastTickY = 0.0
    private var lastTickZ = 0.0

    private fun runSelfDamageCore(): Boolean {
        mc.timer.timerSpeed = 1.0f
        if (bypassMode.equals("damage") || bypassMode.equals("flag")) {
            if (!bypassMode.equals("true")) {
                val damaged = mc.thePlayer.hurtTime > 0 || isDamaged

                if (damaged) {
                    isDamaged = true
                    dmgJumpCount = 999
                }

                return damaged
            }

            isDamaged = true
            dmgJumpCount = 999
            return false
        }

        if (isDamaged) {
            dmgJumpCount = 999
            return false
        }

        mc.thePlayer.jumpMovementFactor = 0.00f

        if (mc.thePlayer.onGround) {
            if (dmgJumpCount >= 4) {
                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
                isDamaged = true
                dmgJumpCount = 999
                return false
            }
            dmgJumpCount++
            MovementUtils.resetMotion(true)
            mc.thePlayer.jump()
        }

        MovementUtils.resetMotion()
        return true
    }

    override fun onEnable() {
		super.onEnable()
        flyTicks = 0
        waitFlag = false
        isStarted = false
        isDamaged = false
        dmgJumpCount = 0
        mc.timer.timerSpeed = 1.0f

        when (bypassMode.get().lowercase()) {
            "instantdamage" -> {
                dmgJumpCount = 11451
                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - 0.0784, mc.thePlayer.posZ, false))
                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.41999998688697815, mc.thePlayer.posZ, false))
                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.7531999805212, mc.thePlayer.posZ, false))
                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 1.0, mc.thePlayer.posZ, true))
                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 1.4199999868869781, mc.thePlayer.posZ, false))
                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 1.7531999805212, mc.thePlayer.posZ, false))
                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 2.0, mc.thePlayer.posZ, true))
                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 2.419999986886978, mc.thePlayer.posZ, false))
                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 2.7531999805212, mc.thePlayer.posZ, false))
                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 3.00133597911214, mc.thePlayer.posZ, false))
                mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 3.00133597911214, mc.thePlayer.posZ)
                waitFlag = true
            }
            "flag" -> {
                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - 2, mc.thePlayer.posZ, true))
                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
            }
            else -> runSelfDamageCore()
        }
    }

    override fun onUpdate() {
        if (flyTicks > 7 && autoDisableValue.get()) {
            fly.state = false
            fly.onDisable()
            return
        }
        
        if (!bypassMode.equals("instantdamage") && runSelfDamageCore())
            return

        if (bypassMode.equals("instantdamage") && dmgJumpCount == 11451) {
            if (!isStarted)
                return

            isStarted = false
            waitFlag = false
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
            dmgJumpCount = 999
        }

        mc.thePlayer.jumpMovementFactor = 0.00f

        if (!isStarted && !waitFlag) {
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - 0.0784, mc.thePlayer.posZ, false))
            waitFlag = true
        }

        if (!isStarted)
            return

        when (flyMode.get().lowercase()) {
            "cancelmove" -> {
                mc.timer.timerSpeed = 1.0f
                MovementUtils.resetMotion(false)
                if (!mc.gameSettings.keyBindSneak.isKeyDown) {
                    MovementUtils.resetMotion(true)

                    if (mc.gameSettings.keyBindJump.isKeyDown)
                        mc.thePlayer.motionY = flyVSpeedValue.get().toDouble()
                }
                
                MovementUtils.strafe(flyHSpeedValue.get())
            }

            "timer" -> {
                flyTicks++
                mc.timer.timerSpeed = flyTimerValue.get()
                MovementUtils.resetMotion(true)

                if (flyTicks > 4)
                    MovementUtils.strafe(flyDistanceValue.get() - 0.005f)
                else
                    MovementUtils.strafe(flyDistanceValue.get() - 0.205f + flyTicks.toFloat() * 0.05f)
            }

            "clip" -> {
                MovementUtils.resetMotion(true)
                if (mc.thePlayer.ticksExisted % 10 != 0)
                    return

                flyTicks++
                val yaw = MathUtils.toRadians(mc.thePlayer.rotationYaw)

                val x = sin(yaw) * flyDistanceValue.get()
                val z = cos(yaw) * flyDistanceValue.get()

                mc.thePlayer.setPosition(mc.thePlayer.posX - x, mc.thePlayer.posY + 0.42, mc.thePlayer.posZ + z)
                PacketUtils.sendPacketNoEvent(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false))
            }
        }
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1.0f
    }

    override fun onSentPacket(event: SentPacketEvent) {
        val packet = event.packet

        if (packet is C03PacketPlayer && waitFlag)
            event.isCancelled = true

        if (packet is C03PacketPlayer && dmgJumpCount < 4 && (bypassMode.equals("selfdamage") || bypassMode.equals("instantdamage")))
            packet.onGround = false

        if (isStarted && flyMode.equals("cancelmove") && packet is C03PacketPlayer) {
            if (packet.rotating) {
                val deltaX = packet.x - lastSentX
                val deltaY = packet.y - lastSentY
                val deltaZ = packet.z - lastSentZ

                val distance = sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ)

                if (distance > flyDistanceValue.get()) {
                    flyTicks++
                    PacketUtils.sendPacketNoEvent(C04PacketPlayerPosition(lastTickX, lastTickY, lastTickZ, false))
                    lastSentX = lastTickX
                    lastSentY = lastTickY
                    lastSentZ = lastTickZ
                }

                lastTickX = packet.x
                lastTickY = packet.y
                lastTickZ = packet.z
            }

            event.isCancelled = true
        }
        
        if (packet is C03PacketPlayer && flyMode.equals("clip") && isStarted)
            event.isCancelled = true
        
        
    }

    override fun onReceivedPacket(event: ReceivedPacketEvent) {
        val packet = event.packet

        if (packet is S08PacketPlayerPosLook) {
            isStarted = true
            waitFlag = false
        }

        if (packet is S08PacketPlayerPosLook) {
            if (flyMode.equals("cancelmove")) {
                lastSentX = packet.x
                lastSentY = packet.y
                lastSentZ = packet.z
                
                if (!bypassMode.equals("instantdamage"))
                    event.isCancelled = true
                
                mc.thePlayer.motionX = 0.0
                PacketUtils.sendPacketNoEvent(C06PacketPlayerPosLook(packet.x, packet.y, packet.z, packet.yaw, packet.pitch, false))
            
                return
            }

            if (bypassMode.equals("instantdamage"))
                PacketUtils.sendPacketNoEvent(C06PacketPlayerPosLook(packet.x, packet.y, packet.z, packet.yaw, packet.pitch, false))
            
            mc.timer.timerSpeed = 1.0f
            flyTicks = 0

        }
        
        if (packet is C0FPacketConfirmTransaction) {
            val transUID = (packet.uid).toInt()
            if (transUID in -31767..-30769) {
                event.isCancelled = true
                PacketUtils.sendPacketNoEvent(packet)
            }
        }
    }
}