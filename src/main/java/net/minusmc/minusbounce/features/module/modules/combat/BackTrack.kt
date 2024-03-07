/*package net.minusmc.minusbounce.features.module.modules.combat

import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.Container
import net.minecraft.network.Packet
import net.minecraft.network.play.client.C0FPacketConfirmTransaction
import net.minecraft.network.play.server.*
import net.minecraft.util.MovingObjectPosition
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.event.PostMotionEvent
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.utils.PendingVelocity
import net.minusmc.minusbounce.utils.storagePacket.StoragePacket
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.value.IntegerValue
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.abs


@ModuleInfo(name = "BackTrack", spacedName = "Back Track", description = "Attack entity in previous look.", category = ModuleCategory.COMBAT)
class BackTrack : Module() {
    private val delay = IntegerValue("Delay", 500, 100, 2000)
    private val minRange = FloatValue("Min range", 2.8f, 1f, 6f)

    private val delayPing = BoolValue("Delay ping", true)
    private val delayVelocity = BoolValue("Delay velocity", true) { delayPing.get() }

    private val storagePackets: CopyOnWriteArrayList<StoragePacket> = CopyOnWriteArrayList<StoragePacket>()

    private var lastTarget: EntityLivingBase? = null

    private var lastCursorTarget: EntityLivingBase? = null
 
    private var cursorTargetTicks = 0

    private var lastVelocity: PendingVelocity? = null

    private val killauraModule = MinusBounce.moduleManager[KillAura::class.java]!!


    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (mc.thePlayer == null || mc.thePlayer.ticksExisted < 5) {
            if (!storagePackets.isEmpty()) {
                storagePackets.clear()
            }
        }

        val currentTarget = currentTarget

        if (currentTarget !== lastTarget) {
            clearPackets()
        }

        if (currentTarget == null) {
            clearPackets()
        } else {
            if (event.packet is S14PacketEntity) {
                val packet: S14PacketEntity = event.packet

                if (packet.getEntity(mc.getNetHandler().clientWorldController) === currentTarget) {
                    val x: Int = currentTarget.serverPosX + packet.x
                    val y: Int = currentTarget.serverPosY + packet.y
                    val z: Int = currentTarget.serverPosZ + packet.z

                    val posX = x.toDouble() / 32.0
                    val posY = y.toDouble() / 32.0
                    val posZ = z.toDouble() / 32.0

                    if (killauraModule.getDistanceCustomPosition(
                            posX,
                            posY,
                            posZ,
                            currentTarget.eyeHeight
                        ) >= minRange.get()
                    ) {
                        event.cancelEvent()
                        storagePackets.add(StoragePacket(packet))
                    }
                }
            } else if (event.packet is S18PacketEntityTeleport) {
                val packet: S18PacketEntityTeleport = event.packet

                if (packet.entityId == currentTarget.entityId) {
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
                        x = currentTarget.posX
                        y = currentTarget.posY
                        z = currentTarget.posZ
                    } else {
                        x = d0
                        y = d1
                        z = d2
                    }

                    if (killauraModule.getDistanceCustomPosition(
                            x,
                            y,
                            z,
                            currentTarget.eyeHeight
                        ) >= minRange.get()
                    ) {
                        event.cancelEvent()
                        storagePackets.add(StoragePacket(packet))
                    }
                }
            } else if (event.packet is S32PacketConfirmTransaction || event.packet is S00PacketKeepAlive) {
                if (!storagePackets.isEmpty() && delayPing.get()) {
                    event.cancelEvent()
                    storagePackets.add(StoragePacket(event.packet))
                }
            } else if (event.packet is S12PacketEntityVelocity) {
                val packet: S12PacketEntityVelocity = event.packet

                if (packet.entityID == mc.thePlayer.getEntityId()) {
                    if (!storagePackets.isEmpty() && delayPing.get() && delayVelocity.get()) {
                        event.cancelEvent()
                        lastVelocity = PendingVelocity(
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
    fun onPostMotion(event: PostMotionEvent) {
        updatePackets()
    }

    val currentTarget: EntityLivingBase?
        get() {
            if (killauraModule.state && killauraModule.target != null) {
                return killauraModule.target
            } else if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit === MovingObjectPosition.MovingObjectType.ENTITY && mc.objectMouseOver.entityHit is EntityLivingBase) {
                lastCursorTarget = mc.objectMouseOver.entityHit as EntityLivingBase

                return mc.objectMouseOver.entityHit as EntityLivingBase
            } else if (lastCursorTarget != null) {
                if (++cursorTargetTicks > 10) {
                    lastCursorTarget = null
                } else {
                    return lastCursorTarget
                }
            }

            return null
        }

    fun updatePackets() {
        if (!storagePackets.isEmpty()) {
            for (p in storagePackets) {
                if (p.timer.hasTimePassed() >= delay.get()) {
                    clearPackets()

                    if (lastVelocity != null) {
                        mc.thePlayer.motionX = lastVelocity!!.x
                        mc.thePlayer.motionY = lastVelocity!!.y
                        mc.thePlayer.motionZ = lastVelocity!!.z
                        lastVelocity = null
                    }

                    return
                }
            }
        }
    }

    fun clearPackets() {
        if (lastVelocity != null) {
            mc.thePlayer.motionX = lastVelocity!!.x
            mc.thePlayer.motionY = lastVelocity!!.y
            mc.thePlayer.motionZ = lastVelocity!!.z
            lastVelocity = null
        }

        if (!storagePackets.isEmpty()) {
            for (p in storagePackets) {
                handlePacket(p.packet())
            }
            storagePackets.clear()
        }
    }

    fun handlePacket(packet: Packet<*>?) {
        if (packet is S14PacketEntity) {
            handleEntityMovement(packet)
        } else if (packet is S18PacketEntityTeleport) {
            handleEntityTeleport(packet)
        } else if (packet is S32PacketConfirmTransaction) {
            handleConfirmTransaction(packet)
        } else if (packet is S00PacketKeepAlive) {
            mc.getNetHandler().handleKeepAlive(packet as S00PacketKeepAlive?)
        }
    }

    fun handleEntityMovement(packetIn: S14PacketEntity) {
        val entity = packetIn.getEntity(mc.getNetHandler().clientWorldController)

        if (entity != null) {
            entity.serverPosX += packetIn.getX()
            entity.serverPosY += packetIn.getY()
            entity.serverPosZ += packetIn.getZ()
            val d0 = entity.serverPosX.toDouble() / 32.0
            val d1 = entity.serverPosY.toDouble() / 32.0
            val d2 = entity.serverPosZ.toDouble() / 32.0
            val f = if (packetIn.func_149060_h()) (packetIn.getYaw() * 360) as Float / 256.0f else entity.rotationYaw
            val f1 =
                if (packetIn.func_149060_h()) (packetIn.getPitch() * 360) as Float / 256.0f else entity.rotationPitch
            entity.setPositionAndRotation2(d0, d1, d2, f, f1, 3, false)
            entity.onGround = packetIn.onGround
        }
    }

    fun handleEntityTeleport(packetIn: S18PacketEntityTeleport) {
        val entity: Entity = mc.getNetHandler().clientWorldController.getEntityByID(packetIn.entityId)

        if (entity != null) {
            entity.serverPosX = packetIn.x
            entity.serverPosY = packetIn.y
            entity.serverPosZ = packetIn.z
            val d0 = entity.serverPosX.toDouble() / 32.0
            val d1 = entity.serverPosY.toDouble() / 32.0
            val d2 = entity.serverPosZ.toDouble() / 32.0
            val f = (packetIn.yaw * 360).toFloat() / 256.0f
            val f1 = (packetIn.pitch * 360).toFloat() / 256.0f

            if (abs(entity.posX - d0) < 0.03125 && abs(entity.posY - d1) < 0.015625 && abs(
                    entity.posZ - d2
                ) < 0.03125
            ) {
                entity.setPositionAndRotation2(entity.posX, entity.posY, entity.posZ, f, f1, 3, true)
            } else {
                entity.setPositionAndRotation2(d0, d1, d2, f, f1, 3, true)
            }

            entity.onGround = packetIn.onGround
        }
    }

    fun handleConfirmTransaction(packetIn: S32PacketConfirmTransaction) {
        var container: Container? = null
        val entityplayer: EntityPlayer = mc.thePlayer

        if (packetIn.windowId == 0) {
            container = entityplayer.inventoryContainer
        } else if (packetIn.windowId == entityplayer.openContainer.windowId) {
            container = entityplayer.openContainer
        }

        if (container != null && !packetIn.func_148888_e()) {
            mc.getNetHandler()
                .addToSendQueue(C0FPacketConfirmTransaction(packetIn.windowId, packetIn.actionNumber, true))
        }
    }

    val isDelaying: Boolean
        get() = !storagePackets.isEmpty()
}*/