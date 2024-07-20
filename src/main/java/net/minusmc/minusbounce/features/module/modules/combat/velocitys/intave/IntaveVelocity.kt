package net.minusmc.minusbounce.features.module.modules.combat.velocitys.intave

import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minusmc.minusbounce.event.AttackEvent
import net.minusmc.minusbounce.event.KnockbackEvent
import net.minusmc.minusbounce.event.MoveInputEvent
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.event.EntityDamageEvent
import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minusmc.minusbounce.utils.RaycastUtils
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.value.BoolValue
import net.minecraft.util.MovingObjectPosition


class IntaveVelocity : VelocityMode("Intave") {
    private val targetRange = FloatValue("TargetRange", 3f, 0f, 5f)
    private val hurtTime = BoolValue("KeepSprintOnlyHurtTime", false)
    private var isRaytracedToEntity = false
    private var counter = 0

    override fun onEnable() {
        isRaytracedToEntity = false
        counter = 0
    }

    override fun onUpdate() {
        RaycastUtils.runWithModifiedRaycastResult(targetRange.get(), 0f) {
            isRaytracedToEntity = it.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY || 
                mc.objectMouseOver?.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY
        }
    }

    override fun onMoveInput(event: MoveInputEvent) {
        if (isRaytracedToEntity && mc.thePlayer.hurtTime == 9 && !mc.thePlayer.isBurning && counter++ % 2 == 0)
            event.jump = true

        if (mc.thePlayer.hurtTime > 0 && isRaytracedToEntity)
            event.forward = 1.0F
    }

    override fun onKnockback(event: KnockbackEvent) {
        if (mc.thePlayer.hurtTime <= 0)
            event.isCancelled = true

        if (hurtTime.get() && mc.thePlayer.hurtTime == 0)
            event.isCancelled = false

        event.reduceY = true
    }
}