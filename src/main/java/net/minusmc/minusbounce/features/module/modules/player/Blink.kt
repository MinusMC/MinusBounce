/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.minusmc.minusbounce.features.module.modules.player

import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.utils.render.RenderUtils
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.network.Packet
import net.minecraft.network.play.client.*
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.event.Render3DEvent
import net.minusmc.minusbounce.event.UpdateEvent
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.features.module.modules.render.BreadCumbs
import net.minusmc.minusbounce.ui.client.hud.element.elements.targets.impl.LiquidBounce
import net.minusmc.minusbounce.utils.render.ColorUtils.rainbow
import net.minusmc.minusbounce.utils.timer.MSTimer
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.IntegerValue
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.*
import java.util.concurrent.LinkedBlockingQueue

@ModuleInfo(name = "Blink", description = "Suspends all movement packets.", category = ModuleCategory.PLAYER)
class Blink : Module() {
    
    
    val c0FValue = BoolValue("C0FCancel", false)
    val pulseValue = BoolValue("Pulse", false)
    private val pulseDelayValue = IntegerValue("PulseDelay", 1000, 500, 5000, "ms")
    val fake = BoolValue("FakePlayer", false)


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
        if (mc.thePlayer == null || disableLogger) return
        if (packet is C03PacketPlayer) // Cancel all movement stuff
            event.cancelEvent()
        if (packet is C04PacketPlayerPosition || packet is C06PacketPlayerPosLook ||
                packet is C08PacketPlayerBlockPlacement ||
                packet is C0APacketAnimation ||
                packet is C0BPacketEntityAction || packet is C02PacketUseEntity || c0FValue.get() && packet is C0FPacketConfirmTransaction) {
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

    @EventTarget
    fun onRender3D(event: Render3DEvent?) {
        val breadcrumbs = MinusBounce.moduleManager.getModule(BreadCumbs::class.java)
        val color = if (breadcrumbs?.colorRainbow?.get() == true) rainbow() else breadcrumbs?.colorRedValue?.let { Color(it.get(), breadcrumbs.colorGreenValue.get(), breadcrumbs.colorBlueValue.get()) }
        synchronized(positions) {
            GL11.glPushMatrix()
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            mc.entityRenderer.disableLightmap()
            GL11.glBegin(GL11.GL_LINE_STRIP)
            val renderPosX: Double = mc.getRenderManager().viewerPosX
            val renderPosY: Double = mc.getRenderManager().viewerPosY
            val renderPosZ: Double = mc.getRenderManager().viewerPosZ
            for (pos in positions) GL11.glVertex3d(pos[0] - renderPosX, pos[1] - renderPosY, pos[2] - renderPosZ)
            GL11.glColor4d(1.0, 1.0, 1.0, 1.0)
            GL11.glEnd()
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GL11.glDisable(GL11.GL_LINE_SMOOTH)
            GL11.glDisable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glPopMatrix()
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