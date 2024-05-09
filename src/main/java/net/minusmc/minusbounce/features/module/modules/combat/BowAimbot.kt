/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.combat

import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.Render3DEvent
import net.minusmc.minusbounce.event.UpdateEvent
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.utils.EntityUtils
import net.minusmc.minusbounce.utils.player.RotationUtils
import net.minusmc.minusbounce.utils.render.RenderUtils
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.value.ListValue
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemBow
import java.awt.Color

@ModuleInfo(name = "BowAimbot", spacedName = "Bow Aimbot", description = "Automatically aims at players when using a bow.", category = ModuleCategory.COMBAT)
class BowAimbot : Module() {

    private val silentValue = BoolValue("Silent", true)
    private val predictValue = BoolValue("Predict", true)
    private val throughWallsValue = BoolValue("ThroughWalls", false)
    private val predictSizeValue = FloatValue("PredictSize", 2F, 0.1F, 5F, "m")
    private val priorityValue = ListValue("Priority", arrayOf("Health", "Distance", "Direction"), "Direction")
    private val markValue = BoolValue("Mark", true)

    private var target: Entity? = null

    override fun onDisable() {
        target = null
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        target = null

        if (mc.thePlayer.itemInUse?.item !is ItemBow)
            return

        val targets = mc.theWorld.loadedEntityList.filter {
            it is EntityLivingBase && EntityUtils.isSelected(it, true) &&
                    (throughWallsValue.get() || mc.thePlayer.canEntityBeSeen(it))
        }.toMutableList()

        when (priorityValue.get().lowercase()) {
            "distance" -> targets.sortBy { mc.thePlayer.getDistanceToEntity(it) }
            "direction" -> targets.sortBy { RotationUtils.getRotationDifference(it) }
            "health" -> targets.sortBy { (it as EntityLivingBase).health }
            else -> null
        }

        val entity = targets.first()

        target = entity
        RotationUtils.faceBow(entity, predictValue.get(), predictSizeValue.get())
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        target?.let {
            if (markValue.get())
                RenderUtils.drawPlatform(it, Color(37, 126, 255, 70))
        }
    }
}
