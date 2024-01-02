/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.render

import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.MotionEvent
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.utils.EntityUtils
import net.minusmc.minusbounce.utils.extensions.getDistanceToEntityBox
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.FloatValue

@ModuleInfo(name = "NoRender", spacedName = "No Render", description = "Increase FPS by decreasing or stop rendering visible entities.", category = ModuleCategory.RENDER)
class NoRender : Module() {

    private val allValue = BoolValue("All", true)
	val nameTagsValue = BoolValue("NameTags", true)
    private val itemsValue = BoolValue("Items", true) { !allValue.get() }
    private val playersValue = BoolValue("Players", true) { !allValue.get() }
    private val mobsValue = BoolValue("Mobs", true) { !allValue.get() }
    private val animalsValue = BoolValue("Animals", true) { !allValue.get() }
    private val armorStandValue = BoolValue("ArmorStand", true) { !allValue.get() }
    private val autoResetValue = BoolValue("AutoReset", true)
    private val maxRenderRange = FloatValue("MaxRenderRange", 4F, 0F, 16F, "m")

    @EventTarget
    fun onMotion(event: MotionEvent) {
    	for (en in mc.theWorld.loadedEntityList) {
            val entity = en!!
    		if (shouldStopRender(entity))
    			entity.renderDistanceWeight = 0.0
            else if (autoResetValue.get())
                entity.renderDistanceWeight = 1.0
    	}
    }

	fun shouldStopRender(entity: Entity): Boolean {
		return (allValue.get()
                ||(itemsValue.get() && entity is EntityItem)
    			|| (playersValue.get() && entity is EntityPlayer)
    			|| (mobsValue.get() && EntityUtils.isMob(entity))
    			|| (animalsValue.get() && EntityUtils.isAnimal(entity))
                || (armorStandValue.get() && entity is EntityArmorStand))
    			&& entity != mc.thePlayer!!
				&& (mc.thePlayer!!.getDistanceToEntityBox(entity).toFloat() > maxRenderRange.get())
	}

 	override fun onDisable() {
 		for (en in mc.theWorld.loadedEntityList) {
            val entity = en!!
 			if (entity != mc.thePlayer!! && entity.renderDistanceWeight <= 0.0)
 				entity.renderDistanceWeight = 1.0
 		}
 	}

}