/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
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
import net.minusmc.minusbounce.utils.extensions.*
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.*
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.features.module.modules.world.scaffold.TowerScaffold
import net.minusmc.minusbounce.ui.font.Fonts
import net.minusmc.minusbounce.utils.*
import net.minusmc.minusbounce.utils.block.BlockUtils
import net.minusmc.minusbounce.utils.block.PlaceInfo
import net.minusmc.minusbounce.utils.block.PlaceInfo.Companion.get
import net.minusmc.minusbounce.injection.access.StaticStorage
import net.minusmc.minusbounce.utils.extensions.rayTraceWithServerSideRotation
import net.minusmc.minusbounce.utils.render.BlurUtils
import net.minusmc.minusbounce.utils.render.RenderUtils
import net.minusmc.minusbounce.utils.timer.MSTimer
import net.minusmc.minusbounce.utils.timer.TimeUtils
import net.minusmc.minusbounce.value.*
import net.minecraft.block.material.Material
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.*

@ModuleInfo(name = "Scaffold", description = "Automatically places blocks beneath your feet.", category = ModuleCategory.WORLD, keyBind = Keyboard.KEY_I)
class Scaffold: Module() {
    //Tower modes
    private val towerModes = ClassUtils.resolvePackage("${this.javaClass.`package`.name}.scaffold.tower", TowerScaffold::class.java)
        .map { it.newInstance() as TowerScaffold }
        .sortedBy { it.modeName }

    private val towerMode: TowerScaffold
        get() = towerModes.find { towerModeValue.get().equals(it.modeName, true) } ?: throw NullPointerException()

    private val placeableDelay = BoolValue("PlaceableDelay", false)
    private val delayValue = IntRangeValue("Delay", 0, 0, 0, 1000) {placeableDelay.get()}

    private val autoBlockMode = ListValue("AutoBlock", arrayOf("Spoof", "Switch", "Off"), "Spoof")
    private val sprintModeValue = ListValue("SprintMode", arrayOf("Always", "OnGround", "OffGround", "Off"), "Off")

    private val swingValue = ListValue("Swing", arrayOf("Normal", "Packet", "Off"), "Normal")
    private val placeModeValue = ListValue("PlaceTiming", arrayOf("Pre", "Post", "Legit"), "Post")

    private val eagleValue = ListValue("Eagle", arrayOf("Normal", "Slient", "Off"), "Off")
    private val blocksToEagleValue = IntegerValue("BlocksToEagle", 0, 0, 10) { !eagleValue.get().equals("Off", true) }
    private val eagleEdgeDistanceValue = FloatValue("EagleEdgeDistance", 0.2F, 0F, 0.5F, "m") {
        !eagleValue.get().equals("Off", true)
    }
    private val expandLengthValue = IntegerValue("ExpandLength", 1, 1, 6, " blocks")

    val rotationsValue = ListValue("Rotation", arrayOf("Normal", "AAC", "None"), "Normal")
    private val yaw = FloatValue("Yaw-Offset", 180f, 0f, 180f, "°")
    val raycast = BoolValue("RayCast", false)
    private val turnSpeed = FloatRangeValue("TurnSpeed", 180f, 180f, 0f, 180f) {!rotationsValue.get().equals("None", true)}
    private val keepLengthValue = IntegerValue("KeepRotationLength", 0, 0, 20) {
        !rotationsValue.get().equals("None", true)
    }

    private val timerValue = FloatValue("Timer", 1F, 0.1F, 10F)
    private val speedModifierValue = FloatValue("SpeedModifier", 1F, 0f, 2F, "x")
    val movementCorrection = BoolValue("MovementCorrection", true)
    
    // Tower
    private val onTowerValue = ListValue("OnTower", arrayOf("Always", "PressSpace", "Off"))
    private val towerModeValue = ListValue("TowerMode", towerModes.map {it.modeName}.toTypedArray(), "Jump") {
        !onTowerValue.get().equals("None", true)
    }

    private val sameYValue = ListValue("SameY", arrayOf("Same", "AutoJump", "MotionY", "DelayedTower", "Off"), "Off")
    private val safeWalkValue = ListValue("SafeWalk", arrayOf("Ground", "Air", "Off"), "Off")
    private val counterDisplayValue = BoolValue("Counter", true)

    private val markValue = BoolValue("Mark", false)
    private val redValue = IntegerValue("Red", 0, 0, 255) { markValue.get() }
    private val greenValue = IntegerValue("Green", 120, 0, 255) { markValue.get() }
    private val blueValue = IntegerValue("Blue", 255, 0, 255) { markValue.get() }

    private var targetPlace: PlaceInfo? = null
    private var lockRotation: Rotation? = null

    // Launch pos
    private var launchY = 0

    // Render thingy
    private var progress = 0f
    private var lastMS = 0L

    // AutoBlock
    private var slot = -1

