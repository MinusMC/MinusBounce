package net.minusmc.minusbounce.features.module.modules.world

import net.minecraft.block.Block
import net.minecraft.block.BlockAir
import net.minecraft.client.settings.KeyBinding
import net.minecraft.item.ItemBlock
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.network.play.server.S2FPacketSetSlot
import net.minecraft.potion.Potion
import net.minecraft.util.*
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.injection.access.StaticStorage
import net.minusmc.minusbounce.utils.InventoryUtils
import net.minusmc.minusbounce.utils.Rotation
import net.minusmc.minusbounce.utils.block.BlockUtils
import net.minusmc.minusbounce.utils.block.PlaceInfo
import net.minusmc.minusbounce.utils.extensions.*
import net.minusmc.minusbounce.utils.misc.MathUtils
import net.minusmc.minusbounce.utils.misc.RandomUtils
import net.minusmc.minusbounce.utils.player.MovementCorrection
import net.minusmc.minusbounce.utils.player.MovementUtils
import net.minusmc.minusbounce.utils.player.RotationUtils
import net.minusmc.minusbounce.utils.timer.MSTimer
import net.minusmc.minusbounce.value.*
import java.util.*
import kotlin.math.*


@ModuleInfo("Scaffold2", "Scaffold Two", "Make ur balls floating in the mid-air", ModuleCategory.WORLD)
class Scaffold2: Module(){

    private val modes = ListValue("Mode", arrayOf("Normal", "Snap", "Telly"), "Normal")

    private val delayValue = IntRangeValue("Delay", 0, 0, 0, 10)
    private val sprint = ListValue("Sprint", arrayOf("Normal", "Matrix", "VulcanToggle", "Bypass", "Omni"), "Normal")
    
    private val speed = FloatRangeValue("Speed", 90f, 90f, 0f, 180f)

    private val eagleValue = ListValue("Eagle", arrayOf("Off", "DelayedBlocks", "DelayedTimer"), "Off")
    private val eagleEdgeDistanceValue = FloatRangeValue("EagleEdgeDistance", 0f, 0f, 0f, 0.2f) { !eagleValue.get().equals("off", true) }
    private val eagleBlocksValue = IntegerValue("EagleBlocks", 0, 1, 10) { eagleValue.get().equals("delayedblocks", true) }
    private val eagleDelayValue = IntegerValue("EagleDelay", 0, 0, 20) { eagleValue.get().equals("delayedtimer", true) }
    private val eagleSilent = BoolValue("Silent", false) { !eagleValue.get().equals("Off", true) }
    
    private val towerModeValue = ListValue("Tower", arrayOf("Off", "Vanilla", "Legit", "Matrix", "Vulcan", "Verus", "Air"), "Off")
    private val rayCastValue = ListValue("rayCast", arrayOf("Off", "Normal", "Strict"))
    private val sameYValue = ListValue("SameY", arrayOf("Off", "Same", "AutoJump"), "Off")
    
    private val timer = FloatValue("Timer", 1f, 0f, 5f)
    private val safeWalk = BoolValue("SafeWalk", false)
    private val movementCorrection = BoolValue("MovementCorrection", true)
    
    private val counter = BoolValue("Counter", false)

    /* Values */
    private var targetBlock: Vec3? = null
    private val placeInfo: PlaceInfo? = null
    private val blockPlace: BlockPos? = null
    private var targetYaw = 0f
    private var targetPitch = 0f
    private var ticksOnAir = 0
    private var sneakingTicks = 0
    private var startY = 0.0

    private var placedBlocksWithoutEagle = 0
    private var eagleSneaking = false

    private val eagleDelayTimer = MSTimer()

    var time = 0
    var active = false

    override fun onDisable() {
        mc.gameSettings.keyBindSneak.pressed = false
        mc.gameSettings.keyBindSprint.pressed = false
        active = false
        mc.timer.timerSpeed = 1f
        RotationUtils.setTargetRotation(RotationUtils.serverRotation)
    }

    override fun onEnable() {
        targetYaw = mc.thePlayer.rotationYaw - 180
        targetPitch = 90f

        startY = floor(mc.thePlayer.posY)
        targetBlock = null

        this.sneakingTicks = -1
    }

    /* Init */
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        // Eagle
        if (!eagleValue.equals("Off")) {
            var dif = 0.5
            val blockPos = BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)

            for (facingType in StaticStorage.facings()) {
                if (facingType == EnumFacing.UP || facingType == EnumFacing.DOWN) continue

                val placeInfo = blockPos.offset(facingType)
                if (BlockUtils.isReplaceable(blockPos)) {
                    var calcDif = if (facingType == EnumFacing.NORTH || facingType == EnumFacing.SOUTH)
                        abs(placeInfo.z + 0.5 - mc.thePlayer.posZ)
                    else
                        abs(placeInfo.x + 0.5 - mc.thePlayer.posX)

                    calcDif -= 0.5

                    if (calcDif < dif)
                        dif = calcDif
                }
            }

