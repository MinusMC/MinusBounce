/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.utils.player

import net.minecraft.client.Minecraft
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
    private val random = Random()
    private var keepLength = 0

    @JvmField
    var targetRotation: Rotation? = null
    val serverRotation = Rotation(0f, 0f)

    var active = false
    private var smoothed = false
    private var silent = false
    private var lastRotations: Rotation? = null
    private var rotations: Rotation? = null
    private var rotationSpeed = 0f

    private var x = random.nextDouble()
    private var y = random.nextDouble()
    private var z = random.nextDouble()

    private fun smooth() {
        if (!smoothed)
            targetRotation = limitAngleChange(lastRotations ?: return, rotations ?: return , rotationSpeed - Math.random().toFloat())

        smoothed = true
        mc.entityRenderer.getMouseOver(1.0F)
    }

    @EventTarget(priority = -2)
    fun onPreUpdate(event: PreUpdateEvent) {
        if (targetRotation == null || lastRotations == null || rotations == null || !active) {
            targetRotation = mc.thePlayer.rotation
            lastRotations = mc.thePlayer.rotation
            rotations = mc.thePlayer.rotation
        }

        if (active) {
            smooth()
        }

        if (random.nextGaussian() > 0.8) x = Math.random()
        if (random.nextGaussian() > 0.8) y = Math.random()
        if (random.nextGaussian() > 0.8) z = Math.random()
    }

    @EventTarget(priority = -2)
    fun onPreMotion(event: PreMotionEvent) {
        if (active && targetRotation != null) {
            keepLength--

            if (silent) {
                event.yaw = targetRotation!!.yaw
                event.pitch = targetRotation!!.pitch
            } else {
                targetRotation!!.toPlayer(mc.thePlayer)
            }

            mc.thePlayer.renderYawOffset = targetRotation!!.yaw
            mc.thePlayer.rotationYawHead = targetRotation!!.yaw

            if (abs((targetRotation!!.yaw - mc.thePlayer.rotationYaw) % 360) < 1 && abs((targetRotation!!.pitch - mc.thePlayer.rotationPitch)) < 1) {
                active = false

                /* Reset Rotation */
                if (silent){
                    /* It will conflict with non-silent */
                    val targetRotation = Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)
                    targetRotation.fixedSensitivity(mc.gameSettings.mouseSensitivity, lastRotations)

                    mc.thePlayer.rotationYaw = targetRotation.yaw + MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw - targetRotation.yaw)
                    mc.thePlayer.rotationPitch = targetRotation.pitch
                }
            }

            lastRotations = targetRotation
        } else {
            lastRotations = Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)
        }

        if (keepLength <= 0) {
            rotations = Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)
        }
        smoothed = false
    }


    @EventTarget(priority = 2)
    fun onPostMotion(event: PostMotionEvent) {
        // set server rotation on post motion
        serverRotation.yaw = mc.thePlayer.lastReportedYaw
        serverRotation.pitch = mc.thePlayer.lastReportedPitch
    }

    /**
     * Set your target rotation
     *
     * @author fmcpe
     * @param rotation your target rotation
     */
    @JvmOverloads
    fun setTargetRotation(
        rotation: Rotation,
        keepLength: Int = 0,
        speed: Float = 180f,
        fixType: MovementCorrection.Type = MovementCorrection.Type.NONE,
        silent: Boolean = true
    ) {
        MovementCorrection.type = if (silent) fixType else MovementCorrection.Type.NONE
        rotationSpeed = speed
        rotations = rotation
        RotationUtils.keepLength = keepLength
        active = true
        this.silent = silent
        smooth()
    }

    /**
     * @return YESSSS!!!
     */
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
                        mc.thePlayer.entityBoundingBox.minY + mc.thePlayer.getEyeHeight(),
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
                    val obj = mc.theWorld.rayTraceBlocks(
                        eyesPos, vector, false,
                        false, true
                    )
                    if (obj.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                        val currentVec = VecRotation(posVec, rotation)
                        if (vecRotation == null || getRotationDifference(currentVec.rotation) < getRotationDifference(
                                vecRotation.rotation
                            )
                        ) vecRotation = currentVec
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
            target.entityBoundingBox.minY + (if (predict) (target.entityBoundingBox.minY - target.prevPosY) * predictSize else 0.0) + target.eyeHeight - 0.15 - (player.entityBoundingBox.minY + if (predict) player.posY - player.prevPosY else 0.0) - player.getEyeHeight(),
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
            bb.minX + (bb.maxX - bb.minX) * x * randomMultiply,
            bb.minY + (bb.maxY - bb.minY) * y * randomMultiply,
            bb.minZ + (bb.maxZ - bb.minZ) * z * randomMultiply
        )

        val randomRotation = toRotation(randomVec, predict)
        val eyes = mc.thePlayer.getPositionEyes(1f)
        var vecRotation: VecRotation? = null

        for (x in 0.15..0.85)
            for (y in 0.0..1.0)
                for (z in 0.15..0.85) {
                    val vec3 = Vec3(
                        bb.minX + (bb.maxX - bb.minX) * x, 
                        bb.minY + (bb.maxY - bb.minY) * y, 
                        bb.minZ + (bb.maxZ - bb.minZ) * z
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
     * Limit your rotation using a turn speed
     *
     * @param currentRotation your current rotation
     * @param targetRotation your goal rotation
     * @param turnSpeed your turn speed
     * @return limited rotation
     */
    fun limitAngleChange(currentRotation: Rotation, targetRotation: Rotation, turnSpeed: Float): Rotation {
        var (yaw, pitch) = targetRotation
        val yDiff = getAngleDifference(targetRotation.yaw, currentRotation.yaw)
        val pDiff = getAngleDifference(targetRotation.pitch, currentRotation.pitch)

        /* I love .let */
        turnSpeed.let{
            val distance = sqrt(yDiff * yDiff + pDiff * pDiff)
            if(it < 0 || distance <= 0) return@let
            val maxYaw = it * abs(yDiff / distance)
            val maxPitch = it * abs(pDiff / distance)

            val yAdd = max(min(yDiff, maxYaw), -maxYaw)
            val pAdd = max(min(pDiff, maxPitch), -maxPitch)

            yaw = currentRotation.yaw + yAdd
            pitch = currentRotation.pitch + pAdd

            /* Randomize */
            for (i in 1.0..Minecraft.getDebugFPS() / 20.0 + Math.random() * 10.0 step 1.0) {
                if (abs(yAdd) + abs(pAdd) > 1) {
                    yaw += (Math.random().toFloat() - 0.5f) / 1000f
                    pitch -= Math.random().toFloat() / 200f
                }

                /* Fixing GCD */
                val rotation = Rotation(yaw, pitch)
                rotation.fixedSensitivity(mc.gameSettings.mouseSensitivity)

                /* Setting Rotation */
                yaw = rotation.yaw
                pitch = rotation.pitch.coerceIn(-90f, 90f)
            }
        }

        return Rotation(yaw, pitch)
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

    /**
     * Allows you to check if your enemy is behind a wall
     */
    fun isVisible(vec3: Vec3?): Boolean {
        val eyesPos = Vec3(
            mc.thePlayer.posX,
            mc.thePlayer.entityBoundingBox.minY + mc.thePlayer.getEyeHeight(),
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