package net.minusmc.minusbounce.features.module.modules.world

import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.value.*
import net.minusmc.minusbounce.utils.extensions.*
import net.minusmc.minusbounce.utils.*
import net.minusmc.minusbounce.utils.player.RotationUtils
import net.minusmc.minusbounce.utils.misc.RandomUtils
import net.minusmc.minusbounce.utils.timer.MSTimer

import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.Vec3
import net.minecraft.block.BlockAir

import kotlin.math.*

@ModuleInfo(name = "Scaffold2", description = "Goofy ah new scaffold.", category = ModuleCategory.WORLD)
class Scaffold2: Module() {

    private val yawSpeedValue = FloatValue("YawSpeed", 5f, 0f, 180f)
    private val pitchSpeedValue = FloatValue("PitchSpeed", 5f, 0f, 180f)

    private val rayCast = BoolValue("RayCast", false)
    private val playerYaw = BoolValue("PlayerYaw", false)
    private val blockSafe = BoolValue("BlockSafe", false)
    private val latestRotate = BoolValue("LatestRotate", false)

    private val sprint = BoolValue("Sprint", false)
    private val moveFix = BoolValue("MoveFix", false)

    private val startSneak = BoolValue("StartSneak", false)
    private val sneak = BoolValue("Sneak", false)
    private val sneakDelayBool = BoolValue("SneakDelay", false)
    private val sneakDelay = IntegerValue("SneakDelay", 1000, 0, 4000)

    private var rotation = Rotation(0f, 0f)
    private var lastRotation = Rotation(0f, 0f)
    private var blockPos: BlockPos? = null
    private var pos = BlockPos(0f, 0f, 0f)
    private var aimPos: Vec3? = null
    private var playerBlock: Block? = null

    private val startTimeHelper = MSTimer()
    private var movingObjectPos: MovingObjectPosition? = null
    private var lastPositions = mutableListOf<Vec3>()


    override fun onEnable() {
        sneakCounter = 4
        blockCounter = 0

        mc.thePlayer ?: return
        mc.timer.timerSpeed = 1.0f
        rotation = mc.thePlayer.rotation
        lastRotation = mc.thePlayer.prevRotation
        blockPos = null
        startTimeHelper.reset()
    }
    
    override fun onDisable() {
        mc.timer.timerSpeed = 1.0f
        rotation = lastRotation
    }

