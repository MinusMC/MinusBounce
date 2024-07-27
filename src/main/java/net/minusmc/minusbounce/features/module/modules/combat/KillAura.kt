/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.combat

import net.minecraft.client.settings.GameSettings
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.item.ItemSword
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.Vec3
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.utils.ClassUtils
import net.minusmc.minusbounce.utils.EntityUtils.isSelected
import net.minusmc.minusbounce.utils.RaycastUtils
import net.minusmc.minusbounce.utils.RaycastUtils.runWithModifiedRaycastResult
import net.minusmc.minusbounce.utils.Rotation
import net.minusmc.minusbounce.utils.extensions.*
import net.minusmc.minusbounce.utils.ClientUtils
import net.minusmc.minusbounce.utils.misc.RandomUtils
import net.minusmc.minusbounce.utils.player.MovementCorrection
import net.minusmc.minusbounce.utils.player.RotationUtils
import net.minusmc.minusbounce.utils.timer.MSTimer
import net.minusmc.minusbounce.value.*
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import kotlin.math.cos
import kotlin.math.sin


@ModuleInfo(name = "KillAura", spacedName = "Kill Aura", description = "Automatically attacks targets around you.", category = ModuleCategory.COMBAT, keyBind = Keyboard.KEY_R)
class KillAura : Module() {
    private val clickModeValue = ListValue("ClickMode", arrayOf("Normal", "Legit"), "Legit")
    private val cps = IntRangeValue("CPS", 5, 8, 1, 20)
    private val hurtTimeValue = IntegerValue("HurtTime", 10, 0, 10)

    val rangeValue: FloatValue = object: FloatValue("Range", 3.7f, 1f, 8f, "m") {
        override fun onPostChange(oldValue: Float, newValue: Float) {
            set(newValue.coerceAtMost(rotationRangeValue.get()))
        }
    }

    private val throughWallsRangeValue = object: FloatValue("ThroughWallsRange", 2f, 0f, 8f, "m") {
        override fun onPostChange(oldValue: Float, newValue: Float) {
            set(newValue.coerceAtMost(rangeValue.get()))
        }
    }

    val rotationRangeValue: FloatValue = object: FloatValue("RotationRange", 5f, 1f, 8f, "m") {
        override fun onPostChange(oldValue: Float, newValue: Float) {
            set(newValue.coerceAtLeast(rangeValue.get()))
        }
    }

    private val swingValue = ListValue("Swing", arrayOf("Normal", "Packet", "None"), "Normal")
    private val swingTimingValue = ListValue("SwingTiming", arrayOf("BeforeAttack", "AfterAttack"), "BeforeAttack")

    private val rotationValue = ListValue("Rotation", arrayOf("Vanilla", "NCP", "Grim", "Intave", "None"), "BackTrack")
    private val intaveRandomAmount = FloatValue("Intave-RandomAmount", 4f, 0.25f, 10f) { rotationValue.get().equals("Intave", true) }
    private val turnSpeed = FloatRangeValue("TurnSpeed", 180f, 180f, 0f, 180f, "°") {
        !rotationValue.get().equals("None", true)
    }

    private val keepRotationLength = IntegerValue("KeepRotationLength", 20, 0, 20)

    private val priorityValue = ListValue("Priority", arrayOf("Health", "Distance", "HurtResistance", "HurtTime", "Armor"), "Distance")
    val targetModeValue = ListValue("TargetMode", arrayOf("Single", "Switch", "Multi"), "Switch")
    private val switchDelayValue = IntegerValue("SwitchDelay", 1000, 1, 2000, "ms") {
        targetModeValue.get().equals("switch", true)
    }
    private val limitedMultiTargetsValue = IntegerValue("LimitedMultiTargets", 1, 1, 50) {
        targetModeValue.get().equals("multi", true)
    }

    private val onlyHitOnMouseToTarget = BoolValue("OnlyHitOnMouseToTarget", false) { !rotationValue.get().equals("none", true) }

    val autoBlockModeValue: ListValue = object : ListValue("AutoBlock", arrayOf("None", "AfterTick", "Vanilla", "NewNCP", "RightHold", "Swing"), "None") {
        override fun onPreChange(oldValue: String, newValue: String) {
            if (state) onDisable()
        }

        override fun onPostChange(oldValue: String, newValue: String) {
            if (state) onEnable()
        }
    }
    private val interactValue = BoolValue("InteractAutoBlock", true)
    private val autoBlockRangeValue = FloatValue("AutoBlock-Range", 5f, 0f, 12f, "m") {
        !autoBlockModeValue.get().equals("None", true)
    }

