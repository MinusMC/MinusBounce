package net.minusmc.minusbounce.features.module.modules.combat.velocitys.grim

import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.network.play.client.C0FPacketConfirmTransaction
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S27PacketExplosion
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.AttackEvent
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.event.UpdateEvent
import net.minusmc.minusbounce.features.module.modules.combat.KillAura
import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minusmc.minusbounce.utils.RaycastUtils
import net.minusmc.minusbounce.utils.extensions.expands
import net.minusmc.minusbounce.utils.extensions.getLookingTargetRange
import net.minusmc.minusbounce.utils.timer.MSTimer
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.IntegerValue

class GrimSmartVelocity: VelocityMode("GrimSmart") {
    private val grimACTicks = IntegerValue("GrimSmartTicks", 0, 0, 10)
    private var grimTicks = 0
    private var grimDisable = 0
    private var explosion = false
    private var velocityTimer = MSTimer()
    private val cancelExplosionPacket = BoolValue("CancelExplosionPacket",false)
    private var transactionCancelCount = 0
    private val clickOnlyNoBlocking = BoolValue("ClickOnlyNoBlocking", false)
    private val clickSwing = BoolValue("ClickSwing", false)

    override fun onUpdate() {
        val thePlayer = mc.thePlayer ?: return

        if (thePlayer.isInWater || thePlayer.isInLava || thePlayer.isInWeb)
            return

        --grimDisable
        if (grimTicks > 0) {
            --grimTicks
            mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, BlockPos(thePlayer.posX, thePlayer.posY, thePlayer.posZ), EnumFacing.UP))
        }
    }

    override fun onPacket(event: PacketEvent) {
        val thePlayer = mc.thePlayer ?: return

        val packet = event.packet

        if (packet is S12PacketEntityVelocity) {

            if ((mc.theWorld?.getEntityByID(packet.entityID) ?: return) != thePlayer)
                return

            velocityTimer.reset()
            if (grimDisable > 0) {
                return
            }
            event.cancelEvent()
            grimTicks = grimACTicks.get()


        } else if (packet is S27PacketExplosion) {
            if (packet.func_149149_c() != 0F ||
                packet.func_149144_d() != 0F ||
                packet.func_149147_e() != 0F) explosion = true
            if (cancelExplosionPacket.get()) event.cancelEvent()
        } else if (packet is C0FPacketConfirmTransaction) {
            if (transactionCancelCount > 0) {
                --transactionCancelCount
                event.cancelEvent()
            }
        } else if (packet is S08PacketPlayerPosLook) {
            grimDisable = 10
        }
    }

    fun attackRayTrace(attack: Int, range: Double, doAttack: Boolean=true): Boolean {
        if (mc.thePlayer == null) return false
        if (clickOnlyNoBlocking.get() && (mc.thePlayer.isBlocking || mc.thePlayer.isUsingItem || MinusBounce.moduleManager[KillAura::class.java]!!.blockingStatus)) return true
        val raycastedEntity = RaycastUtils.raycastEntity(range + 1, object : RaycastUtils.IEntityFilter {
            override fun canRaycast(entity: Entity?): Boolean {
                return entity != null && entity is EntityLivingBase
            }
        })

        raycastedEntity?.let {
            if (it !is EntityPlayer) return true
            if (it.entityBoundingBox.expands(it.collisionBorderSize.toDouble()).getLookingTargetRange(mc.thePlayer) > range) return false
            if (doAttack) {
                MinusBounce.eventManager.callEvent(AttackEvent(it))
                repeat(attack) { _ ->
                    if (clickSwing.get()) mc.thePlayer.swingItem()
                    else mc.netHandler.addToSendQueue(C0APacketAnimation())
                    mc.netHandler.addToSendQueue(C02PacketUseEntity(it, C02PacketUseEntity.Action.ATTACK))
                }
                mc.thePlayer.attackTargetEntityWithCurrentItem(it)
            }
            return true
        }
        return false
    }
}
