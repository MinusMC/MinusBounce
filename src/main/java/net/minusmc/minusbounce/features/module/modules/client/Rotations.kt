/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.client

import net.minecraft.network.play.client.C03PacketPlayer
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.event.Render3DEvent
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.features.module.modules.combat.BowAimbot
import net.minusmc.minusbounce.features.module.modules.combat.KillAura
import net.minusmc.minusbounce.features.module.modules.exploit.Disabler
import net.minusmc.minusbounce.features.module.modules.exploit.disablers.other.WatchdogDisabler
import net.minusmc.minusbounce.features.module.modules.movement.Fly
import net.minusmc.minusbounce.features.module.modules.movement.Sprint
import net.minusmc.minusbounce.features.module.modules.world.Scaffold
import net.minusmc.minusbounce.utils.RotationUtils
import net.minusmc.minusbounce.value.ListValue

@ModuleInfo(name = "Rotations", description = "Allows you to see server-sided head and body rotations.", category = ModuleCategory.CLIENT)
class Rotations : Module() {
    val rotationSensitivity = ListValue("Sensitivity", arrayOf("New", "Old", "None"), "New")
    val modeValue = ListValue("Mode", arrayOf("Head", "Body"), "Body")

    private var playerYaw: Float? = null

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (modeValue.get().equals("head", true) && RotationUtils.serverRotation != null)
            mc.thePlayer.rotationYawHead = RotationUtils.serverRotation!!.yaw
    }
    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (modeValue.get().equals("head", true) || !shouldRotate() || mc.thePlayer == null) {
            playerYaw = null
            return
        }

        val packet = event.packet
        if (packet is C03PacketPlayer && packet.rotating) {
            playerYaw = packet.yaw
            mc.thePlayer.renderYawOffset = packet.getYaw()
            mc.thePlayer.rotationYawHead = packet.getYaw()
        } else {
            if (playerYaw != null)
                mc.thePlayer.renderYawOffset = this.playerYaw!!
            mc.thePlayer.rotationYawHead = mc.thePlayer.renderYawOffset
        }
    }

    private fun getState(module: Class<out Module>) = MinusBounce.moduleManager[module]!!.state

    private fun shouldRotate(): Boolean {
        val killAura = MinusBounce.moduleManager.getModule(KillAura::class.java) as KillAura
        val disabler = MinusBounce.moduleManager.getModule(Disabler::class.java) as Disabler
        val sprint = MinusBounce.moduleManager.getModule(Sprint::class.java) as Sprint
        val watchdogDisabler = disabler.modes.find { it.modeName.equals("Watchdog", true) } as WatchdogDisabler
        return getState(Scaffold::class.java) || 
                (getState(Sprint::class.java) && sprint.allDirectionsValue.get() && sprint.moveDirPatchValue.get()) ||
                (getState(KillAura::class.java) && killAura.target != null) ||
                (getState(Disabler::class.java) && watchdogDisabler.canRenderInto3D) ||
                getState(BowAimbot::class.java) || getState(Fly::class.java)
    }
}
