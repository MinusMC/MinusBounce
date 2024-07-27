package net.minusmc.minusbounce.features.module.modules.movement.flys.other

import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyType
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyMode
import net.minusmc.minusbounce.utils.timer.MSTimer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition

class MinesuchtFly: FlyMode("Minesucht", FlyType.OTHER) {
    private val timer = MSTimer()
    
    override fun onUpdate() {
        val posX = mc.thePlayer.posX
        val posY = mc.thePlayer.posY
        val posZ = mc.thePlayer.posZ

        if (!mc.gameSettings.keyBindForward.isKeyDown)
            return

        if (timer.hasTimePassed(99)) {
            val vec3 = mc.thePlayer.getPositionEyes(0f)
            val vec31 = mc.thePlayer.getLook(0f)
            val vec32 = vec3.addVector(vec31.xCoord * 7, vec31.yCoord * 7, vec31.zCoord * 7)

            if(mc.thePlayer.fallDistance > 0.8) {
                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(posX, posY + 50, posZ, false))
                mc.thePlayer.fall(100f, 100f)
                mc.thePlayer.fallDistance = 0f
                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(posX, posY + 20, posZ, true))
            }

            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(vec32.xCoord, mc.thePlayer.posY + 50, vec32.zCoord, true))
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(posX, posY, posZ, false))
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(vec32.xCoord, posY, vec32.zCoord, true))
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(posX, posY, posZ, false))
            timer.reset()
        } else {
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false))
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(posX, posY, posZ, true))
        }
    }

}