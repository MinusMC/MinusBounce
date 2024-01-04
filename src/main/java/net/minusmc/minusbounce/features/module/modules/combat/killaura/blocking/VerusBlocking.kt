package net.minusmc.minusbounce.features.module.modules.combat.killaura.blocking

import net.minusmc.minusbounce.utils.PacketUtils
import net.minusmc.minusbounce.features.module.modules.combat.killaura.KillAuraBlocking
import net.minecraft.util.*
import net.minusmc.minusbounce.event.PacketEvent
import net.minecraft.network.play.client.*

class VerusBlocking: KillAuraBlocking("Verus") {
    private var verusBlocking = false

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

    override fun onPreUpdate(){
        if (blockingStatus || mc.thePlayer.isBlocking)
            verusBlocking = true
        else if (verusBlocking) {
            verusBlocking = false
            PacketUtils.sendPacketNoEvent(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
        }
    }

    override fun onPreAttack() {
        blockingStatus = false
    }

    override fun onPostAttack(){
        PacketUtils.sendPacketNoEvent(C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()))
        blockingStatus = true
    }
}