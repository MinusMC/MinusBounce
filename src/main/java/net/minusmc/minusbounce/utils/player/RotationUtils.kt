/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.utils.player

import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.*
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.utils.MinecraftInstance
import net.minusmc.minusbounce.utils.RaycastUtils.IEntityFilter
import net.minusmc.minusbounce.utils.RaycastUtils.raycastEntity
import net.minusmc.minusbounce.utils.Rotation
import net.minusmc.minusbounce.utils.VecRotation
import net.minusmc.minusbounce.utils.extensions.*
import net.minusmc.minusbounce.utils.misc.MathUtils
import java.util.*
import kotlin.math.*


object RotationUtils : MinecraftInstance(), Listenable {
    // Rotation
    @JvmField
    var currentRotation: Rotation? = null

    @JvmField
    var targetRotation: Rotation? = null
    var serverRotation = Rotation(0f, 0f)

    private var keepLength = 0
    private var rotationSpeed = 180f

    var active = false

    @EventTarget(priority = 100)
    fun onTick(event: TickEvent) {

        mc.thePlayer ?: return

        currentRotation ?: run {
            if (active) currentRotation = mc.thePlayer.rotation
        }

        currentRotation?.let {
            if (keepLength > 0) {
                keepLength--
                return
            }

            if (getRotationDifference(it, mc.thePlayer.rotation) <= 1)
                resetRotation()
            else {
                val backRotation = limitAngleChange(it, mc.thePlayer.rotation, rotationSpeed)
                backRotation.fixedSensitivity(mc.gameSettings.mouseSensitivity)
                currentRotation = backRotation
            }
        }
    }

    @EventTarget(priority = 100)
    fun onPreMotion(event: PreMotionEvent) {
        currentRotation?.let {

            event.yaw = it.yaw
            event.pitch = it.pitch

            mc.thePlayer.renderYawOffset = it.yaw
            mc.thePlayer.rotationYawHead = it.yaw

            if (active) {
                val limitRotation = limitAngleChange(it, targetRotation ?: return, rotationSpeed)
                currentRotation = limitRotation
                keepLength++

                if (getRotationDifference(limitRotation, it) < 1)
                    active = false
            }
        }
    }

    @EventTarget(priority = 100)
    fun onPostMotion(event: PostMotionEvent) {
        currentRotation?.let {
            serverRotation = it
        }
    }

    fun setTargetRotation(rotation: Rotation, keepLength: Int = 1, speed: Float = 180f, fixType: MovementCorrection.Type = MovementCorrection.Type.NONE) {
        MovementCorrection.type = fixType
        this.rotationSpeed = speed
        this.targetRotation = rotation
        this.keepLength = keepLength
        active = true
    }

    private fun resetRotation() {
        keepLength = 0

        currentRotation?.let {
            mc.thePlayer.rotationYaw = it.yaw + getAngleDifference(mc.thePlayer.rotationYaw, it.yaw)
        }

        currentRotation = null
        targetRotation = null
    }

    override fun handleEvents() = true


    /**
     * Face block
     *
     * @param blockPos target block
     */
    fun faceBlock(blockPos: BlockPos?): VecRotation? {
        if (blockPos == null) return null
        var vecRotation: VecRotation? = null

        for (x in 0.1..0.9){
            for(y in 0.1..0.9){
                for(z in 0.1..0.9){
                    val eyesPos = Vec3(
                        mc.thePlayer.posX,
                        mc.thePlayer.entityBoundingBox.minY + mc.thePlayer.eyeHeight,
                        mc.thePlayer.posZ
                    )
                    val posVec = Vec3(blockPos).addVector(x, y, z)
                    val dist = eyesPos.distanceTo(posVec)
                    val (diffX, diffY, diffZ) = posVec - eyesPos

                    val diffXZ = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ).toDouble()
                    val rotation = Rotation(
                        MathUtils.wrapAngleTo180(Math.toDegrees(atan2(diffZ, diffX)).toFloat() - 90f),
                        MathUtils.wrapAngleTo180(-Math.toDegrees(atan2(diffY, diffXZ)).toFloat())
                    )
                    val rotationVector = getVectorForRotation(rotation)
                    val vector = eyesPos.addVector(
                        rotationVector.xCoord * dist, rotationVector.yCoord * dist,
                        rotationVector.zCoord * dist
                    )
                    val obj = mc.theWorld.rayTraceBlocks(eyesPos, vector, false, false, true)
                    if (obj.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                        val currentVec = VecRotation(posVec, rotation)
                        if (vecRotation == null || getRotationDifference(currentVec.rotation) < getRotationDifference(vecRotation.rotation)) 
                            vecRotation = currentVec
                    }
                }
            }
        }

