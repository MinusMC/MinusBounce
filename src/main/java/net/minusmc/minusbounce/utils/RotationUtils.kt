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
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.utils.RaycastUtils.IEntityFilter
import net.minusmc.minusbounce.utils.RaycastUtils.raycastEntity
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.*


object RotationUtils : MinecraftInstance(), Listenable {
    @JvmField
    var targetRotation: Rotation? = null

    @JvmField
    var serverRotation = Rotation(0f, 0f)

    @JvmField
    var lastRotation = Rotation(0f, 0f)

    @JvmField
    var rotations = Rotation(0f, 0f)

    var rotationSpeed = 0f
    var active = false
    var smoothed = false

    private var x = Random.nextDouble()
    private var y = Random.nextDouble()
    private var z = Random.nextDouble()

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
                    val eyesPos = Vec3(mc.thePlayer.posX, mc.thePlayer.entityBoundingBox.minY + mc.thePlayer.eyeHeight, mc.thePlayer.posZ)
                    val posVec = Vec3(blockPos).addVector(xSearch, ySearch, zSearch)
                    val dist = eyesPos.distanceTo(posVec)
                    val diffX = posVec.xCoord - eyesPos.xCoord
                    val diffY = posVec.yCoord - eyesPos.yCoord
                    val diffZ = posVec.zCoord - eyesPos.zCoord
                    val diffXZ = sqrt(diffX * diffX + diffZ * diffZ).toDouble()
                    val rotation = Rotation(MathHelper.wrapAngleTo180_float(Math.toDegrees(atan2(diffZ, diffX)).toFloat() - 90f), MathHelper.wrapAngleTo180_float(-Math.toDegrees(atan2(diffY, diffXZ)).toFloat()))
                    val rotationVector = getVectorForRotation(rotation)
                    val vector = eyesPos.addVector(rotationVector.xCoord * dist, rotationVector.yCoord * dist,rotationVector.zCoord * dist)
                    val obj = mc.theWorld.rayTraceBlocks(eyesPos, vector, false, false, true)
                    if (obj.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                        val currentVec = VecRotation(posVec, rotation)
                        if (vecRotation == null || getRotationDifference(currentVec.rotation) < getRotationDifference(vecRotation.rotation))
                            vecRotation = currentVec
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
        val posY: Double = target.entityBoundingBox.minY + (if (predict) (target.entityBoundingBox.minY - target.prevPosY) * predictSize else 0.0) + target.eyeHeight - 0.15 - (player.entityBoundingBox.minY + if (predict) player.posY - player.prevPosY else 0.0) - player.eyeHeight
        val posZ: Double = target.posZ + (if (predict) (target.posZ - target.prevPosZ) * predictSize else 0.0) - (player.posZ + if (predict) player.posZ - player.prevPosZ else 0.0)
        val posSqrt = sqrt(posX * posX + posZ * posZ)
        var velocity = player.itemInUseDuration / 20f
        velocity = (velocity * velocity + velocity * 2) / 3
        if (velocity > 1) velocity = 1f
        val rotation = Rotation((atan2(posZ, posX) * 180 / Math.PI).toFloat() - 90, -Math.toDegrees(atan((velocity * velocity - sqrt(velocity * velocity * velocity * velocity - 0.006f * (0.006f * (posSqrt * posSqrt) + 2 * posY * (velocity * velocity)))) / (0.006f * posSqrt))).toFloat())
        if (silent)
            setTargetRot(rotation)
        else 
            limitAngleChange(Rotation(player.rotationYaw, player.rotationPitch), rotation, (10 + Random.nextInt(6)).toFloat()).toPlayer(mc.thePlayer)
    }

    /**
     * Translate vec to rotation
     *
     * @param vec target vec
     * @param predict predict new location of your body
     * @return rotation
     */
    fun toRotation(vec: Vec3, predict: Boolean): Rotation {
        val eyesPos = Vec3(mc.thePlayer.posX, mc.thePlayer.entityBoundingBox.minY + mc.thePlayer.eyeHeight, mc.thePlayer.posZ)
        if (predict) eyesPos.addVector(mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ)
        val diffX = vec.xCoord - eyesPos.xCoord
        val diffY = vec.yCoord - eyesPos.yCoord
        val diffZ = vec.zCoord - eyesPos.zCoord
        return Rotation(
            MathHelper.wrapAngleTo180_float(Math.toDegrees(atan2(diffZ, diffX)).toFloat() - 90f),
            MathHelper.wrapAngleTo180_float((-Math.toDegrees(atan2(diffY,sqrt(diffX * diffX + diffZ * diffZ)))).toFloat())
        )
    }

