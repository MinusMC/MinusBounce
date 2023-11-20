/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.combat

import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.client.settings.KeyBinding
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.item.ItemAxe
import net.minecraft.item.ItemSword
import net.minecraft.network.play.client.*
import net.minecraft.potion.Potion
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3
import net.minecraft.world.WorldSettings
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.features.module.modules.exploit.Disabler
import net.minusmc.minusbounce.features.module.modules.exploit.disablers.other.WatchdogDisabler
import net.minusmc.minusbounce.features.module.modules.movement.TargetStrafe
import net.minusmc.minusbounce.features.module.modules.player.Blink
import net.minusmc.minusbounce.features.module.modules.render.FreeCam
import net.minusmc.minusbounce.features.module.modules.world.Scaffold
import net.minusmc.minusbounce.utils.*
import net.minusmc.minusbounce.utils.EntityUtils.isAlive
import net.minusmc.minusbounce.utils.EntityUtils.isEnemy
import net.minusmc.minusbounce.utils.extensions.getDistanceToEntityBox
import net.minusmc.minusbounce.utils.extensions.getNearestPointBB
import net.minusmc.minusbounce.utils.misc.RandomUtils
import net.minusmc.minusbounce.utils.timer.MSTimer
import net.minusmc.minusbounce.utils.timer.TimeUtils
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.value.IntegerValue
import net.minusmc.minusbounce.value.ListValue
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import java.util.*
import kotlin.math.*

@ModuleInfo(name = "KillAura", spacedName = "Kill Aura", description = "Automatically attacks targets around you.",
        category = ModuleCategory.COMBAT, keyBind = Keyboard.KEY_R)
class KillAura : Module() {

    // CPS - Attack speed
    private val maxCPS: IntegerValue = object : IntegerValue("MaxCPS", 8, 1, 20) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = minCPS.get()
            if (i > newValue) set(i)

