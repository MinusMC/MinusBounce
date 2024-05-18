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
    var counter = -1
    var freezing = false

    protected val killAura: KillAura
		get() = MinusBounce.moduleManager[KillAura::class.java]!!

    private val ticks = IntegerValue("Ticks", 3, 1, 10)

    override fun onEnable() {
        counter = -1
        freezing = false
    }

    fun getExtraTicks(): Int {
        if(counter-- > 0)
            return -1
        freezing = false

        val isInRange = 
            if(killAura.state) 
                killAura.target == null || mc.thePlayer.getDistanceToEntityBox(killAura.target!!) > killAura.rangeValue.get() 
            else false

        if (isInRange && mc.thePlayer.hurtTime <= 2) {
            counter = ticks.get()
            return counter
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