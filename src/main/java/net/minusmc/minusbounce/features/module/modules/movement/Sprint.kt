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
import net.minecraft.item.ItemSword
import net.minecraft.potion.Potion
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3

@ModuleInfo(name = "Sprint", description = "Automatically sprints all the time.", category = ModuleCategory.MOVEMENT)
class Sprint : Module() {

    val allDirectionsValue = BoolValue("AllDirections", true)
    val noPacketPatchValue = BoolValue("AllDir-NoPacketsPatch", true, { allDirectionsValue.get() })
    val moveDirPatchValue = BoolValue("AllDir-MoveDirPatch", false, { allDirectionsValue.get() })
    val blindnessValue = BoolValue("Blindness", true)
    val foodValue = BoolValue("Food", true)
    val checkServerSide = BoolValue("CheckServerSide", false)
    val checkServerSideGround = BoolValue("CheckServerSideOnlyGround", false)


    private var modified = false

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (allDirectionsValue.get() && noPacketPatchValue.get()) {
            if (packet is C0BPacketEntityAction && (packet.getAction() == C0BPacketEntityAction.Action.STOP_SPRINTING || packet.getAction() == C0BPacketEntityAction.Action.START_SPRINTING)) {
                event.cancelEvent()
            }
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val killAura = MinusBounce.moduleManager[KillAura::class.java]!! as KillAura
        val noSlow = MinusBounce.moduleManager[NoSlow::class.java]!! as NoSlow

        if (!MovementUtils.isMoving || mc.thePlayer.isSneaking() ||
                (noSlow.state && noSlow.noSprintValue.get() && noSlow.isSlowing) ||
                (blindnessValue.get() && mc.thePlayer.isPotionActive(Potion.blindness)) ||
                (foodValue.get() && !(mc.thePlayer.getFoodStats().getFoodLevel() > 6.0F || mc.thePlayer.capabilities.allowFlying))
                || (checkServerSide.get() && (mc.thePlayer.onGround || !checkServerSideGround.get())
                        && !allDirectionsValue.get() && RotationUtils.targetRotation != null &&
                        RotationUtils.getRotationDifference(Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)) > 15F) || killAura.target != null && !killAura.keepSprintValue.get()) {
            mc.thePlayer.isSprinting = false
            return
        }

        if (allDirectionsValue.get() || mc.thePlayer.movementInput.moveForward >= 0.8F) {
            mc.thePlayer.isSprinting = true
        }

        if (allDirectionsValue.get() && moveDirPatchValue.get() && killAura.target == null)
            RotationUtils.setTargetRot(Rotation(MovementUtils.getRawDirection(mc.thePlayer.rotationYaw), mc.thePlayer.rotationPitch))
    }

}