    // Delay
    private val delayTimer = MSTimer()
    private var clicks = 0
    private var delay = 0L

    // Eagle
    private var placedBlocksWithoutEagle = 0
    private var eagleSneaking = false

    // Down
    private var down = false

    //Tower
    var towerStatus = false

    // Same Y
    private var canSameY = false
    private var delayedTowerTicks = 0

    override fun onEnable() {
        mc.thePlayer ?: return

        delayedTowerTicks = 0

        progress = 0f
        launchY = mc.thePlayer.posY.toInt()
        slot = mc.thePlayer.inventory.currentItem

        lastMS = System.currentTimeMillis()
    }

    override fun onDisable() {
        mc.thePlayer ?: return

        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)) {
            mc.gameSettings.keyBindSneak.pressed = false
            if (eagleSneaking) mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING))
        }

        lockRotation = null
        mc.timer.timerSpeed = 1f
        down = false

        RotationUtils.setTargetRot(RotationUtils.serverRotation!!, 0)
        if (slot != mc.thePlayer.inventory.currentItem) mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (!towerStatus) mc.timer.timerSpeed = timerValue.get()

        if (towerStatus || mc.thePlayer.isCollidedHorizontally) {
            canSameY = false
            launchY = mc.thePlayer.posY.toInt()
        } else {
            canSameY = when (sameYValue.get().lowercase()) {
                "simple" -> true
                "autojump" -> {
                    if (mc.thePlayer.onGround && MovementUtils.isMoving) mc.thePlayer.jump()
                    true
                }
                "motiony" -> {
                    if (mc.thePlayer.onGround && MovementUtils.isMoving) mc.thePlayer.motionY = 0.42
                    false
                }
                "delayedtower" -> {
                    if (mc.thePlayer.onGround && MovementUtils.isMoving) {
                        mc.thePlayer.jump()
                        delayedTowerTicks++
                    }
                    delayedTowerTicks % 2 == 0
                }
                else -> false
            }
        
            if (mc.thePlayer.onGround) launchY = mc.thePlayer.posY.toInt()
        }        

        if (mc.thePlayer.onGround && !eagleValue.get().equals("Off", true)) {
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

    @EventTarget
    fun onClick(event: ClickEvent){
        if (placeModeValue.get().equals("legit", true)){loopPlace()}
    }
    
    fun loopPlace(){
        if (targetPlace != null){
            while (clicks > 0) {
                place()
                clicks--
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        mc.thePlayer ?: return
        val packet = event.packet

        if (packet is C09PacketHeldItemChange) {
            if (packet.slotId == slot) {
				event.cancelEvent()
			} else {
                slot = packet.slotId
			}
		}

		if (packet is C08PacketPlayerBlockPlacement) {
			packet.facingX = packet.facingX.coerceIn(-1.00000F, 1.00000F)
			packet.facingY = packet.facingY.coerceIn(-1.00000F, 1.00000F)
			packet.facingZ = packet.facingZ.coerceIn(-1.00000F, 1.00000F)
		}

    }

    @EventTarget
    fun onPreMotion(event: PreMotionEvent) {
        findBlock(expandLengthValue.get() > 1 && !towerStatus)

        if (isNotBlock || isNotReplaceable) return

        if (placeModeValue.get().equals("pre", true)) loopPlace()

        if (towerStatus)
            towerMode.onPreMotion(event)
    }

    @EventTarget
    fun onPostMotion(event: PostMotionEvent) {
        towerStatus = BlockUtils.getBlock(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + 2, mc.thePlayer.posZ)) is BlockAir

        val isMoving = mc.gameSettings.keyBindLeft.isKeyDown || mc.gameSettings.keyBindRight.isKeyDown || mc.gameSettings.keyBindForward.isKeyDown || mc.gameSettings.keyBindBack.isKeyDown

        if (towerStatus)
            towerStatus = when (onTowerValue.get().lowercase()) {
                "always" -> isMoving
                "pressspace" -> mc.gameSettings.keyBindJump.isKeyDown
                else -> false
            }

        if (placeModeValue.get().equals("post", true)) loopPlace()

        if (towerStatus)
            towerMode.onPostMotion()
    }

    private fun findBlock(expand: Boolean) {
        val blockPosition = when {
            !canSameY && mc.thePlayer.posY == mc.thePlayer.posY.toInt() + 0.5 -> BlockPos(mc.thePlayer)
            canSameY && launchY <= mc.thePlayer.posY -> BlockPos(mc.thePlayer.posX, launchY - 1.0, mc.thePlayer.posZ)
            else -> BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ).down()
        }

        if (expand) {
            for (i in 0 until expandLengthValue.get()) {
                val x = if (mc.thePlayer.horizontalFacing == EnumFacing.WEST) -i else if (mc.thePlayer.horizontalFacing == EnumFacing.EAST) i else 0
                val z = if (mc.thePlayer.horizontalFacing == EnumFacing.NORTH) -i else if (mc.thePlayer.horizontalFacing == EnumFacing.SOUTH) i else 0
                if (search(blockPosition.add(x, 0, z))) return
            }
        } else {
            if (!BlockUtils.isReplaceable(blockPosition) || search(blockPosition)) 
                return

            for (x in -1..1) {
                for (z in -1..1) {
                    if (search(blockPosition.add(x, 0, z))) return
                }
            }
        }
    }

    private fun place() {
        if(lockRotation != null)
            RotationUtils.setTargetRot(lockRotation!!, keepLengthValue.get())
        
        var blockSlot = -1
        var itemStack = mc.thePlayer.heldItem
        
        if (mc.thePlayer.heldItem == null || !(itemStack.item is ItemBlock && isBlockToScaffold(itemStack.item as ItemBlock))) {
            if (autoBlockMode.get().equals("Off", true)) return
        
            blockSlot = InventoryUtils.findAutoBlockBlock()
            if (blockSlot == -1) return
        
            if (autoBlockMode.get().equals("Spoof", true))
                mc.netHandler.addToSendQueue(C09PacketHeldItemChange(blockSlot - 36))
            else
                mc.thePlayer.inventory.currentItem = blockSlot - 36
        
            itemStack = mc.thePlayer.inventoryContainer.getSlot(blockSlot).stack
        }
        
        if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, itemStack, targetPlace!!.blockPos, targetPlace!!.enumFacing, targetPlace!!.vec3)) {
        
            if (mc.thePlayer.onGround) {
                mc.thePlayer.motionX *= speedModifierValue.get().toDouble()
                mc.thePlayer.motionZ *= speedModifierValue.get().toDouble()
            }
        
            when (swingValue.get().lowercase()) {
                "normal" -> mc.thePlayer.swingItem()
                "packet" -> mc.netHandler.addToSendQueue(C0APacketAnimation())
            }
        }

        targetPlace = null        
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        if (safeWalkValue.get().equals("air", true) || mc.thePlayer.onGround) 
            event.isSafeWalk = true
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

        if(counterDisplayValue.get()) {
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
        if (markValue.get()){
            for (i in 0 until (expandLengthValue.get() + 1)) {
                val xOffset = if (mc.thePlayer.horizontalFacing == EnumFacing.WEST) -i else if (mc.thePlayer.horizontalFacing == EnumFacing.EAST) i else 0
                val yOffset = mc.thePlayer.posY - if (mc.thePlayer.posY == mc.thePlayer.posY.toInt() + 0.5) 0.0 else 1.0
                val zOffset = if (mc.thePlayer.horizontalFacing == EnumFacing.NORTH) -i else if (mc.thePlayer.horizontalFacing == EnumFacing.SOUTH) i else 0
    
                val blockPos = BlockPos(
                    mc.thePlayer.posX + xOffset,
                    yOffset,
                    mc.thePlayer.posZ + zOffset
                )
    
                val placeInfo = get(blockPos)
                
                if (BlockUtils.isReplaceable(blockPos) && placeInfo != null) {
                    RenderUtils.drawBlockBox(blockPos, Color(redValue.get(), greenValue.get(), blueValue.get(), 100), false)
                    break
                }
            }
        }

        if (targetPlace != null && delayTimer.hasTimePassed(delay)) {
            clicks++
            delayTimer.reset()
            delay = TimeUtils.randomDelay(delayValue.getMinValue(), delayValue.getMaxValue())
        }
    }

    private fun search(blockPosition: BlockPos): Boolean {
        if (!BlockUtils.isReplaceable(blockPosition)) return false

        val placeRotation = BlockUtils.searchBlock(blockPosition, raycast.get(), yaw.get()) ?: return false

        lockRotation = when (rotationsValue.get().lowercase()) {
            "normal" -> placeRotation.rotation
            "aac" -> Rotation(mc.thePlayer.rotationYaw + (if (mc.thePlayer.movementInput.moveForward < 0) 0 else 180), placeRotation.rotation.pitch)
            "none" -> null
            else -> return false
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
            "onground" -> mc.thePlayer.onGround
            "offground" -> !mc.thePlayer.onGround
            else -> true
        }

    private val isNotBlock: Boolean
        get() = if (!autoBlockMode.get().equals("off", true)) InventoryUtils.findAutoBlockBlock() == -1 else mc.thePlayer.heldItem == null
    
    private val isNotReplaceable: Boolean
        get() = !(mc.thePlayer.heldItem.item is ItemBlock && isBlockToScaffold(mc.thePlayer.heldItem.item as ItemBlock))
    
    private val rotationSpeed: Float
        get() = (Math.random() * (turnSpeed.getMaxValue() - turnSpeed.getMinValue()) + turnSpeed.getMinValue()).toFloat()

    override val tag: String
        get() = if (towerStatus) "Tower, ${towerModeValue.get()}" else placeModeValue.get()
}