package net.minusmc.minusbounce.features.module.modules.combat

import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.Packet
import net.minecraft.network.ThreadQuickExitException
import net.minecraft.network.play.INetHandlerPlayClient
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.server.*
import net.minecraft.util.AxisAlignedBB
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.utils.EntityUtils
import net.minusmc.minusbounce.utils.PacketUtils
import net.minusmc.minusbounce.utils.extensions.*
import net.minusmc.minusbounce.utils.render.RenderUtils
import net.minusmc.minusbounce.utils.timer.MSTimer
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.IntegerValue
import net.minusmc.minusbounce.value.ListValue
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.*
import java.util.*

import java.lang.reflect.ParameterizedType

@ModuleInfo(name = "BackTrack", spacedName = "Back Track", description = "Let you attack in their previous position", category = ModuleCategory.COMBAT)
class BackTrack : Module() {
    private val modeValue = ListValue("TrackMode", arrayOf("PacketDelay", "Automatic", "Manual"), "Automatic")
    private val esp = BoolValue("ESP", true)
    private val delayValue = IntegerValue("Delay", 200, 0, 2000) { modeValue.get().equals("automatic", true) }
    private val onlyPlayer = BoolValue("OnlyƠlayer", true) { modeValue.get().equals("automatic", true) }
    private val packetSize = IntegerValue("LimitPacketSize", 100, 0, 1000) { modeValue.get().equals("packetdelay", true) }

    private var needFreeze = false
    private val packets = mutableListOf<Packet<INetHandlerPlayClient>>()
    private val storageEntities = mutableListOf<Entity>()

    private var timer = MSTimer()
    private var attacked: Entity? = null

    private val packetEvents = LinkedList<PacketEvent>()
    private val entities = hashMapOf<Int, BackTrackData>()