        return vecRotation
    }

    /**
     * Face target with bow
     *
     * @param target your enemy
     * @param predict predict new enemy position
     * @param predictSize predict size of predict
     */
    fun faceBow(target: Entity, predict: Boolean, predictSize: Float) {
        val player = mc.thePlayer
        val (posX, posY, posZ) = Vec3(
            target.posX + (if (predict) (target.posX - target.prevPosX) * predictSize else 0.0) - (player.posX + (if (predict) player.posX - player.prevPosX else 0.0)),
            target.entityBoundingBox.minY + (if (predict) (target.entityBoundingBox.minY - target.prevPosY) * predictSize else 0.0) + target.eyeHeight - 0.15 - (player.entityBoundingBox.minY + if (predict) player.posY - player.prevPosY else 0.0) - player.eyeHeight,
            target.posZ + (if (predict) (target.posZ - target.prevPosZ) * predictSize else 0.0) - (player.posZ + if (predict) player.posZ - player.prevPosZ else 0.0)
        )
        val posSqrt = sqrt(posX * posX + posZ * posZ)

        var velocity = player.itemInUseDuration / 20f
        velocity = (velocity * velocity + velocity * 2) / 3
        if (velocity > 1) velocity = 1f

        val rotation = Rotation((atan2(posZ, posX) * 180 / Math.PI).toFloat() - 90, -Math.toDegrees(atan((velocity * velocity - sqrt(velocity * velocity * velocity * velocity - 0.006f * (0.006f * (posSqrt * posSqrt) + 2 * posY * (velocity * velocity)))) / (0.006f * posSqrt))).toFloat())
        setTargetRotation(rotation, fixType = MovementCorrection.Type.NONE)
    }

    /**
     * Translate vec to rotation
     * Diff supported
     *
     * @param vec target vec
     * @param predict predict new location of your body
     * @return rotation
     */
    @JvmOverloads
    fun toRotation(vec: Vec3, predict: Boolean = false, diff: Vec3? = null): Rotation {
        val eyesPos = Vec3(mc.thePlayer.posX, mc.thePlayer.entityBoundingBox.minY + mc.thePlayer.eyeHeight, mc.thePlayer.posZ)
        if (predict) eyesPos.addVector(mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ)

        val (diffX, diffY, diffZ) = diff ?: (vec - eyesPos)

        return Rotation(
            MathUtils.wrapAngleTo180(Math.toDegrees(atan2(diffZ, diffX)).toFloat() - 90f),
            MathUtils.wrapAngleTo180((-Math.toDegrees(atan2(diffY, sqrt(diffX * diffX + diffZ * diffZ)))).toFloat())
        )
    }

    /**
     * Get the center of a box
     *
     * @param bb your box
     * @return center of box
     */
    fun getCenter(bb: AxisAlignedBB): Vec3 {
        return Vec3(
            bb.minX + (bb.maxX - bb.minX) * 0.5,
            bb.minY + (bb.maxY - bb.minY) * 0.5,
            bb.minZ + (bb.maxZ - bb.minZ) * 0.5
        )
    }


    /**
     * Search good center
     *
     * @param bb enemy box
     * @param random random option
     * @param predict predict option
     * @param throughWalls throughWalls option
     * @return center
     */
    @JvmOverloads
    fun searchCenter(
        bb: AxisAlignedBB,
        random: Boolean,
        predict: Boolean,
        throughWalls: Boolean,
        distance: Float,
        randomMultiply: Float = 0f,
    ): VecRotation? {
        val randomVec = Vec3(
            bb.minX + (bb.maxX - bb.minX) * 0.5 * randomMultiply,
            bb.minY + (bb.maxY - bb.minY) * 0.5 * randomMultiply,
            bb.minZ + (bb.maxZ - bb.minZ) * 0.5 * randomMultiply
        )

        val randomRotation = toRotation(randomVec, predict)
        val eyes = mc.thePlayer.getPositionEyes(1f)
        var vecRotation: VecRotation? = null

        for (x in 0.15..0.85)
            for (y in 0.0..1.0)
                for (z in 0.15..0.85) {
                    val vec3 = Vec3(
                        bb.minX + (bb.maxX - bb.minX) * 0.5, 
                        bb.minY + (bb.maxY - bb.minY) * 0.5, 
                        bb.minZ + (bb.maxZ - bb.minZ) * 0.5
                    )

                    val rotation = toRotation(vec3, predict)
                    val vecDist = eyes.distanceTo(vec3)

                    if (vecDist > distance)
                        continue

                    if (throughWalls || isVisible(vec3)) {
                        if (vecRotation == null || if (random) getRotationDifference(rotation, randomRotation) < getRotationDifference(vecRotation.rotation, randomRotation) else getRotationDifference(rotation) < getRotationDifference(vecRotation.rotation))
                            vecRotation = VecRotation(vec3, rotation)
                    }
                }

        return vecRotation
    }

    fun limitAngleChange(fromRotation: Rotation, toRotation: Rotation, turnSpeed: Float): Rotation {
        val yawDifference = getAngleDifference(toRotation.yaw, fromRotation.yaw)
        val pitchDifference = getAngleDifference(toRotation.pitch, fromRotation.pitch)
        return Rotation(
            fromRotation.yaw + if (yawDifference > turnSpeed) turnSpeed else max(yawDifference, -turnSpeed),
            fromRotation.pitch + if (pitchDifference > turnSpeed) turnSpeed else max(pitchDifference, -turnSpeed)
        )
    }

    /**
     * Calculate difference between the client rotation and your entity
     *
     * @param entity your entity
     * @return difference between rotation
     */
    fun getRotationDifference(entity: Entity): Double {
        val rotation = toRotation(getCenter(entity.entityBoundingBox), true)
        return getRotationDifference(rotation, Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch))
    }

    /**
     * Calculate difference between the server rotation and your rotation
     *
     * @param rotation your rotation
     * @return difference between rotation
     */
    fun getRotationDifference(rotation: Rotation) = getRotationDifference(rotation, serverRotation)

    /**
     * Calculate difference between two rotations
     *
     * @param a rotation
     * @param b rotation
     * @return difference between rotation
     */
    private fun getRotationDifference(a: Rotation, b: Rotation?): Double {
        return hypot(getAngleDifference(a.yaw, b!!.yaw).toDouble(), (a.pitch - b.pitch).toDouble())
    }

    /**
     * Calculate difference between two angle points
     *
     * @param a angle point
     * @param b angle point
     * @return difference between angle points
     */
    fun getAngleDifference(a: Float, b: Float): Float {
        return ((a - b) % 360f + 540f) % 360f - 180f
    }

    /**
     * Calculate rotation to vector
     *
     * @param rotation your rotation
     * @return target vector
     */
    @JvmStatic
    fun getVectorForRotation(rotation: Rotation): Vec3 {
        val rotX = rotation.yaw * Math.PI / 180f
        val rotY = rotation.pitch * Math.PI / 180f

        return Vec3(-cos(rotY) * sin(rotX), -sin(rotY), cos(rotY) * cos(rotX))
    }

    /**
     * Allows you to check if your crosshair is over your target entity
     *
     * @param targetEntity your target entity
     * @param blockReachDistance your reach
     * @return if crosshair is over target
     */
    fun isFaced(targetEntity: Entity, blockReachDistance: Double): Boolean {
        return raycastEntity(
            blockReachDistance,
            object : IEntityFilter {
                override fun canRaycast(entity: Entity?): Boolean {
                    return entity === targetEntity
                }
            }) != null
    }

    fun isFaced(targetEntity: Entity, blockReachDistance: Double, rotation: Rotation): Boolean {
        return raycastEntity(
            blockReachDistance,
            rotation,
            object : IEntityFilter {
                override fun canRaycast(entity: Entity?): Boolean {
                    return entity === targetEntity
                }
            }) != null
    }


    /**
     * Allows you to check if your enemy is behind a wall
     */
    fun isVisible(vec3: Vec3?): Boolean {
        val eyesPos = Vec3(
            mc.thePlayer.posX,
            mc.thePlayer.entityBoundingBox.minY + mc.thePlayer.eyeHeight,
            mc.thePlayer.posZ
        )
        return mc.theWorld.rayTraceBlocks(eyesPos, vec3) == null
    }

    fun getRotationsEntity(entity: EntityLivingBase): Rotation {
        return getRotations(entity.posX, entity.posY + entity.eyeHeight - 0.4, entity.posZ)
    }

    fun getRotations(posX: Double, posY: Double, posZ: Double): Rotation {
        val x = posX - mc.thePlayer.posX
        val y = posY - mc.thePlayer.posY - mc.thePlayer.eyeHeight.toDouble()
        val z = posZ - mc.thePlayer.posZ

        val dist = sqrt(x * x + z * z)

        return Rotation(
            MathUtils.toDegrees(atan2(z, x)) - 90.0f,
            -MathUtils.toDegrees(atan2(y, dist))
        )
    }
}