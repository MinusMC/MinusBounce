package net.minusmc.minusbounce.features.module.modules.combat

import net.minecraft.entity.EntityLivingBase
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.EventState
import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.PostMotionEvent
import net.minusmc.minusbounce.event.UpdateEvent
import net.minusmc.minusbounce.event.Render3DEvent
import net.minusmc.minusbounce.value.ListValue
import net.minusmc.minusbounce.value.IntegerValue
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.utils.extensions.getDistanceToEntityBox

@ModuleInfo(name = "TickBase", spacedName = "Tick Base", description = "TickBase", category = ModuleCategory.COMBAT)
class TickBase: Module() {
	val modeValue = ListValue("Mode", arrayOf("Vestige", "FDP"), "FDP")
    private val ticksValue = IntegerValue("Ticks", 3, 1, 10) { modeValue.get().equals("Vestige", true) }
    private var ticks = 0

    private val ticksAmount = IntegerValue("BoostTicks", 10, 3, 20) { modeValue.get().equals("FDP", true) }
    private val boostAmount = FloatValue("BoostTimer", 10f, 1f, 50f) { modeValue.get().equals("FDP", true) }
    private val chargeAmount = FloatValue("ChargeTimer", 0.11f, 0.05f, 1f) { modeValue.get().equals("FDP", true) }

    private var counter = -1
    var freezing = false
    var targetTickBase: EntityLivingBase? = null

    private lateinit var killAura: KillAura

    override fun onInitialize() {
    	killAura = MinusBounce.moduleManager[KillAura::class.java] as KillAura
    }

	override fun onEnable() {
		counter = -1
        freezing = false
        mc.timer.timerSpeed = 1f
	}

	override fun onDisable() {
		mc.timer.timerSpeed = 1f
	}

	@EventTarget
    fun onUpdate(event: UpdateEvent) {
    	if (!modeValue.get().equals("FDP", true)) return
        if (ticks == ticksAmount.get()) {
            mc.timer.timerSpeed = chargeAmount.get()
            ticks --
        } else if (ticks > 1) {
            mc.timer.timerSpeed = boostAmount.get()
            ticks --
        } else if (ticks == 1) {
            mc.timer.timerSpeed = 1f
            ticks --
        }
    }

	@EventTarget
	fun onPostMotion(event: PostMotionEvent) {
		if (freezing && modeValue.get().equals("Vestige", true)) {
			mc.thePlayer.posX = mc.thePlayer.lastTickPosX
            mc.thePlayer.posY = mc.thePlayer.lastTickPosY
            mc.thePlayer.posZ = mc.thePlayer.lastTickPosZ
		}
	}

	@EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (freezing && modeValue.get().equals("Vestige", true)) mc.timer.renderPartialTicks = 0F
    }

    fun getExtraTicks(): Int {
		if (counter-- > 0) return -1
		freezing = false

		if (killAura.state && (killAura.target == null || mc.thePlayer.getDistanceToEntityBox(killAura.target!!) > killAura.rangeValue.get())) {
			if (targetTickBase != null && mc.thePlayer.hurtTime <= 2) {
				counter = ticksValue.get()
				return counter
			}
		}

		return 0
	}
}