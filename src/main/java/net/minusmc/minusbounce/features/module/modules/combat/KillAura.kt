/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.combat

import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.item.ItemSword
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.Vec3
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.features.module.modules.combat.killaura.KillAuraBlocking
import net.minusmc.minusbounce.utils.ClassUtils
import net.minusmc.minusbounce.utils.EntityUtils.isSelected
import net.minusmc.minusbounce.utils.RaycastUtils
import net.minusmc.minusbounce.utils.RaycastUtils.runWithModifiedRaycastResult
import net.minusmc.minusbounce.utils.Rotation
import net.minusmc.minusbounce.utils.extensions.*
import net.minusmc.minusbounce.utils.misc.RandomUtils
import net.minusmc.minusbounce.utils.player.MovementCorrection
import net.minusmc.minusbounce.utils.player.RotationUtils
import net.minusmc.minusbounce.utils.timer.MSTimer
import net.minusmc.minusbounce.value.*
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin


@ModuleInfo(name = "KillAura", spacedName = "Kill Aura", description = "Automatically attacks targets around you.", category = ModuleCategory.COMBAT, keyBind = Keyboard.KEY_R)
class KillAura : Module() {
    private val blockingModes = ClassUtils.resolvePackage("${this.javaClass.`package`.name}.killaura.blocking", KillAuraBlocking::class.java)
        .map { it.newInstance() as KillAuraBlocking }
        .sortedBy { it.modeName }

    private val blockingMode: KillAuraBlocking
        get() = blockingModes.find { autoBlockModeValue.get().equals(it.modeName, true) } ?: throw NullPointerException()

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

    private val rotationRangeValue: FloatValue = object: FloatValue("RotationRange", 5f, 1f, 8f, "m") {
        override fun onPostChange(oldValue: Float, newValue: Float) {
            set(newValue.coerceAtLeast(rangeValue.get()))
        }
    }

    private val hitSelectRangeValue = FloatValue("HitSelectRange", 3f, 0f, 4f)

    private val rotationValue = ListValue("RotationMode", arrayOf("Vanilla", "NCP", "Grim", "Intave", "None"), "BackTrack")
    private val intaveRandomAmount = FloatValue("Random", 4f, 0.25f, 10f) { rotationValue.get().equals("Intave", true) }
    private val turnSpeed = FloatRangeValue("TurnSpeed", 180f, 180f, 0f, 180f, "°") {
        !rotationValue.get().equals("None", true)
    }

    private val priorityValue = ListValue("Priority", arrayOf("Health", "Distance", "HurtResistance", "HurtTime", "Armor"), "Distance")
    val targetModeValue = ListValue("TargetMode", arrayOf("Single", "Switch", "Multi"), "Switch")
    private val switchDelayValue = IntegerValue("SwitchDelay", 1000, 1, 2000, "ms") {
        targetModeValue.get().equals("switch", true)
    }
    private val limitedMultiTargetsValue = IntegerValue("LimitedMultiTargets", 1, 1, 50) {
        targetModeValue.get().equals("multi", true)
    }

    private val swingValue = ListValue("Swing", arrayOf("Normal", "Packet", "None"), "Normal")
    private val failSwingValue = BoolValue("FailSwing", true)
    private val hitableCheckValue = BoolValue("HitableCheck", false) { !rotationValue.get().equals("none", true) }
    private val useHitDelay = BoolValue("UseHitDelay", true)
    private val hitDelay = IntegerValue("HitDelay", 100, 0, 1000) {useHitDelay.get()}

