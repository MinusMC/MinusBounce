package net.minusmc.minusbounce.features.module.modules.killaura.blocking

import net.minusmc.minusbounce.features.module.modules.killaura.KillAuraBlocking

class NewNCP: KillAuraBlocking("NewNCP") {
    override fun onPreAttack(){
        mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1))
        mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
        blockingStatus = false
    }

    override fun onPostAttack(){
        mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, null, 0.0f, 0.0f, 0.0f))
        blockingStatus = true
    }
}