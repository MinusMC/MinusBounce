/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.player

import net.minecraft.block.BlockLiquid
import net.minecraft.util.AxisAlignedBB
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.features.module.modules.movement.Fly
import net.minusmc.minusbounce.features.module.modules.player.nofalls.NoFallMode
import net.minusmc.minusbounce.features.module.modules.render.FreeCam
import net.minusmc.minusbounce.utils.ClassUtils
import net.minusmc.minusbounce.utils.PlayerUtils
import net.minusmc.minusbounce.utils.block.BlockUtils
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.ListValue

@ModuleInfo(name = "NoFall", spacedName = "No Fall", description = "Prevents you from taking fall damage.", category = ModuleCategory.PLAYER)
class NoFall : Module() {
    private val modes = ClassUtils.resolvePackage("${this.javaClass.`package`.name}.nofalls", NoFallMode::class.java)
        .map{ it.newInstance() as NoFallMode }
        .sortedBy{ it.modeName }

    private val mode: NoFallMode
        get() = modes.find{ modeValue.get() == it.modeName } ?: throw NullPointerException()

    val modeValue: ListValue = object: ListValue("Mode", modes.map{ it.modeName }.toTypedArray()) {
        override fun onPreChange(oldValue: String, newValue: String) {
            if (state) onDisable()
        }
        override fun onPostChange(oldValue: String, newValue: String) {
            if (state) onEnable()
        }
    }

    private val voidCheckValue = BoolValue("Void-Check", true)
    
    override fun onInitialize() {
        modes.map {mode -> mode.values.forEach {
            value -> value.name = "${mode.modeName}-${value.name}"
        }}
    }

    override fun onEnable() {
        mode.onEnable()
    }
    
    override fun onDisable() {
        mc.timer.timerSpeed = 1.0f
        mode.onDisable()
    }

    @EventTarget(ignoreCondition = true)
    fun onUpdate(event: UpdateEvent) {
        if (!state || freeCamModule.state || mc.thePlayer.isSpectator || mc.thePlayer.capabilities.allowFlying || mc.thePlayer.capabilities.disableDamage)
            return

        if (!flyModule.state && voidCheckValue.get() && !PlayerUtils.isBlockUnder)
            return

        val firstBoundingBox = mc.thePlayer.entityBoundingBox
        val secondBoundingBox = AxisAlignedBB(firstBoundingBox.maxX, firstBoundingBox.maxY, firstBoundingBox.maxZ, firstBoundingBox.minX, firstBoundingBox.minY - 0.01, firstBoundingBox.minZ)

        if (BlockUtils.collideBlock(firstBoundingBox) { it is BlockLiquid } || BlockUtils.collideBlock(secondBoundingBox) {it is BlockLiquid})
            return

        mode.onUpdate()
    }

    @EventTarget
    fun onPreMotion(event: PreMotionEvent) {
        if (!flyModule.state && voidCheckValue.get() && !PlayerUtils.isBlockUnder)
            return

        mode.onPreMotion(event)
    }

    @EventTarget
    fun onPostMotion(event: PostMotionEvent) {
        if (!flyModule.state && voidCheckValue.get() && !PlayerUtils.isBlockUnder)
            return

        mode.onPostMotion(event)
    }

    @EventTarget
    fun onSentPacket(event: SentPacketEvent) {
        if (!flyModule.state && voidCheckValue.get() && !PlayerUtils.isBlockUnder)
            return

        mode.onSentPacket(event)
    }

    @EventTarget
    fun onReceivedPacket(event: ReceivedPacketEvent) {
        if (!flyModule.state && voidCheckValue.get() && !PlayerUtils.isBlockUnder)
            return

        mode.onReceivedPacket(event)
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        if (!flyModule.state && voidCheckValue.get() && !PlayerUtils.isBlockUnder)
            return

        val firstBoundingBox = mc.thePlayer.entityBoundingBox
        val secondBoundingBox = AxisAlignedBB(firstBoundingBox.maxX, firstBoundingBox.maxY, firstBoundingBox.maxZ, firstBoundingBox.minX, firstBoundingBox.minY - 0.01, firstBoundingBox.minZ)

        if (BlockUtils.collideBlock(firstBoundingBox) { it is BlockLiquid } || BlockUtils.collideBlock(secondBoundingBox) {it is BlockLiquid})
            return

        mode.onMove(event)
    }

    @EventTarget(ignoreCondition = true)
    fun onJump(event: JumpEvent) {
        mode.onJump()
    }

    @EventTarget(ignoreCondition = true)
    fun onRender2D(event: Render2DEvent) {
        mode.onRender2D()
    }

    private val flyModule: Fly
        get() = MinusBounce.moduleManager[Fly::class.java]!!

    private val freeCamModule: FreeCam
        get() = MinusBounce.moduleManager[FreeCam::class.java]!!

    override val tag: String
        get() = modeValue.get()

    override val values = super.values.toMutableList().also {
        modes.map {
            mode -> mode.values.forEach { value ->
                val displayableFunction = value.displayableFunction
                it.add(value.displayable { displayableFunction.invoke() && modeValue.get() == mode.modeName })
            }
        }
    }
}
