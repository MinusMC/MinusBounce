package net.minusmc.minusbounce.features.module.modules.combat.velocitys.intave

import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minusmc.minusbounce.event.AttackEvent
import net.minusmc.minusbounce.event.KnockbackEvent
import net.minusmc.minusbounce.event.MoveInputEvent
import net.minusmc.minusbounce.event.EntityDamageEvent
import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minusmc.minusbounce.utils.RaycastUtils
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.value.BoolValue
import net.minecraft.util.MovingObjectPosition


class IntaveVelocity : VelocityMode("Intave") {
    private val targetRange = FloatValue("TargetRange", 3f, 0f, 5f)
    private val hurtTime = BoolValue("KeepSprintOnlyHurtTime", false)
    private var blockVelocity = false
    private var isRaytracedToEntity = false

    override fun onEnable() {
        isRaytracedToEntity = false
    }

    override fun onDisable() {
        mc.thePlayer.movementInput.jump = false
    }

    override fun onUpdate() {
        blockVelocity = true
        isRaytracedToEntity = false

        RaycastUtils.runWithModifiedRaycastResult(targetRange.get(), 0f) {
            isRaytracedToEntity = it.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY || 
                mc.objectMouseOver?.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY
        }

        if (isRaytracedToEntity && mc.thePlayer.hurtTime == 9 && !mc.thePlayer.isBurning)
            mc.thePlayer.movementInput.jump = true
    }

    override fun onEntityDamage(event: EntityDamageEvent) {
        if (isRaytracedToEntity && mc.thePlayer.hurtTime == 9 && !mc.thePlayer.isBurning)
            mc.thePlayer.movementInput.jump = true
    }

    override fun onAttack(event: AttackEvent) {
        if (mc.thePlayer.hurtTime > 0 && blockVelocity) {
            mc.thePlayer.isSprinting = false
            mc.thePlayer.motionX *= 0.6
            mc.thePlayer.motionZ *= 0.6
            blockVelocity = false
        }
    }

    override fun onMoveInput(event: MoveInputEvent) {
        if (mc.thePlayer.hurtTime > 0 && isRaytracedToEntity) {
            event.forward = 1.0F
            event.strafe = 0.0F
        }
    }

    override fun onKnockback(event: KnockbackEvent) {
        if (mc.thePlayer.hurtTime <= 0)
            event.isCancelled = true

        if (hurtTime.get() && mc.thePlayer.hurtTime == 0)
            event.isCancelled = false

        event.reduceY = true
    }
}