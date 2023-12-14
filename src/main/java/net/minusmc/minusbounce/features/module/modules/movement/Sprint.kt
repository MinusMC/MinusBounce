/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.movement

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.event.UpdateEvent
import net.minusmc.minusbounce.features.module.modules.combat.KillAura
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.utils.MovementUtils
import net.minusmc.minusbounce.utils.Rotation
import net.minusmc.minusbounce.utils.RotationUtils
import net.minusmc.minusbounce.value.BoolValue
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.potion.Potion
import net.minusmc.minusbounce.ui.client.hud.element.elements.targets.impl.Minus

@ModuleInfo(name = "Sprint", description = "Automatically sprints all the time.", category = ModuleCategory.MOVEMENT)
class Sprint : Module() {

    val allDirectionsValue = BoolValue("AllDirections", true)
    private val noPacketPatchValue = BoolValue("AllDir-NoPacketsPatch", true) { allDirectionsValue.get() }
    val moveDirPatchValue = BoolValue("AllDir-MoveDirPatch", false) { allDirectionsValue.get() }
    private val blindnessValue = BoolValue("Blindness", true)
    val foodValue = BoolValue("Food", true)

    val checkServerSide = BoolValue("CheckServerSide", false)
    val checkServerSideGround = BoolValue("CheckServerSideOnlyGround", false)

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (allDirectionsValue.get() && noPacketPatchValue.get()) {
            if (packet is C0BPacketEntityAction && (packet.action == C0BPacketEntityAction.Action.STOP_SPRINTING || packet.action == C0BPacketEntityAction.Action.START_SPRINTING)) {
                event.cancelEvent()
            }
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val killAura = MinusBounce.moduleManager[KillAura::class.java]!!
        val noSlow = MinusBounce.moduleManager[NoSlow::class.java]!!

        if (noSlow.state && noSlow.noSprintValue.get() && noSlow.isSlowing) {
            mc.thePlayer.isSprinting = false
            return
        }

        if (!MovementUtils.isMoving || mc.thePlayer.isSneaking
            || (blindnessValue.get() && mc.thePlayer.isPotionActive(Potion.blindness))
            || (foodValue.get() && !(mc.thePlayer.foodStats.foodLevel > 6.0F || mc.thePlayer.capabilities.allowFlying))
            || (checkServerSide.get() && (mc.thePlayer.onGround || !checkServerSideGround.get()) && !allDirectionsValue.get() && RotationUtils.targetRotation != null && RotationUtils.getRotationDifference(
                Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)
            ) > 30F)
        ) {
            mc.thePlayer.isSprinting = false
            return
        }

        if (allDirectionsValue.get() || mc.thePlayer.movementInput.moveForward >= 0.8F)
            mc.thePlayer.isSprinting = true

        if (allDirectionsValue.get() && moveDirPatchValue.get() && killAura.target == null)
            RotationUtils.setTargetRot(Rotation(MovementUtils.rawDirection, mc.thePlayer.rotationPitch))
    }

}
