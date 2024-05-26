package net.minusmc.minusbounce.features.module.modules.world

import net.minecraft.client.settings.GameSettings
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.Vec3
import net.minecraft.util.Vec3i
import net.minecraft.init.Blocks
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.item.Item
import net.minecraft.util.MovingObjectPosition
import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.MoveEvent
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.event.UpdateEvent
import net.minusmc.minusbounce.event.PreMotionEvent
import net.minusmc.minusbounce.event.PostMotionEvent
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.injection.access.StaticStorage
import net.minusmc.minusbounce.utils.PlaceRotation
import net.minusmc.minusbounce.utils.misc.RandomUtils
import net.minusmc.minusbounce.utils.timer.MSTimer
import net.minusmc.minusbounce.utils.InventoryUtils
import net.minusmc.minusbounce.utils.RaycastUtils
import net.minusmc.minusbounce.utils.Rotation
import net.minusmc.minusbounce.utils.block.BlockUtils
import net.minusmc.minusbounce.utils.block.PlaceInfo
import net.minusmc.minusbounce.utils.extensions.*
import net.minusmc.minusbounce.utils.player.MovementUtils
import net.minusmc.minusbounce.utils.player.RotationUtils
import net.minusmc.minusbounce.utils.player.MovementCorrection
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.value.IntegerValue
import net.minusmc.minusbounce.value.ListValue
import net.minusmc.minusbounce.value.IntRangeValue
import net.minusmc.minusbounce.value.FloatRangeValue
import org.lwjgl.input.Keyboard
import kotlin.math.abs
import kotlin.math.round

@ModuleInfo(name = "Scaffold", description = "Automatically places blocks beneath your feet.", category = ModuleCategory.WORLD, keyBind = Keyboard.KEY_I)
class Scaffold: Module() {
    
    private val placeableDelay = BoolValue("PlaceableDelay", true)
    private val delayValue = IntRangeValue("Delay", 0, 0, 0, 1000, "ms") {placeableDelay.get()}

    private val sprintModeValue = ListValue("SprintMode", arrayOf("Always", "OnGround", "OffGround", "Legit", "Matrix", "Watchdog", "BlocksMC", "LuckyVN", "Off"), "Off")

    private val swingValue = ListValue("Swing", arrayOf("Normal", "Packet", "Off"), "Normal")
    private val searchValue = ListValue("Search", arrayOf("Area", "Center", "TryRotation"), "Center")

    private val eagleValue = ListValue("Eagle", arrayOf("Normal", "Silent", "Off"), "Off")
    private val blocksToEagleValue = IntegerValue("BlocksToEagle", 0, 0, 10) { !eagleValue.get().equals("Off", true) }
    private val eagleEdgeDistanceValue = FloatValue("EagleEdgeDistance", 0.2F, 0F, 0.5F, "m") {
        !eagleValue.get().equals("Off", true)
    }

    val rotationsValue = ListValue("Rotation", arrayOf("Normal", "Bridge", "None"), "Normal")

    private val turnSpeed = FloatRangeValue("TurnSpeed", 180f, 180f, 0f, 180f, "°") {!rotationsValue.get().equals("None", true)}

    private val keepLengthValue = IntegerValue("KeepRotationLength", 0, 0, 20) {
        !rotationsValue.get().equals("None", true)
    }

    private val movementCorrection = ListValue("MovementCorrection", arrayOf("Strict", "Normal", "Off"), "Off")
    private val zitterModeValue = ListValue("ZitterMode", arrayOf("Breeze", "Moonwalk", "Off"), "Off")

    private val timerValue = FloatValue("Timer", 1F, 0.1F, 10F)
    private val speedModifierValue = FloatValue("SpeedModifier", 1F, 0f, 2F, "x")
    private val xzMultiplier = FloatValue("XZ-Multiplier", 1F, 0F, 4F, "x")

    private val sameYValue = ListValue("SameY", arrayOf("Same", "AutoJump", "Off"), "Off")
    private val safeWalkValue = ListValue("SafeWalk", arrayOf("Ground", "Air", "Off"), "Off")

    private var lockRotation: Rotation? = null
    private var placeInfo: PlaceInfo? = null
    private var placedBlocksWithoutEagle = 0
    private var eagleSneaking = false

    private var lastMS = 0L
    private var slot = -1
    
    private val delayTimer = MSTimer()
    private var delay = 0L

    private var progress = 0f
    private var launchY = 0
    private var canSameY = false
    var towerStatus = false

    override fun onEnable() {
        mc.thePlayer ?: return

        progress = 0f
        launchY = mc.thePlayer.posY.toInt()
        slot = mc.thePlayer.inventory.currentItem

        lastMS = System.currentTimeMillis()
    }

