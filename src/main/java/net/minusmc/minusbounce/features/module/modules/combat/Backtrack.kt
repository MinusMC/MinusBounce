package net.minusmc.minusbounce.features.module.modules.combat

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.Packet
import net.minecraft.network.play.server.*
import net.minecraft.util.AxisAlignedBB
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.features.module.modules.world.Scaffold
import net.minusmc.minusbounce.utils.Constants
import net.minusmc.minusbounce.utils.render.ColorUtils
import net.minusmc.minusbounce.utils.render.RenderUtils
import net.minusmc.minusbounce.utils.PacketUtils
import net.minusmc.minusbounce.utils.timer.MSTimer
import net.minusmc.minusbounce.utils.misc.RandomUtils
import net.minusmc.minusbounce.utils.EntityUtils
import net.minusmc.minusbounce.value.*
import org.lwjgl.opengl.GL11


@ModuleInfo("BackTrack", "Back Track", "Let you attack in their previous position", ModuleCategory.COMBAT)
class BackTrack : Module() {
    private val delayValue = IntRangeValue("Delay", 100, 200, 0, 1000)
    private val hitRange = FloatValue("Range", 3F, 0F, 10F)
    private val esp = BoolValue("ESP", true)

    private val packets = mutableListOf<Packet<*>>()
    private val timer = MSTimer()
    private var target: EntityLivingBase? = null
    private var canFlushPacket = false

    private var delay = 0L

    override fun onEnable() {
        packets.clear()
        target = null
        delay = 0L
        canFlushPacket = false
    }
    
    @EventTarget(priority = 5)
    fun onPacket(event: ReceivedPacketEvent) {
        mc.thePlayer ?: return
        mc.theWorld ?: return
        mc.netHandler ?: return

        if (MinusBounce.moduleManager[Scaffold::class.java]!!.state) {
            packets.clear()
            return
        }

        val packet = event.packet
        val target = this.target

        when (packet) {
            is S06PacketUpdateHealth -> {
                if (packet.health <= 0)
                    canFlushPacket = true
            }

            is S08PacketPlayerPosLook, is S40PacketDisconnect, is S02PacketChat ->
                canFlushPacket = true

            is S13PacketDestroyEntities -> {
                if (target != null && target.entityId in packet.entityIDs)
                    canFlushPacket = true
            }

            is S14PacketEntity -> {
                val entity = mc.theWorld.getEntityByID(packet.entityId)

                if (entity is EntityLivingBase) {
                    entity.realPosX += packet.func_149062_c()
                    entity.realPosY += packet.func_149061_d()
                    entity.realPosZ += packet.func_149064_e()
                }    
            }

            is S18PacketEntityTeleport -> {
                val entity = mc.theWorld.getEntityByID(packet.entityId)

                if (entity is EntityLivingBase) {
                    entity.realPosX = packet.x.toDouble()
                    entity.realPosY = packet.y.toDouble()
                    entity.realPosZ = packet.z.toDouble()
                }
            }
        }

        if (target == null || canFlushPacket) {
            flushPackets()
            timer.reset()
            return
        }

        addPacket(event)
    }

    @EventTarget
    fun onAttack(event: AttackEvent) {
        val entity = event.targetEntity as? EntityLivingBase ?: run {
            target = null
            return
        }

        if (EntityUtils.isSelected(entity, true)) 
            target = entity
        else target = null
    }

    @EventTarget
    fun onGameLoop(event: GameLoopEvent) {
        mc.thePlayer ?: return
        mc.theWorld ?: return
        val target = this.target ?: return

        if (target.realPosX == 0.0 || target.realPosY == 0.0 || target.realPosZ == 0.0)
            return

        if (target.width == 0f || target.height == 0f)
            return

        val realX = target.realPosX / 32
        val realY = target.realPosY / 32
        val realZ = target.realPosZ / 32

        val realDistance = mc.thePlayer.getDistance(realX, realY, realZ)
        val targetDistance = mc.thePlayer.getDistance(target.posX, target.posY, target.posZ)

        if (canFlushPacket) {
            flushPackets()
            canFlushPacket = false
            return
        }

        if (targetDistance >= realDistance || realDistance > hitRange.get())
            flushPackets()
        else if (timer.hasTimePassed(delay)) {
            timer.reset()
            flushPackets()
            delay = RandomUtils.randomDelay(delayValue.minValue, delayValue.maxValue)
        }
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (!esp.get())
            return

        mc.thePlayer ?: return
        mc.theWorld ?: return
        val target = this.target ?: return

        if (target.realPosX == 0.0 || target.realPosY == 0.0 || target.realPosZ == 0.0)
            return

        if (target.width == 0f || target.height == 0f)
            return

        var render = true
        val realX = target.realPosX / 32
        val realY = target.realPosY / 32
        val realZ = target.realPosZ / 32

        val realDistance = mc.thePlayer.getDistance(realX, realY, realZ)
        val targetDistance = mc.thePlayer.getDistance(target.posX, target.posY, target.posZ)

        if (targetDistance >= realDistance || realDistance > hitRange.get() || timer.hasTimePassed(delay) || canFlushPacket)
            render = false

        if (target != mc.thePlayer && !target.isInvisible && render) {
            val color = ColorUtils.getColor(210.0F, 0.7F, 0.75F)
            val x = realX - mc.renderManager.renderPosX
            val y = realY - mc.renderManager.renderPosY
            val z = realZ - mc.renderManager.renderPosZ

            GlStateManager.pushMatrix()
            RenderUtils.start3D()
            RenderUtils.color(color)
            RenderUtils.renderHitbox(AxisAlignedBB(x - target.width / 2, y, z - target.width / 2, x + target.width / 2, y + target.height, z + target.width / 2), GL11.GL_QUADS)
            RenderUtils.color(color)
            RenderUtils.renderHitbox(AxisAlignedBB(x - target.width / 2, y, z - target.width / 2, x + target.width / 2, y + target.height, z + target.width / 2), GL11.GL_LINE_LOOP)
            RenderUtils.stop3D()
            GlStateManager.popMatrix()
        }
    }

    private fun flushPackets() {
        if (packets.isEmpty())
            return

        synchronized(packets) {
            while (packets.size > 0) {
                val packet = packets.removeFirst()
                PacketUtils.processPacket(packet)
            }
        }
    }

    private fun addPacket(event: ReceivedPacketEvent) {
        synchronized(packets) {
            val packet = event.packet

            if (packet::class.java !in Constants.serverOtherPacketClasses) {
                packets.add(packet)
                event.isCancelled = true
                event.stopRunEvent = true
            }
        }
    }
}