    private val raycastValue = BoolValue("RayCast", true)
    private val silentRotationValue = BoolValue("SilentRotation", true) { !rotationValue.get().equals("none", true) }
    private val movementCorrection = ListValue("MovementCorrection", arrayOf("None", "Normal", "LiquidBounce", "Rise"), "Strict")
    private val predictValue = BoolValue("Predict", true)
    private val predictSizeValue = FloatRangeValue("PredictSize", 1f, 1f, 0.1f, 5f) {predictValue.get()}

    private val circleValue = BoolValue("Circle", true)
    private val accuracyValue = IntegerValue("Accuracy", 59, 0, 59) { circleValue.get() }
    private val red = IntegerValue("Red", 255, 0, 255) { circleValue.get() }
    private val green = IntegerValue("Green", 255, 0, 255) { circleValue.get() }
    private val blue = IntegerValue("Blue", 255, 0, 255) { circleValue.get() }
    private val alpha = IntegerValue("Alpha", 255, 0, 255) { circleValue.get() }

    // Target
    private val prevTargetEntities = mutableListOf<Int>()
    private val discoveredEntities = mutableListOf<EntityLivingBase>()
    var target: EntityLivingBase? = null
    var hitable = false

    // Attack delay
    private val attackTimer = MSTimer()
    private var attackDelay = 0L
    private var clicks = 0

    // Fake block status
    var blockingStatus = false

    override fun onEnable() {
        mc.theWorld ?: return

        updateTarget()
    }

