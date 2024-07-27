/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.world

import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.PreMotionEvent
import net.minusmc.minusbounce.event.SentPacketEvent
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.utils.PacketUtils
import net.minusmc.minusbounce.value.FloatValue
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing

@ModuleInfo(name = "SpeedMine", spacedName = "Speed Mine", description = "Mines blocks faster. (pasted edition)", category = ModuleCategory.WORLD)
class SpeedMine : Module() {
    private val speed = FloatValue("Speed", 1.5f, 1f, 3f)
    private var facing: EnumFacing? = null
    private var pos: BlockPos? = null
    private var boost = false
    private var damage = 0f
    
    @EventTarget
    fun onPreMotion(event: PreMotionEvent) {
        mc.playerController.blockHitDelay = 0
        if (pos != null && boost) {
            val blockState = mc.theWorld.getBlockState(pos) ?: return
            damage += try {
                blockState.block.getPlayerRelativeBlockHardness(mc.thePlayer, mc.theWorld, pos) * speed.get()
            } catch (ex: Exception) {
                ex.printStackTrace()
                return
            }
            if (damage >= 1) {
                try {
                    mc.theWorld.setBlockState(pos, Blocks.air.defaultState, 11)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    return
                }
                PacketUtils.sendPacketNoEvent(C07PacketPlayerDigging(Action.STOP_DESTROY_BLOCK, pos, facing))
                damage = 0f
                boost = false
            }
        }
    }

    @EventTarget
    fun onSentPacket(event: SentPacketEvent) {
        val packet = event.packet

        if (packet !is C07PacketPlayerDigging)
            return

        when (packet.status) {
            Action.START_DESTROY_BLOCK -> {
                boost = true
                pos = packet.position
                facing = packet.facing
                damage = 0f
            }

            Action.ABORT_DESTROY_BLOCK, Action.STOP_DESTROY_BLOCK -> {
                boost = false
                pos = null
                facing = null
            }
        }
    }
}