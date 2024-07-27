/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.movement

import net.minecraft.item.*
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.server.S09PacketHeldItemChange
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.features.module.modules.combat.KillAura
import net.minusmc.minusbounce.features.module.modules.movement.noslows.NoSlowMode
import net.minusmc.minusbounce.utils.timer.MSTimer
import net.minusmc.minusbounce.utils.ClassUtils
import net.minusmc.minusbounce.utils.player.MovementUtils
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.value.ListValue

@ModuleInfo(name = "NoSlow", spacedName = "No Slow", category = ModuleCategory.MOVEMENT, description = "Prevent you from getting slowed down by items (swords, foods, etc.) and liquids.")
class NoSlow : Module() {

    // resolve mode
    private val swordModes = ClassUtils.resolvePackage("${this.javaClass.`package`.name}.noslows.sword", NoSlowMode::class.java)
        .map{it.newInstance() as NoSlowMode}
        .sortedBy{it.modeName}

    private val foodModes = ClassUtils.resolvePackage("${this.javaClass.`package`.name}.noslows.food", NoSlowMode::class.java)
        .map{it.newInstance() as NoSlowMode}
        .sortedBy{it.modeName}

    private val bowModes = ClassUtils.resolvePackage("${this.javaClass.`package`.name}.noslows.bow", NoSlowMode::class.java)
        .map{it.newInstance() as NoSlowMode}
        .sortedBy{it.modeName}

    val swordMode: NoSlowMode
        get() = swordModes.find { swordModeValue.get().equals(it.modeName, true) } ?: throw NullPointerException()

    val foodMode: NoSlowMode
        get() = foodModes.find { foodModeValue.get().equals(it.modeName, true) } ?: throw NullPointerException()

    val bowMode: NoSlowMode
        get() = bowModes.find { bowModeValue.get().equals(it.modeName, true) } ?: throw NullPointerException()

    // sword

    private val swordModeValue: ListValue = object: ListValue("SwordMode", swordModes.map{ it.modeName }.toTypedArray(), "Vanilla") {
        override fun onPreChange(oldValue: String, newValue: String) {
            if (state) onDisable()
        }
        override fun onPostChange(oldValue: String, newValue: String) {
            if (state) onEnable()
        }
    }

    private val swordForwardMultiplier = FloatValue("SwordForwardMultiplier", 1.0F, 0.2F, 1.0F, "x")
    private val swordStrafeMultiplier = FloatValue("SwordStrafeMultiplier", 1.0F, 0.2F, 1.0F, "x")

    private val foodModeValue: ListValue = object: ListValue("FoodMode", foodModes.map{ it.modeName }.toTypedArray(), "Vanilla") {
        override fun onPreChange(oldValue: String, newValue: String) {
            if (state) onDisable()
        }
        override fun onPostChange(oldValue: String, newValue: String) {
            if (state) onEnable()
        }
    }

    private val foodForwardMultiplier = FloatValue("FoodForwardMultiplier", 1.0F, 0.2F, 1.0F, "x")
    private val foodStrafeMultiplier = FloatValue("FoodStrafeMultiplier", 1.0F, 0.2F, 1.0F, "x")

    private val bowModeValue: ListValue = object: ListValue("BowMode", bowModes.map{ it.modeName }.toTypedArray(), "Vanilla") {
        override fun onPreChange(oldValue: String, newValue: String) {
            if (state) onDisable()
        }
        override fun onPostChange(oldValue: String, newValue: String) {
            if (state) onEnable()
        }
    }

    private val bowForwardMultiplier = FloatValue("BowForwardMultiplier", 1.0F, 0.2F, 1.0F, "x")
    private val bowStrafeMultiplier = FloatValue("BowStrafeMultiplier", 1.0F, 0.2F, 1.0F, "x")
    private val sneakMultiplier = FloatValue("SneakMultiplier", 1.0F, 0.3F, 1.0F, "x")

    val soulsandValue = BoolValue("Soulsand", true)
    val liquidPushValue = BoolValue("LiquidPush", true)

    override fun onEnable() {
        swordMode.onEnable()
        bowMode.onEnable()
        foodMode.onEnable()
    }

    override fun onDisable() {
        swordMode.onDisable()
        bowMode.onDisable()
        foodMode.onDisable()
    }

    @EventTarget
    fun onInput(event: MoveInputEvent) {
        event.sneakMultiplier = sneakMultiplier.get().toDouble()
    }

    @EventTarget
    fun onPreMotion(event: PreMotionEvent) {
        mc.thePlayer ?: return
        mc.theWorld ?: return

        if (isEating) foodMode.onPreMotion(event)
        if (isBowing) bowMode.onPreMotion(event)
        if (isBlocking) swordMode.onPreMotion(event)
    }

    @EventTarget
    fun onPostMotion(event: PostMotionEvent) {
        mc.thePlayer ?: return
        mc.theWorld ?: return

        if (isEating) foodMode.onPostMotion(event)
        if (isBowing) bowMode.onPostMotion(event)
        if (isBlocking) swordMode.onPostMotion(event)
    }

    @EventTarget
    fun onSlowDown(event: SlowDownEvent) {
        val heldItem = mc.thePlayer.heldItem?.item
        event.forward = getMultiplier(heldItem, true)
        event.strafe = getMultiplier(heldItem, false)
    }

    private fun getMultiplier(item: Item?, isForward: Boolean) = when (item) {
        is ItemFood, is ItemPotion, is ItemBucketMilk -> if (isForward) foodForwardMultiplier.get() else foodStrafeMultiplier.get()
        is ItemSword -> if (isForward) swordForwardMultiplier.get() else swordStrafeMultiplier.get()
        is ItemBow -> if (isForward) bowForwardMultiplier.get() else bowStrafeMultiplier.get()
        else -> 0.2F
    }

    val isBlocking: Boolean
        get() = (mc.thePlayer.isBlocking || MinusBounce.moduleManager[KillAura::class.java]!!.blockingStatus) && mc.thePlayer.heldItem != null && mc.thePlayer.heldItem.item is ItemSword

    val isEating: Boolean
        get() = mc.thePlayer.isUsingItem && (mc.thePlayer.heldItem.item is ItemFood || mc.thePlayer.heldItem.item is ItemBucketMilk || mc.thePlayer.heldItem.item is ItemPotion && !ItemPotion.isSplash(
            mc.thePlayer.heldItem.metadata
        ))

    val isBowing: Boolean
        get() = mc.thePlayer.isUsingItem && mc.thePlayer.heldItem.item is ItemBow

}
