package net.minusmc.minusbounce.features.module.modules.killaura.blocking

import net.minusmc.minusbounce.utils.MinecraftInstance
import net.minusmc.minusbounce.features.module.modules.combat.KillAura
import net.minecraft.item.ItemSword
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.utils.*
import net.minusmc.minusbounce.features.module.modules.killaura.KillAuraBlocking
import net.minecraft.util.*
import net.minusmc.minusbounce.event.PacketEvent
import net.minecraft.network.play.client.*
import net.minusmc.minusbounce.features.module.modules.combat.KillAura.currentTarget
import net.minusmc.minusbounce.features.module.modules.combat.KillAura.blockingStatus

class Verus: KillAuraBlocking("Verus") {
    private var verusBlocking = false

    override fun onEnable() {
        verusBlocking = false
    }

    override fun onDisable() {
        if (verusBlocking && !blockingStatus && !mc.thePlayer.isBlocking) {
            verusBlocking = false
            PacketUtils.sendPacketNoEvent(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
        }
    }

    override fun onPacket(event: PacketEvent){
        val packet = event.packet
        if (verusBlocking && ((packet is C07PacketPlayerDigging && packet.status == C07PacketPlayerDigging.Action.RELEASE_USE_ITEM) || packet is C08PacketPlayerBlockPlacement))
            event.cancelEvent()

        if (packet is C09PacketHeldItemChange)
            verusBlocking = false
    }

    override fun onUpdate(){
        if (blockingStatus || mc.thePlayer.isBlocking)
            verusBlocking = true
        else if (verusBlocking) {
            verusBlocking = false
            PacketUtils.sendPacketNoEvent(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
        }
    }

    override fun onPreAttack(){
        KillAura.stopBlocking()
    }

    override fun onPostAttack(){
        KillAura.startBlocking()
    }
}