            val canSneak = when (eagleValue.get()) {
                "DelayedBlocks" -> placedBlocksWithoutEagle >= eagleBlocksValue.get()
                "DelayedTimer" -> eagleDelayTimer.hasTimePassed(eagleDelayValue.get() * 100)
                else -> false
            }

            if (canSneak) {
                val shouldEagle = BlockUtils.isReplaceable(blockPos) || dif < RandomUtils.nextFloat(eagleEdgeDistanceValue.getMinValue(), eagleEdgeDistanceValue.getMaxValue())

                if (eagleSilent.get()) {
                    if (eagleSneaking != shouldEagle)
                        mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, if (shouldEagle) C0BPacketEntityAction.Action.START_SNEAKING else C0BPacketEntityAction.Action.STOP_SNEAKING))

                    eagleSneaking = shouldEagle
                } else
                    mc.gameSettings.keyBindSneak.pressed = shouldEagle

                placedBlocksWithoutEagle = 0
            } else
                placedBlocksWithoutEagle++
        }
    }
    @EventTarget
    fun onMove(event: MoveEvent) {
        event.isSafeWalk = safeWalk.get()
    }

    @EventTarget
    fun onMoveInput(event: MoveInputEvent) {
        if (active) {
            event.sneakMultiplier = 0.5
            mc.gameSettings.keyBindSprint.pressed = false
            mc.thePlayer.isSprinting = false
        }
    }

    @EventTarget(priority = -1)
    fun onPreMotion(event: PreMotionEvent){
        if (targetBlock == null || placeInfo == null || blockPlace == null)
            return

        mc.timer.timerSpeed = timer.get()

        active = false
        when (sprint.get().lowercase()) {
            "normal" -> {
                val rotation = RotationUtils.targetRotation ?: return
                val offset = MathUtils.toRadians(mc.thePlayer.rotationYaw - rotation.yaw)

                val moveForward = mc.thePlayer.moveForward
                val moveStrafing = mc.thePlayer.moveStrafing

                val forward = moveForward * cos(offset) + moveStrafing * sin(offset)

                mc.thePlayer.isSprinting = forward > 0.8F
            }
            "omni" -> mc.thePlayer.isSprinting = true
            "matrix" -> active = true
            "vulcantoggle" -> {
                mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING))
                mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING))
            }
            "bypass" -> if (MovementUtils.isMoving && mc.thePlayer.isSprinting && mc.thePlayer.onGround) {
                val direction = MovementUtils.getRawDirection()
                val x = mc.thePlayer.posX + sin(direction) * 0.221
                val z = mc.thePlayer.posZ - cos(direction) * 0.221
                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, event.y, z, false))
            }
            else -> mc.thePlayer.isSprinting = false
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent){
        val packet = event.packet

        if(packet is C08PacketPlayerBlockPlacement && active && packet.placedBlockDirection != 255){
            time = 0
        }

        if (packet is S2FPacketSetSlot) {
            if (packet.func_149174_e() == null) {
                event.cancelEvent()
                return
            }
            try {
                val slot = packet.func_149173_d() - 36
                if (slot < 0) return
                val itemStack = mc.thePlayer.inventory.getStackInSlot(slot)
                val item = packet.func_149174_e().item

                if ((itemStack == null && packet.func_149174_e().stackSize <= 6 && item is ItemBlock && !InventoryUtils.isBlockListBlock(item.block)) ||
                    (itemStack != null && abs(itemStack.stackSize - packet.func_149174_e().stackSize) <= 6) ||
                    (packet.func_149174_e() == null)
                ) {
                    event.cancelEvent()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    @EventTarget
    fun onPreUpdate(event: PreUpdateEvent){
        if (active) {
            time++;
            mc.gameSettings.keyBindSneak.pressed = time >= 4
        }

        if (BlockUtils.blockRelativeToPlayer(0, -1, 0) is BlockAir)
            ticksOnAir++
        else
            ticksOnAir = 0
    }

    @EventTarget
    fun onStrafe(event: StrafeEvent) {
        if (modes.get().equals("telly", true) && mc.thePlayer.onGround && MovementUtils.isMoving)
            mc.thePlayer.jump()

        if (movementCorrection.get())
            return

        if (mc.gameSettings.keyBindForward.pressed || mc.gameSettings.keyBindBack.pressed ||
            mc.gameSettings.keyBindRight.pressed || mc.gameSettings.keyBindLeft.pressed) {

            val direction = MovementUtils.getRawDirection()

            val groundIncrease = 0.1299999676734952 - 0.12739998266255503 + 1E-7 - 1E-8
            val airIncrease = 0.025999999334873708 - 0.025479999685988748 - 1E-8
            val increase = if (mc.thePlayer.onGround) groundIncrease else airIncrease

            if (MovementUtils.isMoving) {
                mc.thePlayer.motionX += -sin(direction) * increase
                mc.thePlayer.motionZ += cos(direction) * increase
            }
        }
    }

    fun getHitVec(): Vec3? {

        val facing = placeInfo?.enumFacing ?: return null
        val blockPlace = this.blockPlace ?: return

        val x = blockPlace.x + Math.random() + if (facing == EnumFacing.EAST) 1 else 0
        val y = blockPlace.y + Math.random() + if (facing == EnumFacing.UP) 1 else 0
        val z = blockPlace.z + Math.random() + if (facing == EnumFacing.SOUTH) 1 else 0
        val obj = RotationUtils.targetRotation?.let { rayTrace(it) } ?: return null

        return if (obj.blockPos == blockPlace && obj.sideHit == facing)
            obj.hitVec
        else Vec3(x, y, z)
    }

    /**
     * Gets the block relative to the player from the offset
     *
     * @return block relative to the player
     */
    
    fun calculateRotations() {

        val currentRotation = RotationUtils.targetRotation ?: return
        val blockPlace = this.blockPlace ?: return
        val facing = placeInfo?.enumFacing ?: return

        when (modes.get().lowercase()) {
            "normal" -> if (ticksOnAir > 0 && !isObjectMouseOverBlock(currentRotation, blockPlace, facing)) getRotations()
            "snap" -> {
                getRotations()

                if (ticksOnAir <= 0 || isObjectMouseOverBlock(currentRotation, blockPlace, facing))
                    targetYaw = direction
            }

            "telly" -> if (ticksOnAir >= 3) {
                if (!isObjectMouseOverBlock(currentRotation, blockPlace, facing))
                    getRotations()

            } else {
                getRotations()
                targetYaw = mc.thePlayer.rotationYaw
            }
        }

        /* Smoothing rotations */
        RotationUtils.setTargetRotation(
            Rotation(targetYaw, targetPitch),
            4,
            RandomUtils.nextFloat(speed.getMinValue(), speed.getMaxValue()),
            if (movementCorrection.get()) MovementCorrection.Type.STRICT
            else MovementCorrection.Type.NONE
        )
    }

    private fun getRotations() {
        var found = false

        val blockPlace = this.blockPlace ?: return
        val facing = placeInfo?.enumFacing ?: return

        for (possibleYaw in (mc.thePlayer.rotationYaw - 180.0)..(mc.thePlayer.rotationYaw + 180.0) step 45.0) {
            var possiblePitch = 90.0
            while (possiblePitch > 30.0 && !found) {
                if (checks(Rotation(possibleYaw, possiblePitch), blockPlace, facing)) {
                    targetYaw = possibleYaw.toFloat()
                    targetPitch = possiblePitch.toFloat()
                    found = true
                }
                possiblePitch -= if (possiblePitch > (if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) 60 else 80)) 1 else 10
            }
        }

        if (!found) {
            val rotations = RotationUtils.toRotation(Vec3(blockPlace) + 0.5 + Vec3(facing.directionVec) * 0.5)

            targetYaw = rotations.yaw
            targetPitch = rotations.pitch
        }
    }

    private fun isObjectMouseOverBlock(rotation: Rotation, block: BlockPos, facing: EnumFacing, obj: MovingObjectPosition? = rayTrace(rotation)): Boolean{
        obj ?: return false
        return when (rayTrace.get().lowercase()) {
            "normal" -> obj.blockPos == block
            "strict" -> obj.blockPos == block && obj.sideHit == facing
            else -> true
        }
    }

    private fun rayTrace(rotation: Rotation, reach: Double = mc.playerController.blockReachDistance.toDouble()): MovingObjectPosition? {
        val eyesPos = mc.thePlayer.getPositionEyes(1f)
        val lookPoint = mc.thePlayer.getVectorForRotation(rotation.pitch, rotation.yaw)
        val r = Vec3(lookPoint.xCoord * reach, lookPoint.yCoord * reach, lookPoint.zCoord * reach)
        return mc.theWorld.rayTraceBlocks(eyesPos, r, false, false, true)
    }

    /**
     * Render counter (made it after we had done item selector)
     */
//    @EventTarget
//    fun onRender2D(event: Render2DEvent) {
//        val sc = ScaledResolution(mc)
//
//    }

}