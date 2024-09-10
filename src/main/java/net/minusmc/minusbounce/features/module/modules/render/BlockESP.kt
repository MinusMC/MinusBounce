package net.minusmc.minusbounce.features.module.modules.render

import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.Render3DEvent
import net.minusmc.minusbounce.event.UpdateEvent
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.utils.block.BlockUtils
import net.minusmc.minusbounce.utils.render.ColorUtils
import net.minusmc.minusbounce.utils.render.RenderUtils
import net.minusmc.minusbounce.utils.timer.MSTimer
import net.minusmc.minusbounce.value.BlockValue
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.IntegerValue
import net.minusmc.minusbounce.value.ListValue
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import java.awt.Color

@ModuleInfo(name = "BlockESP", spacedName = "Block ESP", description = "Allows you to see a selected block through walls.", category = ModuleCategory.RENDER)
class BlockESP: Module() {
    private val modeValue = ListValue("Mode", arrayOf("Box", "2D"), "Box")

    private val blockValue = BlockValue("Block", 168)
    private val radiusValue = IntegerValue("Radius", 40, 5, 120)
    private val limitFoundValue = IntegerValue("LimitFoundBlocks", 256, 1, 2048)

    private val colorRedValue = IntegerValue("Red", 255, 0, 255)
    private val colorGreenValue = IntegerValue("Green", 179, 0, 255)
    private val colorBlueValue = IntegerValue("Blue", 72, 0, 255)
    private val colorRainbow = BoolValue("Rainbow", false)

    private val searchTimer = MSTimer()
    private val posList = mutableListOf<BlockPos>()
    private var thread: Thread? = null

    override fun onDisable() {
        thread?.stop()
        thread = null
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val thread = this.thread

        if (searchTimer.hasTimePassed(1000L) && (thread == null || !thread.isAlive()) && blockValue.block != Blocks.air) {
            this.thread = Thread {
                val blocks = BlockUtils.searchBlocks(radiusValue.get(), blockValue.block, limitFoundValue.get())
                searchTimer.reset()

                synchronized(posList) {
                    posList.clear()
                    posList += blocks
                }
            }

            this.thread?.start()
        }
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) = synchronized(posList) {
        val color = if (colorRainbow.get())
            ColorUtils.rainbow()
        else Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get())

        for (blockPos in posList) {
            when (modeValue.get().lowercase()) {
                "box" -> RenderUtils.drawBlockBox(blockPos, color, true)
                "2d" -> RenderUtils.draw2D(blockPos, color.rgb, Color.BLACK.rgb)
            }
        }
    }

    override val tag: String
        get() = blockValue.block.localizedName
}