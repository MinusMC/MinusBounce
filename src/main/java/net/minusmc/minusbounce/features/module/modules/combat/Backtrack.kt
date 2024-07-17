package net.minusmc.minusbounce.features.module.modules.combat

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.Packet
import net.minecraft.network.play.server.S14PacketEntity
import net.minecraft.network.play.server.S18PacketEntityTeleport
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
import net.minusmc.minusbounce.value.*
import org.lwjgl.opengl.GL11


@ModuleInfo("BackTrack", "Back Track", "Let you attack in their previous position", ModuleCategory.COMBAT)
class BackTrack : Module() {
    private val delay = IntegerValue("Delay", 400, 0, 1000)
    private val hitRange = FloatValue("Range", 3F, 0F, 10F)
    private val esp = BoolValue("ESP", true)

    private val packets = mutableListOf<Packet<*>>()
    private val timer = MSTimer()

    override fun onEnable() {
        packets.clear()
    }
    
    @EventTarget(priority = 5)
    fun onPacket(event: PacketEvent) {
        mc.thePlayer ?: return
        mc.theWorld ?: return
        mc.netHandler ?: return

        if (MinusBounce.moduleManager[Scaffold::class.java]!!.state) {
            packets.clear()
            return
        }

        val packet = event.packet

        if (packet::class.java !in Constants.serverPacketClasses)
            return

        when (packet) {
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

        if (target == null) {
            flushPackets()
            return
        }

        addPacket(event)
    }

    private val target: EntityLivingBase?
        get() = MinusBounce.moduleManager[KillAura::class.java]?.target

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

        if (targetDistance >= realDistance || realDistance > hitRange.get())
            flushPackets()
        else if (timer.hasTimePassed(delay.get())) {
            timer.reset()
            flushPackets()
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

        if (targetDistance >= realDistance || realDistance > hitRange.get() || timer.hasTimePassed(delay.get()))
            render = false

        if (target != mc.thePlayer && !target.isInvisible && 
            target.width != 0f && target.height != 0f && render) {

            val color = ColorUtils.getColor(210.0F, 0.7F, 0.75F)
            val x = realX / 32.0 - mc.renderManager.renderPosX
            val y = realY / 32.0 - mc.renderManager.renderPosY
            val z = realZ / 32.0 - mc.renderManager.renderPosZ

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

    private fun addPacket(event: PacketEvent) {
        synchronized(packets) {
            val packet = event.packet

            if (packet::class.java !in Constants.clientOtherPacketClasses) {
                packets.add(packet)
                event.isCancelled = true
                event.stopRunEvent = true
            }
        }
    }
}