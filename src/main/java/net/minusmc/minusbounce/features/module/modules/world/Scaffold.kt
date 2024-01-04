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
import net.minecraft.util.*
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.features.module.modules.world.scaffold.TowerScaffold
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
import net.minusmc.minusbounce.value.*
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.*

@ModuleInfo(name = "Scaffold", description = "Automatically places blocks beneath your feet.", category = ModuleCategory.WORLD, keyBind = Keyboard.KEY_I)
class Scaffold: Module() {
    //Tower modes
    private val towerModes = ClassUtils.resolvePackage("${this.javaClass.`package`.name}.killaura.tower", TowerScaffold::class.java)
        .map { it.newInstance() as TowerScaffold }
        .sortedBy { it.modeName }

    private val towerMode: TowerScaffold
        get() = towerModes.find { towerModeValue.get().equals(it.modeName, true) } ?: throw NullPointerException()

    private val placeableDelay = ListValue("PlaceableDelay", arrayOf("Normal", "Smart", "Off"), "Normal")
    private val delayValue = IntRangeValue("Delay", 0, 0, 0, 1000) {!placeableDelay.get().equals("off", true)}

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

    val rotationsValue = ListValue("Rotation", arrayOf("Normal", "AAC", "Novoline", "Intave", "Rise", "Backwards", "Custom", "None"), "Normal")
    private val aacOffsetValue = FloatValue("AAC-Offset", 4f, 0f, 50f, "°") { rotationsValue.get().equals("aac", true) }

    private val customYawValue = FloatValue("Custom-Yaw", 135F, -180F, 180F, "°") {
        rotationsValue.get().equals("custom", true)
    }
    private val customPitchValue = FloatValue("Custom-Pitch", 86F, -90F, 90F, "°") {
        rotationsValue.get().equals("custom", true)
    }

    private val turnSpeed = FloatRangeValue("TurnSpeed", 180f, 180f, 0f, 180f) {!rotationsValue.get().equals("None", true)}
    private val keepLengthValue = IntegerValue("KeepRotationLength", 0, 0, 20) {
        !rotationsValue.get().equals("None", true)
    }
    private val placeConditionValue = ListValue("PlaceCondition", arrayOf("Always", "Air", "FallDown"), "Always")

    private val timerValue = FloatValue("Timer", 1F, 0.1F, 10F)
    private val speedModifierValue = FloatValue("SpeedModifier", 1F, 0f, 2F, "x")
    private val xzMultiplier = FloatValue("XZ-Multiplier", 1F, 0F, 4F, "x")
    val movementCorrection = BoolValue("MovementCorrection", true)
    
    // Tower
    private val onTowerValue = ListValue("OnTower", arrayOf("Always", "PressSpace", "NoMove", "Off"))
    private val towerModeValue = ListValue("TowerMode", towerModes.map {it.modeName}.toTypedArray(), "Jump") {
        !onTowerValue.get().equals("None", true)
    }

    private val stopWhenBlockAbove = BoolValue("StopWhenBlockAbove", false) { !onTowerValue.get().equals("None", true) }

    private val sameYValue = ListValue("SameY", arrayOf("Same", "AutoJump", "MotionY", "DelayedTower", "Off"), "Off")
    private val blocksPerJump = IntegerValue("BlocksPerJump", 5, 0, 10)

    private val safeWalkValue = ListValue("SafeWalk", arrayOf("Ground", "Air", "Off"), "Off")

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

    var towerStatus = false


    // Same Y
    private var canSameY = false
    private var blocksStart = 0
    private var delayedTowerTicks = 0

    override fun onEnable() {
        mc.thePlayer ?: return

        blocksStart = blocksAmount

        delayedTowerTicks = 0

        progress = 0f
        spinYaw = 0f
        launchY = mc.thePlayer.posY.toInt()
        slot = mc.thePlayer.inventory.currentItem

        lastMS = System.currentTimeMillis()
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
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
                "blocksjump" -> if (blocksStart - blocksAmount >= blocksPerJump.get()) {
                    canSameY = false
                    if (mc.thePlayer.onGround && MovementUtils.isMoving) {
                        mc.thePlayer.jump()
                        blocksStart = blocksAmount
                    }
                } else canSameY = true
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

        // Down
        shouldGoDown = downValue.get() && GameSettings.isKeyDown(mc.gameSettings.keyBindSneak) && blocksAmount > 1
        if (shouldGoDown) mc.gameSettings.keyBindSneak.pressed = false


        if (mc.thePlayer.onGround) {
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
    }

    @EventTarget
    fun onPreUpdate(event: PreUpdateEvent) {
        findBlock(expandLengthValue.get() > 1 && !towerStatus)
    }

    @EventTarget
    fun onPreMotion(event: PreMotionEvent) {
        towerMode.onPreMotion(event)

        if (!placeCondition || if (!autoBlockMode.get().equals("off", true)) InventoryUtils.findAutoBlockBlock() == -1 else mc.thePlayer.heldItem == null || !(mc.thePlayer.heldItem.item is ItemBlock && isBlockToScaffold(mc.thePlayer.heldItem.item as ItemBlock))) {
            return
        }

        if (placeModeValue.get().equals("pre", true)) place()

        if (targetPlace == null && !placeableDelay.get().equals("Off", true) && !towerStatus) {
            delayTimer.reset()
        }
    }

    @EventTarget
    fun onPostMotion(event: PostMotionEvent) {
        towerStatus = false

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

        towerMode.onPostMotion()

        if (placeModeValue.get().equals("post", true)) place()

        if (targetPlace == null && !placeableDelay.get().equals("Off", true) && !towerStatus) {
            delayTimer.reset()
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
            delay = TimeUtils.randomDelay(delayValue.getMinValue(), delayValue.getMaxValue())

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

        RotationUtils.setTargetRot(RotationUtils.serverRotation!!, 0)
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

        if (!rotationsValue.get().equals("None", true) && !towerStatus) {
            lockRotation = when(rotationsValue.get().lowercase()) {
                "custom" -> Rotation(mc.thePlayer.rotationYaw + customYawValue.get(), customPitchValue.get())
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
            if (!rotationsValue.get().equals("None", true) && lockRotation != null) {
                RotationUtils.setTargetRot(lockRotation!!, keepLengthValue.get())
            }
        }

        targetPlace = placeRotation.placeInfo
        return true
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

    private val placeCondition: Boolean
        get() = when (placeConditionValue.get().lowercase()) {
            "always" -> true
            "air" -> !mc.thePlayer.onGround
            "falldown" -> mc.thePlayer.fallDistance > 0f
            else -> false
        }

    private val rotationSpeed: Float
        get() = (Math.random() * (turnSpeed.getMaxValue() - turnSpeed.getMinValue()) + turnSpeed.getMinValue()).toFloat()

    override val tag: String
        get() = if (towerStatus) "Tower, ${towerModeValue.get()}" else placeModeValue.get()
}