    val autoBlockModeValue: ListValue = object : ListValue("AutoBlock", blockingModes.map { it.modeName }.toTypedArray(), "None") {
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
    private val movementCorrection = ListValue("MovementCorrection", arrayOf("None", "Normal", "Strict"), "Strict")
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
    private var lastMovingObjectPosition: MovingObjectPosition? = null
    private var lastTimeAttack = 0L
    var target: EntityLivingBase? = null
    var hitable = false

    // Attack delay
    private val attackTimer = MSTimer()
    private var attackDelay = 0L
    private var clicks = 0

    private val hitSelectTimer = MSTimer()
    private var canHitSelect = false

    // Fake block status
    var blockingStatus = false

    override fun onEnable() {
        updateTarget()
    }

    override fun onDisable() {
        target = null
        hitable = false
        canHitSelect = false
        attackTimer.reset()
        lastMovingObjectPosition = null
        lastTimeAttack = 0L
        clicks = 0
        prevTargetEntities.clear()
        stopBlocking()
        blockingMode.onDisable()
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        lastMovingObjectPosition = null
        lastTimeAttack = 0L
    }

    @EventTarget
    fun onPreUpdate(event: PreUpdateEvent) {
        blockingMode.onPreUpdate()
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (hitSelectRangeValue.get() > 0f) {
            if (target == null && hitSelectTimer.hasTimePassed(900L))
                canHitSelect = false
            else if (mc.thePlayer.hurtTime > 7 || mc.thePlayer.getDistanceToEntityBox(target!!) < hitSelectRangeValue.get()) {
                canHitSelect = true
                hitSelectTimer.reset()
            }

            if (!canHitSelect) {
                if (clicks > 0) clicks = 1
                return
            }
        }

        target ?: run {
            stopBlocking()
            return
        }

        repeat (clicks) {
            runAttack(it + 1 == clicks)
            clicks--
        }
    }

    @EventTarget
    fun onStrafe(event: StrafeEvent) {
        updateTarget()
    }

    @EventTarget
    fun onPreMotion(event: PreMotionEvent) {
        blockingMode.onPreMotion()
    }

    @EventTarget
    fun onPostMotion(event: PostMotionEvent) {
        blockingMode.onPostMotion()
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        blockingMode.onPacket(event)
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
            attackDelay = RandomUtils.randomClickDelay(cps.getMinValue(), cps.getMaxValue())
        }
    }

    /**
     * Run attack with raycast check entity
     * Author: CCBluex, fmcpe
     * Refactor: toidicakhia
     */