            attackDelay = TimeUtils.randomClickDelay(minCPS.get(), this.get())
        }
    }

    private val minCPS: IntegerValue = object : IntegerValue("MinCPS", 5, 1, 20) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = maxCPS.get()
            if (i < newValue) set(i)

            attackDelay = TimeUtils.randomClickDelay(this.get(), maxCPS.get())
        }
    }

    private val hurtTimeValue = IntegerValue("HurtTime", 10, 0, 10)

    // Range
    val rangeValue = FloatValue("Range", 3.7f, 1f, 8f, "m")
    private val throughWallsRangeValue = FloatValue("ThroughWallsRange", 3f, 0f, 8f, "m")

    // Modes
    private val rotations = ListValue("RotationMode", arrayOf("Vanilla", "BackTrack", "NCP", "Grim", "Intave", "Smooth", "None"), "BackTrack")
    private val intaveRandomAmount = FloatValue("RandomAmount", 4f, 0.25f, 10f) { rotations.get().equals("Intave", true) }

    // Turn Speed
    private val maxTurnSpeed: FloatValue = object : FloatValue("MaxTurnSpeed", 180f, 0f, 180f, "°", { !rotations.get().equals("none", true) }) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = minTurnSpeed.get()
            if (v > newValue) set(v)
        }
    }

    private val minTurnSpeed: FloatValue = object : FloatValue("MinTurnSpeed", 180f, 0f, 180f, "°", { !rotations.get().equals("none", true) }) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = maxTurnSpeed.get()
            if (v < newValue) set(v)
        }
    }

    private val noHitCheck = BoolValue("NoHitCheck", false) { !rotations.get().equals("none", true) }
    private val blinkCheck = BoolValue("BlinkCheck", true)

    private val priorityValue = ListValue("Priority", arrayOf("Health", "Distance", "Insane", "LivingTime", "Armor", "HurtResistance", "HurtTime", "HealthAbsorption", "RegenAmplifier"), "Distance")
    val targetModeValue = ListValue("TargetMode", arrayOf("Single", "Switch", "Multi"), "Switch")

    private val switchDelayValue = IntegerValue("SwitchDelay", 1000, 1, 2000, "ms") {
        targetModeValue.get().equals("switch", true)
    }

    // Bypass
    private val swingValue = ListValue("Swing", arrayOf("Normal", "Packet", "None"), "Normal")
    private val keepSprintValue = BoolValue("KeepSprint", true)

    private val attackModeValue = ListValue("AttackTiming", arrayOf("Pre", "Post", "Normal", "All"), "Normal")
    val autoBlockModeValue = ListValue(
        "AutoBlock",
        arrayOf(
            "None",
            "Fake",
            "AfterTick",
            "Vanilla",
            "Packet",
            "Polar",
            "OldIntave",
            "Watchdog",
            "Vulcan",
            "Grim",
            "Verus",
            "RightHold",
            "OldHypixel"
        ),
        "None"
    )

    private val autoBlockRangeValue = FloatValue("AutoBlock-Range", 5f, 0f, 8f, "m") {
        !autoBlockModeValue.get().equals("None", true) && !autoBlockModeValue.get().equals("Fake", true)
    }
    private val interactAutoBlockValue = BoolValue("InteractAutoBlock", true) {
        !autoBlockModeValue.get().equals("None", true) && !autoBlockModeValue.get().equals("Fake", true)
    }
    private val abThruWallValue = BoolValue("AutoBlockThroughWalls", false) {
        !autoBlockModeValue.get().equals("None", true) && !autoBlockModeValue.get().equals("Fake", true)
    }
    private val smartAutoBlockValue = BoolValue("SmartAutoBlock", false) {
        !autoBlockModeValue.get().equals("None", true) && !autoBlockModeValue.get().equals("Fake", true)
    }
    private val blockRate = IntegerValue("BlockRate", 100, 1, 100, "%") {
        !autoBlockModeValue.get().equals("None", true) && !autoBlockModeValue.get().equals("Fake", true)
    }

    // Raycast
    private val raycastValue = BoolValue("RayCast", true)
    private val raycastIgnoredValue = BoolValue("RayCastIgnored", false)
    private val livingRaycastValue = BoolValue("LivingRayCast", true)
    val aacValue = BoolValue("AAC", false)

    private val silentRotationValue = BoolValue("SilentRotation", true) { !rotations.get().equals("none", true) }
    val rotationStrafeValue = ListValue("Strafe", arrayOf("Off", "Strict", "Silent", "Vestige", "FDP"), "Off")
    private val fdpSlientStrafe = BoolValue("Strafe-FDPSlient", true) { rotationStrafeValue.get().equals("FDP", true) }
    private val fovValue = FloatValue("FOV", 180f, 0f, 180f)

    // Predict
    private val predictValue = BoolValue("Predict", true)

    private val maxPredictSize: FloatValue = object : FloatValue("MaxPredictSize", 1f, 0.1f, 5f, { predictValue.get() }) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = minPredictSize.get()
            if (v > newValue) set(v)
        }
    }

    private val minPredictSize: FloatValue = object : FloatValue("MinPredictSize", 1f, 0.1f, 5f, { predictValue.get() }) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = maxPredictSize.get()
            if (v < newValue) set(v)
        }
    }

    private val randomCenterValue = BoolValue("RandomCenter", false) { !rotations.get().equals("none", true) }
    private val randomCenterNewValue = BoolValue("NewCalc", true) {
        !rotations.get().equals("none", true) && randomCenterValue.get()
    }
    private val minRand: FloatValue = object : FloatValue("MinMultiply", 0.8f, 0f, 2f, "x", { !rotations.get().equals("none", true) && randomCenterValue.get() }) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = maxRand.get()
            if (v < newValue) set(v)
        }
    }
    private val maxRand: FloatValue = object : FloatValue("MaxMultiply", 0.8f, 0f, 2f, "x", { !rotations.get().equals("none", true) && randomCenterValue.get() }) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = minRand.get()
            if (v > newValue) set(v)
        }
    }
    private val outborderValue = BoolValue("Outborder", false)

    // Bypass
    private val failRateValue = FloatValue("FailRate", 0f, 0f, 100f)
    private val noInventoryAttackValue = BoolValue("NoInvAttack", false)
    private val noInventoryDelayValue = IntegerValue("NoInvDelay", 200, 0, 500, "ms") { noInventoryAttackValue.get() }
    private val limitedMultiTargetsValue = IntegerValue("LimitedMultiTargets", 0, 0, 50) {
        targetModeValue.get().equals("multi", true)
    }

    // idk
    private val noScaffValue = BoolValue("NoScaffold", true)

    // Visuals
    private val circleValue = BoolValue("Circle", true)
    private val accuracyValue = IntegerValue("Accuracy", 59, 0, 59) { circleValue.get() }
    private val fakeSharpValue = BoolValue("FakeSharp", true)
    private val fakeSharpSword = BoolValue("FakeSharp-SwordOnly", true) { fakeSharpValue.get() }
    private val red = IntegerValue("Red", 255, 0, 255) { circleValue.get() }
    private val green = IntegerValue("Green", 255, 0, 255) { circleValue.get() }
    private val blue = IntegerValue("Blue", 255, 0, 255) { circleValue.get() }
    private val alpha = IntegerValue("Alpha", 255, 0, 255) { circleValue.get() }

    /**
     * MODULE
     */

    // Target
    var target: EntityLivingBase? = null
    private var currentTarget: EntityLivingBase? = null
    var hitable = false
    private val prevTargetEntities = mutableListOf<Int>()

    private var fixedRotation: FixedRotation? = null 

    // Attack delay
    private val attackTimer = MSTimer()
    private var attackDelay = 0L
    private var blockTimer = MSTimer()
    private var clicks = 0
    private var legitBlocking = 0

    // Container Delay
    private var containerOpen = -1L

    // Fake block status
    var blockingStatus = false
    private var verusBlocking = false

    // Rotation
    private var rotSpeed = 15.0

    override fun onEnable() {
        mc.thePlayer ?: return
        mc.theWorld ?: return

        rotSpeed = 15.0

        fixedRotation = FixedRotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)

        updateTarget()
        verusBlocking = false
        legitBlocking = 0
    }

    override fun onDisable() {
        if (target != null && rotations.get().equals("Smooth", true)) {
            mc.thePlayer.rotationYaw = fixedRotation!!.yaw
        }

        target = null
        currentTarget = null
        hitable = false
        prevTargetEntities.clear()
        attackTimer.reset()
        clicks = 0
        stopBlocking()
        mc.gameSettings.keyBindUseItem.pressed = false

        if (verusBlocking && !blockingStatus && !mc.thePlayer.isBlocking) {
            verusBlocking = false
            if (autoBlockModeValue.get().equals("Verus", true))
                PacketUtils.sendPacketNoEvent(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
        }
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if ((attackModeValue.get() == "Pre" && event.eventState == EventState.PRE) || (attackModeValue.get() == "Post" && event.eventState == EventState.POST) || attackModeValue.get() == "All")
            updateKA()

        if (event.eventState == EventState.PRE) {
            if (canBlock && currentTarget != null) {
                startBlocking(currentTarget!!, interactAutoBlockValue.get())
            }
        }

        if (event.eventState == EventState.POST) {
            target ?: return
            currentTarget ?: return

            // Update hitable
            updateHitable()

            // AutoBlock
            if (autoBlockModeValue.get().equals("AfterTick", true) && canBlock)
                startBlocking(currentTarget!!, hitable)

            if (autoBlockModeValue.get().equals("RightHold", true) && canBlock) {
                mc.gameSettings.keyBindUseItem.pressed = true
            }

            if (autoBlockModeValue.get().equals("OldHypixel", true)) {
                when (mc.thePlayer.swingProgressInt) {
                    1 -> stopBlocking()
                    2 -> startBlocking(currentTarget!!, interactAutoBlockValue.get() && mc.thePlayer.getDistanceToEntityBox(currentTarget!!) < maxRange)
                }
            }
        }

        if (rotationStrafeValue.get().equals("Off", true))
            update()
    }

    @EventTarget
    fun onStrafe(event: StrafeEvent) {
        val targetStrafe = MinusBounce.moduleManager.getModule(TargetStrafe::class.java)!!
        if (rotationStrafeValue.get().equals("Off", true) && !targetStrafe.state)
            return

        update()

        if (currentTarget != null && RotationUtils.targetRotation != null) {
            if (targetStrafe.canStrafe) {
                val strafingData = targetStrafe.getData()
                MovementUtils.strafeCustom(MovementUtils.speed, strafingData[0], strafingData[1], strafingData[2])
                event.cancelEvent()
            } else when (rotationStrafeValue.get().lowercase()) {
                "strict" -> {
                    val (yaw) = RotationUtils.targetRotation ?: return
                    var strafe = event.strafe
                    var forward = event.forward
                    val friction = event.friction

                    var f = strafe * strafe + forward * forward

                    if (f >= 1.0E-4F) {
                        f = MathHelper.sqrt_float(f)

                        if (f < 1.0F)
                            f = 1.0F

                        f = friction / f
                        strafe *= f
                        forward *= f

                        val yawSin = MathHelper.sin((yaw * Math.PI / 180F).toFloat())
                        val yawCos = MathHelper.cos((yaw * Math.PI / 180F).toFloat())

                        mc.thePlayer.motionX += strafe * yawCos - forward * yawSin
                        mc.thePlayer.motionZ += forward * yawCos + strafe * yawSin
                    }
                    event.cancelEvent()
                }
                "silent" -> {
                    update()

                    RotationUtils.targetRotation!!.applyStrafeToPlayer(event)
                    event.cancelEvent()
                }
                "vestige" -> {
                    event.yaw = fixedRotation!!.yaw
                    val diff = MathHelper.wrapAngleTo180_float(
                        MathHelper.wrapAngleTo180_float(fixedRotation!!.yaw) - MathHelper.wrapAngleTo180_float(MovementUtils.getPlayerDirection())
                    ) + 22.5F

                    val adjustedDiff = if (diff < 0) 360 + diff else diff
                    val a = (adjustedDiff / 45.0).toInt()

                    val value = if (event.forward != 0f) abs(event.forward) else abs(event.strafe)
                    var forward = value
                    var strafe = 0f

                    for (i in 0 until 8 - a) {
                        val dirs = MovementUtils.incrementMoveDirection(forward, strafe)
                        forward = dirs[0]
                        strafe = dirs[1]
                    }

                    event.forward = forward
                    event.strafe = strafe
                }
                "fdp" -> {
                    val (yaw) = RotationUtils.targetRotation ?: return
                    var strafe = event.strafe
                    var forward = event.forward
                    var friction = event.friction
                    var factor = strafe * strafe + forward * forward
                    
                    val angleDiff = ((MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw - yaw - 22.5f - 135.0f) + 180.0) / (45.0).toDouble()).toInt()
                    val calcYaw = if (fdpSlientStrafe.get()) { yaw + 45.0f * angleDiff.toFloat() } else yaw
                    
                    var calcMoveDir = abs(strafe).coerceAtLeast(abs(forward))
                    calcMoveDir *= calcMoveDir
                    val calcMultiplier = MathHelper.sqrt_float(calcMoveDir / 1.0f.coerceAtMost(calcMoveDir * 2.0f))
                    
                    if (fdpSlientStrafe.get()) {
                        when (angleDiff) {
                            1, 3, 5, 7, 9 -> {
                                if ((abs(forward) > 0.005 || abs(strafe) > 0.005) && !(abs(forward) > 0.005 && abs(strafe) > 0.005)) {
                                    friction /= calcMultiplier
                                } else if (abs(forward) > 0.005 && abs(strafe) > 0.005) {
                                    friction *= calcMultiplier
                                }
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

                        val yawSin = MathHelper.sin((calcYaw * Math.PI / 180F).toFloat())
                        val yawCos = MathHelper.cos((calcYaw * Math.PI / 180F).toFloat())

                        mc.thePlayer.motionX += strafe * yawCos - forward * yawSin
                        mc.thePlayer.motionZ += forward * yawCos + strafe * yawSin
                    }
                    event.cancelEvent()
                }
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (verusBlocking
            && ((packet is C07PacketPlayerDigging
                    && packet.status == C07PacketPlayerDigging.Action.RELEASE_USE_ITEM)
                    || packet is C08PacketPlayerBlockPlacement)
            && autoBlockModeValue.get().equals("Verus", true))
            event.cancelEvent()

        if (packet is C09PacketHeldItemChange)
            verusBlocking = false
    }

    fun update() {
        if (cancelRun || (noInventoryAttackValue.get() && (mc.currentScreen is GuiContainer ||
                    System.currentTimeMillis() - containerOpen < noInventoryDelayValue.get()))
        )
            return

        // Update target
        updateTarget()

        if (target == null) {
            stopBlocking()
            return
        }

        // Target
        currentTarget = target

        if (!targetModeValue.get().equals("Switch", ignoreCase = true) && isEnemy(currentTarget))
            target = currentTarget
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (attackModeValue.get() == "Normal") updateKA()

        if (blockingStatus || mc.thePlayer.isBlocking)
            verusBlocking = true
        else if (verusBlocking) {
            verusBlocking = false
            if (autoBlockModeValue.get().equals("Verus", true))
                PacketUtils.sendPacketNoEvent(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
        }
    }

    private fun updateKA() {
        if (cancelRun) {
            target = null
            currentTarget = null
            hitable = false
            stopBlocking()
            return
        }

        if (noInventoryAttackValue.get() && (mc.currentScreen is GuiContainer ||
                        System.currentTimeMillis() - containerOpen < noInventoryDelayValue.get())) {
            target = null
            currentTarget = null
            hitable = false
            if (mc.currentScreen is GuiContainer) containerOpen = System.currentTimeMillis()
            return
        }

        if (target != null && currentTarget != null) {
            while (clicks > 0) {
                runAttack()
                clicks--
            }
        }
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (circleValue.get()) {
            GL11.glPushMatrix()
            GL11.glTranslated(
                mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * mc.timer.renderPartialTicks - mc.renderManager.renderPosX,
                mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * mc.timer.renderPartialTicks - mc.renderManager.renderPosY,
                mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * mc.timer.renderPartialTicks - mc.renderManager.renderPosZ
            )
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

            GL11.glLineWidth(1F)
            GL11.glColor4f(red.get().toFloat() / 255.0F, green.get().toFloat() / 255.0F, blue.get().toFloat() / 255.0F, alpha.get().toFloat() / 255.0F)
            GL11.glRotatef(90F, 1F, 0F, 0F)
            GL11.glBegin(GL11.GL_LINE_STRIP)

            for (i in 0..360 step 60 - accuracyValue.get()) { // You can change circle accuracy  (60 - accuracy)
                GL11.glVertex2f(cos(i * Math.PI / 180.0).toFloat() * rangeValue.get(), (sin(i * Math.PI / 180.0).toFloat() * rangeValue.get()))
            }

            GL11.glEnd()

            GL11.glDisable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GL11.glDisable(GL11.GL_LINE_SMOOTH)

            GL11.glPopMatrix()
        }

        if (cancelRun) {
            target = null
            currentTarget = null
            hitable = false
            stopBlocking()
            return
        }

        if (noInventoryAttackValue.get() && (mc.currentScreen is GuiContainer ||
                        System.currentTimeMillis() - containerOpen < noInventoryDelayValue.get())) {
            target = null
            currentTarget = null
            hitable = false
            if (mc.currentScreen is GuiContainer) containerOpen = System.currentTimeMillis()
            return
        }

        target ?: return

        if (currentTarget != null && attackTimer.hasTimePassed(attackDelay) &&
                currentTarget!!.hurtTime <= hurtTimeValue.get()) {
            clicks++
            attackTimer.reset()
            attackDelay = TimeUtils.randomClickDelay(minCPS.get(), maxCPS.get())
        }
    }

    @EventTarget
    fun onEntityMove(event: EntityMovementEvent) {
        val movedEntity = event.movedEntity

        if (target == null || movedEntity != currentTarget)
            return

        updateHitable()
    }

    private fun runAttack() {
        target ?: return
        currentTarget ?: return

        // Settings
        val failRate = failRateValue.get()
        val multi = targetModeValue.get().equals("Multi", ignoreCase = true)
        val openInventory = aacValue.get() && mc.currentScreen is GuiInventory
        val failHit = failRate > 0 && Random().nextInt(100) <= failRate

        // Close inventory when open
        if (openInventory)
            mc.netHandler.addToSendQueue(C0DPacketCloseWindow())

        // Check is not hitable or check failrate
        if (!hitable || failHit) {
            runSwing()
        } else {
            // Attack
            if (!multi) {
                attackEntity(currentTarget!!)
            } else {
                var targets = 0

                for (entity in mc.theWorld.loadedEntityList) {
                    val distance = mc.thePlayer.getDistanceToEntityBox(entity)

                    if (entity is EntityLivingBase && isEnemy(entity) && distance <= getRange(entity)) {
                        attackEntity(entity)

                        targets += 1

                        if (limitedMultiTargetsValue.get() != 0 && limitedMultiTargetsValue.get() <= targets)
                            break
                    }
                }
            }

            prevTargetEntities.add(if (aacValue.get()) target!!.entityId else currentTarget!!.entityId)

            if (target == currentTarget)
                target = null
        }

        if (targetModeValue.get().equals("Switch", ignoreCase = true) && attackTimer.hasTimePassed((switchDelayValue.get()).toLong())) {
            if (switchDelayValue.get() != 0) {
                prevTargetEntities.add(if (aacValue.get()) target!!.entityId else currentTarget!!.entityId)
                attackTimer.reset()
            }
        }

        // Open inventory
        if (openInventory)
            mc.netHandler.addToSendQueue(C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT))
    }

    fun runSwing() {
        when (swingValue.get().lowercase()) {
            "normal" -> mc.thePlayer.swingItem()
            "packet" -> mc.netHandler.addToSendQueue(C0APacketAnimation())
        }
    }

    private fun updateTarget() {
        // Settings
        val hurtTime = hurtTimeValue.get()
        val fov = fovValue.get()
        val switchMode = targetModeValue.get().equals("Switch", ignoreCase = true)

        // Find possible targets
        val targets = mutableListOf<EntityLivingBase>()

        for (entity in mc.theWorld.loadedEntityList) {
            if (entity !is EntityLivingBase || !isEnemy(entity) || (switchMode && prevTargetEntities.contains(entity.entityId))/* || (!focusEntityName.isEmpty() && !focusEntityName.contains(entity.name.lowercase()))*/)
                continue

            val distance = mc.thePlayer.getDistanceToEntityBox(entity)
            val entityFov = RotationUtils.getRotationDifference(entity)

            if (distance <= maxRange && (fov == 180F || entityFov <= fov) && entity.hurtTime <= hurtTime)
                targets.add(entity)
        }

        // Sort targets by priority
        when (priorityValue.get().lowercase()) {
            "distance" -> targets.sortBy { mc.thePlayer.getDistanceToEntityBox(it) } // Sort by distance
            "health" -> targets.sortBy { it.health } // Sort by health
            "insane" -> targets.sortBy { RotationUtils.getRotationDifference(it) } // Sort by FOV
            "livingtime" -> targets.sortBy { -it.ticksExisted } // Sort by existence
            "hurtresistance" -> targets.sortBy { it.hurtResistantTime } // Sort by armor hurt time
            "hurttime" -> targets.sortBy { it.hurtTime } // Sort by hurt time
            "healthabsorption" -> targets.sortBy { it.health + it.absorptionAmount } // Sort by full health with absorption effect
            "regenamplifier" -> targets.sortBy { if (it.isPotionActive(Potion.regeneration)) it.getActivePotionEffect(Potion.regeneration).amplifier else -1 }
        }

        var found = false

        // Find best target
        for (entity in targets) {
            // Update rotations to current target
            if (!updateRotations(entity)) // when failed then try another target
                continue

            // Set target to current entity
            target = entity
            found = true
            break
        }
        val tickBase = MinusBounce.moduleManager[TickBase::class.java]!!
        if (found) {
            if (targets.size > 1 && targets[0] == target) {
                tickBase.targetTickBase = targets[1]
            } else {
                tickBase.targetTickBase = targets[0]
            }
            return
        }

        target = null
        tickBase.targetTickBase = null

        // Cleanup last targets when no target found and try again
        if (prevTargetEntities.isNotEmpty()) {
            prevTargetEntities.clear()
            updateTarget()
        }
    }

    private fun attackEntity(entity: EntityLivingBase) {
        // Call attack event
        val event = AttackEvent(entity)
        MinusBounce.eventManager.callEvent(event)
        if (event.isCancelled) return

        preBlocking()

        // Attack target
        runSwing()

        mc.netHandler.addToSendQueue(C02PacketUseEntity(entity, C02PacketUseEntity.Action.ATTACK))

        if (keepSprintValue.get()) {
            if (mc.thePlayer.fallDistance > 0F && !mc.thePlayer.onGround && !mc.thePlayer.isOnLadder && !mc.thePlayer.isInWater && !mc.thePlayer.isPotionActive(Potion.blindness) && !mc.thePlayer.isRiding)
                mc.thePlayer.onCriticalHit(entity)

            // Enchant Effect
            if (EnchantmentHelper.getModifierForCreature(mc.thePlayer.heldItem, entity.creatureAttribute) > 0F)
                mc.thePlayer.onEnchantmentCritical(entity)
        } else {
            if (mc.playerController.currentGameType != WorldSettings.GameType.SPECTATOR)
                mc.thePlayer.attackTargetEntityWithCurrentItem(entity)
        }

        val criticals = MinusBounce.moduleManager[Criticals::class.java] as Criticals

        for (i in 0..2) {
            // Critical Effect
            if (mc.thePlayer.fallDistance > 0F && !mc.thePlayer.onGround && !mc.thePlayer.isOnLadder && !mc.thePlayer.isInWater && !mc.thePlayer.isPotionActive(Potion.blindness) && mc.thePlayer.ridingEntity == null || criticals.state && criticals.msTimer.hasTimePassed(criticals.delayValue.get().toLong()) && !mc.thePlayer.isInWater && !mc.thePlayer.isInLava && !mc.thePlayer.isInWeb)
                mc.thePlayer.onCriticalHit(target)

            // Enchant Effect
            if (EnchantmentHelper.getModifierForCreature(mc.thePlayer.heldItem, target!!.creatureAttribute) > 0.0f || (fakeSharpValue.get() && (!fakeSharpSword.get() || canBlock)))
                mc.thePlayer.onEnchantmentCritical(target)
        }

        postBlocking(entity)
    }

    private fun updateRotations(entity: Entity): Boolean {
        if (rotations.get().equals("none", true)) return true

        val disabler = MinusBounce.moduleManager[Disabler::class.java]!!
        val watchdogDisabler = disabler.modes.find { it.modeName.equals("Watchdog", true) } as WatchdogDisabler

        if (watchdogDisabler.canModifyRotation) return true

        val defRotation = getTargetRotation(entity) ?: return false
        fixedRotation!!.updateRotations(defRotation.yaw, defRotation.pitch)

        if (silentRotationValue.get()) {
            RotationUtils.setTargetRot(defRotation, if (aacValue.get() && !rotations.get().equals("Spin", ignoreCase = true)) 15 else 0)
        } else {
            defRotation.toPlayer(mc.thePlayer!!)
        }

        if (!rotations.get().equals("None", true) && rotationStrafeValue.get().equals("Vestige", true)) {
            val diff = MathHelper.wrapAngleTo180_float(
                MathHelper.wrapAngleTo180_float(fixedRotation!!.yaw) - MathHelper.wrapAngleTo180_float(MovementUtils.getPlayerDirection())
            ) + 22.5F

            val adjustedDiff = if (diff < 0) 360 + diff else diff
            val a = (adjustedDiff / 45.0).toInt()

            val value = if (mc.thePlayer.moveForward != 0f) abs(mc.thePlayer.moveForward) else abs(mc.thePlayer.moveStrafing)
            var forward = value
            var strafe = 0f

            for (i in 0 until 8 - a) {
                val dirs = MovementUtils.incrementMoveDirection(forward, strafe)
                forward = dirs[0]
                strafe = dirs[1]
            }

            if (forward <= 0.8f) mc.gameSettings.keyBindSprint.pressed = false
        }

        return true
    }

    private fun getTargetRotation(entity: Entity): Rotation? {
        var boundingBox = entity.entityBoundingBox

        if (predictValue.get() && !rotations.get().equals("Grim", true) && !rotations.get().equals("Intave", true)) {
            boundingBox = boundingBox.offset(
                    (entity.posX - entity.prevPosX) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get()),
                    (entity.posY - entity.prevPosY) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get()),
                    (entity.posZ - entity.prevPosZ) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get())
            )
        }

        val rotationSpeed = (Math.random() * (maxTurnSpeed.get() - minTurnSpeed.get()) + minTurnSpeed.get()).toFloat()
        return when (rotations.get().lowercase()) {
            "vanilla" -> {
                if (maxTurnSpeed.get() <= 0F) RotationUtils.serverRotation

                val (_, rotation) = RotationUtils.searchCenter(
                        boundingBox,
                        outborderValue.get() && !attackTimer.hasTimePassed(attackDelay / 2),
                        randomCenterValue.get(),
                        predictValue.get(),
                        mc.thePlayer!!.getDistanceToEntityBox(entity) < throughWallsRangeValue.get(),
                        maxRange,
                        RandomUtils.nextFloat(minRand.get(), maxRand.get()),
                        randomCenterNewValue.get()
                ) ?: return null

                val limitedRotation = RotationUtils.limitAngleChange(RotationUtils.serverRotation!!, rotation, rotationSpeed)

                limitedRotation
            }
            "backtrack" -> {
                val rotation = RotationUtils.otherRotation(boundingBox, RotationUtils.getCenter(entity.entityBoundingBox), predictValue.get(), mc.thePlayer!!.getDistanceToEntityBox(entity) < throughWallsRangeValue.get(), maxRange)
                val limitedRotation = RotationUtils.limitAngleChange(RotationUtils.serverRotation!!, rotation, rotationSpeed)

                limitedRotation
            }
            "grim" -> {
                RotationUtils.calculate(getNearestPointBB(mc.thePlayer.getPositionEyes(1F), boundingBox))
            }
            "intave" -> {
                val rotation: Rotation? = RotationUtils.getAngles(entity)
                val amount = intaveRandomAmount.get()
                val yaw = rotation!!.yaw + Math.random() * amount - amount / 2
                val pitch = rotation.pitch + Math.random() * amount - amount / 2
                Rotation(yaw.toFloat(), pitch.toFloat())
            }
            "ncp" -> {
                val rotation = RotationUtils.otherRotation(boundingBox, RotationUtils.getCenter(entity.entityBoundingBox), false, mc.thePlayer!!.getDistanceToEntityBox(entity) < rangeValue.get() - 0.5f, maxRange)
                val limitedRotation = RotationUtils.limitAngleChange(RotationUtils.serverRotation!!, rotation, rotationSpeed)
                limitedRotation
            }
            "smooth" -> {
                var yaw = fixedRotation!!.yaw
                val rots = RotationUtils.getRotationsToEntity(entity as EntityLivingBase, false)
                val currentYaw = MathHelper.wrapAngleTo180_float(yaw)
                val diff = abs(currentYaw - rots!!.yaw)

                if (diff >= 8) {
                    if (diff > 35) {
                        rotSpeed += 4 - Math.random()
                        rotSpeed = rotSpeed.coerceAtLeast(31.0 - Math.random())
                    } else {
                        rotSpeed -= 6.5 - Math.random()
                        rotSpeed = rotSpeed.coerceAtLeast(14.0 - Math.random())
                    }
                    if (diff <= 180) {
                        if (currentYaw > rots.yaw) yaw -= rotSpeed.toFloat()
                        else yaw += rotSpeed.toFloat()
                    } else {
                        if (currentYaw > rots.yaw) yaw += rotSpeed.toFloat()
                        else yaw -= rotSpeed.toFloat()
                    }
                } else {
                    if (currentYaw > rots.yaw) {
                        yaw -= diff * 0.8f
                    } else {
                        yaw += diff * 0.8f
                    }
                }

                yaw += (Math.random() * 0.7 - 0.35).toFloat()
                var pitch = (mc.thePlayer.rotationPitch + (rots.pitch - mc.thePlayer.rotationPitch) * 0.6).toFloat()
                pitch += (Math.random() * 0.5 - 0.25).toFloat()

                Rotation(yaw, pitch)
            }
            "fixed" -> {
                val yaw = fixedRotation!!.yaw
                val pitch = fixedRotation!!.pitch
                Rotation(fixedRotation!!.yaw, pitch)

            }
            else -> RotationUtils.serverRotation
        }
    }
    private fun updateHitable() {
        if (rotations.get().equals("none", true)) {
            hitable = true
            return
        }

        val disabler = MinusBounce.moduleManager[Disabler::class.java]!!
        val watchdogDisabler = disabler.modes.find { it.modeName.equals("Watchdog", true) } as WatchdogDisabler

        // Completely disable rotation check if turn speed equals to 0 or NoHitCheck is enabled
        if (maxTurnSpeed.get() <= 0F || noHitCheck.get() || watchdogDisabler.canModifyRotation) {
            hitable = true
            return
        }

        val reach = min(maxRange.toDouble(), mc.thePlayer.getDistanceToEntityBox(target!!)) + 1

        if (raycastValue.get()) {
            val raycastedEntity = RaycastUtils.raycastEntity(reach, object: RaycastUtils.IEntityFilter {
                override fun canRaycast(entity: Entity?): Boolean {
                    return (!livingRaycastValue.get() || entity is EntityLivingBase && entity !is EntityArmorStand) &&
                            (isEnemy(entity) || raycastIgnoredValue.get() || aacValue.get() && mc.theWorld.getEntitiesWithinAABBExcludingEntity(entity, entity!!.entityBoundingBox).isNotEmpty())
                }
            })

            if (raycastValue.get() && raycastedEntity is EntityLivingBase)
                currentTarget = raycastedEntity

            hitable = if (maxTurnSpeed.get() > 0F) currentTarget == raycastedEntity else true
        } else
            hitable = RotationUtils.isFaced(currentTarget!!, reach)
    }

    private fun preBlocking() {
        if (!mc.thePlayer.isBlocking && !blockingStatus) return
        when (autoBlockModeValue.get().lowercase()) {
            "vanilla", "packet" -> mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
            "grim" -> {
                mc.netHandler.addToSendQueue(C09PacketHeldItemChange((mc.thePlayer.inventory.currentItem + 1) % 9))
                mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
            }
            "vulcan" -> if (autoBlockModeValue.get().equals("Vulcan", true) && blockTimer.hasTimePassed(50)) {
                PacketUtils.sendPacketNoEvent(C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()))
                blockTimer.reset()
            }
            "watchdog" -> KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.keyCode, mc.thePlayer.hurtTime > 6)
            "oldintave" -> {
                mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1))
                mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                blockingStatus = false
            }
            "aftertick" -> stopBlocking()
            else -> return
        }
    }

    private fun postBlocking(entity: EntityLivingBase) {
        if (!(blockRate.get() > 0 && Random().nextInt(100) <= blockRate.get()))
            return

        if (smartAutoBlockValue.get() && clicks != 1 && mc.thePlayer.getDistanceToEntityBox(entity) < maxRange && mc.thePlayer.hurtTime < 4)
            return

        when (autoBlockModeValue.get().lowercase()) {
            "vanilla", "oldintave" -> startBlocking(entity, interactAutoBlockValue.get())
            "polar" -> if (mc.thePlayer.hurtTime < 8 && mc.thePlayer.hurtTime != 1 && mc.thePlayer.fallDistance > 0) startBlocking(entity, interactAutoBlockValue.get())
            else -> return
        }
    }

    private fun startBlocking(interactEntity: Entity, interact: Boolean) {
        if (mc.thePlayer.getDistanceToEntityBox(interactEntity) > autoBlockRangeValue.get())
            return

        if (blockingStatus) return

        if (!abThruWallValue.get() && interactEntity is EntityLivingBase) {
            if (!interactEntity.canEntityBeSeen(mc.thePlayer!!)) return
        }

        if (interact) {
            val positionEye = mc.renderViewEntity?.getPositionEyes(1F)

            val expandSize = interactEntity.collisionBorderSize.toDouble()
            val boundingBox = interactEntity.entityBoundingBox.expand(expandSize, expandSize, expandSize)

            val (yaw, pitch) = RotationUtils.targetRotation
                    ?: Rotation(mc.thePlayer!!.rotationYaw, mc.thePlayer!!.rotationPitch)
            val yawCos = cos(-yaw * 0.017453292F - Math.PI.toFloat())
            val yawSin = sin(-yaw * 0.017453292F - Math.PI.toFloat())
            val pitchCos = -cos(-pitch * 0.017453292F)
            val pitchSin = sin(-pitch * 0.017453292F)
            val range = min(maxRange.toDouble(), mc.thePlayer!!.getDistanceToEntityBox(interactEntity)) + 1
            val lookAt = positionEye!!.addVector(yawSin * pitchCos * range, pitchSin * range, yawCos * pitchCos * range)

            val movingObject = boundingBox.calculateIntercept(positionEye, lookAt) ?: return
            val hitVec = movingObject.hitVec

            mc.netHandler.addToSendQueue(C02PacketUseEntity(interactEntity, Vec3(
                    hitVec.xCoord - interactEntity.posX,
                    hitVec.yCoord - interactEntity.posY,
                    hitVec.zCoord - interactEntity.posZ)
            ))
        }

        mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()))
        blockingStatus = true
    }

    private fun stopBlocking() {
        if (blockingStatus) {
            mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))

            blockingStatus = false
        }
    }

    private val cancelRun: Boolean
        get() = mc.thePlayer.isSpectator || !isAlive(mc.thePlayer)
                || (blinkCheck.get() && MinusBounce.moduleManager[Blink::class.java]!!.state) || MinusBounce.moduleManager[FreeCam::class.java]!!.state ||
                (noScaffValue.get() && MinusBounce.moduleManager[Scaffold::class.java]!!.state)

    private val canBlock: Boolean
        get() = mc.thePlayer.heldItem != null && mc.thePlayer.heldItem.item is ItemSword

    private val maxRange: Float
        get() = max(rangeValue.get(), throughWallsRangeValue.get())

    private fun getRange(entity: Entity) =
        if (mc.thePlayer.getDistanceToEntityBox(entity) >= throughWallsRangeValue.get()) rangeValue.get() else throughWallsRangeValue.get()

    override val tag: String
        get() = targetModeValue.get()
}
