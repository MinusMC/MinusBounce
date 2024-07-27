/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.utils.player

import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.*
import net.minecraft.network.play.client.C03PacketPlayer
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.utils.MinecraftInstance
import net.minusmc.minusbounce.utils.RaycastUtils.raycastEntity
import net.minusmc.minusbounce.utils.Rotation
import net.minusmc.minusbounce.utils.VecRotation
import net.minusmc.minusbounce.utils.misc.RandomUtils
import net.minusmc.minusbounce.utils.extensions.*
import net.minusmc.minusbounce.utils.misc.MathUtils
import kotlin.math.*


object RotationUtils : MinecraftInstance(), Listenable {
    // Rotation
    @JvmField
    var currentRotation: Rotation? = null

    @JvmField
    var targetRotation: Rotation? = null
    var serverRotation = Rotation(0f, 0f)

    private var keepLength = 0
    private var minRotationSpeed = 180f
    private var maxRotationSpeed = 180f

    private var lastRotationSpeed = 180f

    private val rotationSpeed: Float
        get() = RandomUtils.nextFloat(lastRotationSpeed, maxRotationSpeed)

    var active = false

    @EventTarget(priority = 100)
    fun onTick(event: TickEvent) {

        mc.thePlayer ?: return

        currentRotation?.let {
            if (active) {
                val limitRotation = limitAngleChange(it, targetRotation ?: return, rotationSpeed)
                limitRotation.fixedSensitivity(mc.gameSettings.mouseSensitivity)

                if (getRotationDifference(limitRotation, it) < 1) {
                    currentRotation = targetRotation
                    active = false
                } else {
                    currentRotation = limitRotation
                }

                mc.entityRenderer.getMouseOver(1f)
                return
            }

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

                mc.entityRenderer.getMouseOver(1f)
            }
        } ?: if (active) currentRotation = mc.thePlayer.rotation
    }

    @EventTarget
    fun onSentPacket(event: SentPacketEvent) {
        val packet = event.packet

        if (packet !is C03PacketPlayer || !packet.rotating)
            return

        currentRotation?.let {
            packet.yaw = it.yaw
            packet.pitch = it.pitch
        }

        serverRotation = Rotation(packet.yaw, packet.pitch)
    }

    @EventTarget
    fun onLook(event: LookEvent) {
        currentRotation?.let {
            event.yaw = it.yaw
            event.pitch = it.pitch
        }
    }

    fun setTargetRotation(rotation: Rotation, keepLength: Int = 1, minRotationSpeed: Float = 180f, maxRotationSpeed: Float = 180f, fixType: MovementCorrection.Type = MovementCorrection.Type.NONE) {
        if (rotation.yaw.isNaN() || rotation.pitch.isNaN() || rotation.pitch > 90 || rotation.pitch < -90)
            return

        MovementCorrection.type = fixType
        this.minRotationSpeed = minRotationSpeed
        this.maxRotationSpeed = maxRotationSpeed
        this.targetRotation = rotation
        this.keepLength = keepLength
        this.lastRotationSpeed = minRotationSpeed
        active = true
    }

    private fun resetRotation() {
        keepLength = 0

        currentRotation?.let {
            mc.thePlayer.rotationYaw = it.yaw + getAngleDifference(mc.thePlayer.rotationYaw, it.yaw)
            mc.thePlayer.prevRotationYaw = mc.thePlayer.rotationYaw
            mc.thePlayer.prevRotationPitch = mc.thePlayer.rotationPitch
            mc.thePlayer.renderArmYaw = mc.thePlayer.rotationYaw
            mc.thePlayer.renderArmPitch = mc.thePlayer.rotationPitch
            mc.thePlayer.prevRenderArmYaw = mc.thePlayer.rotationYaw
            mc.thePlayer.prevRotationPitch = mc.thePlayer.rotationPitch
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
    fun faceBow(target: Entity, predict: Boolean, predictSize: Float): Rotation {

        var deltaPredictX = 0.0
        var deltaPredictY = 0.0
        var deltaPredictZ = 0.0

        if (predict) {
            deltaPredictX = (target.posX - target.prevPosX) * predictSize - mc.thePlayer.posX + mc.thePlayer.prevPosX
            deltaPredictY = (target.entityBoundingBox.minY - target.prevPosY) * predictSize - mc.thePlayer.posY + mc.thePlayer.prevPosY
            deltaPredictZ = (target.posZ - target.prevPosZ) * predictSize - mc.thePlayer.posZ + mc.thePlayer.prevPosZ
        }

        val (posX, posY, posZ) = Vec3(
            target.posX - mc.thePlayer.posX + deltaPredictX,
            target.entityBoundingBox.minY + target.eyeHeight - 0.15 - mc.thePlayer.entityBoundingBox.minY - mc.thePlayer.eyeHeight + deltaPredictY,
            target.posZ - mc.thePlayer.posZ + deltaPredictZ
        )

        val distance = sqrt(posX * posX + posZ * posZ)

        var velocity = mc.thePlayer.itemInUseDuration / 20f
        velocity = (velocity * velocity + velocity * 2) / 3

        if (velocity > 1) 
            velocity = 1f

        val yaw = MathUtils.toDegrees(atan2(posZ, posX)).toFloat() - 90
        val pitch = MathUtils.toDegrees(-atan((velocity * velocity - sqrt(velocity * velocity * velocity * velocity - 0.006f * 0.006f * distance * distance + 0.003f * posY * velocity * velocity)) / 0.006f / distance)).toFloat()

        return Rotation(yaw, pitch)
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
     * @param predict predict option
     * @return center
     */
    fun searchCenter(bb: AxisAlignedBB, predict: Boolean, throughWallsRange: Float, lookRange: Float): VecRotation? {
        val eyes = mc.thePlayer.getPositionEyes(1f)
        var attackRotation: VecRotation? = null

        val currentRotation = this.currentRotation ?: mc.thePlayer.rotation

        for (x in 0.0..1.0)
            for (y in 0.0..1.0)
                for (z in 0.0..1.0) {
                    val vec3 = Vec3(
                        bb.minX + (bb.maxX - bb.minX) * x, 
                        bb.minY + (bb.maxY - bb.minY) * y, 
                        bb.minZ + (bb.maxZ - bb.minZ) * z
                    )

                    val rotation = toRotation(vec3, predict)
                    rotation.fixedSensitivity(mc.gameSettings.mouseSensitivity)

                    val vecDist = eyes.distanceTo(vec3)
                    if (vecDist > lookRange)
                        continue

                    if (vecDist > throughWallsRange || isVisible(vec3)) {
                        if (attackRotation == null || getRotationDifference(rotation, currentRotation) < getRotationDifference(attackRotation.rotation, currentRotation))
                            attackRotation = VecRotation(vec3, rotation)
                    }
                }

        return attackRotation
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
    fun getRotationDifference(a: Rotation, b: Rotation?): Double {
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
        return raycastEntity(blockReachDistance) {it === targetEntity} != null
    }

    fun isFaced(targetEntity: Entity, blockReachDistance: Double, rotation: Rotation): Boolean {
        return raycastEntity(blockReachDistance, rotation) {it === targetEntity} != null
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