    override fun onDisable() {
        mc.thePlayer ?: return

        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)) {
            mc.gameSettings.keyBindSneak.pressed = false
            if (eagleSneaking)
                mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING))
        }

        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindRight)) mc.gameSettings.keyBindRight.pressed = false
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindLeft)) mc.gameSettings.keyBindLeft.pressed = false

        lockRotation = null
        mc.timer.timerSpeed = 1f

        if (slot != mc.thePlayer.inventory.currentItem)
            mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        mc.timer.timerSpeed = timerValue.get()

        if (towerStatus || mc.thePlayer.isCollidedHorizontally) {
            canSameY = false
            launchY = mc.thePlayer.posY.toInt()
        } else {
            when (sameYValue.get().lowercase()) {
                "simple" -> canSameY = true
                "autojump" -> {
                    canSameY = true
                    if (mc.thePlayer.onGround && MovementUtils.isMoving)
                        mc.thePlayer.jump()
                }
                else -> canSameY = false
            }

            if (mc.thePlayer.onGround)
                launchY = mc.thePlayer.posY.toInt()
        }

        when (sprintModeValue.get().lowercase()) {
            "matrix" -> if (mc.thePlayer.onGround) MovementUtils.setMotion(0.18, false)
            "watchdog" -> {
                mc.thePlayer.motionX *= 0.8
                mc.thePlayer.motionZ *= 0.8
            }

            "blocksmc" -> if (mc.thePlayer.onGround) {
                mc.thePlayer.motionX *= 1.185
                mc.thePlayer.motionZ *= 1.185
            } else {
                mc.thePlayer.motionX *= 0.845
                mc.thePlayer.motionZ *= 0.845
            }

            "luckyvn" -> {
                mc.thePlayer.motionX *= 0.89
                mc.thePlayer.motionZ *= 0.89
            }
        }

        if (!mc.thePlayer.onGround)
            return

        if (!eagleValue.get().equals("Off", true)) {
            var dif = 0.5
            val blockPos = BlockPos(mc.thePlayer).down()

            for (side in StaticStorage.facings()) {
                if (side.axis == EnumFacing.Axis.Y)
                    continue

                val placeInfo = blockPos.offset(side)
                if (BlockUtils.isReplaceable(blockPos)) {
                    var calcDif = if (side.axis == EnumFacing.Axis.Z)
                        abs(placeInfo.z + 0.5 - mc.thePlayer.posZ)
                    else
                        abs(placeInfo.x + 0.5 - mc.thePlayer.posX)

                    calcDif -= 0.5

                    if (calcDif < dif)
                        dif = calcDif
                }
            }

            if (placedBlocksWithoutEagle >= blocksToEagleValue.get()) {
                val shouldEagle = BlockUtils.isReplaceable(blockPos) || (eagleEdgeDistanceValue.get() > 0 && dif < eagleEdgeDistanceValue.get())

                if (eagleValue.get().equals("silent", true)) {
                    if (eagleSneaking != shouldEagle)
                        mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, if (shouldEagle) C0BPacketEntityAction.Action.START_SNEAKING else C0BPacketEntityAction.Action.STOP_SNEAKING))
                } else
                    mc.gameSettings.keyBindSneak.pressed = shouldEagle

                eagleSneaking = shouldEagle

                placedBlocksWithoutEagle = 0
            } else
                placedBlocksWithoutEagle++
        }

