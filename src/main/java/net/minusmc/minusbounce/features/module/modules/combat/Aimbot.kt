/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.combat

import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.StrafeEvent
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.utils.EntityUtils
import net.minusmc.minusbounce.utils.RotationUtils
import net.minusmc.minusbounce.utils.extensions.getDistanceToEntityBox
import net.minusmc.minusbounce.utils.extensions.rotation
import net.minusmc.minusbounce.utils.misc.RandomUtils
import net.minusmc.minusbounce.utils.timer.MSTimer
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.FloatValue
import kotlin.random.Random

/*
TASK: Better aimbot (aimassist) cai nay thuc ra la y tuong wow
 */

@ModuleInfo(name = "Aimbot", description = "Automatically faces selected entities around you.", category = ModuleCategory.COMBAT)
class Aimbot : Module() {

    private val rangeValue = FloatValue("Range", 4.4F, 1F, 8F, "m")
    private val turnSpeedValue = FloatValue("TurnSpeed", 2F, 1F, 180F, "°")
    private val fovValue = FloatValue("FOV", 180F, 1F, 180F, "°")
    private val centerValue = BoolValue("Center", false)
    private val lockValue = BoolValue("Lock", true)
    private val onClickValue = BoolValue("OnClick", false)
    private val jitterValue = BoolValue("Jitter", false)

    private val clickTimer = MSTimer()

    @EventTarget
    fun onStrafe(event: StrafeEvent) {
        if (mc.gameSettings.keyBindAttack.isKeyDown)
            clickTimer.reset()

        if (onClickValue.get() && clickTimer.hasTimePassed(500L))
            return

        val player = mc.thePlayer ?: return

        val range = rangeValue.get()
        val entity = mc.theWorld.loadedEntityList
                .filter {
                    EntityUtils.isSelected(it, true) && player.canEntityBeSeen(it) &&
                            player.getDistanceToEntityBox(it) <= range && RotationUtils.getRotationDifference(it) <= fovValue.get()
                }
                .minByOrNull { RotationUtils.getRotationDifference(it) } ?: return

        if (!lockValue.get() && RotationUtils.isFaced(entity, range.toDouble()))
            return

        val boundingBox = entity.entityBoundingBox ?: return

        val destinationRotation = if (centerValue.get()) {
            RotationUtils.toRotation(RotationUtils.getCenter(boundingBox), true)
        } else {
            RotationUtils.searchCenter(
                boundingBox,
                random = false,
                outborder = false,
                predict = true,
                throughWalls = false,
                distance = range
            )!!.rotation
        }
        val rotation = RotationUtils.limitAngleChange(player.rotation, destinationRotation, (turnSpeedValue.get() + Math.random()).toFloat())

        rotation.toPlayer(player)

        if (jitterValue.get()) {
            val yaw = Random.nextBoolean()
            val pitch = Random.nextBoolean()
            val yawNegative = Random.nextBoolean()
            val pitchNegative = Random.nextBoolean()

            if (yaw)
                player.rotationYaw += if (yawNegative) -RandomUtils.nextFloat(0F, 1F) else RandomUtils.nextFloat(0F, 1F)

            if (pitch) {
                player.rotationPitch += if (pitchNegative) -RandomUtils.nextFloat(0F, 1F) else RandomUtils.nextFloat(0F, 1F)
                if (player.rotationPitch > 90.0F)
                    player.rotationPitch = 90F
                else if (player.rotationPitch < -90.0F)
                    player.rotationPitch = -90F
            }
        }
    }
}
