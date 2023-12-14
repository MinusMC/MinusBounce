package net.minusmc.minusbounce.features.module.modules.combat.velocitys.grim

import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.value.IntegerValue
import net.minusmc.minusbounce.value.BoolValue
import net.minecraft.item.ItemFood
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S32PacketConfirmTransaction

class GrimExtendVelocity : VelocityMode("GrimExtend") {
    private var cancelPacket = IntegerValue("CancelPacket", 6, 0, 20)
    private var resetPersec = IntegerValue("ResetPerMin", 10, 0, 30)
    private var onlyGroundValue = BoolValue("OnlyGround", false)
    private var onEatingValue = BoolValue("OnEating", false)
    private var enableDelayValue = IntegerValue("EnablingDelay", 30, 0, 70)
    private var grimTCancel = 0
    private var enableDelay = 0
    private var updates = 0
    private var disabling = false

    private fun canBeEaten(): Boolean {
        val currentSlot = mc.thePlayer.inventory.currentItem + 36
        val stack = mc.thePlayer.inventoryContainer.getSlot(currentSlot).stack ?: return false
        return stack.item is ItemFood
    }

    override fun onEnable() {
        grimTCancel = 0
        enableDelay = 0
    }

    override fun onUpdate() {
        updates++

        if ((!mc.thePlayer.onGround && onlyGroundValue.get()) || (mc.thePlayer.isEating && onEatingValue.get())) disabling =
            true
        else if (disabling) {
            disabling = false
            enableDelay = enableDelayValue.get()
        }

        if (enableDelay > 0) enableDelay--

        if (resetPersec.get() > 0) {
            if (updates >= 20 * 60 / resetPersec.get()) {
                updates = 0
                if (grimTCancel > 0) grimTCancel--
            }
        }

        if (mc.thePlayer.isEating && onEatingValue.get() && canBeEaten()) grimTCancel = 0
    }

    override fun onPacket(event: PacketEvent) {
        if ((!mc.thePlayer.onGround && onlyGroundValue.get()) || (mc.thePlayer.isEating && onEatingValue.get() && canBeEaten()) ||
            enableDelay > 0) {
            grimTCancel = 0
        } else {
            val packet = event.packet
            if (packet is S12PacketEntityVelocity && packet.entityID == mc.thePlayer.entityId) {
                event.cancelEvent()
                grimTCancel = cancelPacket.get()
            }
            if (packet is S32PacketConfirmTransaction && grimTCancel > 0) {
                event.cancelEvent()
                grimTCancel--
            }
        }
    }
}