package net.minusmc.minusbounce.features.module.modules.movement.flys.normal

import net.minecraft.network.play.client.C03PacketPlayer.C05PacketPlayerLook
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.util.BlockPos
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.MoveEvent
import net.minusmc.minusbounce.event.ReceivedPacketEvent
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyMode
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyType
import net.minusmc.minusbounce.ui.client.hud.element.elements.Notification
import net.minusmc.minusbounce.utils.player.MovementUtils
import net.minusmc.minusbounce.utils.PlayerUtils
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.value.ListValue

class PearlFly: FlyMode("Pearl", FlyType.NORMAL) {
    private val vanillaSpeedValue = FloatValue("Speed", 2f, 0f, 5f)
    private val pearlActivateCheck = ListValue("PearlActiveCheck", arrayOf("Teleport", "Damage"), "Teleport")

    private var pearlState: Int = 0

    override fun onEnable() {
		super.onEnable()
        pearlState = 0
    }

    override fun onUpdate() {
        mc.thePlayer.capabilities.isFlying = false
        mc.thePlayer.motionX = 0.0
        mc.thePlayer.motionY = 0.0
        mc.thePlayer.motionZ = 0.0

        val enderPearlSlot = PlayerUtils.getPearlSlot()
        if (pearlState == 0) {
            if (enderPearlSlot == -1) {
                MinusBounce.hud.addNotification(Notification("Fly", "You don't have any ender pearl!", Notification.Type.ERROR))
                pearlState = -1
                fly.state = false
                return
            }

            if (mc.thePlayer.inventory.currentItem != enderPearlSlot)
                mc.netHandler.addToSendQueue(C09PacketHeldItemChange(enderPearlSlot))

            mc.netHandler.addToSendQueue(C05PacketPlayerLook(mc.thePlayer.rotationYaw, 90f, mc.thePlayer.onGround))
            mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(BlockPos(-1.0, -1.0, -1.0), 255, mc.thePlayer.inventoryContainer.getSlot(enderPearlSlot + 36).stack, 0f, 0f, 0f))
            
            if (enderPearlSlot != mc.thePlayer.inventory.currentItem)
                mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))

            pearlState = 1    
        }

        if (pearlActivateCheck.get().equals("damage", true) && pearlState == 1 && mc.thePlayer.hurtTime > 0) 
            pearlState = 2

        if (pearlState == 2) {
            if (mc.gameSettings.keyBindJump.isKeyDown)
                mc.thePlayer.motionY += vanillaSpeedValue.get().toDouble()

            if (mc.gameSettings.keyBindSneak.isKeyDown)
                mc.thePlayer.motionY -= vanillaSpeedValue.get().toDouble()

            MovementUtils.strafe(vanillaSpeedValue.get())
        }
    }

    override fun onReceivedPacket(event: ReceivedPacketEvent) {
        val packet = event.packet
        if (packet is S08PacketPlayerPosLook && pearlActivateCheck.get().equals("teleport", true) && pearlState == 1)
            pearlState = 2
    }

    override fun onMove(event: MoveEvent) {
        if (pearlState != 2 && pearlState != -1)
            event.isCancelled = true
    }
}