/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.utils

import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.projectile.EntityEgg
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.*
import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.Listenable
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.event.TickEvent
import net.minusmc.minusbounce.utils.RaycastUtils.IEntityFilter
import net.minusmc.minusbounce.utils.RaycastUtils.raycastEntity
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.*


object RotationUtils : MinecraftInstance(), Listenable {
    private val random = Random()
    private var keepLength = 0
    @JvmField
    var targetRotation: Rotation? = null
    @JvmField
    var serverRotation: Rotation? = Rotation(0f, 0f)
    var keepCurrentRotation = false
    private var x = random.nextDouble()
    private var y = random.nextDouble()
    private var z = random.nextDouble()

    /**
     * Handle minecraft tick
     *
     * @param event Tick event
     */
    @EventTarget
    fun onTick(event: TickEvent?) {
        if (targetRotation != null) {
            keepLength--
            if (keepLength <= 0) reset()
        }
        if (random.nextGaussian() > 0.8) x = Math.random()
        if (random.nextGaussian() > 0.8) y = Math.random()
        if (random.nextGaussian() > 0.8) z = Math.random()
    }

    /**
     * Handle packet
     *
     * @param event Packet Event
     */
    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is C03PacketPlayer) {
            val packetPlayer = packet
            if (targetRotation != null && !keepCurrentRotation && (targetRotation!!.yaw != serverRotation!!.yaw || targetRotation!!.pitch != serverRotation!!.pitch)) {
                packetPlayer.yaw = targetRotation!!.yaw
                packetPlayer.pitch = targetRotation!!.pitch
                packetPlayer.rotating = true
            }
            if (packetPlayer.rotating) serverRotation = Rotation(packetPlayer.yaw, packetPlayer.pitch)
        }
    }

    /**
     * @return YESSSS!!!
     */
    override fun handleEvents(): Boolean {
        return true
    }

    /**
     * @author aquavit
     *
     * epic skid moment
     */
    fun otherRotation(
        bb: AxisAlignedBB,
        vec: Vec3,
        predict: Boolean,
        throughWalls: Boolean,
        distance: Float
    ): Rotation {
        val eyesPos = Vec3(
            mc.thePlayer.posX, mc.thePlayer.entityBoundingBox.minY +
                    mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ
        )
        val eyes = mc.thePlayer.getPositionEyes(1f)
        var vecRotation: VecRotation? = null
        var xSearch = 0.15
        while (xSearch < 0.85) {
            var ySearch = 0.15
            while (ySearch < 1.0) {
                var zSearch = 0.15
                while (zSearch < 0.85) {
                    val vec3 = Vec3(
                        bb.minX + (bb.maxX - bb.minX) * xSearch,
                        bb.minY + (bb.maxY - bb.minY) * ySearch, bb.minZ + (bb.maxZ - bb.minZ) * zSearch
                    )
                    val rotation = toRotation(vec3, predict)
                    val vecDist = eyes.distanceTo(vec3)
                    if (vecDist > distance) {
                        zSearch += 0.1
                        continue
                    }
                    if (throughWalls || isVisible(vec3)) {
                        val currentVec = VecRotation(vec3, rotation)
                        if (vecRotation == null) vecRotation = currentVec
                    }
                    zSearch += 0.1
                }
                ySearch += 0.1
            }
            xSearch += 0.1
        }
        if (predict) eyesPos.addVector(mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ)
        val diffX = vec.xCoord - eyesPos.xCoord
        val diffY = vec.yCoord - eyesPos.yCoord
        val diffZ = vec.zCoord - eyesPos.zCoord
        return Rotation(
            MathHelper.wrapAngleTo180_float(
                Math.toDegrees(atan2(diffZ, diffX)).toFloat() - 90f
            ),
            MathHelper.wrapAngleTo180_float(
                (-Math.toDegrees(
                    atan2(
                        diffY,
                        sqrt(diffX * diffX + diffZ * diffZ)
                    )
                )).toFloat()
            )
        )
    }

    /**
     * Face block
     *
     * @param blockPos target block
     */
    fun faceBlock(blockPos: BlockPos?): VecRotation? {
        if (blockPos == null) return null
        var vecRotation: VecRotation? = null
        var xSearch = 0.1
        while (xSearch < 0.9) {
            var ySearch = 0.1
            while (ySearch < 0.9) {
                var zSearch = 0.1
                while (zSearch < 0.9) {
                    val eyesPos = Vec3(
                        mc.thePlayer.posX,
                        mc.thePlayer.entityBoundingBox.minY + mc.thePlayer.getEyeHeight(),
                        mc.thePlayer.posZ
                    )
                    val posVec = Vec3(blockPos).addVector(xSearch, ySearch, zSearch)
                    val dist = eyesPos.distanceTo(posVec)
                    val diffX = posVec.xCoord - eyesPos.xCoord
                    val diffY = posVec.yCoord - eyesPos.yCoord
                    val diffZ = posVec.zCoord - eyesPos.zCoord
                    val diffXZ = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ).toDouble()
                    val rotation = Rotation(
                        MathHelper.wrapAngleTo180_float(Math.toDegrees(atan2(diffZ, diffX)).toFloat() - 90f),
                        MathHelper.wrapAngleTo180_float(-Math.toDegrees(atan2(diffY, diffXZ)).toFloat())
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
                    zSearch += 0.1
                }
                ySearch += 0.1
            }
            xSearch += 0.1
        }
        return vecRotation
    }

    /**
     * Face target with bow
     *
     * @param target your enemy
     * @param silent client side rotations
     * @param predict predict new enemy position
     * @param predictSize predict size of predict
     */
    fun faceBow(target: Entity, silent: Boolean, predict: Boolean, predictSize: Float) {
        val player = mc.thePlayer
        val posX: Double = target.posX + (if (predict) (target.posX - target.prevPosX) * predictSize else 0.0) - (player.posX + (if (predict) player.posX - player.prevPosX else 0.0))
        val posY: Double =
            target.entityBoundingBox.minY + (if (predict) (target.entityBoundingBox.minY - target.prevPosY) * predictSize else 0.0) + target.eyeHeight - 0.15 - (player.entityBoundingBox.minY + if (predict) player.posY - player.prevPosY else 0.0) - player.getEyeHeight()
        val posZ: Double =
            target.posZ + (if (predict) (target.posZ - target.prevPosZ) * predictSize else 0.0) - (player.posZ + if (predict) player.posZ - player.prevPosZ else 0.0)
        val posSqrt = sqrt(posX * posX + posZ * posZ)
        var velocity = player.itemInUseDuration / 20f
        velocity = (velocity * velocity + velocity * 2) / 3
        if (velocity > 1) velocity = 1f
        val rotation = Rotation(
            (atan2(posZ, posX) * 180 / Math.PI).toFloat() - 90,
            -Math.toDegrees(atan((velocity * velocity - sqrt(velocity * velocity * velocity * velocity - 0.006f * (0.006f * (posSqrt * posSqrt) + 2 * posY * (velocity * velocity)))) / (0.006f * posSqrt)))
                .toFloat()
        )
        if (silent) setTargetRot(rotation) else limitAngleChange(
            Rotation(player.rotationYaw, player.rotationPitch), rotation, (10 +
                    Random().nextInt(6)).toFloat()
        ).toPlayer(mc.thePlayer)
    }

    /**
     * Translate vec to rotation
     *
     * @param vec target vec
     * @param predict predict new location of your body
     * @return rotation
     */
    fun toRotation(vec: Vec3, predict: Boolean): Rotation {
        val eyesPos = Vec3(
            mc.thePlayer.posX, mc.thePlayer.entityBoundingBox.minY +
                    mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ
        )
        if (predict) eyesPos.addVector(mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ)
        val diffX = vec.xCoord - eyesPos.xCoord
        val diffY = vec.yCoord - eyesPos.yCoord
        val diffZ = vec.zCoord - eyesPos.zCoord
        return Rotation(
            MathHelper.wrapAngleTo180_float(
                Math.toDegrees(atan2(diffZ, diffX)).toFloat() - 90f
            ),
            MathHelper.wrapAngleTo180_float(
                (-Math.toDegrees(
                    atan2(
                        diffY,
                        sqrt(diffX * diffX + diffZ * diffZ)
                    )
                )).toFloat()
            )
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

    fun roundRotation(yaw: Float, strength: Int): Float {
        return (Math.round(yaw / strength) * strength).toFloat()
    }

    /**
     * Search good center
     *
     * @param bb enemy box
     * @param outborder outborder option
     * @param random random option
     * @param predict predict option
     * @param throughWalls throughWalls option
     * @return center
     */
    @JvmOverloads
    fun searchCenter(
        bb: AxisAlignedBB,
        outborder: Boolean,
        random: Boolean,
        predict: Boolean,
        throughWalls: Boolean,
        distance: Float,
        randomMultiply: Float = 0f,
        newRandom: Boolean = false
    ): VecRotation? {
        if (outborder) {
            val vec3 = Vec3(
                bb.minX + (bb.maxX - bb.minX) * (x * 0.3 + 1.0),
                bb.minY + (bb.maxY - bb.minY) * (y * 0.3 + 1.0),
                bb.minZ + (bb.maxZ - bb.minZ) * (z * 0.3 + 1.0)
            )
            return VecRotation(vec3, toRotation(vec3, predict))
        }
        val randomVec = Vec3(
            bb.minX + (bb.maxX - bb.minX) * x * randomMultiply * if (newRandom) Math.random() else 1.0,
            bb.minY + (bb.maxY - bb.minY) * y * randomMultiply * if (newRandom) Math.random() else 1.0,
            bb.minZ + (bb.maxZ - bb.minZ) * z * randomMultiply * if (newRandom) Math.random() else 1.0
        )
        val randomRotation = toRotation(randomVec, predict)
        val eyes = mc.thePlayer.getPositionEyes(1f)
        var vecRotation: VecRotation? = null
        var xSearch = 0.15
        while (xSearch < 0.85) {
            var ySearch = 0.15
            while (ySearch < 1.0) {
                var zSearch = 0.15
                while (zSearch < 0.85) {
                    val vec3 = Vec3(
                        bb.minX + (bb.maxX - bb.minX) * xSearch,
                        bb.minY + (bb.maxY - bb.minY) * ySearch, bb.minZ + (bb.maxZ - bb.minZ) * zSearch
                    )
                    val rotation = toRotation(vec3, predict)
                    val vecDist = eyes.distanceTo(vec3)
                    if (vecDist > distance) {
                        zSearch += 0.1
                        continue
                    }
                    if (throughWalls || isVisible(vec3)) {
                        val currentVec = VecRotation(vec3, rotation)
                        if (vecRotation == null || (if (random) getRotationDifference(
                                currentVec.rotation,
                                randomRotation
                            ) < getRotationDifference(
                                vecRotation.rotation,
                                randomRotation
                            ) else getRotationDifference(currentVec.rotation) < getRotationDifference(vecRotation.rotation))
                        ) vecRotation = currentVec
                    }
                    zSearch += 0.1
                }
                ySearch += 0.1
            }
            xSearch += 0.1
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
     * Calculate difference between the client rotation and your entity's back
     *
     * @param entity your entity
     * @return difference between rotation
     */
    fun getRotationBackDifference(entity: Entity): Double {
        val rotation = toRotation(getCenter(entity.entityBoundingBox), true)
        return getRotationDifference(rotation, Rotation(mc.thePlayer.rotationYaw - 180, mc.thePlayer.rotationPitch))
    }

    /**
     * Calculate difference between the server rotation and your rotation
     *
     * @param rotation your rotation
     * @return difference between rotation
     */
    fun getRotationDifference(rotation: Rotation): Double {
        return if (serverRotation == null) 0.0 else getRotationDifference(rotation, serverRotation)
    }

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
     * Limit your rotation using a turn speed
     *
     * @param currentRotation your current rotation
     * @param targetRotation your goal rotation
     * @param turnSpeed your turn speed
     * @return limited rotation
     */
    fun limitAngleChange(currentRotation: Rotation, targetRotation: Rotation, turnSpeed: Float): Rotation {
        val yawDifference = getAngleDifference(targetRotation.yaw, currentRotation.yaw)
        val pitchDifference = getAngleDifference(targetRotation.pitch, currentRotation.pitch)
        return Rotation(
            currentRotation.yaw + if (yawDifference > turnSpeed) turnSpeed else max(yawDifference, -turnSpeed),
            currentRotation.pitch + if (pitchDifference > turnSpeed) turnSpeed else max(pitchDifference, -turnSpeed)
        )
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
    fun getVectorForRotation(rotation: Rotation): Vec3 {
        val yawCos = MathHelper.cos(-rotation.yaw * 0.017453292f - Math.PI.toFloat())
        val yawSin = MathHelper.sin(-rotation.yaw * 0.017453292f - Math.PI.toFloat())
        val pitchCos = -MathHelper.cos(-rotation.pitch * 0.017453292f)
        val pitchSin = MathHelper.sin(-rotation.pitch * 0.017453292f)
        return Vec3((yawSin * pitchCos).toDouble(), pitchSin.toDouble(), (yawCos * pitchCos).toDouble())
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

    /**
     * Set your target rotation
     *
     * @param rotation your target rotation
     */
    fun setTargetRot(rotation: Rotation) {
        setTargetRot(rotation, 0)
    }

    /**
     * Set your target rotation
     *
     * @param rotation your target rotation
     */
    fun setTargetRot(rotation: Rotation, keepLength: Int) {
        if (java.lang.Double.isNaN(rotation.yaw.toDouble()) || java.lang.Double.isNaN(rotation.pitch.toDouble()) || rotation.pitch > 90 || rotation.pitch < -90) return
        rotation.fixedSensitivity(mc.gameSettings.mouseSensitivity)
        targetRotation = rotation
        this.keepLength = keepLength
    }

    /**
     * Reset your target rotation
     */
    fun reset() {
        keepLength = 0
        targetRotation = null
    }

    fun getRotationsEntity(entity: EntityLivingBase): Rotation {
        return getRotations(entity.posX, entity.posY + entity.eyeHeight - 0.4, entity.posZ)
    }

    fun getRotations(ent: Entity): Rotation {
        val x = ent.posX
        val z = ent.posZ
        val y = ent.posY + (ent.eyeHeight / 2.0f).toDouble()
        return getRotationFromPosition(x, z, y)
    }

    fun getRotations(posX: Double, posY: Double, posZ: Double): Rotation {
        val player = mc.thePlayer
        val x = posX - player.posX
        val y = posY - (player.posY + player.getEyeHeight().toDouble())
        val z = posZ - player.posZ
        val dist = MathHelper.sqrt_double(x * x + z * z).toDouble()
        val yaw = (atan2(z, x) * 180.0 / 3.141592653589793).toFloat() - 90.0f
        val pitch = (-(atan2(y, dist) * 180.0 / 3.141592653589793)).toFloat()
        return Rotation(yaw, pitch)
    }

    fun getRotationFromPosition(x: Double, z: Double, y: Double): Rotation {
        val xDiff = x - mc.thePlayer.posX
        val zDiff = z - mc.thePlayer.posZ
        val yDiff = y - mc.thePlayer.posY - 1.2
        val dist = MathHelper.sqrt_double(xDiff * xDiff + zDiff * zDiff).toDouble()
        val yaw = (atan2(zDiff, xDiff) * 180.0 / Math.PI).toFloat() - 90.0f
        val pitch = (-atan2(yDiff, dist) * 180.0 / Math.PI).toFloat()
        return Rotation(yaw, pitch)
    }

    fun calculate(from: Vec3?, to: Vec3): Rotation {
        val diff = to.subtract(from)
        val distance = hypot(diff.xCoord, diff.zCoord)
        val yaw = (MathHelper.atan2(diff.zCoord, diff.xCoord) * (180f / Math.PI)).toFloat() - 90.0f
        val pitch = (-(MathHelper.atan2(diff.yCoord, distance) * (180f / Math.PI))).toFloat()
        return Rotation(yaw, pitch)
    }

    fun calculate(to: Vec3): Rotation {
        return calculate(
            mc.thePlayer.positionVector.add(Vec3(0.0, mc.thePlayer.getEyeHeight().toDouble(), 0.0)),
            Vec3(to.xCoord, to.yCoord, to.zCoord)
        )
    }

    fun getAngles(entity: Entity?): Rotation? {
        if (entity == null) return null
        val thePlayer = mc.thePlayer
        val diffX = entity.posX - thePlayer.posX
        val diffY = entity.posY + entity.eyeHeight * 0.9 - (thePlayer.posY + thePlayer.getEyeHeight())
        val diffZ = entity.posZ - thePlayer.posZ
        val dist = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ).toDouble() // @on
        val yaw = (atan2(diffZ, diffX) * 180.0 / Math.PI).toFloat() - 90.0f
        val pitch = -(atan2(diffY, dist) * 180.0 / Math.PI).toFloat()
        return Rotation(
            thePlayer.rotationYaw + MathHelper.wrapAngleTo180_float(yaw - thePlayer.rotationYaw),
            thePlayer.rotationPitch + MathHelper.wrapAngleTo180_float(pitch - thePlayer.rotationPitch)
        )
    }

    fun getDirectionToBlock(x: Double, y: Double, z: Double, enumfacing: EnumFacing): Rotation {
        val var4 = EntityEgg(mc.theWorld)
        var4.posX = x + 0.5
        var4.posY = y + 0.5
        var4.posZ = z + 0.5
        var4.posX += enumfacing.directionVec.x.toDouble() * 0.5
        var4.posY += enumfacing.directionVec.y.toDouble() * 0.5
        var4.posZ += enumfacing.directionVec.z.toDouble() * 0.5
        return getRotations(var4.posX, var4.posY, var4.posZ)
    }

    // Vestige
    fun getRotationsToEntity(entity: EntityLivingBase, usePartialTicks: Boolean): Rotation? {
        val partialTicks = mc.timer.renderPartialTicks
        val entityX =
            if (usePartialTicks) entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks else entity.posX
        val entityY =
            if (usePartialTicks) entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks else entity.posY
        val entityZ =
            if (usePartialTicks) entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks else entity.posZ
        val yDiff = mc.thePlayer.posY - entityY
        val finalEntityY =
            if (yDiff >= 0) entityY + entity.eyeHeight else if (-yDiff < mc.thePlayer.getEyeHeight()) mc.thePlayer.posY + mc.thePlayer.getEyeHeight() else entityY
        return getRotationsToPosition(entityX, finalEntityY, entityZ)
    }

    fun getRotationsToPosition(x: Double, y: Double, z: Double): Rotation {
        val deltaX = x - mc.thePlayer.posX
        val deltaY = y - mc.thePlayer.posY - mc.thePlayer.getEyeHeight()
        val deltaZ = z - mc.thePlayer.posZ
        val horizontalDistance = sqrt(deltaX * deltaX + deltaZ * deltaZ)
        val yaw = Math.toDegrees(-atan2(deltaX, deltaZ)).toFloat()
        val pitch = Math.toDegrees(-atan2(deltaY, horizontalDistance)).toFloat()
        return Rotation(yaw, pitch)
    }
    
    fun getVec3(pos: BlockPos, facing: EnumFacing, randomised: Boolean): Vec3 {
        var vec3 = Vec3(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())

        var amount1 = 0.5
        var amount2 = 0.5
        
        if (randomised) {
            amount1 = 0.45 + Math.random() * 0.1
            amount2 = 0.45 + Math.random() * 0.1
        }
        when (facing) {
            EnumFacing.UP -> vec3 = vec3.addVector(amount1, 1.0, amount2)
            EnumFacing.DOWN -> vec3 = vec3.addVector(amount1, 0.0, amount2)
            EnumFacing.EAST -> vec3 = vec3.addVector(1.0, amount1, amount2)
            EnumFacing.WEST -> vec3 = vec3.addVector(0.0, amount1, amount2)
            EnumFacing.NORTH -> vec3 = vec3.addVector(amount1, amount2, 0.0)
            EnumFacing.SOUTH -> vec3 = vec3.addVector(amount1, amount2, 1.0)
        }
        return vec3
    }

    fun predictPlayerMovement(player: EntityPlayer, interval: Float): Vec3 {
        val currentVelocity = Vec3(player.motionX, player.motionY, player.motionZ)
        val velocityChange = calculateVelocityChange(player, interval)
        val predictedVelocity = currentVelocity.add(velocityChange)

        return Vec3(
            player.posX + predictedVelocity.xCoord * interval,
            player.posY + predictedVelocity.yCoord * interval,
            player.posZ + predictedVelocity.zCoord * interval
        )
    }

    fun calculateCenter(smooth: Boolean, randMode: Boolean, randomRange: Double, bb: AxisAlignedBB, predict: Boolean, throughWalls: Boolean): VecRotation? {
        var vecRotation: VecRotation? = null
        var xMin = 0.15
        var xMax = 0.85
        var xDist = 0.1
        var yMin = 0.15
        var yMax = 1.00
        var yDist = 0.1
        var zMin = 0.15
        var zMax = 0.85
        var zDist = 0.1
        var curVec3: Vec3? = null
        if (smooth) {
            xMin = 0.10
            xMax = 0.90
            xDist = 0.1
            yMin = 0.50
            yMax = 0.90
            yDist = 0.1
            zMin = 0.10
            zMax = 0.90
            zDist = 0.1
        }
        var xSearch = xMin
        while (xSearch < xMax) {
            var ySearch = yMin
            while (ySearch < yMax) {
                var zSearch = zMin
                while (zSearch < zMax) {
                    val vec3 = Vec3(
                        bb.minX + (bb.maxX - bb.minX) * xSearch,
                        bb.minY + (bb.maxY - bb.minY) * ySearch,
                        bb.minZ + (bb.maxZ - bb.minZ) * zSearch
                    )
                    val rotation = toRotation(vec3, predict)
                    if (throughWalls || isVisible(vec3)) {
                        val currentVec = VecRotation(vec3, rotation)
                        if (vecRotation == null || getRotationDifference(currentVec.rotation) < getRotationDifference(vecRotation.rotation)) {
                            vecRotation = currentVec
                            curVec3 = vec3
                        }
                    }
                    zSearch += zDist
                }
                ySearch += yDist
            }
            xSearch += xDist
        }
        if (vecRotation == null || !randMode) return vecRotation
        var rand1 = random.nextDouble()
        var rand2 = random.nextDouble()
        var rand3 = random.nextDouble()
        val xRange = bb.maxX - bb.minX
        val yRange = bb.maxY - bb.minY
        val zRange = bb.maxZ - bb.minZ
        var minRange = 999999.0
        if (xRange <= minRange) minRange = xRange
        if (yRange <= minRange) minRange = yRange
        if (zRange <= minRange) minRange = zRange
        rand1 = rand1 * minRange * randomRange
        rand2 = rand2 * minRange * randomRange
        rand3 = rand3 * minRange * randomRange
        val xPrecent = minRange * randomRange / xRange
        val yPrecent = minRange * randomRange / yRange
        val zPrecent = minRange * randomRange / zRange
        val randomVec3 = Vec3(
            curVec3!!.xCoord - xPrecent * (curVec3!!.xCoord - bb.minX) + rand1,
            curVec3!!.yCoord - yPrecent * (curVec3!!.yCoord - bb.minY) + rand2,
            curVec3!!.zCoord - zPrecent * (curVec3!!.zCoord - bb.minZ) + rand3
        )
        val randomRotation = toRotation(randomVec3, predict)
        vecRotation = VecRotation(randomVec3, randomRotation)
        return vecRotation
    }

    private fun calculateVelocityChange(player: EntityPlayer, interval: Float): Vec3 {
        if (mc.thePlayer.movementInput.jump) {
            val jumpVelocity = Vec3(0.0, 0.3, 0.0)
            return jumpVelocity
        } else {
            return Vec3(0.0, 0.0, 0.0)
        }
    }
    fun interpolateRotation(currentRotation: Rotation, targetRotation: Rotation, speed: Float): Rotation {
        val interpolatedYaw = interpolateAngle(currentRotation.yaw, targetRotation.yaw, speed)
        val interpolatedPitch = interpolateAngle(currentRotation.pitch, targetRotation.pitch, speed)

        return Rotation(interpolatedYaw, interpolatedPitch)
    }

    fun interpolateAngle(currentAngle: Float, targetAngle: Float, speed: Float): Float {
        val difference = MathHelper.wrapAngleTo180_double(targetAngle.toDouble() - currentAngle.toDouble()).toFloat()
        val clampedDifference = MathHelper.clamp_double(difference.toDouble(), -speed.toDouble(), speed.toDouble()).toFloat()

        return MathHelper.wrapAngleTo180_float(currentAngle + clampedDifference)
    }
    fun randomizeRotation(rotation: Rotation): Rotation {
        val randomYaw = rotation.yaw + nextFloat(-10F, 10F)
        val randomPitch = rotation.pitch + nextFloat(-5F, 5F)

        return Rotation(randomYaw, randomPitch)
    }
    fun nextFloat(origin: Float, bound: Float): Float {
        return if (origin == bound) origin else ThreadLocalRandom.current().nextDouble(origin.toDouble(), bound.toDouble()).toFloat()
    }
}
