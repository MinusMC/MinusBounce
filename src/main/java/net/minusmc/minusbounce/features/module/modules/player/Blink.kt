package net.minusmc.minusbounce.features.module.modules.player

import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.utils.render.RenderUtils
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.network.play.INetHandlerPlayClient
import net.minecraft.network.Packet
import net.minecraft.network.play.client.*
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.event.Render3DEvent
import net.minusmc.minusbounce.event.UpdateEvent
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.utils.render.ColorUtils.rainbow
import net.minusmc.minusbounce.utils.timer.MSTimer
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.IntegerValue
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.*
import java.util.concurrent.LinkedBlockingQueue

@ModuleInfo(name = "Blink", description = "Suspends all player packets.", category = ModuleCategory.PLAYER)
class Blink : Module() {
    
    private val C0F = BoolValue("C0F", false)
    private val C00 = BoolValue("C00", true)
    private val S12 = BoolValue("S12", true)
    private val disableSPacket = BoolValue("PacketStartwith - S", false) {!S12.get()}
    val pulseValue = BoolValue("Pulse", false)
    private val pulseDelayValue = IntegerValue("PulseDelay", 1000, 500, 5000, "ms") {pulseValue.get()}
    private val Ground = BoolValue("BlinkOnGround", false) {pulseValue.get()}
    private val fake = BoolValue("FakePlayer", false)

    private val packets = LinkedBlockingQueue<Packet<*>>()
    private var fakePlayer: EntityOtherPlayerMP? = null
    private var disableLogger = false
    private val positions = LinkedList<DoubleArray>()
    private val pulseTimer = MSTimer()
    
    override fun onEnable() {
        if (mc.thePlayer == null) return
        if (!pulseValue.get()) {
            if (fake.get()) {
                fakePlayer = EntityOtherPlayerMP(mc.theWorld, mc.thePlayer.getGameProfile())
                fakePlayer!!.clonePlayer(mc.thePlayer, true)
                fakePlayer!!.copyLocationAndAnglesFrom(mc.thePlayer)
                fakePlayer!!.rotationYawHead = mc.thePlayer.rotationYawHead
                mc.theWorld.addEntityToWorld(-1337, fakePlayer)
            }
        }
        synchronized(positions) {
            positions.add(doubleArrayOf(mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY + mc.thePlayer.getEyeHeight() / 2, mc.thePlayer.posZ))
            positions.add(doubleArrayOf(mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY, mc.thePlayer.posZ))
        }
        pulseTimer.reset()
    }

    override fun onDisable() {
        if (mc.thePlayer == null) return
        blink()
        if (fake.get()) {
            if (fakePlayer != null) {
                mc.theWorld.removeEntityFromWorld(fakePlayer!!.entityId)
                fakePlayer = null
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (mc.thePlayer == null || disableLogger || !(Ground.get() || !mc.thePlayer.onGround)) return
        if (packet is C03PacketPlayer) // Cancel all player stuff
            event.cancelEvent()
        if (disableSPacket.get() && packet.javaClass.simpleName.startsWith("S", ignoreCase = true)) { // Lol this bypass intave
            if (mc.thePlayer.ticksExisted < 20) return
            event.cancelEvent()
            packets.add(packet as Packet<INetHandlerPlayClient>)
        }
        if (S12.get() && packet is S12PacketEntityVelocity) // Lol this bypass outtave 
            event.cancelEvent()
        if (packet is C04PacketPlayerPosition || packet is C06PacketPlayerPosLook ||
                packet is C08PacketPlayerBlockPlacement ||
                packet is C0APacketAnimation ||
                packet is C0BPacketEntityAction || packet is C02PacketUseEntity || C0F.get() && packet is C0FPacketConfirmTransaction || C00.get() && packet is C00PacketKeepAlive) {
            event.cancelEvent()
            packets.add(packet)
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        synchronized(positions) { positions.add(doubleArrayOf(mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY, mc.thePlayer.posZ)) }
        if (pulseValue.get() && pulseTimer.hasTimePassed(pulseDelayValue.get().toLong())) {
            blink()
            pulseTimer.reset()
        }
    }

    override val tag: String
        get() = packets.size.toString()

    private fun blink() {
        try {
            disableLogger = true
            while (!packets.isEmpty()) {
                mc.getNetHandler().getNetworkManager().sendPacket(packets.take())
            }
            disableLogger = false
        } catch (e: Exception) {
            e.printStackTrace()
            disableLogger = false
        }
        synchronized(positions) { positions.clear() }
    }
}