    override fun onDisable() {
        target = null
        hitable = false
        attackTimer.reset()
        clicks = 0
        prevTargetEntities.clear()
        stopBlocking {
            mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
        }
        mc.gameSettings.keyBindUseItem.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindUseItem)
    }

    @EventTarget
    fun onPreUpdate(event: PreUpdateEvent) {
        if (autoBlockModeValue.get().equals("righthold", true)) {
            val target = this.target ?: run {
                mc.gameSettings.keyBindUseItem.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindUseItem)
                return
            }

            if (canBlock && mc.thePlayer.getDistanceToEntityBox(target) < autoBlockRangeValue.get())
                mc.gameSettings.keyBindUseItem.pressed = true
            else
                mc.gameSettings.keyBindUseItem.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindUseItem)
        }
            
    }

    @EventTarget
    fun onTick(event: TickEvent) {
        target ?: run {
            stopBlocking { mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN)) }
            return
        }

        repeat(clicks) {
            runAttack(it + 1 == clicks)
            clicks--
        }
    }

    @EventTarget
    fun onPreMotion(event: PreMotionEvent) {
        updateTarget()
    }

    @EventTarget
    fun onPostMotion(event: PostMotionEvent) {
        if (!mc.thePlayer.isBlocking || canBlock) {
            when (autoBlockModeValue.get().lowercase()) {
                "aftertick" -> startBlocking {
                    mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()))
                }
                "swing" -> when (mc.thePlayer.swingProgressInt) {
                    1 -> startBlocking { mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem())) }
                    2 -> stopBlocking { mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN)) }
                }
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

        target ?: return

        if (attackTimer.hasTimePassed(attackDelay)) {
            clicks++
            attackTimer.reset()
            attackDelay = RandomUtils.randomClickDelay(cps.minValue, cps.maxValue)
        }
    }

    private fun runAttack(isLastClicks: Boolean) {
        updateHitable()

        val target = this.target ?: return

        when (clickModeValue.get().lowercase()) {
            "legit" -> clickLegit(isLastClicks)
            else -> clickNormal(target)
        }

        if (targetModeValue.get().equals("Switch", true)) {
            if (attackTimer.hasTimePassed(switchDelayValue.get())) {
                prevTargetEntities.add(target.entityId)
                attackTimer.reset()
            }
        }
    }

    private fun clickNormal(target: EntityLivingBase) {
        if (!hitable || target.hurtTime > hurtTimeValue.get())
            return

        // Attack
        if (!targetModeValue.get().equals("multi", true)) {
            if (mc.thePlayer.getDistanceToEntityBox(target) <= rangeValue.get())
                attackEntity(target)
        } else discoveredEntities
            .filter { mc.thePlayer.getDistanceToEntityBox(it) <= rangeValue.get() }
            .take(limitedMultiTargetsValue.get())
            .forEach(this::attackEntity)

        prevTargetEntities.add(target.entityId)
    }

    private fun clickLegit(isLastClicks: Boolean) {
        runWithModifiedRaycastResult(rangeValue.get(), throughWallsRangeValue.get()) {
            if (it?.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                val entity = it.entityHit

                if (entity is EntityLivingBase)
                    attackEntity(entity)

            } else mc.clickMouse()

            if (isLastClicks)
                mc.sendClickBlockToController(false)
        }
    }

    private fun updateTarget() {
        discoveredEntities.clear()

        for (entity in mc.theWorld.loadedEntityList) {
            if (entity !is EntityLivingBase || !isSelected(entity, true))
                continue

            if (targetModeValue.get().equals("switch", true) && prevTargetEntities.contains(entity.entityId))
                continue

            if (mc.thePlayer.getDistanceToEntityBox(entity) <= rotationRangeValue.get())
                discoveredEntities.add(entity)
        }

        when (priorityValue.get().lowercase()) {
            "health" -> discoveredEntities.sortBy { it.health + it.absorptionAmount }
            "hurtresistance" -> discoveredEntities.sortBy { it.hurtResistantTime }
            "hurttime" -> discoveredEntities.sortBy { it.hurtTime }
            "armor" -> discoveredEntities.sortBy { it.totalArmorValue }
            else -> discoveredEntities.sortBy { mc.thePlayer.getDistanceToEntityBox(it) }
        }

        discoveredEntities.forEach {
            if (updateRotations(it)) {
                target = it
                return
            }
        }

        /* If we couldn't find it, null here */
        target = null

        if (prevTargetEntities.isNotEmpty()) {
            prevTargetEntities.clear()
            updateTarget()
        }
    }

    private fun updateHitable() {
        var canHitable = false

        if (rotationValue.get().equals("none", true)) {
            this.hitable = true
            return
        }

        val rotation = RotationUtils.currentRotation ?: mc.thePlayer.rotation

        val target = this.target ?: return

        val raycastEntity = RaycastUtils.raycastEntity(rangeValue.get().toDouble(), rotation.yaw, rotation.pitch) {
            it is EntityLivingBase && it !is EntityArmorStand && isSelected(it, true)
        }

        canHitable = if (raycastValue.get()) raycastEntity == this.target else RotationUtils.isFaced(target, rangeValue.get().toDouble(), rotation)

        if (!canHitable) {
            this.hitable = false
            return
        }

        val targetToCheck = raycastEntity ?: this.target ?: return

        if (targetToCheck.hitBox.isVecInside(mc.thePlayer.getPositionEyes(1f)))
            return

        val eyes = mc.thePlayer.getPositionEyes(1f)

        val intercept = targetToCheck.hitBox.calculateIntercept(eyes,
            eyes + RotationUtils.getVectorForRotation(rotation) * rangeValue.get().toDouble()
        )

        canHitable = mc.theWorld.rayTraceBlocks(mc.thePlayer.eyes, intercept.hitVec) == null || mc.thePlayer.getDistanceToEntityBox(targetToCheck) < throughWallsRangeValue.get()

        if (onlyHitOnMouseToTarget.get() && mc.objectMouseOver != null)
            canHitable = mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY
    
        this.hitable = canHitable
    }

    private fun updateRotations(entity: EntityLivingBase): Boolean {
        if (rotationValue.get().equals("none", true) || turnSpeed.maxValue <= 0.0f)
            return true

        val rotation = getTargetRotation(entity) ?: return false
        rotation.fixedSensitivity(mc.gameSettings.mouseSensitivity)

        if (silentRotationValue.get()) {
            val movementCorrectionType = when (movementCorrection.get().lowercase()) {
                "liquidbounce" -> MovementCorrection.Type.LIQUID_BOUNCE
                "rise" -> MovementCorrection.Type.RISE
                "normal" -> MovementCorrection.Type.NORMAL
                else -> MovementCorrection.Type.NONE
            }

            RotationUtils.setTargetRotation(rotation, keepRotationLength.get(), turnSpeed.minValue, turnSpeed.maxValue, movementCorrectionType)
        } else {
            val limitRotation = RotationUtils.limitAngleChange(mc.thePlayer.rotation, rotation, RandomUtils.nextFloat(turnSpeed.minValue, turnSpeed.maxValue))
            limitRotation.toPlayer(mc.thePlayer)
        }

        return true
    }

    private fun attackEntity(entity: EntityLivingBase) {
        if (mc.thePlayer.isBlocking || blockingStatus)
            onPreAttack()

        if (swingTimingValue.get().equals("beforeattack", true))
            swing()

        mc.playerController.attackEntity(mc.thePlayer, entity)

        if (swingTimingValue.get().equals("afterattack", true))
            swing()

        if (!mc.thePlayer.isBlocking || canBlock)
            onPostAttack()
    }

    private fun swing() {
        when (swingValue.get().lowercase()) {
            "normal" -> mc.thePlayer.swingItem()
            "packet" -> mc.netHandler.addToSendQueue(C0APacketAnimation())
        }
    }

    private fun getTargetRotation(entity: Entity): Rotation? {
        var boundingBox = entity.entityBoundingBox

        if (predictValue.get()) {
            boundingBox = boundingBox.offset(
                (entity.posX - entity.prevPosX) * predictSize,
                (entity.posY - entity.prevPosY) * predictSize,
                (entity.posZ - entity.prevPosZ) * predictSize
            )
        }

        return when (rotationValue.get().lowercase()) {
            "vanilla" -> {
                val (_, rotation) = RotationUtils.searchCenter(
                    boundingBox, predictValue.get(), throughWallsRangeValue.get(), rotationRangeValue.get()) ?: return null
                rotation
            }
            "ncp" -> RotationUtils.toRotation(RotationUtils.getCenter(entity.entityBoundingBox))
            "grim" -> RotationUtils.toRotation(getNearestPointBB(mc.thePlayer.getPositionEyes(1F), boundingBox))
            "intave" -> {
                val rotation = RotationUtils.toRotation(
                    Vec3(0.0, 0.0, 0.0),
                    diff = Vec3(
                        entity.posX - mc.thePlayer.posX,
                        entity.posY + entity.eyeHeight * 0.9 - (mc.thePlayer.posY + mc.thePlayer.eyeHeight),
                        entity.posZ - mc.thePlayer.posZ
                    )
                )
                Rotation(
                    rotation.yaw + Math.random().toFloat() * intaveRandomAmount.get() - intaveRandomAmount.get() / 2,
                    rotation.pitch + Math.random().toFloat() * intaveRandomAmount.get() - intaveRandomAmount.get() / 2
                )
            }
            else -> RotationUtils.serverRotation
        }
    }

    private fun onPostAttack() {
        when (autoBlockModeValue.get().lowercase()) {
            "vanilla" -> startBlocking {
                mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()))
            }
            "newncp" -> startBlocking {
                mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, null, 0.0f, 0.0f, 0.0f))
            }
        }
    }

    private fun onPreAttack() {
        when (autoBlockModeValue.get().lowercase()) {
            "aftertick", "vanilla" -> stopBlocking {
                mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
            }
            "newncp" -> stopBlocking {
                mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1))
                mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
            }
        }
    }

    fun startBlocking(sendPacketBlocking: () -> Unit) {
        val target = this.target ?: return

        if (mc.thePlayer.getDistanceToEntityBox(target) > autoBlockRangeValue.get())
            return

        if (blockingStatus)
            return

        if (interactValue.get()) { // From CCBluex
            val positionEye = mc.thePlayer.eyes
            val boundingBox = target.hitBox
            val rotation = RotationUtils.currentRotation ?: mc.thePlayer.rotation

            val vec = RotationUtils.getVectorForRotation(rotation)

            val lookAt = positionEye.add(vec * rangeValue.get().toDouble())

            val movingObject = boundingBox.calculateIntercept(positionEye, lookAt) ?: return
            val hitVec = movingObject.hitVec

            mc.netHandler.addToSendQueue(C02PacketUseEntity(target, hitVec - target.positionVector))
            mc.netHandler.addToSendQueue(C02PacketUseEntity(target, C02PacketUseEntity.Action.INTERACT))
        }

        sendPacketBlocking()

        blockingStatus = true
    }

    fun stopBlocking(sendPacketUnblocking: () -> Unit) {
        if (blockingStatus) {   
            sendPacketUnblocking()
            blockingStatus = false
        }
    }

    val canBlock: Boolean
        get() = mc.thePlayer.heldItem != null && mc.thePlayer.heldItem.item is ItemSword

    private val predictSize: Float
        get() = RandomUtils.nextFloat(predictSizeValue.minValue, predictSizeValue.maxValue)

    override val tag: String
        get() = targetModeValue.get()
}