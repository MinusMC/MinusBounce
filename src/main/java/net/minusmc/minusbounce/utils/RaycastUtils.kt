/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.utils

import com.google.common.base.Predicate
import com.google.common.base.Predicates
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.projectile.EntityLargeFireball
import net.minecraft.util.*
import net.minusmc.minusbounce.utils.extensions.*
import net.minusmc.minusbounce.utils.player.RotationUtils

object RaycastUtils : MinecraftInstance() {
    /**
     * Modified mouse object pickup
     */

    fun runWithModifiedRaycastResult(range: Float, wallRange: Float, action: (MovingObjectPosition) -> Unit) {
        val rotation = RotationUtils.currentRotation ?: RotationUtils.serverRotation
        runWithModifiedRaycastResult(rotation, range, wallRange, action)
    }

    fun runWithModifiedRaycastResult(rotation: Rotation, range: Float, wallRange: Float, action: (MovingObjectPosition) -> Unit) {
        val entity = mc.renderViewEntity

        val prevPointedEntity = mc.pointedEntity
        val prevObjectMouseOver = mc.objectMouseOver

        if (entity != null && mc.theWorld != null) {
            mc.pointedEntity = null

            val buildReach = if (mc.playerController.currentGameType.isCreative) 5.0 else 4.5

            val vec3 = entity.eyes
            val vec31 = RotationUtils.getVectorForRotation(rotation)
            val vec32 = vec3.addVector(vec31.xCoord * buildReach, vec31.yCoord * buildReach, vec31.zCoord * buildReach)

            mc.objectMouseOver = entity.worldObj.rayTraceBlocks(vec3, vec32, false, false, true)

            var d1 = buildReach
            var flag = false

            if (mc.playerController.extendedReach()) {
                d1 = 6.0
            } else {
                flag = true
            }

            if (mc.objectMouseOver != null) {
                d1 = mc.objectMouseOver.hitVec.distanceTo(vec3)
            }

            var pointedEntity: Entity? = null
            var vec33: Vec3? = null

            val list = mc.theWorld.getEntities(EntityLivingBase::class.java) {
                it != null && (it !is EntityPlayer || !it.isSpectator) && it.canBeCollidedWith() && it != entity
            }

            var d2 = d1

            for (entity1 in list) {
                val f1 = entity1.collisionBorderSize
                val boxes = mutableListOf<AxisAlignedBB>()

                boxes.add(entity1.entityBoundingBox.expand(f1.toDouble(), f1.toDouble(), f1.toDouble()))

                for (box in boxes) {
                    val intercept = box.calculateIntercept(vec3, vec32)

                    if (box.isVecInside(vec3)) {
                        if (d2 >= 0) {
                            pointedEntity = entity1
                            vec33 = if (intercept == null) vec3 else intercept.hitVec
                            d2 = 0.0
                        }
                    } else if (intercept != null) {
                        val d3 = vec3.distanceTo(intercept.hitVec)

                        if (!RotationUtils.isVisible(intercept.hitVec)) {
                            if (d3 <= wallRange) {
                                if (d3 < d2 || d2 == 0.0) {
                                    pointedEntity = entity1
                                    vec33 = intercept.hitVec
                                    d2 = d3
                                }
                            }

                            continue
                        }

                        if (d3 < d2 || d2 == 0.0) {
                            if (entity1 === entity.ridingEntity && !entity.canRiderInteract()) {
                                if (d2 == 0.0) {
                                    pointedEntity = entity1
                                    vec33 = intercept.hitVec
                                }
                            } else {
                                pointedEntity = entity1
                                vec33 = intercept.hitVec
                                d2 = d3
                            }
                        }
                    }
                }
            }

            if (pointedEntity != null && flag && vec3.distanceTo(vec33) > range) {
                pointedEntity = null
                mc.objectMouseOver = MovingObjectPosition(
                    MovingObjectPosition.MovingObjectType.MISS,
                    vec33,
                    null,
                    BlockPos(vec33)
                )
            }

            if (pointedEntity != null && (d2 < d1 || mc.objectMouseOver == null)) {
                mc.objectMouseOver = MovingObjectPosition(pointedEntity, vec33)
                mc.pointedEntity = pointedEntity
            }

            action(mc.objectMouseOver)

            mc.objectMouseOver = prevObjectMouseOver
            mc.pointedEntity = prevPointedEntity
        }
    }

    fun raycastEntity(range: Double, filter: (Entity) -> Boolean): Entity? {
        return raycastEntity(range, RotationUtils.serverRotation, filter)
    }

    fun raycastEntity(range: Double, yaw: Float, pitch: Float, filter: (Entity) -> Boolean): Entity? {
        return raycastEntity(range, Rotation(yaw, pitch), filter)
    }

    fun raycastEntity(range: Double, rotation: Rotation, filter: (Entity) -> Boolean): Entity? {
        val renderViewEntity = mc.renderViewEntity

        if (renderViewEntity == null || mc.theWorld == null)
            return null

        var blockReachDistance = range
        val eyePosition = renderViewEntity.eyes
        val entityLook = RotationUtils.getVectorForRotation(rotation)
        val vec = eyePosition + (entityLook * blockReachDistance)

        val entityList = mc.theWorld.getEntities(Entity::class.java) {
            it != null && (it is EntityLivingBase || it is EntityLargeFireball) && (it !is EntityPlayer || !it.isSpectator) && it.canBeCollidedWith() && it != renderViewEntity
        }

        var pointedEntity: Entity? = null

        for (entity in entityList) {
            if (!filter(entity))
                continue

            val axisAlignedBB = entity.hitBox
            val movingObjectPosition = axisAlignedBB.calculateIntercept(eyePosition, vec)

            if (axisAlignedBB.isVecInside(eyePosition)) {
                if (blockReachDistance >= 0.0) {
                    pointedEntity = entity
                    blockReachDistance = 0.0
                }
            } else if (movingObjectPosition != null) {
                val eyeDistance = eyePosition.distanceTo(movingObjectPosition.hitVec)

                if (eyeDistance < blockReachDistance || blockReachDistance == 0.0) {
                    if (entity == renderViewEntity.ridingEntity && !renderViewEntity.canRiderInteract()) {
                        if (blockReachDistance == 0.0)
                            pointedEntity = entity
                    } else {
                        pointedEntity = entity
                        blockReachDistance = eyeDistance
                    }
                }
            }
        }

        return pointedEntity
    }

    fun performBlockRaytrace(rotation: Rotation, maxReach: Float): MovingObjectPosition? {
        val player = mc.thePlayer ?: return null
        val world = mc.theWorld ?: return null

        val eyes = player.eyes
        val rotationVec = RotationUtils.getVectorForRotation(rotation)

        val reach = eyes + (rotationVec * maxReach.toDouble())

        return world.rayTraceBlocks(eyes, reach, false, false, true)
    }

    fun performBlockRaytrace(maxReach: Float) = performBlockRaytrace(RotationUtils.currentRotation ?: mc.thePlayer.rotation, maxReach)
}