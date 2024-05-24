package net.minusmc.minusbounce.features.module.modules.world

import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.UpdateEvent
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.injection.access.StaticStorage
import net.minusmc.minusbounce.utils.block.BlockUtils
import net.minusmc.minusbounce.utils.player.MovementUtils
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.value.IntegerValue
import net.minusmc.minusbounce.value.ListValue
import org.lwjgl.input.Keyboard
import kotlin.math.abs

@ModuleInfo(name = "Scaffold", description = "Automatically places blocks beneath your feet.", category = ModuleCategory.WORLD, keyBind = Keyboard.KEY_I)
class Scaffold: Module() {
    
    private val placeableDelay = BoolValue("PlaceableDelay", true)
    private val maxDelayValue: IntegerValue = object: IntegerValue("MaxDelay", 0, 0, 1000, "ms", {placeableDelay.get()}) {
        override fun onPostChange(oldValue: Int, newValue: Int) {
            val i = minDelayValue.get()
            if (i > newValue) {set(i)}
        }
    }

    private val minDelayValue: IntegerValue = object: IntegerValue("MinDelay", 0, 0, 1000, "ms", {placeableDelay.get()}) {
        override fun onPostChange(oldValue: Int, newValue: Int) {
            val i = maxDelayValue.get()
            if (i < newValue) {set(i)}
        }
    }

    private val sprintModeValue = ListValue("SprintMode", arrayOf("Always", "OnGround", "OffGround", "Legit", "Matrix", "Watchdog", "BlocksMC", "LuckyVN", "Off"), "Off")

    private val swingValue = ListValue("Swing", arrayOf("Normal", "Packet", "Off"), "Normal")
    private val searchValue = ListValue("Search", arrayOf("Area", "Center", "TryRotation"), "Center")

    private val eagleValue = ListValue("Eagle", arrayOf("Normal", "Silent", "Off"), "Off")
    private val blocksToEagleValue = IntegerValue("BlocksToEagle", 0, 0, 10) { !eagleValue.get().equals("Off", true) }
    private val eagleEdgeDistanceValue = FloatValue("EagleEdgeDistance", 0.2F, 0F, 0.5F, "m") {
        !eagleValue.get().equals("Off", true)
    }

    val rotationsValue = ListValue("Rotation", arrayOf("Normal", "Bridge", "None"), "Normal")

    private val maxTurnSpeed: FloatValue = object: FloatValue("MaxTurnSpeed", 180F, 0F, 180F, "°", {!rotationsValue.get().equals("None", true)}) {
        override fun onPostChange(oldValue: Float, newValue: Float) {
            val i = minTurnSpeed.get()
            if (i > newValue) {set(i)}
        }
    }

    private val minTurnSpeed: FloatValue = object: FloatValue("MinTurnSpeed", 180F, 0F, 180F, "°", {!rotationsValue.get().equals("None", true)}) {
        override fun onPostChange(oldValue: Float, newValue: Float) {
            val i = maxTurnSpeed.get()
            if (i < newValue) {set(i)}
        }
    }

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

    private var placedBlocksWithoutEagle = 0
    private var eagleSneaking = false

    private var lastMS = 0L
    private var slot = -1

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
    }

    val canSprint: Boolean
        get() = MovementUtils.isMoving && when (sprintModeValue.get().lowercase()) {
            "off" -> false
            "legit" -> mc.thePlayer.ticksExisted % 20 <= 8
            "onground" -> mc.thePlayer.onGround
            "offground" -> !mc.thePlayer.onGround
            else -> true
        }
}