    fun searchCenter(bb: AxisAlignedBB, predict: Boolean, throughWalls: Boolean, distance: Float): VecRotation? {
        val eyes = mc.thePlayer.getPositionEyes(1f)
        var vecRotation: VecRotation? = null
        var xSearch = 0.15
        while (xSearch < 0.85) {
            var ySearch = 0.15
            while (ySearch < 1.0) {
                var zSearch = 0.15
                while (zSearch < 0.85) {
                    val vec3 = getCustomVec3(bb, xSearch, ySearch, zSearch)
                    val rotation = toRotation(vec3, predict)
                    val vecDist = eyes.distanceTo(vec3)
                    if (vecDist > distance) {
                        zSearch += 0.1
                        continue
                    }
                    if (throughWalls || isVisible(vec3)) {
                        val currentVec = VecRotation(vec3, rotation)
                        if (vecRotation == null || getRotationDifference(currentVec.rotation) < getRotationDifference(vecRotation.rotation))
                            vecRotation = currentVec
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
     * @author aquavit
     *
     * epic skid moment
     */
    fun backTrackRotation(bb: AxisAlignedBB, vec: Vec3, predict: Boolean, throughWalls: Boolean, distance: Float): Rotation {
        val eyesPos = Vec3(mc.thePlayer.posX, mc.thePlayer.entityBoundingBox.minY + mc.thePlayer.eyeHeight, mc.thePlayer.posZ)
        val eyes = mc.thePlayer.getPositionEyes(1f)
        var vecRotation: VecRotation? = null
        var xSearch = 0.15
        while (xSearch < 0.85) {
            var ySearch = 0.15
            while (ySearch < 1.0) {
                var zSearch = 0.15
                while (zSearch < 0.85) {
                    val vec3 = getCustomVec3(bb, xSearch, ySearch, zSearch)
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

        val yaw = MathHelper.wrapAngleTo180_float(Math.toDegrees(atan2(diffZ, diffX)).toFloat() - 90f)
        val pitch = MathHelper.wrapAngleTo180_float((-Math.toDegrees(atan2(diffY,sqrt(diffX * diffX + diffZ * diffZ)))).toFloat())

        return Rotation(yaw, pitch)
    }

    fun ncpRotation(vec: Vec3, predict: Boolean) {
        val eyesPos = Vec3(mc.thePlayer.posX, mc.thePlayer.entityBoundingBox.minY + mc.thePlayer.eyeHeight, mc.thePlayer.posZ)

        if (predict)
            eyesPos.addVector(mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ)

        val diffX = vec.xCoord - eyesPos.xCoord
        val diffY = vec.yCoord - eyesPos.yCoord
        val diffZ = vec.zCoord - eyesPos.zCoord
        val hypotenuse = sqrt(diffX * diffX + diffZ * diffZ)
        return Rotation(
            (atan2(diffZ, diffX) * 180.0 / Math.PI).toFloat() - 90.0f,
            (-atan2(diffY, hypotenuse) * 180.0 / Math.PI).toFloat()
        )
    }

    fun legitRotation(entity: Entity): Rotation {
        return calculate(
            Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.eyeHeight, mc.thePlayer.posZ),
            Vec3(entity.posX, entity.posY + max(0, min(mc.thePlayer.posY - entity.posY + mc.thePlayer.eyeHeight, (entity.entityBoundingBox.maxY - entity.entityBoundingBox.minY) * 0.9)), entity.posZ)
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
        val yawCos = cos(-rotation.yaw * 0.017453292f - Math.PI.toFloat())
        val yawSin = sin(-rotation.yaw * 0.017453292f - Math.PI.toFloat())
        val pitchCos = -cos(-rotation.pitch * 0.017453292f)
        val pitchSin = sin(-rotation.pitch * 0.017453292f)
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
        return raycastEntity(blockReachDistance, object : IEntityFilter {
            override fun canRaycast(entity: Entity?): Boolean = entity === targetEntity
        }) != null
    }

    /**
     * Allows you to check if your enemy is behind a wall
     */
    fun isVisible(vec3: Vec3?): Boolean {
        val eyesPos = Vec3(mc.thePlayer.posX, mc.thePlayer.entityBoundingBox.minY + mc.thePlayer.eyeHeight, mc.thePlayer.posZ)
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

    fun getCustomVec3(bb: AxisAlignedBB, x: Double, y: Double, z: Double): Vec3 {
        return Vec3(bb.minX + (bb.maxX - bb.minX) * x, bb.minY + (bb.maxY - bb.minY) * y, bb.minZ + (bb.maxZ - bb.minZ) * z)
    }

    fun getCenter(bb: AxisAlignedBB): Vec3 {
        return getCustomVec3(bb, 0.5, 0.5, 0.5)
    }

    fun roundRotation(yaw: Float, strength: Int): Float {
        return (Math.round(yaw / strength) * strength).toFloat()
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
        val y = posY - (player.posY + player.eyeHeight.toDouble())
        val z = posZ - player.posZ
        val dist = sqrt(x * x + z * z).toDouble()
        val yaw = (atan2(z, x) * 180.0 / Math.PI).toFloat() - 90.0f
        val pitch = (-(atan2(y, dist) * 180.0 / Math.PI)).toFloat()
        return Rotation(yaw, pitch)
    }

    fun getRotationFromPosition(x: Double, z: Double, y: Double): Rotation {
        val xDiff = x - mc.thePlayer.posX
        val zDiff = z - mc.thePlayer.posZ
        val yDiff = y - mc.thePlayer.posY - 1.2
        val dist = sqrt(xDiff * xDiff + zDiff * zDiff).toDouble()
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
            mc.thePlayer.positionVector.add(Vec3(0.0, mc.thePlayer.eyeHeight.toDouble(), 0.0)),
            Vec3(to.xCoord, to.yCoord, to.zCoord)
        )
    }

    fun getAngles(entity: Entity?): Rotation? {
        if (entity == null) return null
        val thePlayer = mc.thePlayer
        val diffX = entity.posX - thePlayer.posX
        val diffY = entity.posY + entity.eyeHeight * 0.9 - (thePlayer.posY + thePlayer.eyeHeight)
        val diffZ = entity.posZ - thePlayer.posZ
        val dist = sqrt(diffX * diffX + diffZ * diffZ).toDouble() // @on
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

    fun getRotationsToPosition(x: Double, y: Double, z: Double): Rotation {
        val deltaX = x - mc.thePlayer.posX
        val deltaY = y - mc.thePlayer.posY - mc.thePlayer.eyeHeight
        val deltaZ = z - mc.thePlayer.posZ
        val horizontalDistance = sqrt(deltaX * deltaX + deltaZ * deltaZ)
        val yaw = Math.toDegrees(-atan2(deltaX, deltaZ)).toFloat()
        val pitch = Math.toDegrees(-atan2(deltaY, horizontalDistance)).toFloat()
        return Rotation(yaw, pitch)
    }


    fun applySensitivityPatch(rotation: Rotation): Rotation {
        return applySensitivityPatch(rotation, serverRotation)
    }

    fun applySensitivityPatch(rotation: Rotation, old: Rotation): Rotation {
        val previousRotation = old
        val mouseSensitivity = mc.gameSettings.mouseSensitivity * (1.0 + Random.nextDouble() / 10000000.0).toFloat() * 0.6F + 0.2F
        val multiplier = mouseSensitivity * mouseSensitivity * mouseSensitivity * 8.0f * 0.15f
        val yaw = previousRotation.yaw + round((rotation.yaw - previousRotation.yaw) / multiplier) * multiplier)
        val pitch = previousRotation.pitch + round((rotation.pitch - previousRotation.pitch) / multiplier) * multiplier)
        return Rotation(yaw, MathHelper.clamp_float(pitch, -90f, 90f))
    }

    fun resetRotation(rotation: Rotation): Rotation? {
        rotation ?: return null

        val yaw = rotation.yaw + MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw - rotation.yaw)
        val pitch = mc.thePlayer.rotationPitch
        return Rotation(yaw, pitch)
    }


    fun smooth(lastRotation: Rotation, targetRotation: Rotation, speed: Float): Rotation {
        var yaw = targetRotation.yaw
        var pitch = targetRotation.pitch
        val lastYaw = lastRotation.yaw
        val lastPitch = lastRotation.pitch

        if (speed > 0) {
            val yawSpeed = speed
            val pitchSpeed = speed

            val deltaYaw = MathHelper.wrapAngleTo180_float(targetRotation.yaw - lastRotation.yaw)
            val deltaPitch = pitch - lastPitch

            val distance = Math.sqrt(deltaYaw * deltaYaw + deltaPitch * deltaPitch)
            val distributionYaw = Math.abs(deltaYaw / distance)
            val distributionPitch = Math.abs(deltaPitch / distance)

            val maxYaw = yawSpeed * distributionYaw
            val maxPitch = pitchSpeed * distributionPitch

            val moveYaw = max(min(deltaYaw, maxYaw), -maxYaw)
            val movePitch = max(min(deltaPitch, maxPitch), -maxPitch)

            yaw = lastYaw + moveYaw
            pitch = lastPitch + movePitch

            val rangeEnd = Minecraft.debugFPS / 20f + (Random.nextDouble() * 10).toInt()

            for (i in 1..rangeEnd) {
                if (abs(moveYaw) + abs(movePitch) > 1) {
                    yaw += (Random.nextDouble() - 0.5) / 1000
                    pitch -= Random.nextDouble() / 200
                }

                /*
                * Fixing GCD
                */
                val rotations = new Rotation(yaw, pitch)
                val fixedRotations = applySensitivityPatch(rotations)

                /*
                * Setting rotations
                */
                yaw = fixedRotations.yaw
                pitch = max(-90, min(90, fixedRotations.pitch))
            }
        }

        return Rotation(yaw, pitch)
    }


    @EventTarget
    fun onPreUpdate(event: PreUpdateEvent) {
        if(!active || rotations == null || targetRotation == null || lastRotation == null) {
            rotations = Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)
            targetRotation = rotations
            lastRotation = rotations
        }

        if (active)
            smooth()
    }

    @EventTarget
    fun onMoveInput(event: MoveInputEvent) {
        if (active && rotations != null)
            MovementUtils.lbFixMove(event, rotations.yaw)
    }

    // @EventTarget
    // fun onLook(event: LookEvent) {
    //     if (active && rotations != null) {
    //         event.oldRotation = lastRotation
    //         event.currentRotation = rotations
    //     }
    // }

    @EventTarget
    fun onStrafe(event: StrafeEvent) {
        if (active && rotations != null)
            event.yaw = rotations.yaw
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        if (active && rotations != null)
            event.yaw = rotations.yaw
    }

    @EventTarget
    fun onPreMotion(event: PreMotionEvent) { // pre motion instead of motion
        if (active && rotations != null) {
            event.yaw = rotations.yaw
            event.pitch = rotations.pitch

            val diffYaw = rotations.yaw - mc.thePlayer.rotationYaw
            val diffPitch = rotations.pitch - mc.thePlayer.rotationPitch

            if (abs(diffYaw % 360) < 1 && abs(diffPitch) < 1) {
                active = false
                correctDisabledRotations()
            }

            lastRotation = rotations
        } else {
            lastRotation = Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch) 
        }

        targetRotation = Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)
        smoothed = false

        if (random.nextGaussian() > 0.8) x = random.nextDouble()
        if (random.nextGaussian() > 0.8) y = random.nextDouble()
        if (random.nextGaussian() > 0.8) z = random.nextDouble()
    }

    private fun correctDisabledRotations() {
        val rotations = Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)
        val fixedRotations = resetRotation(applySensitivityPatch(rotations, lastRotation))

        mc.thePlayer.rotationYaw = fixedRotations.yaw
        mc.thePlayer.rotationPitch = fixedRotations.pitch
    }

    fun smooth(){
        if (!smoothed)
            rotations = smooth(lastRotation, targetRotation, rotationSpeed)
        smoothed = true

        mc.entityRenderer.getMouseOver(1)
    }

    /**
     * @return YESSSS!!!
     */
    override fun handleEvents() = true
}
