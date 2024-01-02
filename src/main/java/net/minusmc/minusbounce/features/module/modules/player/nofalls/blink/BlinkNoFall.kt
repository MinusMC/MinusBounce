package net.minusmc.minusbounce.features.module.modules.player.nofalls.blink

import net.minecraft.network.play.client.C03PacketPlayer
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.event.Render2DEvent
import net.minusmc.minusbounce.features.module.modules.combat.KillAura
import net.minusmc.minusbounce.features.module.modules.movement.SafeWalk
import net.minusmc.minusbounce.features.module.modules.player.Blink
import net.minusmc.minusbounce.features.module.modules.player.nofalls.NoFallMode
import net.minusmc.minusbounce.features.module.modules.world.Scaffold
import net.minusmc.minusbounce.utils.PlayerUtils
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.IntegerValue
import net.minecraft.client.gui.ScaledResolution
import java.awt.Color
import org.lwjgl.input.Mouse

class BlinkNoFall: NoFallMode("Blink") {

    private var start = false
    private var disable = false

    private val fallDistValue = IntegerValue("FallDistance", 9, 3, 15)
    private val limitValue = BoolValue("limitUse", true)
    private val noMouse = BoolValue("NoMouse", false)

    override fun onPacket(event: PacketEvent) {
        val killAura = MinusBounce.moduleManager[KillAura::class.java]!!
        val scaffold = MinusBounce.moduleManager[Scaffold::class.java]!!
        val safeWalk = MinusBounce.moduleManager[SafeWalk::class.java]!!
        val blink = MinusBounce.moduleManager[Blink::class.java]!!
        val state = (killAura.state || scaffold.state || safeWalk.state || (noMouse.get() && (Mouse.isButtonDown(0) || Mouse.isButtonDown(1))))
        if (limitValue.get() && state) {
            start = false
            disable = false
            return
        }
        if (PlayerUtils.isOnEdge() && mc.thePlayer.fallDistance < fallDistValue.get()) {
            start = true
        }
        if (start) {
            blink.state = true
            if (event.packet is C03PacketPlayer) {
                event.packet.onGround = true
            }
        } else {
            blink.state = false
        }
        if (mc.thePlayer.fallDistance > 0.5) {
            disable = true
        }
        if (mc.thePlayer.onGround && disable) {
            disable = false
            start = false
        }
    }

    override fun onRender2D() {
        if (start) {
            mc.fontRendererObj.drawStringWithShadow(
                "Blinking",
                ScaledResolution(mc).scaledWidth / 2F,
                ScaledResolution(mc).scaledHeight / 2F + 10F, Color.WHITE.rgb,
            )
        }
    }
}