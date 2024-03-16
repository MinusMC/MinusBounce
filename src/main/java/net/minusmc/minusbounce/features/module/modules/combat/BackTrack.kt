package net.minusmc.minusbounce.features.module.modules.combat

import net.minecraft.client.network.NetHandlerPlayClient
import net.minecraft.network.Packet
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.INetHandlerPlayClient
import net.minecraft.network.play.server.*
import net.minecraft.network.INetHandler
import net.minecraft.util.Vec3
import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.utils.PacketUtils

import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue

@ModuleInfo(name = "BackTrack", description = "backtrack.", category = ModuleCategory.COMBAT)
class BackTrack: Module() {

    var packets: Queue<Packet<*>> = ConcurrentLinkedQueue()
    private var realPosition: Vec3? = null
    private var ticksSinceHit = 0
    private var delayTicks = 0
    private var target: Entity? = null
    private var lastAttacked: Entity? = null

    override fun onDisable() {
        delayTicks = 0
        target = null
        lastAttacked = null
        realPosition = null

        if (mc.thePlayer != null && mc.thePlayer.ticksExisted > 20) {
            packets.forEach(PacketUtils::sendPacketNoEvent)
            packets.clear()
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (mc.thePlayer == null || mc.thePlayer.ticksExisted < 20) {
            packets.clear()
            return
        }

        if (delayTicks <= 0 && !packets.isEmpty()) {
            packets.forEach(PacketUtils::sendPacketNoEvent)
            packets.clear()
        }

        val packet = event.packet
        if (packet is C02PacketUseEntity) {
            if (packet.action != C02PacketUseEntity.Action.ATTACK)
                return

            val entity = packet.getEntityFromWorld(mc.theWorld)

            if (entity is EntityPlayer) {
                if (lastAttacked != target)
                    realPosition = target!!.positionVector

                lastAttacked = entity
            }
        }


        if (packet is C03PacketPlayer) {
            ticksSinceHit++
            if (delayTicks > 0)
                delayTicks--

            if (target == null || ticksSinceHit > 30)
                return

            if (ticksSinceHit < 30 && delayTicks <= 0)
                delayTicks = 35

            val pos = realPosition ?: return

            if (mc.thePlayer.hurtTime > 0 || mc.thePlayer.getDistance(pos.xCoord, pos.yCoord, pos.zCoord) < 3.05)
                delayTicks = 0
        }

        if (delayTicks > 0) {
            if (packet is S32PacketConfirmTransaction || packet is S00PacketKeepAlive || 
                packet is S12PacketEntityVelocity || packet is S08PacketPlayerPosLook || 
                packet is S14PacketEntity || packet is S18PacketEntityTeleport || 
                packet is S23PacketBlockChange) {

                if (packet is S14PacketEntity) {
                    if (packet.getEntity(mc.theWorld) != target)
                        return

                    val d0 = packet.func_149062_c().toDouble() / 32.0
                    val d1 = packet.func_149061_d().toDouble() / 32.0
                    val d2 = packet.func_149064_e().toDouble() / 32.0
                    realPosition = realPosition!!.add(Vec3(d0, d1, d2))
                }

                if (packet is S18PacketEntityTeleport) {
                    if (target!!.entityId == packet.entityId)
                        realPosition = Vec3(packet.x / 32.0, packet.y / 32.0, packet.z / 32.0)
                }

                packets.add(packet)
                event.cancelEvent()
            }
        }
    }

}