//        if (zitterModeValue.get().equals("breeze", true) && MovementUtils.isMoving) {
//            // calc tick movement
//
//        }

        place()
    }

    @EventTarget
    fun onPostMotion(event: PostMotionEvent) {
        if (InventoryUtils.findAutoBlockBlock() == -1 || !(mc.thePlayer.heldItem.item is ItemBlock && isBlockToScaffold(mc.thePlayer.heldItem.item as ItemBlock)))
            return
        
        findBlock()

        val movementCorrectionType = when (movementCorrection.get().lowercase()) {
            "strict" -> MovementCorrection.Type.STRICT
            "normal" -> MovementCorrection.Type.NORMAL
            else -> MovementCorrection.Type.NONE
        }

        lockRotation?.let {
            RotationUtils.setTargetRotation(it, keepLengthValue.get(), turnSpeed.getMinValue(), turnSpeed.getMaxValue(), movementCorrectionType)
        }
    }


    @EventTarget
    fun onPacket(event: PacketEvent) {
        mc.thePlayer ?: return
        val packet = event.packet

        if (packet is C09PacketHeldItemChange) {
            slot = packet.slotId
        }
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        event.isSafeWalk = when (safeWalkValue.get()) {
            "ground" -> mc.thePlayer.onGround
            "air" -> true
            else -> false
        }
    }

    private fun place() {

        val targetPlace = placeInfo ?: run {
            if (placeableDelay.get())
                delayTimer.reset()
            return
        }

        if (!towerStatus && (!delayTimer.hasTimePassed(delay) || (canSameY && launchY - 1 != targetPlace.vec3.yCoord.toInt())))
            return

        var itemStack = mc.thePlayer.heldItem

        if (mc.thePlayer.heldItem == null || !(mc.thePlayer.heldItem.item is ItemBlock && isBlockToScaffold(mc.thePlayer.heldItem.item as ItemBlock))) {
            var blockSlot = InventoryUtils.findAutoBlockBlock()
            if (blockSlot == -1) 
                return

            mc.thePlayer.inventory.currentItem = blockSlot - 36
            itemStack = mc.thePlayer.inventoryContainer.getSlot(blockSlot).stack
        }

        val raytrace = RaycastUtils.performBlockRaytrace(currentRotation, mc.playerController.blockReachDistance) ?: return

        if (raytrace.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK)
            return

        if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, itemStack, raytrace.blockPos, raytrace.sideHit, raytrace.hitVec)) {
            delay = RandomUtils.randomDelay(delayValue.getMinValue(), delayValue.getMaxValue())

            when (swingValue.get().lowercase()) {
                "normal" -> mc.thePlayer.swingItem()
                "packet" -> mc.netHandler.addToSendQueue(C0APacketAnimation())
            }
        }

        placeInfo = null

    }

    private fun findBlock() {
        mc.thePlayer ?: return

        val blockPosition = if (canSameY && launchY <= mc.thePlayer.posY)
            BlockPos(mc.thePlayer.posX, launchY - 1.0, mc.thePlayer.posZ)
        else if (mc.thePlayer.posY == mc.thePlayer.posY.toInt() + 0.5)
            BlockPos(mc.thePlayer)
        else
            BlockPos(mc.thePlayer).down()

        if (!BlockUtils.isReplaceable(blockPosition) || search(blockPosition, searchValue.get()))
            return

        for (x in -1..1) {
            for (z in -1..1) {
                val blockPos = blockPosition.add(Vec3i(x, 0, z))

                if (BlockUtils.canBeClicked(blockPos) || search(blockPos, searchValue.get()))
                    return
            }
        }
    }

    private fun search(blockPosition: BlockPos, searchMode: String): Boolean {
        mc.thePlayer ?: return false

        val playerEye = mc.thePlayer.eyes
        val maxReach = mc.playerController.blockReachDistance

        var placeRotation: PlaceRotation? = null

        for (side in StaticStorage.facings()) {
            val neighborBlock = blockPosition.offset(side)

            if (!BlockUtils.canBeClicked(neighborBlock))
                continue

            when (searchMode.lowercase()) {
                "area" -> for (x in 0.1..0.9) {
                    for (y in 0.1..0.9) {
                        for (z in 0.1..0.9) {
                            val currentPlaceRotation = findTargetPlace(blockPosition, neighborBlock, Vec3(x, y, z), side, playerEye, maxReach) ?: continue

                            if (placeRotation == null || RotationUtils.getRotationDifference(currentPlaceRotation.rotation, currentRotation) < RotationUtils.getRotationDifference(placeRotation.rotation, currentRotation))
                                placeRotation = currentPlaceRotation
                        }
                    }
                }

                "center" -> {
                    val currentPlaceRotation = findTargetPlace(blockPosition, neighborBlock, Vec3(0.5, 0.5, 0.5), side, playerEye, maxReach) ?: continue

                    if (placeRotation == null || RotationUtils.getRotationDifference(currentPlaceRotation.rotation, currentRotation) < RotationUtils.getRotationDifference(placeRotation.rotation, currentRotation))
                        placeRotation = currentPlaceRotation
                }

                "tryrotation" -> {
                    val currentPlaceRotation = findTargetPlaceByRotation(neighborBlock, side, maxReach) ?: continue
                    if (placeRotation == null || RotationUtils.getRotationDifference(currentPlaceRotation.rotation, currentRotation) < RotationUtils.getRotationDifference(placeRotation.rotation, currentRotation))
                        placeRotation = currentPlaceRotation
                }
            }
        }

        placeRotation ?: return false
        
        placeRotation.rotation.fixedSensitivity(mc.gameSettings.mouseSensitivity)

        lockRotation = placeRotation.rotation
        placeInfo = placeRotation.placeInfo

        return true
    }

    /**
     * @author CCBluex
     */

    private fun findTargetPlaceByRotation(neighborBlock: BlockPos, side: EnumFacing, maxReach: Float): PlaceRotation? {
        var placeRotation: PlaceRotation? = null

        RaycastUtils.performBlockRaytrace(currentRotation, maxReach)?.let {
            if (it.blockPos == neighborBlock && it.sideHit == side.opposite && currentRotation.pitch in 55.0..76.7) {
                val placeInfo = PlaceInfo(it.blockPos, side.opposite, modifyVec(it.hitVec, side, Vec3(neighborBlock)))
                return PlaceRotation(placeInfo, currentRotation)
            }
        }

        val yawListPossible = arrayOf(-135f, -45f, 45f, 135f)
        
        for (yaw in yawListPossible) {
            for (pitch in 55.0..76.7 step 0.1) {
                val rotation = Rotation(yaw, pitch.toFloat())
                val raytrace = RaycastUtils.performBlockRaytrace(rotation, maxReach) ?: continue

                val currPlaceRotation = PlaceRotation(PlaceInfo(raytrace.blockPos, raytrace.sideHit, raytrace.hitVec), rotation)

                if (placeRotation == null || RotationUtils.getRotationDifference(rotation, currentRotation) < RotationUtils.getRotationDifference(placeRotation.rotation, currentRotation))
                    placeRotation = currPlaceRotation
            }
        }

        return placeRotation
    }

    private fun findTargetPlace(blockPosition: BlockPos, neighborBlock: BlockPos, vec3: Vec3, side: EnumFacing, eyes: Vec3, maxReach: Float): PlaceRotation? {
        mc.theWorld ?: return null

        val vec = (Vec3(blockPosition) + vec3).addVector(
            side.directionVec.x * vec3.xCoord, side.directionVec.y  * vec3.yCoord, side.directionVec.z * vec3.zCoord
        )

        val distance = eyes.distanceTo(vec)

        if (distance > maxReach || mc.theWorld.rayTraceBlocks(eyes, vec, false, true, false) != null)
            return null

        var rotation = RotationUtils.toRotation(vec, false)

        rotation = when (rotationsValue.get().lowercase()) {
            "bridge" -> Rotation(round(rotation.yaw / 45f) * 45f, rotation.pitch)
            else -> rotation
        }

        rotation.fixedSensitivity(mc.gameSettings.mouseSensitivity)

        // check raytrace
        RaycastUtils.performBlockRaytrace(currentRotation, maxReach)?.let {
            if (it.blockPos == neighborBlock && it.sideHit == side.opposite) {
                val placeInfo = PlaceInfo(it.blockPos, side.opposite, modifyVec(it.hitVec, side, Vec3(neighborBlock)))

                return PlaceRotation(placeInfo, currentRotation)
            }
        }

        val raytrace = RaycastUtils.performBlockRaytrace(rotation, maxReach) ?: return null

        return if (raytrace.blockPos == neighborBlock && raytrace.sideHit == side.opposite) {
            val placeInfo = PlaceInfo(raytrace.blockPos, side.opposite, modifyVec(raytrace.hitVec, side, Vec3(neighborBlock)))
            PlaceRotation(placeInfo, rotation)
        } else null
    }

    private fun modifyVec(original: Vec3, direction: EnumFacing, pos: Vec3): Vec3 {
        val x = original.xCoord
        val y = original.yCoord
        val z = original.zCoord

        val side = direction.opposite

        return when (side.axis ?: return original) {
            EnumFacing.Axis.Y -> Vec3(x, pos.yCoord + side.directionVec.y.coerceAtLeast(0), z)
            EnumFacing.Axis.X -> Vec3(pos.xCoord + side.directionVec.x.coerceAtLeast(0), y, z)
            EnumFacing.Axis.Z -> Vec3(x, y, pos.zCoord + side.directionVec.z.coerceAtLeast(0))
        }

    }

    private fun isBlockToScaffold(itemBlock: ItemBlock): Boolean {
        val block = itemBlock.block
        return !InventoryUtils.BLOCK_BLACKLIST.contains(block) && block.isFullCube
    }

    val canSprint: Boolean
        get() = MovementUtils.isMoving && when (sprintModeValue.get().lowercase()) {
            "off" -> false
            "legit" -> mc.thePlayer.ticksExisted % 20 <= 8
            "onground" -> mc.thePlayer.onGround
            "offground" -> !mc.thePlayer.onGround
            else -> true
        }


    val currentRotation: Rotation
        get() = RotationUtils.currentRotation ?: mc.thePlayer.rotation
}