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
import net.minusmc.minusbounce.value.*
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import java.util.*
import kotlin.math.*

@ModuleInfo(name = "KillAura", spacedName = "Kill Aura", description = "Automatically attacks targets around you.",
        category = ModuleCategory.COMBAT, keyBind = Keyboard.KEY_R)
class KillAura : Module() {

    private val cps = IntRangeValue("CPS", 5, 8, 1, 20)

    private val hurtTimeValue = IntegerValue("HurtTime", 10, 0, 10)

    // Range
    val rangeValue = FloatValue("Range", 3.7f, 1f, 8f, "m")
    private val throughWallsValue = BoolValue("ThroughWalls", true)

    // Modes
    private val rotations = ListValue("RotationMode", arrayOf("Vanilla", "BackTrack", "Grim", "Intave", "None"), "BackTrack")
    private val intaveRandomAmount = FloatValue("RandomAmount", 4f, 0.25f, 10f) { rotations.get().equals("Intave", true) }

    private val turnSpeed = FloatRangeValue("TurnSpeed", 180f, 180f, 0f, 180f, "°", {!rotations.get().equals("None", true)})

    private val noHitCheck = BoolValue("NoHitCheck", false) { !rotations.get().equals("none", true) }
    private val blinkCheck = BoolValue("BlinkCheck", true)
    private val noScaffValue = BoolValue("NoScaffold", true)

    private val priorityValue = ListValue("Priority", arrayOf("Health", "Distance", "FOV", "LivingTime", "Armor", "HurtResistance", "HurtTime", "HealthAbsorption", "RegenAmplifier"), "Distance")
    val targetModeValue = ListValue("TargetMode", arrayOf("Single", "Switch", "Multi"), "Switch")

    private val switchDelayValue = IntegerValue("SwitchDelay", 1000, 1, 2000, "ms") {
        targetModeValue.get().equals("switch", true)
    }

    // Bypass
    private val swingValue = ListValue("Swing", arrayOf("Normal", "Packet", "None"), "Normal")
    val keepSprintValue = BoolValue("KeepSprint", true)

    val autoBlockModeValue = ListValue(
        "AutoBlock",
        arrayOf(
            "None",
            "AfterTick",
            "Vanilla",
            "Polar",
            "NewNCP",
            "OldIntave",
            "Watchdog",
            "Verus",
            "RightHold",
            "KeyBlock",
            "OldHypixel",
        ),
        "None"
    )
    private val interactAutoBlockValue = BoolValue("InteractAutoBlock", true) {
        !autoBlockModeValue.get().equals("None", true)
    }
    private val autoBlockRangeValue = FloatValue("AutoBlock-Range", 5f, 0f, 12f, "m") {
        !autoBlockModeValue.get().equals("None", true)
    }

    // Raycast
    private val raycastValue = BoolValue("RayCast", true)

    private val silentRotationValue = BoolValue("SilentRotation", true) { !rotations.get().equals("none", true) }
    val movementCorrection = BoolValue("MovementCorrection", true)
    private val fovValue = FloatValue("FOV", 180f, 0f, 180f)

    // Predict
    private val predictValue = BoolValue("Predict", true)
    private val predictSize = FloatRangeValue("PredictSize", 1f, 1f, 0.1f, 5f) {predictValue.get()}

    // Bypass
    private val limitedMultiTargetsValue = IntegerValue("LimitedMultiTargets", 0, 0, 50) {
        targetModeValue.get().equals("multi", true)
    }

    // Visuals
    private val circleValue = BoolValue("Circle", true)
    private val accuracyValue = IntegerValue("Accuracy", 59, 0, 59) { circleValue.get() }
    private val red = IntegerValue("Red", 255, 0, 255) { circleValue.get() }
    private val green = IntegerValue("Green", 255, 0, 255) { circleValue.get() }
    private val blue = IntegerValue("Blue", 255, 0, 255) { circleValue.get() }
    private val alpha = IntegerValue("Alpha", 255, 0, 255) { circleValue.get() }

    /**
     * MODULE
     */

    // Target
    public var currentTarget: EntityLivingBase? = null
    var hitable = false
    private val prevTargetEntities = mutableListOf<Int>()

    // Attack delay
    private val attackTimer = MSTimer()
    private var attackDelay = 0L
    private var blockTimer = MSTimer()
    private var clicks = 0
    private var legitBlocking = 0

    // Fake block status
    var blockingStatus = false
    private var verusBlocking = false

    //Hypixel Autoblock
    private var watchdogc02 = 0
    private var watchdogdelay = 0
    private var watchdogcancelTicks = 0
    private var watchdogunblockdelay = 0
    private var watchdogkaing = false
    private var watchdogblinking = false
    private var watchdogblock = false
    private var watchdogblocked = false
    private var watchdogcancelc02 = false

