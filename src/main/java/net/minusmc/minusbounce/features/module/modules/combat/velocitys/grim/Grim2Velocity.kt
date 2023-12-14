package net.minusmc.minusbounce.features.module.modules.combat.velocitys.grim

import net.minecraft.network.play.client.C0FPacketConfirmTransaction
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.features.module.modules.combat.KillAura
import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minusmc.minusbounce.value.ListValue

class Grim2Velocity : VelocityMode("Grim2") {
    private val onMode = ListValue("Mode", arrayOf("Combat", "Damage", "All"), "Combat")

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is C0FPacketConfirmTransaction) {
            when (onMode.get().lowercase()) {
                "damage" -> if (mc.thePlayer.hurtTime > 0) event.cancelEvent()
                "combat" -> if (MinusBounce.moduleManager[KillAura::class.java]!!.target != null) event.cancelEvent()
                "both" -> if (MinusBounce.moduleManager[KillAura::class.java]!!.target != null && mc.thePlayer.hurtTime > 0) event.cancelEvent()
            }
        }
        if (packet is S12PacketEntityVelocity) event.cancelEvent()
    }
}