    override fun onDisable() {
        releasePackets()
        clear()
    }


    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (modeValue.get().equals("packetdelay", true))
            entities.forEach {_, entity -> entity.update()}
    }

    @EventTarget(priority = 1000)
    fun onPacket(event: PacketEvent) {
        mc.thePlayer ?: return
        val packet = event.packet
        
        if (modeValue.get().equals("automatic", true) || modeValue.get().equals("manual", true)) {

            if (!state)
                return

            if (packet.javaClass.name.contains("play.server.", true)) {
                if (packet is S14PacketEntity) {
                    val entity = packet.getEntity(mc.theWorld)

                    if (entity == null && entity !is EntityLivingBase || (onlyPlayer.get() && entity !is EntityPlayer)) 
                        return

                    entity.serverPosX += packet.func_149062_c().toInt()
                    entity.serverPosY += packet.func_149061_d().toInt()
                    entity.serverPosZ += packet.func_149064_e().toInt()

                    val x = entity.serverPosX.toDouble() / 32.0
                    val y = entity.serverPosY.toDouble() / 32.0
                    val z = entity.serverPosZ.toDouble() / 32.0
                    if (EntityUtils.isSelected(entity, true)) {
                        val afterBB = AxisAlignedBB(x - 0.4F, y - 0.1F, z - 0.4F, x + 0.4F, y + 1.9F, z + 0.4F)

                        val eyes = mc.thePlayer.getPositionEyes(1F)
                        val afterRange = getNearestPointBB(eyes, afterBB).distanceTo(eyes)
                        val beforeRange = mc.thePlayer.getDistanceToEntityBox(entity)


                        if (beforeRange <= 6) {
                            if (afterRange > beforeRange + 0.02 && afterRange < 6f) {
                                if (!needFreeze) {
                                    timer.reset()
                                    needFreeze = true
                                }

                                if (!storageEntities.contains(entity)) 
                                    storageEntities.add(entity)

                                event.cancelEvent()
                                return
                            }
                        } else if (modeValue.get().equals("automatic", true) && afterRange < beforeRange && needFreeze) {
                            releasePackets()
                        }
                    }

                    if (needFreeze) {
                        if (!storageEntities.contains(entity))
                            storageEntities.add(entity)

                        event.cancelEvent()
                        return
                    }

                    if (!needFreeze) {
                        MinusBounce.eventManager.callEvent(EntityMovementEvent(entity))
                        val yaw = if (packet.func_149060_h()) packet.func_149066_f().toFloat() * 360 / 256.0f else entity.rotationYaw
                        val pitch = if (packet.func_149060_h()) packet.func_149063_g().toFloat() * 360 / 256.0f else entity.rotationPitch
                        entity.setPositionAndRotation2(x, y, z, yaw, pitch, 3, false)
                        entity.onGround = packet.onGround
                    }

                    event.cancelEvent()
                } else if (needFreeze) {
                    if (packet is S19PacketEntityStatus && packet.opCode == 2.toByte())
                        return

                    packets.add(packet as Packet<INetHandlerPlayClient>)
                    event.cancelEvent()
                }
            }
        } else {
            if (packet is S03PacketTimeUpdate)
                return

            if (packet is S01PacketJoinGame || packet is S07PacketRespawn) {
                clear()
                return
            }

            if (packet.javaClass.name.contains("play.server.", true)) {
                packetEvents.add(event)
                event.cancelEvent()
            }

            if (packet is S0CPacketSpawnPlayer) {
                if (entities[packet.entityID] == null)
                    return
                val backTrackData = BackTrackData()

                backTrackData.x = packet.x
                backTrackData.y = packet.y
                backTrackData.z = packet.z
                backTrackData.prevX = packet.z / 32.0
                backTrackData.prevY = packet.y / 32.0
                backTrackData.prevZ = packet.z / 32.0

                entities[packet.entityID] = backTrackData
            }

            if (packet is S14PacketEntity) {
                val entity = packet.getEntity(mc.theWorld) ?: return
                val backTrackData = entities[entity.entityId] ?: run {
                    if (entity is EntityArmorStand)
                        return

                    val entityBackTrackData = BackTrackData()
                    entityBackTrackData.x = entity.serverPosX
                    entityBackTrackData.y = entity.serverPosY
                    entityBackTrackData.z = entity.serverPosZ

                    val borderSize = entity.collisionBorderSize
                    entityBackTrackData.width = entity.width / 2 + borderSize
                    entityBackTrackData.height = entity.height + borderSize

                    entityBackTrackData
                }

                entities[entity.entityId] = backTrackData
                backTrackData.updateMotionX(packet.func_149062_c())
                backTrackData.updateMotionY(packet.func_149061_d())
                backTrackData.updateMotionZ(packet.func_149064_e())
            }

            if (packet is S18PacketEntityTeleport) {

                val backTrackData = entities[packet.entityId] ?: run {
                    val entity = mc.theWorld.getEntityByID(packet.entityId)
                    val entityBackTrackData = BackTrackData()

                    if (entity != null) {
                        val borderSize = entity.collisionBorderSize
                        entityBackTrackData.width = entity.width / 2 + borderSize
                        entityBackTrackData.height = entity.height + borderSize
                    }

                    entityBackTrackData
                }

                backTrackData.x = packet.x
                backTrackData.y = packet.y
                backTrackData.z = packet.z

                entities[packet.entityId] = backTrackData
            }

            if (packet is C08PacketPlayerBlockPlacement && packet.placedBlockDirection == 255)
                return
        }
    }

    private fun clear(size: Int) {
        if (!modeValue.get().equals("packetdelay", true))
            return

        while (packetEvents.size > size) {
            val event = packetEvents.pollFirst() ?: continue
            PacketUtils.handlePacketNoEvent(event.packet)
        }
    }

    @EventTarget
    fun onPostMotion(event: PostMotionEvent) {
        mc.thePlayer ?: return

        if (!modeValue.get().equals("packetdelay", true)) {
            clear(packetSize.get())
            return
        }

        if (!needFreeze)
            return

        if (!modeValue.get().equals("manual", true) && timer.hasTimePassed(delayValue.get())) {
            releasePackets()
            return
        }

        if (storageEntities.isEmpty())
            return

        var release = false
        for (entity in storageEntities) {
            val x = entity.serverPosX.toDouble() / 32.0
            val y = entity.serverPosY.toDouble() / 32.0
            val z = entity.serverPosZ.toDouble() / 32.0

            val entityBB = AxisAlignedBB(x - 0.4F, y - 0.1F, z - 0.4F, x + 0.4F, y + 1.9F, z + 0.4F)
            var range = entityBB.getLookingTargetRange(mc.thePlayer)

            if (range == Double.MAX_VALUE) {
                val eyes = mc.thePlayer.getPositionEyes(1F)
                range = getNearestPointBB(eyes, entityBB).distanceTo(eyes) + 0.075
            }

            if (range <= 0.5) {
                release = true
                break
            }

            if (attacked != entity) 
                continue

            if (!modeValue.get().equals("manual", true) && timer.hasTimePassed(delayValue.get()) || range >= 6)
                break
        }

        if (!modeValue.get().equals("manual", true) && release)
            releasePackets()
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {

        if (modeValue.get().equals("packetdelay", true)) {
            clear()
            return
        }

        attacked = null
        storageEntities.clear()
        event.worldClient ?: packets.clear()
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (!esp.get())
            return

        GL11.glPushMatrix()
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glDisable(GL_DEPTH_TEST)
        glDepthMask(false)

        // drawing
        if (modeValue.get().equals("packetdelay", true)) {
            for (entity in entities.values) {
                val target = MinusBounce.combatManager.target ?: continue

                val px = entity.posX - mc.renderManager.renderPosX
                val py = entity.posY - mc.renderManager.renderPosY
                val pz = entity.posZ - mc.renderManager.renderPosZ

                val bb = AxisAlignedBB(px - entity.width, py, pz - entity.width, px + entity.width, py + entity.height, pz + entity.width)
                if (target.hurtTime > 0) 
                    RenderUtils.glColor(255, 32, 32, 35) 
                else 
                    RenderUtils.glColor(32, 255, 32, 35)

                RenderUtils.drawFilledBox(bb)
            }
        } else for (entity in storageEntities) {
            val x = entity.serverPosX / 32.0 - mc.renderManager.renderPosX
            val y = entity.serverPosY / 32.0 - mc.renderManager.renderPosY
            val z = entity.serverPosZ / 32.0 - mc.renderManager.renderPosZ

            if (entity is EntityPlayer) {
                if (entity.hurtTime > 0)
                    RenderUtils.glColor(255, 32, 32, 35)
                else 
                    RenderUtils.glColor(32, 255, 32, 35)

                RenderUtils.drawFilledBox(AxisAlignedBB(x - 0.4F, y, z - 0.4F, x + 0.4F, y + 1.9F, z + 0.4F))
            }
        }

        glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        glDepthMask(true)
        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)
        glEnable(GL_DEPTH_TEST)
        glPopMatrix()
    }

    private fun releasePackets() {
        if (modeValue.get().equals("packetdelay", true)) 
            return

        attacked = null

        if (packets.isEmpty())
            return

        packets.map {it}.forEach { 
            val event = PacketEvent(it)

            if (!PacketUtils.packetList.contains(it))
                MinusBounce.eventManager.callEvent(event)

            if (!event.isCancelled)
                it.processPacket(mc.netHandler)
        }
        packets.clear()

        storageEntities.map {it}.forEach {
            if (!it.isDead) {
                val x = it.serverPosX.toDouble() / 32.0
                val y = it.serverPosY.toDouble() / 32.0
                val z = it.serverPosZ.toDouble() / 32.0
                it.setPosition(x, y, z)
            }
        }
        storageEntities.clear()

        needFreeze = false
    }

    private fun clear() {
        if (modeValue.get().equals("packetdelay", true)) {
            clear(0)
            entities.clear()
        }
    }

    override val tag: String
        get() = modeValue.get()
}


private class BackTrackData {
    var height = 1.9f
    var width = 0.4f
    var x = 0
    var y = 0
    var z = 0
    var prevX = 0.0
    var prevY = 0.0
    var prevZ = 0.0
    private var increment = 0

    val posX: Double
        get() = x / 32.0

    val posY: Double
        get() = y / 32.0

    val posZ: Double
        get() = z / 32.0

    fun updateMotionX(xIncrement: Byte) {
        prevX = posX
        x += xIncrement
        increment = 3
    }

    fun updateMotionY(yIncrement: Byte) {
        prevY = posY
        y += yIncrement
        increment = 3
    }

    fun updateMotionZ(zIncrement: Byte) {
        prevZ = posZ
        z += zIncrement
        increment = 3
    }

    fun update() {
        if (increment > 0) {
            prevX += ((x / 32.0) - prevX) / increment
            prevY += ((y / 32.0) - prevY) / increment
            prevZ += ((z / 32.0) - prevZ) / increment
            --increment
        }
    }
}
