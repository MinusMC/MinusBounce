package net.minusmc.minusbounce.features.module.modules.combat

import net.minecraft.client.settings.GameSettings
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.value.*
import net.minecraft.entity.player.EntityPlayer
import net.minusmc.minusbounce.utils.extensions.getDistanceToEntityBox
import net.minusmc.minusbounce.utils.timer.TickTimer

/**
 * @author KevinClient
 */

@ModuleInfo(name = "KeepRange", spacedName = "Keep Range", description = "Keep yourself out of a range with your target.", category = ModuleCategory.COMBAT)
class KeepRange : Module() {
	private val modeValue = ListValue("Mode", arrayOf("Release", "CancelMovement"), "Release")
    private val minDistance = FloatValue("MinDistance", 2.3F, 0F, 4F, "m")
    private val maxDistance = FloatValue("MaxDistance", 4.0F, 3F, 7F, "m")

    private val onlyForward = BoolValue("OnlyForward", true)
    private val onlyNoHurt = BoolValue("OnlyNoHurt", true)

    private val keepTick = IntegerValue("KeepTick", 10, 0, 40, "tick")
    private val restTick = IntegerValue("RestTick", 4, 0, 40, "tick")

    private val tickTimer = TickTimer()
    private var target: EntityPlayer? = null
    

    @EventTarget
    fun onAttack(event: AttackEvent) {
    	if (event.targetEntity is EntityPlayer)
    		target = event.targetEntity
    }

    @EventTarget 
    fun onStrafe(event: StrafeEvent) {
        if (!modeValue.get().equals("cancelmove", true))
        	return

        target?.let {
        	val distance = mc.thePlayer.getDistanceToEntityBox(it)

            if (distance <= minDistance.get() && !tickTimer.hasTimePassed(keepTick.get()))
                if (!onlyForward.get() || event.forward > 0F)
                    event.cancelEvent()
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
    	target?.let {
    		if (onlyNoHurt.get() && mc.thePlayer.hurtTime > 0)
    			return

	        if (tickTimer.hasTimePassed(keepTick.get() + restTick.get())) 
	        	tickTimer.reset()

	        tickTimer.update()
	        val distance = mc.thePlayer.getDistanceToEntityBox(it)

	        if (it.isDead || distance >= maxDistance.get()) {
	            target = null
	            mc.gameSettings.keyBindForward.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindForward)
	            mc.gameSettings.keyBindBack.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindBack)
	            mc.gameSettings.keyBindRight.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindRight)
	            mc.gameSettings.keyBindLeft.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindLeft)
	            return
	        }

	        if (modeValue.get().equals("release", true)) {
	            if (distance <= minDistance.get() && !tickTimer.hasTimePassed(keepTick.get())) {
	            	mc.gameSettings.keyBindForward.pressed = false

	                if (!onlyForward.get()) {
	                	mc.gameSettings.keyBindBack.pressed = false
			            mc.gameSettings.keyBindRight.pressed = false
			            mc.gameSettings.keyBindLeft.pressed = false
	                }
	                return
	            }

	            mc.gameSettings.keyBindForward.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindForward)
	            mc.gameSettings.keyBindBack.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindBack)
	            mc.gameSettings.keyBindRight.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindRight)
	            mc.gameSettings.keyBindLeft.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindLeft)
	        }
    	}
    }
}