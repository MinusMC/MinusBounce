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
    private val ticks = IntegerValue("Ticks", 3, 1, 10)

    private var counter = -1
    var freezing = false
    private var canTickBase = true

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

        killAura.target?.let { targetDistance = mc.thePlayer.getDistanceToEntityBox(it) } ?: run {
            canTickBase = true
        }

        if (killAura.state && targetDistance > killAura.rangeValue.get()) {
            if (targetDistance <= killAura.rotationRangeValue.get() && mc.thePlayer.hurtTime <= 2 && canTickBase) {
                canTickBase = false
                counter = ticks.get()
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