    // Rotation
    private var rotSpeed = 15.0

    override fun onEnable() {
        mc.thePlayer ?: return
        mc.theWorld ?: return

        rotSpeed = 15.0

        updateTarget()
        verusBlocking = false
        legitBlocking = 0
    }

    override fun onDisable() {
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

        watchdogkaing = false
        watchdogblocked = false
        watchdogc02 = 0
        watchdogdelay = 0
    }

    @EventTarget
    fun onPreMotion(event: PreMotionEvent) {
        if (autoBlockModeValue.get().equals("Watchdog", true)) {
            if (mc.thePlayer.heldItem.item is ItemSword && currentTarget != null) {
                watchdogkaing = true
                watchdogcancelc02 = false
                watchdogcancelTicks = 0
                watchdogunblockdelay = 0
                if (!watchdogblinking) {
                    BlinkUtils.setBlinkState(all = true)
                    watchdogblinking = true
                    watchdogblocked = false
                }
                if (watchdogblinking && !watchdogblock) {
                    watchdogdelay++
                    if (watchdogdelay >= 2) {
                        mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1))
                        mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                        watchdogblocked = false
                        watchdogblock = true
                        watchdogdelay = 0
                    }
                }
                if (watchdogblinking && watchdogblock) {
                    if (watchdogc02 > 1) {
                        BlinkUtils.setBlinkState(off = true, release = true)
                        mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement())
                        watchdogblinking = false
                        watchdogblock = false
                        watchdogblocked = true
                        watchdogc02 = 0
                    }
                }
            }
            if (watchdogkaing && currentTarget == null) {
                watchdogkaing = false
                watchdogblocked = false
                watchdogc02 = 0
                watchdogdelay = 0
                BlinkUtils.setBlinkState(off = true, release = true)
                watchdogcancelc02 = true
                watchdogcancelTicks = 0
                if (mc.thePlayer.heldItem.item is ItemSword) {
                    mc.netHandler.addToSendQueue(C07PacketPlayerDigging())
                }
            }
            if (watchdogcancelc02) {
                watchdogcancelTicks++
                if (watchdogcancelTicks >= 3) {
                    watchdogcancelc02 = false
                    watchdogcancelTicks = 0
                }
            }
        }
    }

    @EventTarget
    fun onPostMotion(event: PostMotionEvent) {
        updateHitable()
            
        if (autoBlockModeValue.get().equals("OldHypixel", true)) {
            when (mc.thePlayer.swingProgressInt) {
                1 -> stopBlocking()
                2 -> startBlocking(currentTarget!!, interactAutoBlockValue.get() && mc.thePlayer.getDistanceToEntityBox(currentTarget!!) < rangeValue.get())
            }
        }
        
        startBlocking(currentTarget!!, hitable)
    }

    @EventTarget
    fun onStrafe(event: StrafeEvent) {
        val targetStrafe = MinusBounce.moduleManager[TargetStrafe::class.java]!!
        if (!targetStrafe.state) return

        if (currentTarget != null && RotationUtils.targetRotation != null) {
            if (targetStrafe.canStrafe) {
                val strafingData = targetStrafe.getData()
                MovementUtils.strafeCustom(MovementUtils.speed, strafingData[0], strafingData[1], strafingData[2])
                event.cancelEvent()
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (verusBlocking && ((packet is C07PacketPlayerDigging && packet.status == C07PacketPlayerDigging.Action.RELEASE_USE_ITEM) || packet is C08PacketPlayerBlockPlacement) && autoBlockModeValue.get().equals("Verus", true))
            event.cancelEvent()

        if (packet is C09PacketHeldItemChange)
            verusBlocking = false

        if (autoBlockModeValue.get().equals("Watchdog", true)) {
            if (mc.thePlayer.heldItem?.item is ItemSword && currentTarget != null && watchdogkaing) {
                if (packet is C08PacketPlayerBlockPlacement || packet is C07PacketPlayerDigging) {
                    event.cancelEvent()
                }
            }
            if (mc.thePlayer.heldItem?.item is ItemSword && currentTarget != null && watchdogblocked || watchdogcancelc02) {
                if (packet is C02PacketUseEntity) {
                    event.cancelEvent()
                    watchdogblocked = false
                }
            }
            if (packet is C02PacketUseEntity && watchdogblinking) {
                watchdogc02++
            }
        }
    }

    @EventTarget
    fun onPreUpdate(event: PreUpdateEvent){
        // Update target
        updateTarget()

        while (clicks > 0) {
            runAttack()
            clicks--
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if(currentTarget == null){
            stopBlocking()
        }

        if (autoBlockModeValue.get().equals("RightHold", true) && canBlock) {
            mc.gameSettings.keyBindUseItem.pressed = currentTarget != null && mc.thePlayer.getDistanceToEntityBox(currentTarget!!) < rangeValue.get()
        }

        if (blockingStatus || mc.thePlayer.isBlocking)
            verusBlocking = true
        else if (verusBlocking) {
            verusBlocking = false
            if (autoBlockModeValue.get().equals("Verus", true))
                PacketUtils.sendPacketNoEvent(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
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

        if (currentTarget != null && attackTimer.hasTimePassed(attackDelay) &&
                currentTarget!!.hurtTime <= hurtTimeValue.get()) {
            clicks++
            attackTimer.reset()
            attackDelay = TimeUtils.randomClickDelay(cps.getMinValue(), cps.getMaxValue())
        }

        if (currentTarget != null && attackTimer.hasTimePassed((attackDelay.toDouble() * 0.9).toLong()) && autoBlockModeValue.get().equals("KeyBlock", true) && canBlock) {
            mc.gameSettings.keyBindUseItem.pressed = false
        }

        if (currentTarget != null && blockTimer.hasTimePassed(25) && autoBlockModeValue.get().equals("KeyBlock", true) && canBlock) {
            mc.gameSettings.keyBindUseItem.pressed = true
        }
    }

    private fun runAttack() {
        currentTarget ?: return

        // Settings
        val multi = targetModeValue.get().equals("Multi", ignoreCase = true)

        // Check is not hitable or check failrate
        if (!hitable) {
            runSwing()
        } else {
            // Attack
            if (!multi) {
                attackEntity(currentTarget!!)
            } else {
                var targets = 0

                for (entity in mc.theWorld.loadedEntityList) {
                    val distance = mc.thePlayer.getDistanceToEntityBox(entity)

                    if (entity is EntityLivingBase && isEnemy(entity) && distance <= rangeValue.get()) {
                        attackEntity(entity)

                        targets += 1

                        if (limitedMultiTargetsValue.get() != 0 && limitedMultiTargetsValue.get() <= targets)
                            break
                    }
                }
            }

            prevTargetEntities.add(currentTarget!!.entityId)

        }

        if (targetModeValue.get().equals("Switch", ignoreCase = true) && attackTimer.hasTimePassed((switchDelayValue.get()).toLong())) {
            if (switchDelayValue.get() != 0) {
                prevTargetEntities.add(currentTarget!!.entityId)
                attackTimer.reset()
            }
        }
    }

    private fun runSwing() {
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

            if (distance <= rangeValue.get() && (fov == 180F || entityFov <= fov) && entity.hurtTime <= hurtTime)
                targets.add(entity)
        }

        // Sort targets by priority
        when (priorityValue.get().lowercase()) {
            "distance" -> targets.sortBy { mc.thePlayer.getDistanceToEntityBox(it) } // Sort by distance
            "health" -> targets.sortBy { it.health } // Sort by health
            "fov" -> targets.sortBy { RotationUtils.getRotationDifference(it) } // Sort by FOV
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
            currentTarget = entity
            found = true
            break
        }

        if(!found) currentTarget = null

        // Cleanup last targets when no target found and try again
        if (prevTargetEntities.isNotEmpty()) {
            prevTargetEntities.clear()
            updateTarget()
        }
    }

    private fun attackEntity(entity: EntityLivingBase) {
        val criticals = MinusBounce.moduleManager[Criticals::class.java] as Criticals

        stopBlocking()

        MinusBounce.eventManager.callEvent(AttackEvent(entity))

        // Attack target
        runSwing()

        mc.netHandler.addToSendQueue(C02PacketUseEntity(entity, C02PacketUseEntity.Action.ATTACK))

        if (keepSprintValue.get()) {
            if (mc.thePlayer.fallDistance > 0F && !mc.thePlayer.onGround && !mc.thePlayer.isOnLadder && !mc.thePlayer.isInWater && !mc.thePlayer.isPotionActive(Potion.blindness) && mc.thePlayer.ridingEntity == null || criticals.state && criticals.msTimer.hasTimePassed(criticals.delayValue.get().toLong()) && !mc.thePlayer.isInWater && !mc.thePlayer.isInLava && !mc.thePlayer.isInWeb)
                mc.thePlayer.onCriticalHit(entity)

            // Enchant Effect
            if (EnchantmentHelper.getModifierForCreature(mc.thePlayer.heldItem, entity.creatureAttribute) > 0F)
                mc.thePlayer.onEnchantmentCritical(entity)
        } else {
            if (mc.playerController.currentGameType != WorldSettings.GameType.SPECTATOR)
                mc.thePlayer.attackTargetEntityWithCurrentItem(entity)
        }

    }

    private fun updateRotations(entity: Entity): Boolean {
        if (rotations.get().equals("none", true)) return true

        val disabler = MinusBounce.moduleManager[Disabler::class.java]!!
        val watchdogDisabler = disabler.modes.find { it.modeName.equals("Watchdog", true) } as WatchdogDisabler

        if (watchdogDisabler.canModifyRotation) return true

        val defRotation = getTargetRotation(entity) ?: return false

        if (silentRotationValue.get()) {
            RotationUtils.setTargetRot(defRotation, 0)
        } else {
            defRotation.toPlayer(mc.thePlayer!!)
        }

        return true
    }

    private fun getTargetRotation(entity: Entity): Rotation? {
        var boundingBox = entity.entityBoundingBox

        if (predictValue.get() && !rotations.get().equals("Grim", true) && !rotations.get().equals("Intave", true)) {
            boundingBox = boundingBox.offset(
                    (entity.posX - entity.prevPosX) * RandomUtils.nextFloat(predictSize.getMinValue(), predictSize.getMaxValue()),
                    (entity.posY - entity.prevPosY) * RandomUtils.nextFloat(predictSize.getMinValue(), predictSize.getMaxValue()),
                    (entity.posZ - entity.prevPosZ) * RandomUtils.nextFloat(predictSize.getMinValue(), predictSize.getMaxValue())
            )
        }

        val rotationSpeed = (Math.random() * (turnSpeed.getMaxValue() - turnSpeed.getMinValue()) + turnSpeed.getMinValue()).toFloat()
        return when (rotations.get().lowercase()) {
            "vanilla" -> {
                if (turnSpeed.getMaxValue() <= 0F) RotationUtils.serverRotation

                val (_, rotation) = RotationUtils.searchCenter(
                        boundingBox,
                        predictValue.get(),
                        throughWallsValue.get(),
                        rangeValue.get()
                ) ?: return null

                val limitedRotation = RotationUtils.limitAngleChange(RotationUtils.serverRotation!!, rotation, rotationSpeed)

                limitedRotation
            }
            "backtrack" -> {
                val rotation = RotationUtils.backTrackRotation(boundingBox, RotationUtils.getCenter(entity.entityBoundingBox), predictValue.get(), throughWallsValue.get(), rangeValue.get())
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

        if (turnSpeed.getMaxValue() <= 0F || noHitCheck.get() || watchdogDisabler.canModifyRotation) {
            hitable = true
            return
        }

        val reach = min(rangeValue.get().toDouble(), mc.thePlayer.getDistanceToEntityBox(currentTarget!!)) + 1

        if (raycastValue.get()) {
            val raycastedEntity = RaycastUtils.raycastEntity(reach, object: RaycastUtils.IEntityFilter {
                override fun canRaycast(entity: Entity?): Boolean {
                    return entity is EntityLivingBase && entity !is EntityArmorStand && isEnemy(entity)
                }
            })

            if (raycastValue.get() && raycastedEntity is EntityLivingBase)
                currentTarget = raycastedEntity

            hitable = if (turnSpeed.getMaxValue() > 0F) currentTarget == raycastedEntity else true
        } else
            hitable = RotationUtils.isFaced(currentTarget!!, reach)
    }

    private fun startBlocking(interactEntity: Entity, interact: Boolean) {
        if (autoBlockModeValue.get().equals("none", true) || !canBlock ||mc.thePlayer.getDistanceToEntityBox(interactEntity) > autoBlockRangeValue.get())
            return

        when (autoBlockModeValue.get().lowercase()) {
            "newncp" -> {
                mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, null, 0.0f, 0.0f, 0.0f))
                return
            }
            "polar" -> if (mc.thePlayer.hurtTime < 8 && mc.thePlayer.hurtTime != 1 && mc.thePlayer.fallDistance > 0) return
            "keyblock" -> {
                blockTimer.reset()
                return
            }
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
            val range = min(rangeValue.get().toDouble(), mc.thePlayer!!.getDistanceToEntityBox(interactEntity)) + 1
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
            when (autoBlockModeValue.get().lowercase()) {
                "newncp" -> {
                    mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1))
                    mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                }
                "oldintave" -> {
                    mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1))
                    mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                    return
                }
                "keyblock" -> mc.gameSettings.keyBindUseItem.pressed = false
                else -> mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
            }

            
            blockingStatus = false
        }
    }

    private val cancelRun: Boolean
        get() = mc.thePlayer.isSpectator || !isAlive(mc.thePlayer)
                || (blinkCheck.get() && MinusBounce.moduleManager[Blink::class.java]!!.state) || MinusBounce.moduleManager[FreeCam::class.java]!!.state ||
                (noScaffValue.get() && MinusBounce.moduleManager[Scaffold::class.java]!!.state)

    private val canBlock: Boolean
        get() = mc.thePlayer.heldItem != null && mc.thePlayer.heldItem.item is ItemSword 

    override val tag: String
        get() = targetModeValue.get()
}
