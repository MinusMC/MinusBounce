package net.minusmc.minusbounce.features.module.modules.player.antivoids.other

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.features.module.modules.world.Scaffold
import net.minusmc.minusbounce.features.module.modules.player.antivoids.AntiVoidMode
import net.minusmc.minusbounce.utils.misc.FallingPlayer
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.FloatValue
import net.minecraft.network.play.client.C03PacketPlayer


class BlinkAntiVoid: AntiVoidMode("Blink") {
	private val resetMotionValue = BoolValue("ResetMotion", false)
    private val startFallDistValue = FloatValue("StartFallDistance", 2F, 0F, 5F)
    private val autoScaffoldValue = BoolValue("AutoScaffold", true)

    private val packetCache = ArrayList<C03PacketPlayer>()
    private var blink = false
    private var canBlink = false

    private var posX = 0.0
    private var posY = 0.0
    private var posZ = 0.0
    private var motionX = 0.0
    private var motionY = 0.0
    private var motionZ = 0.0

    override fun onEnable() {
    	blink = false
        canBlink = false
    }

    override fun onUpdate() {
        if (!blink) {
            val collide = FallingPlayer(mc.thePlayer).findCollision(60)
            if (canBlink && (collide == null || (mc.thePlayer.posY - collide.pos!!.y) > startFallDistValue.get())) {
                posX = mc.thePlayer.posX
                posY = mc.thePlayer.posY
                posZ = mc.thePlayer.posZ
                motionX = mc.thePlayer.motionX
                motionY = mc.thePlayer.motionY
                motionZ = mc.thePlayer.motionZ

                packetCache.clear()
                blink = true
            }

            if (mc.thePlayer.onGround) {
                canBlink = true
            }
        } else {
            if (mc.thePlayer.fallDistance > antivoid.maxFallDistValue.get()) {
                mc.thePlayer.setPositionAndUpdate(posX, posY, posZ)
                if (resetMotionValue.get()) {
                    mc.thePlayer.motionX = 0.0
                    mc.thePlayer.motionY = 0.0
                    mc.thePlayer.motionZ = 0.0
                } else {
                    mc.thePlayer.motionX = motionX
                    mc.thePlayer.motionY = motionY
                    mc.thePlayer.motionZ = motionZ
                }

                mc.thePlayer.jumpMovementFactor = 0.00f

                if (autoScaffoldValue.get()) {
                    MinusBounce.moduleManager[Scaffold::class.java]!!.state = true
                }

                packetCache.clear()
                blink = false
                canBlink = false
            } else if (mc.thePlayer.onGround) {
                blink = false

                for (packet in packetCache) {
                    mc.netHandler.addToSendQueue(packet)
                }
            }
        }
    }

    override fun onPacket(event: PacketEvent) {
    	val packet = event.packet
    	if (blink && (packet is C03PacketPlayer)) {
            packetCache.add(packet)
            event.cancelEvent()
        }
    }
}