package net.minusmc.minusbounce.features.module.modules.combat.killaura.blocking

import net.minusmc.minusbounce.utils.MinecraftInstance
import net.minusmc.minusbounce.features.module.modules.combat.KillAura
import net.minecraft.item.ItemSword
import net.minusmc.minusbounce.utils.*
import net.minecraft.util.*
import net.minusmc.minusbounce.event.PacketEvent
import net.minecraft.network.play.client.*
import net.minusmc.minusbounce.features.module.modules.combat.killaura.KillAuraBlocking

class WatchdogBlocking: KillAuraBlocking("Watchdog") {

	private var watchdogc02 = 0
    private var watchdogdelay = 0
    private var watchdogcancelTicks = 0
    private var watchdogunblockdelay = 0
    private var watchdogkaing = false
    private var watchdogblinking = false
    private var watchdogblock = false
    private var watchdogblocked = false
    private var watchdogcancelc02 = false

    override fun onPreMotion() {
    	if (mc.thePlayer.heldItem.item is ItemSword && killAura.currentTarget != null) {
            watchdogkaing = true
            watchdogcancelc02 = false
            watchdogcancelTicks = 0
            watchdogunblockdelay = 0
            if (!watchdogblinking) {
                BlinkUtils.setBlinkState(all = true)
                watchdogblinking = true
                watchdogblocked = false
            }
            if (watchdogblinking && !watchdogblock) {
                watchdogdelay++
                if (watchdogdelay >= 2) {
                    mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1))
                    mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                    watchdogblocked = false
                    watchdogblock = true
                    watchdogdelay = 0
                }
            }
            if (watchdogblinking && watchdogblock) {
                if (watchdogc02 > 1) {
                    BlinkUtils.setBlinkState(off = true, release = true)
                    mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement())
                    watchdogblinking = false
                    watchdogblock = false
                    watchdogblocked = true
                    watchdogc02 = 0
                }
            }
        }
        if (watchdogkaing && killAura.currentTarget == null) {
            watchdogkaing = false
            watchdogblocked = false
            watchdogc02 = 0
            watchdogdelay = 0
            BlinkUtils.setBlinkState(off = true, release = true)
            watchdogcancelc02 = true
            watchdogcancelTicks = 0
            if (mc.thePlayer.heldItem.item is ItemSword) {
                mc.netHandler.addToSendQueue(C07PacketPlayerDigging())
            }
        }
        if (watchdogcancelc02) {
            watchdogcancelTicks++
            if (watchdogcancelTicks >= 3) {
                watchdogcancelc02 = false
                watchdogcancelTicks = 0
            }
        }
    }

    override fun onDisable() {
        watchdogkaing = false
        watchdogblocked = false
        watchdogc02 = 0
        watchdogdelay = 0
    }

    override fun onPacket(event: PacketEvent){
        val packet = event.packet
        if (mc.thePlayer.heldItem?.item is ItemSword && killAura.currentTarget != null && watchdogkaing) {
            if (packet is C08PacketPlayerBlockPlacement || packet is C07PacketPlayerDigging) {
                event.cancelEvent()
            }
        }
        if (mc.thePlayer.heldItem?.item is ItemSword && killAura.currentTarget != null && watchdogblocked || watchdogcancelc02) {
            if (packet is C02PacketUseEntity) {
                event.cancelEvent()
                watchdogblocked = false
            }
        }
        if (packet is C02PacketUseEntity && watchdogblinking) {
            watchdogc02++
        }
    }

}