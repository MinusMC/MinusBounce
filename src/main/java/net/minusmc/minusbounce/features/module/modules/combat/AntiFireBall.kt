/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce/
 */
package net.minusmc.minusbounce.features.module.modules.combat

import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.utils.extensions.*
import net.minusmc.minusbounce.utils.player.RotationUtils
import net.minusmc.minusbounce.utils.misc.RandomUtils.nextFloat
import net.minusmc.minusbounce.value.FloatRangeValue
import net.minusmc.minusbounce.value.FloatValue
import net.minecraft.entity.Entity
import net.minecraft.entity.projectile.EntityFireball
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.world.WorldSettings
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.utils.PacketUtils.sendPacketNoEvent
import net.minusmc.minusbounce.utils.player.MovementCorrection

@ModuleInfo(name = "AntiFireball", description = "Make fireballs roll back", category = ModuleCategory.COMBAT)
class AntiFireBall : Module() {
    private val range = FloatValue("Range", 4.5f, 3f,8f)
    private val turnSpeed = FloatRangeValue("TurnSpeed", 0f, 180f, 180f, 180f)

    private var target: Entity? = null
    @EventTarget
    private fun onMotion(event: PreUpdateEvent) {
        val player = mc.thePlayer ?: return

        target = null

        for (entity in mc.theWorld.loadedEntityList.filterIsInstance<EntityFireball>()
            .sortedBy { player.getDistanceToBox(it.hitBox) }) {
            val nearestPoint = getNearestPointBB(player.eyes, entity.hitBox)

            val entityPrediction = entity.currPos - entity.prevPos

            val normalDistance = player.getDistanceToBox(entity.hitBox)

            val predictedDistance = player.getDistanceToBox(
                entity.hitBox.offset(
                    entityPrediction.xCoord,
                    entityPrediction.yCoord,
                    entityPrediction.zCoord
                )
            )

            // Skip if the predicted distance is (further than/same as) the normal distance or the predicted distance is out of reach
            if (predictedDistance >= normalDistance || predictedDistance > range.get()) {
                continue
            }

            RotationUtils.setTargetRotation(
                RotationUtils.toRotation(nearestPoint, true),
                keepLength = 2,
                minRotationSpeed = turnSpeed.minValue,
                maxRotationSpeed = turnSpeed.maxValue,
                fixType = MovementCorrection.Type.LIQUID_BOUNCE
            )

            target = entity
            break
        }
    }

    @EventTarget
    fun onTick(event: TickEvent) {
        val player = mc.thePlayer ?: return
        val entity = target ?: return

        if (RotationUtils.isFaced(entity, range.get().toDouble())
        ) {
            mc.thePlayer.swingItem()

            sendPacketNoEvent(C02PacketUseEntity(entity, C02PacketUseEntity.Action.ATTACK))

            if (mc.playerController.currentGameType != WorldSettings.GameType.SPECTATOR) {
                player.attackTargetEntityWithCurrentItem(entity)
            }

            target = null
        }
    }
}