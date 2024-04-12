package net.minusmc.minusbounce.features.module.modules.combat

import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.Packet
import net.minecraft.network.play.client.C0FPacketConfirmTransaction
import net.minecraft.network.play.server.*
import net.minecraft.util.Vec3
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.event.PostMotionEvent
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.utils.EntityUtils
import net.minusmc.minusbounce.utils.timer.MSTimer
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.value.IntegerValue
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.abs


@ModuleInfo("BackTrack", "Back Track", "Let you attack in their previous position", ModuleCategory.COMBAT)
class BackTrack : Module() {
    private val delay = IntegerValue("Delay", 500, 100, 2000)
    private val minRange = FloatValue("Range", 2.8f, 1f, 6f)

    private val delayPing = BoolValue("Ping", true)
    private val delayVelocity = BoolValue("Velocity", true) { delayPing.get() }

    private val delayedPackets = CopyOnWriteArrayList<DelayedPacket>()

    private var lastTarget: EntityLivingBase? = null

    private var lastCursorTarget: EntityLivingBase? = null

    private var cursorTargetTicks = 0

    private var lastVelocity: Vec3? = null

    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (mc.thePlayer == null || mc.thePlayer.ticksExisted < 5) {
            if (!delayedPackets.isEmpty()) {
                delayedPackets.clear()
            }
        }

        if (currentTarget !== lastTarget) {
            clearPackets()
        }

        if (currentTarget == null) {
            clearPackets()
        } else {
            if (packet is S14PacketEntity) {
                if (packet.getEntity(mc.netHandler.clientWorldController) === currentTarget) {
                    val x = (currentTarget!!.serverPosX + packet.posX) / 32.0
                    val y = (currentTarget!!.serverPosY + packet.posY) / 32.0
                    val z = (currentTarget!!.serverPosZ + packet.posZ) / 32.0

                    if (getDistanceCustomPosition(x, y, z, currentTarget!!.eyeHeight.toDouble()) >= minRange.get()
                    ) {
                        event.cancelEvent()
                        delayedPackets.add(DelayedPacket(packet))
                    }
                }
            } else if (packet is S18PacketEntityTeleport) {
                if (packet.entityId == currentTarget!!.entityId) {
                    val serverX = packet.x.toDouble()
                    val serverY = packet.y.toDouble()
                    val serverZ = packet.z.toDouble()

                    val d0 = serverX / 32.0
                    val d1 = serverY / 32.0
                    val d2 = serverZ / 32.0

                    val x: Double
                    val y: Double
                    val z: Double

                    if (abs(serverX - d0) < 0.03125 && abs(serverY - d1) < 0.015625 && abs(
                            serverZ - d2
                        ) < 0.03125
                    ) {
                        x = currentTarget!!.posX
                        y = currentTarget!!.posY
                        z = currentTarget!!.posZ
                    } else {
                        x = d0
                        y = d1
                        z = d2
                    }

                    if (getDistanceCustomPosition(
                            x,
                            y,
                            z,
                            currentTarget!!.eyeHeight.toDouble()
                        ) >= minRange.get()
                    ) {
                        event.cancelEvent()
                        delayedPackets.add(DelayedPacket(packet))
                    }
                }
            } else if (packet is S32PacketConfirmTransaction || packet is S00PacketKeepAlive) {
                if (!delayedPackets.isEmpty() && delayPing.get()) {
                    event.cancelEvent()
                    delayedPackets.add(DelayedPacket(packet))
                }
            } else if (packet is S12PacketEntityVelocity) {
                if (packet.entityID == mc.thePlayer.entityId) {
                    if (!delayedPackets.isEmpty() && delayPing.get() && delayVelocity.get()) {
                        event.cancelEvent()
                        lastVelocity = Vec3(
                            packet.getMotionX() / 8000.0,
                            packet.getMotionY() / 8000.0,
                            packet.getMotionZ() / 8000.0
                        )
                    }
                }
            }
        }

