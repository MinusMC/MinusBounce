/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.combat

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.utils.extensions.*
import net.minusmc.minusbounce.value.*

@ModuleInfo(name = "TickBase", description = "Tick Base", category = ModuleCategory.COMBAT)
class TickBase : Module() {
    private val extraTicksValue = IntegerValue("ExtraTicks", 3, 1, 10)
    private val inRangeValue = FloatValue("InRange", 3.5f, 0f, 8f)
    private val outRangeValue = FloatValue("OutRange", 4f, 0f, 8f)

    private var canTickBase = true
    private var counter = -1
    var freezing = false

    override fun onEnable() {
        counter = -1
        freezing = false
        canTickBase = true
    }

    fun getExtraTicks(): Int {
        if (counter-- > 0)
            return -1
            
        freezing = false

        val killAura = MinusBounce.moduleManager[KillAura::class.java] ?: return 0
        var targetDistance = -1.0

        val target = killAura.target

        if (target != null)
            targetDistance = mc.thePlayer.getDistanceToEntityBox(target)
        else
            canTickBase = true

        if (killAura.state && targetDistance > outRangeValue.get()) {
            if (targetDistance <= inRangeValue.get() && canTickBase) {
                canTickBase = false
                counter = extraTicksValue.get()
                return counter
            }
        }

        return 0
    }

    @EventTarget
    fun onPostMotion(event: PostMotionEvent) {
        if (freezing) {
            mc.thePlayer.posX = mc.thePlayer.lastTickPosX
            mc.thePlayer.posY = mc.thePlayer.lastTickPosY
            mc.thePlayer.posZ = mc.thePlayer.lastTickPosZ
        }
    }

    @EventTarget
    fun onRender(event: Render2DEvent) {
        if (freezing) mc.timer.renderPartialTicks = 0F
    }
}