    private fun runAttack(isLastClicks: Boolean) {
        updateHitable()

        if (hitable) {
            target?.let {
                if (it.hurtTime > hurtTimeValue.get())
                    return
            } ?: return

            // Attack
            if (!targetModeValue.get().equals("Multi", true))
                attackEntity(target!!)
            else
                discoveredEntities
                    .filter { mc.thePlayer.getDistanceToEntityBox(it) < rangeValue.get() }
                    .take(limitedMultiTargetsValue.get())
                    .forEach(this::attackEntity)

            prevTargetEntities.add(target!!.entityId)
        } else if (swingValue.get().equals("none", true) && failSwingValue.get())
            runWithModifiedRaycastResult(rangeValue.get(), throughWallsRangeValue.get()) { obj ->
                if (shouldDelayClick(obj.typeOfHit)) {
                    if (obj.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                        val entity = obj.entityHit

                        if (entity is EntityLivingBase)
                            attackEntity(entity)

                    } else
                        mc.clickMouse()

                    lastMovingObjectPosition = obj
                    lastTimeAttack = System.currentTimeMillis()
                }

                if (isLastClicks)
                    mc.sendClickBlockToController(false)
            }

        if (targetModeValue.get().equals("Switch", true)) {
            if (attackTimer.hasTimePassed(switchDelayValue.get().toLong())) {
                prevTargetEntities.add(target!!.entityId)
                attackTimer.reset()
            }
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

    private fun shouldDelayClick(typeOfHit: MovingObjectPosition.MovingObjectType): Boolean {

        if (!useHitDelay.get())
            return false

        return lastMovingObjectPosition?.let {
            it.typeOfHit != typeOfHit && 
            System.currentTimeMillis() - lastTimeAttack <= hitDelay.get()
        } ?: false
    }

    private fun updateHitable() {
        val rotation = RotationUtils.currentRotation ?: mc.thePlayer.rotation

        val target = this.target ?: return

        val raycastEntity = RaycastUtils.raycastEntity(rangeValue.get().toDouble(), rotation.yaw, rotation.pitch, object: RaycastUtils.IEntityFilter {
            override fun canRaycast(entity: Entity?): Boolean {
                return entity is EntityLivingBase && entity !is EntityArmorStand && isSelected(entity, true)
            }
        })

        hitable = if (raycastValue.get()) raycastEntity == this.target else RotationUtils.isFaced(target, rangeValue.get().toDouble(), rotation)

        if (!hitable)
            return

        val targetToCheck = raycastEntity ?: this.target ?: return

        if (targetToCheck.hitBox.isVecInside(mc.thePlayer.getPositionEyes(1f)))
            return

        val eyes = mc.thePlayer.getPositionEyes(1f)

        val intercept = targetToCheck.hitBox.calculateIntercept(eyes,
            eyes + RotationUtils.getVectorForRotation(rotation) * rangeValue.get().toDouble()
        )

        hitable = mc.theWorld.rayTraceBlocks(mc.thePlayer.eyes, intercept.hitVec) == null || mc.thePlayer.getDistanceToEntityBox(targetToCheck) < throughWallsRangeValue.get()

        if (hitableCheckValue.get() && mc.objectMouseOver != null)
            hitable = mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY
    }

    private fun updateRotations(entity: EntityLivingBase): Boolean {
        if (rotationValue.get().equals("none", true) || turnSpeed.getMaxValue() <= 0.0f)
            return true

        val rotation = getTargetRotation(entity) ?: return false
        rotation.fixedSensitivity(mc.gameSettings.mouseSensitivity)

        if (silentRotationValue.get()) {
            val movementCorrectionType = when (movementCorrection.get().lowercase()) {
                "strict" -> MovementCorrection.Type.STRICT
                "normal" -> MovementCorrection.Type.NORMAL
                else -> MovementCorrection.Type.NONE
            }

            RotationUtils.setTargetRotation(rotation, 10, turnSpeed.getMinValue(), turnSpeed.getMaxValue(), movementCorrectionType)
        } else {
            val limitRotation = RotationUtils.limitAngleChange(mc.thePlayer.rotation, rotation, RandomUtils.nextFloat(turnSpeed.getMinValue(), turnSpeed.getMaxValue()))
            limitRotation.toPlayer(mc.thePlayer)
        }

        return true
    }

    private fun attackEntity(entity: EntityLivingBase) {
        blockingMode.onPreAttack()

        if (shouldDelayClick(MovingObjectPosition.MovingObjectType.ENTITY))
            return

        when (swingValue.get().lowercase()) {
            "normal" -> mc.thePlayer.swingItem()
            "packet" -> mc.netHandler.addToSendQueue(C0APacketAnimation())
        }

        mc.playerController.attackEntity(mc.thePlayer, entity)

        if (interactValue.get()) {
            mc.playerController.isPlayerRightClickingOnEntity(mc.thePlayer, mc.objectMouseOver.entityHit, mc.objectMouseOver)
            mc.playerController.interactWithEntitySendPacket(mc.thePlayer, mc.objectMouseOver.entityHit)
        }

        blockingMode.onPostAttack()
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

    fun startBlocking() {
        if (canBlock && mc.thePlayer.getDistanceToEntityBox(target ?: return) <= autoBlockRangeValue.get()) {
            mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()))
            blockingStatus = true
        }
    }

    fun stopBlocking() {
        if (blockingStatus) {
            mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
            blockingStatus = false
        }
    }

    val canBlock: Boolean
        get() = mc.thePlayer.heldItem != null && mc.thePlayer.heldItem.item is ItemSword

    private val predictSize: Float
        get() = RandomUtils.nextFloat(predictSizeValue.getMinValue(), predictSizeValue.getMaxValue())

    override val tag: String
        get() = targetModeValue.get()
}