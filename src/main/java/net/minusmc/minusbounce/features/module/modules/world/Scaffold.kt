package net.minusmc.minusbounce.features.module.modules.world

import net.minecraft.block.BlockAir
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.settings.GameSettings
import net.minecraft.entity.passive.EntityPig
import net.minecraft.init.Blocks
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.item.Item
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.stats.StatList
import net.minecraft.util.*
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.injection.access.StaticStorage
import net.minusmc.minusbounce.ui.font.Fonts
import net.minusmc.minusbounce.utils.*
import net.minusmc.minusbounce.utils.block.BlockUtils
import net.minusmc.minusbounce.utils.block.PlaceInfo
import net.minusmc.minusbounce.utils.block.PlaceInfo.Companion.get
import net.minusmc.minusbounce.utils.extensions.rayTraceWithServerSideRotation
import net.minusmc.minusbounce.utils.render.BlurUtils
import net.minusmc.minusbounce.utils.render.RenderUtils
import net.minusmc.minusbounce.utils.timer.MSTimer
import net.minusmc.minusbounce.utils.timer.TimeUtils
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.value.IntegerValue
import net.minusmc.minusbounce.value.ListValue
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.*

@ModuleInfo(name = "Scaffold", description = "Automatically places blocks beneath your feet.", category = ModuleCategory.WORLD, keyBind = Keyboard.KEY_I)
class Scaffold: Module() {
    // add scaffold modes? ex: telly, ninja, fruit, moonwalk?
    private val placeableDelay = ListValue("PlaceableDelay", arrayOf("Normal", "Smart", "Off"), "Normal")
    private val maxDelayValue: IntegerValue = object: IntegerValue("MaxDelay", 0, 0, 1000, "ms", {!placeableDelay.get().equals("off", true)}) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = minDelayValue.get()
            if (i > newValue) {set(i)}
        }
    }

    private val minDelayValue: IntegerValue = object: IntegerValue("MinDelay", 0, 0, 1000, "ms", {!placeableDelay.get().equals("off", true)}) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = maxDelayValue.get()
            if (i < newValue) {set(i)}
        }
    }

    private val autoBlockMode = ListValue("AutoBlock", arrayOf("Spoof", "LiteSpoof", "Switch", "Off"), "Spoof")
    private val sprintModeValue = ListValue("SprintMode", arrayOf("Always", "OnGround", "OffGround", "Legit", "Matrix", "Watchdog", "BlocksMC", "LuckyVN", "Off"), "Off")

    private val swingValue = ListValue("Swing", arrayOf("Normal", "Packet", "Off"), "Normal")
    private val downValue = BoolValue("Down", false)
    private val searchValue = BoolValue("Search", true)
    private val placeModeValue = ListValue("PlaceTiming", arrayOf("Pre", "Post", "Legit"), "Post")

    private val eagleValue = ListValue("Eagle", arrayOf("Normal", "Slient", "Off"), "Off")
    private val blocksToEagleValue = IntegerValue("BlocksToEagle", 0, 0, 10) { !eagleValue.get().equals("Off", true) }
    private val eagleEdgeDistanceValue = FloatValue("EagleEdgeDistance", 0.2F, 0F, 0.5F, "m") {
        !eagleValue.get().equals("Off", true)
    }
    private val expandLengthValue = IntegerValue("ExpandLength", 1, 1, 6, " blocks")

    val rotationsValue = ListValue("Rotation", arrayOf("Normal", "AAC", "Novoline", "Spin", "Intave", "Rise", "Backwards", "Custom", "None"), "Normal")

    private val aacOffsetValue = FloatValue("AAC-Offset", 4f, 0f, 50f, "°") { rotationsValue.get().equals("aac", true) }

    private val customYawValue = FloatValue("Custom-Yaw", 135F, -180F, 180F, "°") {
        rotationsValue.get().equals("custom", true)
    }
    private val customPitchValue = FloatValue("Custom-Pitch", 86F, -90F, 90F, "°") {
        rotationsValue.get().equals("custom", true)
    }

    private val speenSpeedValue = FloatValue("Spin-Speed", 5F, -90F, 90F, "°") {
        rotationsValue.get().equals("spin", true)
    }
    private val speenPitchValue = FloatValue("Spin-Pitch", 90F, -90F, 90F, "°") {
        rotationsValue.get().equals("spin", true)
    }

    private val towerRotationsValue = ListValue("TowerRotation", arrayOf("Normal", "AAC", "Backwards", "None"), "Normal")

    private val maxTurnSpeed: FloatValue = object: FloatValue("MaxTurnSpeed", 180F, 0F, 180F, "°", {!rotationsValue.get().equals("None", true)}) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val i = minTurnSpeed.get()
            if (i > newValue) {set(i)}
        }
    }

    private val minTurnSpeed: FloatValue = object: FloatValue("MinTurnSpeed", 180F, 0F, 180F, "°", {!rotationsValue.get().equals("None", true)}) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val i = maxTurnSpeed.get()
            if (i < newValue) {set(i)}
        }
    }

    private val keepLengthValue = IntegerValue("KeepRotationLength", 0, 0, 20) {
        !rotationsValue.get().equals("None", true)
    }
    private val placeConditionValue = ListValue("PlaceCondition", arrayOf("Always", "Air", "FallDown"), "Always")
    private val rotationStrafeValue = ListValue("RotationStrafe", arrayOf("LiquidBounce", "FDP", "Off"), "LiquidBounce")

    private val zitterModeValue = ListValue("ZitterMode", arrayOf("Teleport", "Smooth", "Off"), "Off")
    private val zitterSpeed = FloatValue("ZitterSpeed", 0.13F, 0.1F, 0.3F) {
        zitterModeValue.get().equals("teleport", true)
    }
    private val zitterStrength = FloatValue("ZitterStrength", 0.072F, 0.05F, 0.2F) {
        zitterModeValue.get().equals("teleport", true)
    }
    private val zitterDelay = IntegerValue("ZitterDelay", 100, 0, 500, "ms") {
        zitterModeValue.get().equals("smooth", true)
    }

    private val timerValue = FloatValue("Timer", 1F, 0.1F, 10F)
    private val speedModifierValue = FloatValue("SpeedModifier", 1F, 0f, 2F, "x")
    private val xzMultiplier = FloatValue("XZ-Multiplier", 1F, 0F, 4F, "x")

    // Tower
    private val onTowerValue = ListValue("OnTower", arrayOf("Always", "PressSpace", "NoMove", "Off"))
    private val towerModeValue = ListValue("TowerMode", arrayOf("Jump", "Motion", "NCP", "MotionTP2", "AAC3.3.9", "AAC3.6.4", "Verus", "Universocraft", "Watchdog"), "Jump") {
        !onTowerValue.get().equals("None", true)
    }

    private val watchdogTowerSpeed = FloatValue("WatchdogTowerSpeed", 100f, 0f, 100f) {towerModeValue.get().equals("Watchdog", true)}
    private val stopWhenBlockAbove = BoolValue("StopWhenBlockAbove", false) { !onTowerValue.get().equals("None", true) }
    private val towerTimerValue = FloatValue("TowerTimer", 1F, 0.1F, 10F) { !onTowerValue.get().equals("None", true) }

    private val sameYValue = ListValue("SameY", arrayOf("Same", "AutoJump", "MotionY", "DelayedTower", "Off"), "Off")
    private val safeWalkValue = ListValue("SafeWalk", arrayOf("Ground", "Air", "Off"), "Off")
    private val hitableCheckValue = BoolValue("HitableCheck", true)
    // Blocks
    private val blocksPerJump = IntegerValue("BlocksPerJump", 5, 0, 10)
    private val allowTntBlock = BoolValue("AllowTntBlock", false)

    private val counterDisplayValue = ListValue("Counter", arrayOf("Simple", "Advanced", "Rise", "Sigma", "Novoline", "Off"), "Simple")
    private val blurValue = BoolValue("Blur-Advanced", false) { counterDisplayValue.get().equals("advanced", true) }
    private val blurStrength = FloatValue("Blur-Strength", 1F, 0F, 30F, "x") {
        counterDisplayValue.get().equals("advanced", true)
    }

    private val markValue = BoolValue("Mark", false)
    private val redValue = IntegerValue("Red", 0, 0, 255) { markValue.get() }
    private val greenValue = IntegerValue("Green", 120, 0, 255) { markValue.get() }
    private val blueValue = IntegerValue("Blue", 255, 0, 255) { markValue.get() }

    private var targetPlace: PlaceInfo? = null

    private var lockRotation: Rotation? = null
    private var speenRotation: Rotation? = null

    // Launch pos
    private var launchY = 0

    // Render thingy
    private var progress = 0f
    private var spinYaw = 0f
    private var lastMS = 0L

    // AutoBlock
    private var slot = -1

    // Zitter
    private var zitterDirection = false

    // Delay
    private val delayTimer = MSTimer()
    private val zitterTimer = MSTimer()
    private var delay = 0L

    // Eagle
    private var placedBlocksWithoutEagle = 0
    private var eagleSneaking = false

    // Down
    private var shouldGoDown = false

    // Verus Tower
    private var verusState = 0
    private var verusJumped = false
    private var offGroundTicks = 0
    private var towerStatus = false

    // Watchdog Tower
    private var watchdogTick = 5
    private var watchdogTowerTick = 0
    private var watchdogC03OnGround = false

    // Same Y
    private var canSameY = false

    private var blocksStart = 0

    private var delayedTowerTicks = 0

    override fun onEnable() {
        mc.thePlayer ?: return

        blocksStart = blocksAmount

        watchdogTick = 5
        delayedTowerTicks = 0

        progress = 0f
        spinYaw = 0f
        launchY = mc.thePlayer.posY.toInt()
        slot = mc.thePlayer.inventory.currentItem

        lastMS = System.currentTimeMillis()
    }

    private fun tower(event: MotionEvent) {
        when (towerModeValue.get().lowercase()) {
            "ncp" -> if (mc.thePlayer.posY % 1 <= 0.00153598) {
                mc.thePlayer.setPosition(mc.thePlayer.posX, floor(mc.thePlayer.posY), mc.thePlayer.posZ)
                mc.thePlayer.motionY = 0.42
            } else if (mc.thePlayer.posY % 1 < 0.1 && offGroundTicks != 0)
                mc.thePlayer.setPosition(mc.thePlayer.posX, floor(mc.thePlayer.posY), mc.thePlayer.posZ)

            "motion" -> if (mc.thePlayer.onGround) {
                fakeJump()
                mc.thePlayer.motionY = 0.42
            } else if (mc.thePlayer.motionY < 0.1) mc.thePlayer.motionY = -0.3

            "jump" -> if (mc.thePlayer.onGround) {
                fakeJump()
                mc.thePlayer.motionY = 0.42
            }

            "motiontp2" -> if (mc.thePlayer.onGround) {
                fakeJump()
                mc.thePlayer.motionY = 0.41999998688698
            } else if (mc.thePlayer.motionY < 0.23) {
                mc.thePlayer.setPosition(mc.thePlayer.posX, truncate(mc.thePlayer.posY), mc.thePlayer.posZ)
                mc.thePlayer.onGround = true
                mc.thePlayer.motionY = 0.41999998688698
            }

            "aac3.6.4" -> if (mc.thePlayer.ticksExisted % 4 == 1) {
                mc.thePlayer.motionY = 0.4195464
                mc.thePlayer.setPosition(mc.thePlayer.posX - 0.035, mc.thePlayer.posY, mc.thePlayer.posZ)
            } else if (mc.thePlayer.ticksExisted % 4 == 0) {
                mc.thePlayer.motionY = -0.5
                mc.thePlayer.setPosition(mc.thePlayer.posX + 0.035, mc.thePlayer.posY, mc.thePlayer.posZ)
            }

            "aac3.3.9" -> {
                if (mc.thePlayer.onGround) {
                    fakeJump()
                    mc.thePlayer.motionY = 0.4001
                }
                mc.timer.timerSpeed = 1f
                if (mc.thePlayer.motionY < 0) {
                    mc.thePlayer.motionY -= 0.00000945
                    mc.timer.timerSpeed = 1.6f
                }
            }

            "verus" -> {
                if (mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.entityBoundingBox.offset(0.0, -0.01, 0.0))
                        .isNotEmpty() && mc.thePlayer.onGround && mc.thePlayer.isCollidedVertically) {
                    verusState = 0
                    verusJumped = true
                }
                if (verusJumped) {
                    MovementUtils.strafe()
                    when (verusState) {
                        0 -> {
                            fakeJump()
                            mc.thePlayer.motionY = 0.41999998688697815
                            ++verusState
                        }

                        1 -> ++verusState
                        2 -> ++verusState
                        3 -> {
                            event.onGround = true
                            mc.thePlayer.motionY = 0.0
                            ++verusState
                        }

                        4 -> ++verusState
                    }
                    verusJumped = false
                }
                verusJumped = true
            }
            "universocraft" -> {
                if (mc.thePlayer.onGround) {
                    fakeJump()
                    mc.thePlayer.motionY = 0.41999998688698
                } else if (mc.thePlayer.motionY < 0.19) {
                    mc.thePlayer.setPosition(mc.thePlayer.posX, truncate(mc.thePlayer.posY), mc.thePlayer.posZ)
                    mc.thePlayer.onGround = true
                    mc.thePlayer.motionY = 0.41999998688698
                }
            }
            "watchdog" -> if (mc.thePlayer.onGround) {
                if (watchdogTowerTick == 0 || watchdogTowerTick == 5) {
                    val f = mc.thePlayer.rotationYaw * (Math.PI.toFloat() / 180)
                    mc.thePlayer.motionX -= sin(f) * 0.2f * watchdogTowerSpeed.get() / 100.0
                    mc.thePlayer.motionY = 0.42
                    mc.thePlayer.motionZ += cos(f) * 0.2f * watchdogTowerSpeed.get() / 100.0
                    watchdogTowerTick = 1
                }
            } else if (mc.thePlayer.motionY > -0.0784000015258789) {
                val n = Math.round(mc.thePlayer.posY % 1.0 * 100.0).toInt()
                when (n) {
                    42 -> mc.thePlayer.motionY = 0.33

                    75 -> {
                        mc.thePlayer.motionY = 1.0 - mc.thePlayer.posY % 1.0
                        watchdogC03OnGround = true
                    }

                    0 -> mc.thePlayer.motionY = -0.0784000015258789
                }
            }
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        // Watchdog tower
        if (towerModeValue.get().equals("watchdog")) {
            if (watchdogTick != 0) {
                watchdogTowerTick = 0
                return
            }

            if (watchdogTowerTick > 0) {
                watchdogTowerTick++
                if (watchdogTowerTick > 6) {
                    val forward = MathHelper.wrapAngleTo180_float(Math.toDegrees(atan2(mc.thePlayer.motionZ, mc.thePlayer.motionX)).toFloat() - 90.0f)
                    MovementUtils.setMotion2(MovementUtils.speed * ((100 - watchdogTowerSpeed.get()) / 100.0), forward)
                }

                if (watchdogTowerTick > 16) {
                    watchdogTowerTick = 0
                }
            }
        }


        if (towerStatus && !towerModeValue.get().equals("aac3.3.9", true)) mc.timer.timerSpeed = towerTimerValue.get()
        if (!towerStatus) mc.timer.timerSpeed = timerValue.get()

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
                "motiony" -> {
                    canSameY = false
                    if (mc.thePlayer.onGround && MovementUtils.isMoving)
                        mc.thePlayer.motionY = 0.42
                }
                "delayedtower" -> {
                    canSameY = delayedTowerTicks % 2 == 0
                    if (mc.thePlayer.onGround && MovementUtils.isMoving) {
                        mc.thePlayer.jump()
                        delayedTowerTicks++
                    }
                }
                else -> canSameY = false
            }

            if (blocksPerJump.get() != 0 && blocksStart - blocksAmount >= blocksPerJump.get()) {
                canSameY = false
                if (mc.thePlayer.onGround && MovementUtils.isMoving) {
                    mc.thePlayer.jump()
                    blocksStart = blocksAmount
                }
            }

            if (mc.thePlayer.onGround)
                launchY = mc.thePlayer.posY.toInt()
        }

        if (blocksStart < blocksAmount)
            blocksStart = blocksAmount

        mc.thePlayer.isSprinting = canSprint

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

        // NCP Tower
        if (mc.thePlayer.onGround) offGroundTicks = 0
        else offGroundTicks++

        // Down
        shouldGoDown = downValue.get() && GameSettings.isKeyDown(mc.gameSettings.keyBindSneak) && blocksAmount > 1
        if (shouldGoDown) mc.gameSettings.keyBindSneak.pressed = false


        if (mc.thePlayer.onGround) {
            // Smooth Zitter
            if (zitterModeValue.equals("smooth")) {
                if (!GameSettings.isKeyDown(mc.gameSettings.keyBindRight)) mc.gameSettings.keyBindRight.pressed = false
                if (!GameSettings.isKeyDown(mc.gameSettings.keyBindLeft)) mc.gameSettings.keyBindLeft.pressed = false
                if (zitterTimer.hasTimePassed(100)) {
                    zitterDirection = !zitterDirection
                    zitterTimer.reset()
                }
                if (zitterDirection) {
                    mc.gameSettings.keyBindRight.pressed = true
                    mc.gameSettings.keyBindLeft.pressed = false
                } else {
                    mc.gameSettings.keyBindRight.pressed = false
                    mc.gameSettings.keyBindLeft.pressed = true
                }
            }

            // Eagle
            if (!eagleValue.get().equals("Off", true) && !shouldGoDown) {
                var dif = 0.5
                val blockPos = BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)

                if (eagleEdgeDistanceValue.get() > 0) {
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
                }
                if (placedBlocksWithoutEagle >= blocksToEagleValue.get()) {
                    val shouldEagle = BlockUtils.isReplaceable(blockPos) || (eagleEdgeDistanceValue.get() > 0 && dif < eagleEdgeDistanceValue.get())

                    if (eagleValue.get().equals("Slient", true)) {
                        if (eagleSneaking != shouldEagle)
                            mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, if (shouldEagle) C0BPacketEntityAction.Action.START_SNEAKING else C0BPacketEntityAction.Action.STOP_SNEAKING))

                        eagleSneaking = shouldEagle
                    } else
                        mc.gameSettings.keyBindSneak.pressed = shouldEagle

                    placedBlocksWithoutEagle = 0
                } else
                    placedBlocksWithoutEagle++
            }

            // Zitter
            if (zitterModeValue.get().equals("teleport", true)) {
                MovementUtils.strafe(zitterSpeed.get())
                val yaw = Math.toRadians(mc.thePlayer.rotationYaw + if (zitterDirection) 90.0 else -90.0)
                mc.thePlayer.motionX -= sin(yaw) * zitterStrength.get()
                mc.thePlayer.motionZ += cos(yaw) * zitterStrength.get()
                zitterDirection = !zitterDirection
            }
        }
        if (placeModeValue.get() == "Legit") place()
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        mc.thePlayer ?: return
        val packet = event.packet

        if (packet is C09PacketHeldItemChange) {
            slot = packet.slotId
        }

        // watchdog tower
        if (towerModeValue.get().equals("watchdog", true) && packet is C03PacketPlayer && watchdogC03OnGround){
            packet.onGround = true
            watchdogC03OnGround = false
        }
    }


    @EventTarget
    fun onMotion(event: MotionEvent) {
        val eventState = event.eventState
        towerStatus = false

        if (towerModeValue.get().equals("watchdog", true))
            if (watchdogTick > 0) watchdogTick--

        // XZ Modifier
        mc.thePlayer.motionX *= xzMultiplier.get().toDouble()
        mc.thePlayer.motionZ *= xzMultiplier.get().toDouble()

        towerStatus = !stopWhenBlockAbove.get() || BlockUtils.getBlock(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + 2, mc.thePlayer.posZ)) is BlockAir

        val isMoving = mc.gameSettings.keyBindLeft.isKeyDown || mc.gameSettings.keyBindRight.isKeyDown || mc.gameSettings.keyBindForward.isKeyDown || mc.gameSettings.keyBindBack.isKeyDown

        if (towerStatus)
            towerStatus = when (onTowerValue.get().lowercase()) {
                "always" -> isMoving
                "nomove" -> !isMoving
                "pressspace" -> mc.gameSettings.keyBindJump.isKeyDown
                else -> false
            }

        if (!towerStatus) {
            verusState = 0
        }

        if (towerStatus) tower(event)


        if (!rotationsValue.get().equals("None", true) && keepLengthValue.get() > 0 && lockRotation != null) {
            if (rotationsValue.get().equals("Spin", true)) {
                spinYaw += speenSpeedValue.get()
                spinYaw = MathHelper.wrapAngleTo180_float(spinYaw)
                speenRotation = Rotation(spinYaw, speenPitchValue.get())
                RotationUtils.setTargetRot(speenRotation!!)
            } else {
                RotationUtils.setTargetRot(RotationUtils.limitAngleChange(RotationUtils.serverRotation!!, lockRotation!!, rotationSpeed), keepLengthValue.get())
            }
        }

        if (eventState == EventState.PRE) {
            if (!placeCondition || if (!autoBlockMode.get().equals("off", true)) InventoryUtils.findAutoBlockBlock() == -1 else mc.thePlayer.heldItem == null || !(mc.thePlayer.heldItem.item is ItemBlock && isBlockToScaffold(mc.thePlayer.heldItem.item as ItemBlock))) {
                return
            }

            findBlock(expandLengthValue.get() > 1 && !towerStatus)
        }

        if (placeModeValue.get().equals(eventState.stateName, true)) place()

        // Placeable delay
        if (targetPlace == null && !placeableDelay.get().equals("Off", true) && !towerStatus) {
            delayTimer.reset()
        }
        
    }

    @EventTarget
    fun onStrafe(event: StrafeEvent) {
        lockRotation ?: return
        when (rotationStrafeValue.get().lowercase()) {
            "liquidbounce" -> {
                val dif = ((MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw - lockRotation!!.yaw - 23.5f - 135) + 180) / 45).toInt()
                val yaw = lockRotation!!.yaw
                val strafe = event.strafe
                val forward = event.forward
                val friction = event.friction
                var calcForward = 0f
                var calcStrafe = 0f
                when (dif) {
                    0 -> {
                        calcForward = forward
                        calcStrafe = strafe
                    }

                    1 -> {
                        calcForward += forward
                        calcStrafe -= forward
                        calcForward += strafe
                        calcStrafe += strafe
                    }

                    2 -> {
                        calcForward = strafe
                        calcStrafe = -forward
                    }

                    3 -> {
                        calcForward -= forward
                        calcStrafe -= forward
                        calcForward += strafe
                        calcStrafe -= strafe
                    }

                    4 -> {
                        calcForward = -forward
                        calcStrafe = -strafe
                    }

                    5 -> {
                        calcForward -= forward
                        calcStrafe += forward
                        calcForward -= strafe
                        calcStrafe -= strafe
                    }

                    6 -> {
                        calcForward = -strafe
                        calcStrafe = forward
                    }

                    7 -> {
                        calcForward += forward
                        calcStrafe += forward
                        calcForward -= strafe
                        calcStrafe += strafe
                    }
                }
                if (calcForward > 1f || calcForward < 0.9f && calcForward > 0.3f || calcForward < -1f || calcForward > -0.9f && calcForward < -0.3f) {
                    calcForward *= 0.5f
                }
                if (calcStrafe > 1f || calcStrafe < 0.9f && calcStrafe > 0.3f || calcStrafe < -1f || calcStrafe > -0.9f && calcStrafe < -0.3f) {
                    calcStrafe *= 0.5f
                }
                var f = calcStrafe * calcStrafe + calcForward * calcForward
                if (f >= 1.0E-4f) {
                    f = MathHelper.sqrt_float(f)
                    if (f < 1.0f) f = 1.0f
                    f = friction / f
                    calcStrafe *= f
                    calcForward *= f
                    val yawSin = sin((yaw * Math.PI / 180f).toFloat())
                    val yawCos = cos((yaw * Math.PI / 180f).toFloat())
                    mc.thePlayer.motionX += (calcStrafe * yawCos - calcForward * yawSin).toDouble()
                    mc.thePlayer.motionZ += (calcForward * yawCos + calcStrafe * yawSin).toDouble()
                }
                event.cancelEvent()
            }
            "fdp" -> {
                val (yaw) = RotationUtils.targetRotation ?: return
                var strafe = event.strafe
                var forward = event.forward
                var friction = event.friction
                var factor = strafe * strafe + forward * forward

                val angleDiff =
                    ((MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw - yaw - 22.5f - 135.0f) + 180.0) / (45.0).toDouble()).toInt()
                //alert("Diff: " + angleDiff + " friction: " + friction + " factor: " + factor);
                val calcYaw = { yaw + 45.0f * angleDiff.toFloat() }.toString().toFloat()

                var calcMoveDir = abs(strafe).coerceAtLeast(abs(forward))
                calcMoveDir *= calcMoveDir
                val calcMultiplier = MathHelper.sqrt_float(calcMoveDir / 1.0f.coerceAtMost(calcMoveDir * 2.0f))

                when (angleDiff) {
                    1, 3, 5, 7, 9 -> {
                        if ((abs(forward) > 0.005 || abs(strafe) > 0.005) && !(abs(forward) > 0.005 && abs(strafe) > 0.005)) {
                            friction /= calcMultiplier
                        } else if (abs(forward) > 0.005 && abs(strafe) > 0.005) {
                            friction *= calcMultiplier
                        }
                    }
                }

                if (factor >= 1.0E-4F) {
                    factor = MathHelper.sqrt_float(factor)

                    if (factor < 1.0F) {
                        factor = 1.0F
                    }

                    factor = friction / factor
                    strafe *= factor
                    forward *= factor

                    val yawSin = sin((calcYaw * Math.PI / 180F).toFloat())
                    val yawCos = cos((calcYaw * Math.PI / 180F).toFloat())

                    mc.thePlayer.motionX += strafe * yawCos - forward * yawSin
                    mc.thePlayer.motionZ += forward * yawCos + strafe * yawSin
                }
                event.cancelEvent()
            }
        }
    }

    private fun findBlock(expand: Boolean) {
        val blockPosition = if (shouldGoDown) {
            if (mc.thePlayer.posY == mc.thePlayer.posY.toInt() + 0.5)
                BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.6, mc.thePlayer.posZ)
            else
                BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.6, mc.thePlayer.posZ).down()
        } else if (mc.thePlayer.posY == mc.thePlayer.posY.toInt() + 0.5 && !canSameY) {
            BlockPos(mc.thePlayer)
        } else if (canSameY && launchY <= mc.thePlayer.posY) {
            BlockPos(mc.thePlayer.posX, launchY - 1.0, mc.thePlayer.posZ)
        } else {
            BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ).down()
        }

        if (!expand && (!BlockUtils.isReplaceable(blockPosition) || search(blockPosition, !shouldGoDown))) return
        if (expand) {
            for (i in 0 until expandLengthValue.get()) {
                val x = if (mc.thePlayer.horizontalFacing == EnumFacing.WEST) -i else if (mc.thePlayer.horizontalFacing == EnumFacing.EAST) i else 0
                val z = if (mc.thePlayer.horizontalFacing == EnumFacing.NORTH) -i else if (mc.thePlayer.horizontalFacing == EnumFacing.SOUTH) i else 0
                if (search(blockPosition.add(x, 0, z), false)) return
            }
        } else if (searchValue.get()) {
            for (x in -1..1) {
                for (z in -1..1) {
                    if (search(blockPosition.add(x, 0, z), !shouldGoDown)) return
                }
            }
        }
    }

    private fun place() {
        if (targetPlace == null) {
            if ((placeableDelay.get().equals("Smart", true) && mc.rightClickDelayTimer > 0) || placeableDelay.get() == "Normal")
                delayTimer.reset()
            return
        }

        if (!towerStatus && (!delayTimer.hasTimePassed(delay) || (canSameY && launchY - 1 != targetPlace!!.vec3.yCoord.toInt())))
            return

        if (!rotationsValue.get().equals("none", true)) {
            val rayTraceInfo = mc.thePlayer.rayTraceWithServerSideRotation(5.0)
            if (rayTraceInfo != null && !rayTraceInfo.blockPos.equals(targetPlace!!.blockPos) && hitableCheckValue.get()) {
                return
            }
        }

        var blockSlot = -1
        var itemStack = mc.thePlayer.heldItem

        if (mc.thePlayer.heldItem == null || !(mc.thePlayer.heldItem.item is ItemBlock && isBlockToScaffold(mc.thePlayer.heldItem.item as ItemBlock))) {
            if (autoBlockMode.get().equals("Off", true)) return

            blockSlot = InventoryUtils.findAutoBlockBlock()
            if (blockSlot == -1) return

            if (autoBlockMode.get().equals("LiteSpoof", true) || autoBlockMode.get().equals("Spoof", true))
                mc.netHandler.addToSendQueue(C09PacketHeldItemChange(blockSlot - 36))
            else
                mc.thePlayer.inventory.currentItem = blockSlot - 36
            itemStack = mc.thePlayer.inventoryContainer.getSlot(blockSlot).stack
        }

        if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, itemStack, targetPlace!!.blockPos, targetPlace!!.enumFacing, targetPlace!!.vec3)) {
            delay = TimeUtils.randomDelay(minDelayValue.get(), maxDelayValue.get())

            if (mc.thePlayer.onGround) {
                val modifier = speedModifierValue.get()
                mc.thePlayer.motionX *= modifier.toDouble()
                mc.thePlayer.motionZ *= modifier.toDouble()
            }

            when (swingValue.get().lowercase()) {
                "normal" -> mc.thePlayer.swingItem()
                "packet" -> mc.netHandler.addToSendQueue(C0APacketAnimation())
            }
        }

        if (autoBlockMode.get().equals("LiteSpoof", true) && blockSlot >= 0) {
            mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
        }

        targetPlace = null

    }

    override fun onDisable() {
        mc.thePlayer ?: return

        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)) {
            mc.gameSettings.keyBindSneak.pressed = false
            if (eagleSneaking) mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING))
        }

        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindRight)) mc.gameSettings.keyBindRight.pressed = false
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindLeft)) mc.gameSettings.keyBindLeft.pressed = false

        lockRotation = null
        mc.timer.timerSpeed = 1f
        shouldGoDown = false

        val limitedRotation = RotationUtils.limitAngleChange(RotationUtils.serverRotation!!,
            Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch), 58f)
        RotationUtils.setTargetRot(limitedRotation, 2)
        if (slot != mc.thePlayer.inventory.currentItem) mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        if (safeWalkValue.get().equals("off", true) || shouldGoDown) return
        if (safeWalkValue.get().equals("air", true) || mc.thePlayer.onGround) event.isSafeWalk = true
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        if (towerStatus) event.cancelEvent()
    }

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        progress = (System.currentTimeMillis() - lastMS).toFloat() / 100F
        if (progress >= 1) progress = 1f

        val scaledResolution = ScaledResolution(mc)
        val info = "$blocksAmount blocks"

        val infoWidth = Fonts.fontSFUI40.getStringWidth(info)
        val infoWidth2 = Fonts.minecraftFont.getStringWidth(blocksAmount.toString())

        when (counterDisplayValue.get().lowercase()) {
            "simple" -> {
                Fonts.minecraftFont.drawString(blocksAmount.toString(), (scaledResolution.scaledWidth / 2 - (infoWidth2 / 2) - 1).toFloat(), (scaledResolution.scaledHeight / 2 - 36).toFloat(), 0xff000000.toInt(), false)
                Fonts.minecraftFont.drawString(blocksAmount.toString(), (scaledResolution.scaledWidth / 2 - (infoWidth2 / 2) + 1).toFloat(), (scaledResolution.scaledHeight / 2 - 36).toFloat(), 0xff000000.toInt(), false)
                Fonts.minecraftFont.drawString(blocksAmount.toString(), (scaledResolution.scaledWidth / 2 - (infoWidth2 / 2)).toFloat(), (scaledResolution.scaledHeight / 2 - 35).toFloat(), 0xff000000.toInt(), false)
                Fonts.minecraftFont.drawString(blocksAmount.toString(), (scaledResolution.scaledWidth / 2 - (infoWidth2 / 2)).toFloat(), (scaledResolution.scaledHeight / 2 - 37).toFloat(), 0xff000000.toInt(), false)
                Fonts.minecraftFont.drawString(blocksAmount.toString(), (scaledResolution.scaledWidth / 2 - (infoWidth2 / 2)).toFloat(), (scaledResolution.scaledHeight / 2 - 36).toFloat(), -1, false)
            }
            "advanced" -> {
                val canRenderStack = slot in 0..8 && mc.thePlayer.inventory.mainInventory[slot] != null && mc.thePlayer.inventory.mainInventory[slot].item != null && mc.thePlayer.inventory.mainInventory[slot].item is ItemBlock
                if (blurValue.get())
                    BlurUtils.blurArea((scaledResolution.scaledWidth / 2 - (infoWidth / 2) - 4).toFloat(), (scaledResolution.scaledHeight / 2 - 39).toFloat(), (scaledResolution.scaledWidth / 2 + (infoWidth / 2) + 4).toFloat(), (scaledResolution.scaledHeight / 2 - (if (canRenderStack) 5 else 26)).toFloat(), blurStrength.get())

                RenderUtils.drawRect((scaledResolution.scaledWidth / 2 - (infoWidth / 2) - 4).toFloat(), (scaledResolution.scaledHeight / 2 - 40).toFloat(), (scaledResolution.scaledWidth / 2 + (infoWidth / 2) + 4).toFloat(), (scaledResolution.scaledHeight / 2 - 39).toFloat(), (if (blocksAmount > 1) 0xffffffff else 0xffff1010).toInt())
                RenderUtils.drawRect((scaledResolution.scaledWidth / 2 - (infoWidth / 2) - 4).toFloat(), (scaledResolution.scaledHeight / 2 - 39).toFloat(), (scaledResolution.scaledWidth / 2 + (infoWidth / 2) + 4).toFloat(), (scaledResolution.scaledHeight / 2 - 26).toFloat(), 0xa0000000.toInt())

                if (canRenderStack) {
                    RenderUtils.drawRect((scaledResolution.scaledWidth / 2 - (infoWidth / 2) - 4).toFloat(), (scaledResolution.scaledHeight / 2 - 26).toFloat(), (scaledResolution.scaledWidth / 2 + (infoWidth / 2) + 4).toFloat(), (scaledResolution.scaledHeight / 2 - 5).toFloat(), 0xa0000000.toInt())
                    GlStateManager.pushMatrix()
                    GlStateManager.translate((scaledResolution.scaledWidth / 2 - 8).toDouble(), (scaledResolution.scaledHeight / 2 - 25).toDouble(), (scaledResolution.scaledWidth / 2 - 8).toDouble())
                    renderItemStack(mc.thePlayer.inventory.mainInventory[slot], 0, 0)
                    GlStateManager.popMatrix()
                }
                GlStateManager.resetColor()

                Fonts.fontSFUI40.drawCenteredString(info, (scaledResolution.scaledWidth / 2).toFloat(), (scaledResolution.scaledHeight / 2 - 36).toFloat(), -1)
            }
            "sigma" -> {
                GlStateManager.translate(0.0, (-14F - (progress * 4F)).toDouble(), 0.0)
                GL11.glEnable(GL11.GL_BLEND)
                GL11.glDisable(GL11.GL_TEXTURE_2D)
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
                GL11.glEnable(GL11.GL_LINE_SMOOTH)
                GL11.glColor4f(0.15F, 0.15F, 0.15F, progress)
                GL11.glBegin(GL11.GL_TRIANGLE_FAN)
                GL11.glVertex2d((scaledResolution.scaledWidth / 2 - 3).toDouble(), (scaledResolution.scaledHeight - 60).toDouble())
                GL11.glVertex2d((scaledResolution.scaledWidth / 2).toDouble(), (scaledResolution.scaledHeight - 57).toDouble())
                GL11.glVertex2d((scaledResolution.scaledWidth / 2 + 3).toDouble(), (scaledResolution.scaledHeight - 60).toDouble())
                GL11.glEnd()
                GL11.glEnable(GL11.GL_TEXTURE_2D)
                GL11.glDisable(GL11.GL_BLEND)
                GL11.glDisable(GL11.GL_LINE_SMOOTH)
                RenderUtils.drawRoundedRect((scaledResolution.scaledWidth / 2 - (infoWidth / 2) - 4).toFloat(), (scaledResolution.scaledHeight - 60).toFloat(), (scaledResolution.scaledWidth / 2 + (infoWidth / 2) + 4).toFloat(), (scaledResolution.scaledHeight - 74).toFloat(), 2F, Color(0.15F, 0.15F, 0.15F, progress).rgb)
                GlStateManager.resetColor()
                Fonts.fontSFUI35.drawCenteredString(info, (scaledResolution.scaledWidth / 2).toFloat() + 0.1F, (scaledResolution.scaledHeight - 70).toFloat(), Color(1F, 1F, 1F, 0.8F * progress).rgb, false)
                GlStateManager.translate(0.0, (14F + (progress * 4F)).toDouble(), 0.0)
            }
            "novoline" -> {
                if (slot in 0..8 && mc.thePlayer.inventory.mainInventory[slot] != null && mc.thePlayer.inventory.mainInventory[slot].item != null && mc.thePlayer.inventory.mainInventory[slot].item is ItemBlock) {
                    GlStateManager.pushMatrix()
                    GlStateManager.translate((scaledResolution.scaledWidth / 2 - 22).toDouble(), (scaledResolution.scaledHeight / 2 + 16).toDouble(), (scaledResolution.scaledWidth / 2 - 22).toDouble())
                    renderItemStack(mc.thePlayer.inventory.mainInventory[slot], 0, 0)
                    GlStateManager.popMatrix()
                }
                GlStateManager.resetColor()

                Fonts.minecraftFont.drawString(info, (scaledResolution.scaledWidth / 2).toFloat(), (scaledResolution.scaledHeight / 2 + 20).toFloat(), -1, true)
            }
            "rise" -> {
                GlStateManager.pushMatrix()
                val info = blocksAmount.toString()
                val slot = InventoryUtils.findAutoBlockBlock()
                val scaledResolution = ScaledResolution(mc)
                val height = scaledResolution.scaledHeight
                val width = scaledResolution.scaledWidth
                val w2 = mc.fontRendererObj.getStringWidth(info)
                RenderUtils.drawRoundedCornerRect((width - w2 - 20) / 2f, height * 0.8f - 24f, (width + w2 + 18) / 2f, height * 0.8f + 12f, 5f, Color(20, 20, 20, 100).rgb)
                var stack = ItemStack(Item.getItemById(166), 0, 0)
                if (slot != -1) {
                    if (mc.thePlayer.inventory.getCurrentItem() != null) {
                        val handItem = mc.thePlayer.inventory.getCurrentItem().item
                        if (handItem is ItemBlock && InventoryUtils.canPlaceBlock(handItem.block)) {
                            stack = mc.thePlayer.inventory.getCurrentItem()
                        }
                    }
                    if (stack == ItemStack(Item.getItemById(166), 0, 0)) {
                        stack = mc.thePlayer.inventory.getStackInSlot(InventoryUtils.findAutoBlockBlock() - 36)
                        if (stack == null) {
                            stack = ItemStack(Item.getItemById(166), 0, 0)
                        }
                    }
                }

                RenderHelper.enableGUIStandardItemLighting()
                GlStateManager.enableBlend()
                mc.renderItem.renderItemIntoGUI(stack, width / 2 - 9, (height * 0.8 - 20).toInt())
                RenderHelper.disableStandardItemLighting()
                mc.fontRendererObj.drawString(info, width / 2f, height * 0.8f, Color(255,255,255).rgb, false)
                GlStateManager.disableAlpha()
                GlStateManager.disableBlend()
                GlStateManager.popMatrix()
            }
        }
    }

    private fun renderItemStack(stack: ItemStack, x: Int, y: Int) {
        GlStateManager.pushMatrix()
        GlStateManager.enableRescaleNormal()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        RenderHelper.enableGUIStandardItemLighting()
        mc.renderItem.renderItemAndEffectIntoGUI(stack, x, y)
        mc.renderItem.renderItemOverlays(mc.fontRendererObj, stack, x, y)
        RenderHelper.disableStandardItemLighting()
        GlStateManager.disableRescaleNormal()
        GlStateManager.disableBlend()
        GlStateManager.popMatrix()
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (!markValue.get()) return
        for (i in 0 until (expandLengthValue.get() + 1)) {
            val blockPos = BlockPos(
                mc.thePlayer.posX + if (mc.thePlayer.horizontalFacing == EnumFacing.WEST) -i else if (mc.thePlayer.horizontalFacing == EnumFacing.EAST) i else 0,
                mc.thePlayer.posY - (if (mc.thePlayer.posY == mc.thePlayer.posY.toInt() + 0.5) { 0.0 } else { 1.0 }) - (if (shouldGoDown) { 1.0 } else { 0.0 }),
                mc.thePlayer.posZ + if (mc.thePlayer.horizontalFacing == EnumFacing.NORTH) -i else if (mc.thePlayer.horizontalFacing == EnumFacing.SOUTH) i else 0
            )
            val placeInfo = get(blockPos)
            if (BlockUtils.isReplaceable(blockPos) && placeInfo != null) {
                RenderUtils.drawBlockBox(blockPos, Color(redValue.get(), greenValue.get(), blueValue.get(), 100), false)
                break
            }
        }
    }

    private fun search(blockPosition: BlockPos, checks: Boolean): Boolean {
        if (!BlockUtils.isReplaceable(blockPosition)) return false

        val eyesPos = Vec3(mc.thePlayer.posX, mc.thePlayer.entityBoundingBox.minY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ)

        var placeRotation: PlaceRotation? = null

        for (side in StaticStorage.facings()) {
            val neighbor = blockPosition.offset(side)
            if (!BlockUtils.canBeClicked(neighbor)) continue

            val dirVec = Vec3(side.directionVec)

            var xSearch = 0.1
            while (xSearch < 0.9) {
                var ySearch = 0.1
                while (ySearch < 0.9) {
                    var zSearch = 0.1
                    while (zSearch < 0.9) {
                        val posVec = Vec3(blockPosition).addVector(xSearch, ySearch, zSearch)
                        val distanceSqPosVec = eyesPos.squareDistanceTo(posVec)
                        val hitVec = posVec.add(Vec3(dirVec.xCoord * 0.5, dirVec.yCoord * 0.5, dirVec.zCoord * 0.5))

                        if (checks && (eyesPos.squareDistanceTo(hitVec) > 18.0 || distanceSqPosVec > eyesPos.squareDistanceTo(posVec.add(dirVec)) || mc.theWorld.rayTraceBlocks(eyesPos, hitVec, false, true, false) != null)) {
                            zSearch += 0.1
                            continue
                        }

                        val diffX = hitVec.xCoord - eyesPos.xCoord
                        val diffY = hitVec.yCoord - eyesPos.yCoord
                        val diffZ = hitVec.zCoord - eyesPos.zCoord
                        val diffXZ = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ).toDouble()
                        val rotation = Rotation(
                            MathHelper.wrapAngleTo180_float(Math.toDegrees(atan2(diffZ, diffX)).toFloat() - 90f),
                            MathHelper.wrapAngleTo180_float((-Math.toDegrees(atan2(diffY, diffXZ))).toFloat())
                        )
                        val rotationVector = RotationUtils.getVectorForRotation(rotation)
                        val vector = eyesPos.addVector(
                            rotationVector.xCoord * 4,
                            rotationVector.yCoord * 4,
                            rotationVector.zCoord * 4
                        )
                        val obj = mc.theWorld.rayTraceBlocks(eyesPos, vector, false, false, true)
                        if (!(obj.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && obj.blockPos == neighbor)) {
                            zSearch += 0.1
                            continue
                        }
                        if (placeRotation == null || RotationUtils.getRotationDifference(rotation) < RotationUtils.getRotationDifference(placeRotation.rotation))
                            placeRotation = PlaceRotation(PlaceInfo(neighbor, side.opposite, hitVec), rotation)

                        zSearch += 0.1
                    }
                    ySearch += 0.1
                }
                xSearch += 0.1
            }
        }

        placeRotation ?: return false

        if (towerRotationsValue.get() != "None" && towerStatus) {
            lockRotation = when (towerRotationsValue.get().lowercase()) {
                "aac" -> Rotation(mc.thePlayer.rotationYaw + (if (mc.thePlayer.movementInput.moveForward < 0) 0 else 180) + aacOffsetValue.get(), placeRotation.rotation.pitch)
                "normal" -> placeRotation.rotation
                "backwards" -> {
                    val calcyaw = ((MovementUtils.movingYaw - 180) / 45).roundToInt() * 45
                    val calcpitch = if (calcyaw % 90 == 0) {
                        82f
                    } else {
                        78f
                    }
                    Rotation(calcyaw.toFloat(), calcpitch)
                }
                else -> return false
            }
            val limitedRotation = RotationUtils.limitAngleChange(RotationUtils.serverRotation!!, lockRotation!!, rotationSpeed)
            RotationUtils.setTargetRot(limitedRotation, keepLengthValue.get())
        }

        if (!rotationsValue.get().equals("None", true) && !towerStatus) {
            lockRotation = when(rotationsValue.get().lowercase()) {
                "custom" -> Rotation(mc.thePlayer.rotationYaw + customYawValue.get(), customPitchValue.get())
                "spin" -> if (speenRotation != null) speenRotation else return false
                "novoline" -> {
                    val blockData = get(blockPosition)
                    val entity = EntityPig(mc.theWorld)
                    if (blockData != null) {
                        entity.posX = blockData.blockPos.x + 0.5
                        entity.posY = blockData.blockPos.y + 0.5
                        entity.posZ = blockData.blockPos.z + 0.5
                    }

                    RotationUtils.getAngles(entity)
                }
                "normal" -> placeRotation.rotation
                "aac" -> Rotation(mc.thePlayer.rotationYaw + (if (mc.thePlayer.movementInput.moveForward < 0) 0 else 180) + aacOffsetValue.get(), placeRotation.rotation.pitch)
                "rise" -> {
                    val blockData = get(blockPosition) ?: return false
                    RotationUtils.getDirectionToBlock(blockData.blockPos.x.toDouble(), blockData.blockPos.y.toDouble(), blockData.blockPos.z.toDouble(), blockData.enumFacing)
                }
                "intave" -> Rotation(mc.thePlayer.rotationYaw + 180, placeRotation.rotation.pitch)
                "backwards" -> {
                    val calcyaw = ((MovementUtils.movingYaw - 180) / 45).roundToInt() * 45
                    val calcpitch = if (calcyaw % 90 == 0) 82f else 78f
                    Rotation(calcyaw.toFloat(), calcpitch)
                }
                else -> return false
            }
            if (rotationsValue.get().equals("Intave", true)) {
                RotationUtils.setTargetRot(lockRotation!!)
            } else if (rotationsValue.get().equals("Normal", true) || (rotationsValue.get().equals("Grim", true) && !mc.thePlayer.onGround)){
                val limitedRotation = RotationUtils.limitAngleChange(RotationUtils.serverRotation!!, lockRotation!!, rotationSpeed)
                RotationUtils.setTargetRot(limitedRotation, keepLengthValue.get())
            } else RotationUtils.setTargetRot(lockRotation!!)
        }

        targetPlace = placeRotation.placeInfo
        return true
    }

    private fun fakeJump() {
        mc.thePlayer.isAirBorne = true
        mc.thePlayer.triggerAchievement(StatList.jumpStat)
    }

    private val blocksAmount: Int
        get() {
            var amount = 0

            for (i in 36..44) {
                val itemStack = mc.thePlayer.inventoryContainer.getSlot(i).stack
                if (itemStack != null && itemStack.item is ItemBlock) {
                    if (isBlockToScaffold(itemStack.item as ItemBlock)) amount += itemStack.stackSize
                }
            }

            return amount
        }

    private fun isBlockToScaffold(itemBlock: ItemBlock): Boolean {
        val block = itemBlock.block
        return (!InventoryUtils.BLOCK_BLACKLIST.contains(block) && block.isFullCube) || (allowTntBlock.get() && block == Blocks.tnt)
    }

    val canSprint: Boolean
        get() = MovementUtils.isMoving && when (sprintModeValue.get().lowercase()) {
            "off" -> false
            "legit" -> mc.thePlayer.ticksExisted % 20 <= 8
            "onground" -> mc.thePlayer.onGround
            "offground" -> !mc.thePlayer.onGround
            else -> true
        }

    private val placeCondition: Boolean
        get() = when (placeConditionValue.get().lowercase()) {
            "always" -> true
            "air" -> !mc.thePlayer.onGround
            "falldown" -> mc.thePlayer.fallDistance > 0f
            else -> false
        }

    private val rotationSpeed: Float
        get() = (Math.random() * (maxTurnSpeed.get() - minTurnSpeed.get()) + minTurnSpeed.get()).toFloat()

    override val tag: String
        get() = if (towerStatus) "Tower, ${towerModeValue.get()}" else placeModeValue.get()
}