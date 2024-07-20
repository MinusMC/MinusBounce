package net.minusmc.minusbounce.features.module.modules.movement.speeds.other

import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedMode
import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedType
import net.minusmc.minusbounce.utils.player.MovementUtils
import net.minusmc.minusbounce.utils.player.RotationUtils
import net.minusmc.minusbounce.utils.misc.MathUtils
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.event.PreMotionEvent

import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand

import kotlin.math.*

class GrimCollideSpeed: SpeedMode("GrimCollide", SpeedType.OTHER) {

    private val boostSpeed = FloatValue("BoostSpeed", 0.01f, 0.01f, 0.08f)

    override fun onUpdate() {
        if (MovementUtils.isMoving && mc.thePlayer.onGround && !mc.gameSettings.keyBindJump.pressed) 
            mc.thePlayer.jump()
    }

    override fun onPreMotion(event: PreMotionEvent) {
        if (!MovementUtils.isMoving)
            return

        val playerBox = mc.thePlayer.entityBoundingBox.expand(1.0, 1.0, 1.0)

        val collisions = mc.theWorld.loadedEntityList.count {
            it != mc.thePlayer && it is EntityLivingBase && 
            it !is EntityArmorStand && playerBox.intersectsWith(it.entityBoundingBox)
        }

        val rotation = RotationUtils.currentRotation ?: mc.thePlayer.rotation

        val yaw = MovementUtils.getDirectionToRadian(rotation.yaw)
        val boost = boostSpeed.get().toDouble() * collisions
        mc.thePlayer.addVelocity(-sin(yaw) * boost, 0.0, cos(yaw) * boost)
    }
}