        lastTarget = currentTarget
    }

    @EventTarget
    fun onPostMotion(event: PostMotionEvent?) {
        updatePackets()
    }

    val currentTarget: EntityLivingBase?
        get(){
            assert(mc.objectMouseOver != null)
            return MinusBounce.moduleManager.getModule(KillAura::class.java)?.let {
                if(it.state && it.target != null) it.target else null
            } ?: when {
                EntityUtils.isSelected(mc.objectMouseOver.entityHit, true) -> {
                    lastCursorTarget = mc.objectMouseOver.entityHit as EntityLivingBase
                    mc.objectMouseOver.entityHit as EntityLivingBase
                }
                lastCursorTarget != null -> {
                    if (++cursorTargetTicks > 10) {
                        lastCursorTarget = null
                        null
                    } else lastCursorTarget
                }
                else -> null
            }
        }

    fun updatePackets() {
        if (!delayedPackets.isEmpty()) {
            for (p in delayedPackets) {
                if (p.timer.hasTimePassed(delay.get())) {
                    clearPackets()

                    if (lastVelocity != null) {
                        mc.thePlayer.motionX = lastVelocity!!.xCoord
                        mc.thePlayer.motionY = lastVelocity!!.yCoord
                        mc.thePlayer.motionZ = lastVelocity!!.zCoord
                        lastVelocity = null
                    }

                    return
                }
            }
        }
    }

    fun clearPackets() {
        if (lastVelocity != null) {
            mc.thePlayer.motionX = lastVelocity!!.xCoord
            mc.thePlayer.motionY = lastVelocity!!.yCoord
            mc.thePlayer.motionZ = lastVelocity!!.zCoord
            lastVelocity = null
        }

        if (delayedPackets.isNotEmpty()) {
            delayedPackets.forEach { p ->
                when (val packet = p.packet) {
                    is S14PacketEntity -> handleEntityMovement(packet)
                    is S18PacketEntityTeleport -> handleEntityTeleport(packet)
                    is S32PacketConfirmTransaction -> handleConfirmTransaction(packet)
                    is S00PacketKeepAlive -> mc.netHandler.handleKeepAlive(packet)
                }
            }
            delayedPackets.clear()
        }
    }

    private fun handleEntityMovement(packetIn: S14PacketEntity) {
        packetIn.getEntity(mc.netHandler.clientWorldController)?.apply {
            serverPosX += packetIn.posX
            serverPosY += packetIn.posY
            serverPosZ += packetIn.posZ
            val d0 = serverPosX / 32.0
            val d1 = serverPosY / 32.0
            val d2 = serverPosZ / 32.0
            val f = if (packetIn.func_149060_h()) (packetIn.yaw * 360) / 256.0f else rotationYaw
            val f1 = if (packetIn.func_149060_h()) (packetIn.pitch * 360) / 256.0f else rotationPitch
            setPositionAndRotation2(d0, d1, d2, f, f1, 3, false)
            onGround = packetIn.onGround
        }
    }

    fun handleEntityTeleport(packetIn: S18PacketEntityTeleport) {
        mc.netHandler.clientWorldController.getEntityByID(packetIn.entityId)?.apply {
            serverPosX = packetIn.x
            serverPosY = packetIn.y
            serverPosZ = packetIn.z
            val d0 = serverPosX / 32.0
            val d1 = serverPosY / 32.0
            val d2 = serverPosZ / 32.0
            val f = (packetIn.yaw * 360).toFloat() / 256.0f
            val f1 = (packetIn.pitch * 360).toFloat() / 256.0f

            val isCloseEnough = abs(posX - d0) < 0.03125 &&
                    abs(posY - d1) < 0.015625 &&
                    abs(posZ - d2) < 0.03125

            setPositionAndRotation2(if (isCloseEnough) posX else d0, d1, d2, f, f1, 3, true)
            onGround = packetIn.onGround
        }
    }

    fun handleConfirmTransaction(packetIn: S32PacketConfirmTransaction) {
        when (packetIn.windowId) {
            0 -> mc.thePlayer.inventoryContainer
            mc.thePlayer.openContainer.windowId -> mc.thePlayer.openContainer
            else -> null
        }?.takeIf { !packetIn.func_148888_e() }?.let {
            mc.netHandler.addToSendQueue(C0FPacketConfirmTransaction(packetIn.windowId, packetIn.actionNumber, true))
        }
    }

    fun getDistanceCustomPosition(x: Double, y: Double, z: Double, eyeHeight: Double): Double {
        val playerVec = Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ)

        val yDiff = mc.thePlayer.posY - y

        val targetY =
            if (yDiff > 0) y + eyeHeight else if (-yDiff < mc.thePlayer.getEyeHeight()) mc.thePlayer.posY + mc.thePlayer.getEyeHeight() else y

        val targetVec = Vec3(x, targetY, z)

        return playerVec.distanceTo(targetVec) - 0.3f
    }
}

data class DelayedPacket(val packet: Packet<*>, val timer: MSTimer = MSTimer())
