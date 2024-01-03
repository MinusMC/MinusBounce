/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.combat

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.features.module.ModuleManager
import net.minusmc.minusbounce.value.IntegerValue
import net.minusmc.minusbounce.value.BoolValue
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.client.C0APacketAnimation

@ModuleInfo(name = "ComboOneHit", spacedName = "Combo One Hit", description = "Automatically deals hits within one click. Only works if no attack delay is present.", category = ModuleCategory.COMBAT)
class ComboOneHit : Module() {

    private val amountValue = IntegerValue("Packets", 200, 0, 500, "x")
    private val swingItemValue = BoolValue("SwingPacket", false)
    private val onlyAuraValue = BoolValue("OnlyAura", false)

    @EventTarget
    fun onAttack(event: AttackEvent) {
        event.targetEntity ?: return
        if (onlyAuraValue.get() && !MinusBounce.moduleManager[KillAura::class.java]!!.state && !MinusBounce.moduleManager[TeleportAura::class.java]!!.state) return

        repeat (amountValue.get()) {
            if(swingItemValue.get()) mc.netHandler.addToSendQueue(C0APacketAnimation())
            mc.netHandler.addToSendQueue(C02PacketUseEntity(event.targetEntity, C02PacketUseEntity.Action.ATTACK))
        }
    }

}