    @EventTarget
    fun onTick(event: TickEvent) {
        movingObjectPos = null
        blockPos = getBlockPos()

        if (playerYaw.get() && rayCast.get()) {
            if (blockPos != null) {
                if (lastPositions.size > 20)
                    lastPositions.removeFirst()
                
                lastPositions.add(Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ))
                lastRotation = rotation

                rotation = getRotation()

                if (mc.thePlayer.hurtResistantTime > 0 && blockSafe.get())
                    rotation = getRayCastRots()

                if (movingObjectPos != null)
                    aimPos = movingObjectPos.hitVec

            }
            else lastRotation = rotation
        } else if (!rayCast.get()) {
            rotation = mc.thePlayer.rotation
            lastRotation = mc.thePlayer.prevRotation
            blockPos?.let {aimPos = Vec3(it.x + 0.5, it.y + 0.5, it.z + 0.5)}
            
        } else if (rayCast.get() && !playerYaw.get()) {
            if (blockPos != null) {
                if (lastPositions.size > 20)
                    lastPositions.removeFirst()

                lastPositions.add(Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ))
                lastRotation = rotation
                rotation = getRayCastRots()

                if (movingObjectPos != null)
                    aimPos = movingObjectPos.hitVec
            }
            else 
                lastRotation = rotation
        }
        setRotation()

        playerBlock = mc.theWorld.getBlockState(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.5, mc.thePlayer.posZ)).block
    }

    private fun getBlockPos(): BlockPos? {
        val playerPos = BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)
        val positions = mutableListOf<BlockPos>()

        for (x in -5..5)
            for (y in -5..5)
                for (z in -5..5) {
                    val blockPos = BlockPos(playerPos.x + x, playerPos.y + y, playerPos.z + z)
                    if (mc.theWorld.getBlockState(blockPos).block !is BlockAir)
                        positions.add(blockPos)
                }

        val bestBlockPos = positions.minByOrNull {
            val block = mc.theWorld.getBlockState(it).block

            val vecX = mc.thePlayer.posX.coerceIn(it.x, it.x + block.blockBoundsMaxX)
            val vecY = mc.thePlayer.posY.coerceIn(it.y, it.y + block.blockBoundsMaxY)
            val vecZ = mc.thePlayer.posZ.coerceIn(it.z, it.z + block.blockBoundsMaxZ)

            val vec3 = Vec3(vecX, vecY, vecZ)
            mc.thePlayer.getDistanceSq(vec3.xCoord, vec3.yCoord, vec3.zCoord)
        } ?: return null

        return if (towerStatus && bestBlockPos.y != mc.thePlayer.posY - 1.5)
            BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.5, mc.thePlayer.posZ)
        else bestBlockPos
    }

    private fun getRotation(): Rotation {
        var yaw = rotation.yaw
        val yawSpeed = (yawSpeedValue.get() + RandomUtils.nextFloat(0.0f, 15.0f)).coerceIn(0.0f, 180.0f)
        val pitchSpeed = (pitchSpeedValue.get() + RandomUtils.nextFloat(0.0f, 15.0f)).coerceIn(0.0f, 180.0f)
        
        if (towerStatus) {
            val objectPosition = mc.objectMouseOver
            if (objectPosition != null) {
                val pitch = RotationUtils.rotateToPitch(yawSpeed, rots, 90.0f)
                yaw = RotationUtils.rotateToYaw(pitchSpeed, rots, mc.thePlayer.rotationYaw - 180.0f)
                return Rotation(yaw, pitch)
            }
        }

        var calcRotation = Rotation(yaw, rotation.pitch)

        if (mc.thePlayer.motionX == 0.0 && mc.thePlayer.motionZ == 0.0 && mc.thePlayer.onGround) {
            if (startTimeHelper.hasTimePassed(200L))
                startTimeHelper.reset()

            val pitch = RotationUtils.rotateToPitch(pitchSpeed, rots, 80.34f)
            yaw = RotationUtils.rotateToYaw(yawSpeed, rots, mc.thePlayer.rotationYaw - 180.0f)
            calcRotation = Rotation(yaw, pitch)
        }

        val realYaw = mc.thePlayer.rotationYaw
        if (mc.gameSettings.keyBindBack.pressed) {
            realYaw += 180.0f
            if (mc.gameSettings.keyBindLeft.pressed)
                realYaw += 45.0f
            else if (mc.gameSettings.keyBindRight.pressed)
                realYaw -= 45.0f
        }
        else if (mc.gameSettings.keyBindForward.pressed) {
            if (mc.gameSettings.keyBindLeft.pressed)
                realYaw -= 45.0f
            else if (mc.gameSettings.keyBindRight.pressed)
                realYaw += 45.0f
        }
        else if (mc.gameSettings.keyBindRight.pressed)
            realYaw += 90.0f
        else if (mc.gameSettings.keyBindLeft.pressed)
            realYaw -= 90.0f

        yaw = RotationUtils.rotateToYaw(yawSpeed, rotation.pitch, realYaw - 180.0f)
        calcRotation.yaw = yaw

        if (shouldBuild()) {
            val movingObjectPos1 = RaycastUtils.rayCast(1.0f, calcRotation)
            if (movingObjectPos1.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && mc.theWorld.getBlockState(movingObjectPos1.blockPos).block !is BlockAir && 
                isNearbyBlockPos(movingObjectPos1.blockPos) && movingObjectPos1.sideHit != EnumFacing.DOWN && movingObjectPos1.sideHit != EnumFacing.UP) {
                movingObjectPos = movingObjectPos1
                return calcRotation
            }
            val pitchs = mutableListOf<Pair<Float, MovingObjectPosition>>()
            var i = max(rotation.pitch - 30f, -90f)
            while (i < min(rotation.pitch + 20f, 90f)) {
                val (_, sensPitch) = RotationUtils.mouseSens(yaw, i, rotation.yaw, rotation.pitch)
                val objectPos = RaycastUtils.rayCastRotation(1.0f, Rotation(yaw, sensPitch))
                if (objectPos.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && mc.theWorld.getBlockState(objectPos.blockPos).block !is BlockAir && isNearbyBlockPos(objectPos.blockPos) && objectPos.sideHit != EnumFacing.DOWN && objectPos.sideHit != EnumFacing.UP)
                    pitchs.add(Pair(sensPitch, objectPos))
                i += 0.05f
            }

            if (pitchs.isNotEmpty())
                pitchs.minOf {abs(it.first - rotation.pitch)}.run {
                    calcRotation.pitch = it.first
                    movingObjectPos = it.second
                }
            else if (blockSafe.get()) {
                for (yawLoops in 0..179) {
                    val yaw2 = yaw + yawLoops
                    val yaw3 = yaw - yawLoops
                    val currentPitch = rotation.pitch
                    for (pitchLoops in 0..24) {
                        val pitchPredict = (currentPitch + pitchLoops).coerceIn(-90f, 90f)
                        val pitchPredict2 = (currentPitch - pitchLoops).coerceIn(-90f, 90f)
                        val ffff = RotationUtils.mouseSens(yaw3, pitchPredict2, rotation.yaw, rotation.pitch)
                        val m3 = RaycastUtils.rayCast(1.0f, ffff)
                        if (m3.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && mc.theWorld.getBlockState(m3.blockPos).block !is BlockAir && isNearbyBlockPos(m3.blockPos) && m3.sideHit != EnumFacing.DOWN && m3.sideHit != EnumFacing.UP) {
                            movingObjectPos = m3
                            return ffff
                        }
                        val fff = RotationUtils.mouseSens(yaw3, pitchPredict, rotation.yaw, rotation.pitch)
                        val m4 = RaycastUtils.rayCast(1.0f, fff)
                        if (m4.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && mc.theWorld.getBlockState(m4.blockPos).block !is BlockAir && isNearbyBlockPos(m4.blockPos) && m4.sideHit != EnumFacing.DOWN && m4.sideHit != EnumFacing.UP) {
                            movingObjectPos = m4
                            return fff
                        }
                        val ff = RotationUtils.mouseSens(yaw2, pitchPredict2, rotation.yaw, rotation.pitch)
                        val m5 = RaycastUtils.rayCast(1.0f, ff)
                        if (m5.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && mc.theWorld.getBlockState(m5.blockPos).block !is BlockAir && isNearbyBlockPos(m5.blockPos) && m5.sideHit != EnumFacing.DOWN && m5.sideHit != EnumFacing.UP) {
                            movingObjectPos = m5
                            return ff
                        }
                        val f2 = RotationUtils.mouseSens(yaw2, pitchPredict, rotation.yaw, rotation.pitch)
                        val m6 = RaycastUtils.rayCast(1.0f, f2)
                        if (m6.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && mc.theWorld.getBlockState(m6.blockPos).block !is BlockAir && isNearbyBlockPos(m6.blockPos) && m6.sideHit != EnumFacing.DOWN && m6.sideHit != EnumFacing.UP) {
                            movingObjectPos = m6
                            return f2
                        }
                    }
                }
            }
        }
        return calcRotation
    }

    private fun shouldBuild(): Boolean {
        if (latestRotate.get() && rayCast.get()) {
            var x = mc.thePlayer.posX
            var z = mc.thePlayer.posZ
            pos = BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)
            val maxX = blockPos.x + 1.282
            val minX = blockPos.x - 0.282
            val maxZ = blockPos.z + 1.282
            val minZ = blockPos.z - 0.282
            return x > maxX || x < minX || z > maxZ || z < minZ
        }

        val playerPos = BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.5, mc.thePlayer.posZ)
        return mc.theWorld.getBlockState(playerPos).block is BlockAir
    }

    private fun isNearbyBlockPos(blockPos: BlockPos): Boolean {
        if (mc.thePlayer.onGround)
            return blockPos == this.blockPos

        for (x in -1..1)
            for (z in -1..1)
                if (blockPos == BlockPos(blockPos.x + x, blockPos.y, blockPos.z + z))
                    return true

        return false
        
    }
}