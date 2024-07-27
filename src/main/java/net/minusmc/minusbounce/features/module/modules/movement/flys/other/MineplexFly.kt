package net.minusmc.minusbounce.features.module.modules.movement.flys.other

import net.minecraft.block.BlockAir
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.Vec3
import net.minusmc.minusbounce.event.BlockBBEvent
import net.minusmc.minusbounce.event.JumpEvent
import net.minusmc.minusbounce.event.SentPacketEvent
import net.minusmc.minusbounce.event.StepEvent
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyMode
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyType
import net.minusmc.minusbounce.utils.ClientUtils
import net.minusmc.minusbounce.utils.player.MovementUtils
import net.minusmc.minusbounce.utils.misc.MathUtils
import net.minusmc.minusbounce.utils.timer.MSTimer
import net.minusmc.minusbounce.value.FloatValue

class MineplexFly: FlyMode("Mineplex", FlyType.OTHER) {
	private val mineplexSpeed = FloatValue("Speed", 1f, 0.5f, 10f)
    private val mineplexTimer = MSTimer()

    override fun onUpdate() {

        mc.thePlayer ?: return

        mc.thePlayer.inventory.getCurrentItem() ?: run {
            mc.timer.timerSpeed = 1f
            fly.state = false
            ClientUtils.displayChatMessage("§8[§c§lMineplex-§a§lFly§8] §aSelect an empty slot to fly.")
            return
        }

        if (mc.gameSettings.keyBindJump.isKeyDown && mineplexTimer.hasTimePassed(100)) {
            mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.6, mc.thePlayer.posZ)
            mineplexTimer.reset()
        }

        if(mc.thePlayer.isSneaking && mineplexTimer.hasTimePassed(100)) {
            mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY - 0.6, mc.thePlayer.posZ)
            mineplexTimer.reset()
        }

        val boundingBox = mc.thePlayer.entityBoundingBox ?: return

        val blockPos = BlockPos(mc.thePlayer.posX, boundingBox.minY - 1, mc.thePlayer.posZ)
        val vec = Vec3(blockPos).addVector(0.4, 0.4, 0.4).add(Vec3(EnumFacing.UP.directionVec))
        mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.heldItem, blockPos, EnumFacing.UP, Vec3(vec.xCoord * 0.4f, vec.yCoord * 0.4f, vec.zCoord * 0.4f))
        MovementUtils.strafe(0.27f)

        mc.timer.timerSpeed = 1 + mineplexSpeed.get()
    }

    override fun onSentPacket(event: SentPacketEvent) {
        mc.thePlayer.inventory.getCurrentItem() ?: return

        val packet = event.packet
        if (packet is C03PacketPlayer)
            packet.onGround = true
    }

    override fun onBlockBB(event: BlockBBEvent) {
        mc.thePlayer.inventory.getCurrentItem() ?: return

        if (event.block is BlockAir && event.y < mc.thePlayer.posY)
            event.boundingBox = AxisAlignedBB.fromBounds(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(), (event.x + 1).toDouble(), mc.thePlayer.posY, (event.z + 1).toDouble())
    }

    override fun onJump(event: JumpEvent) {
        if (mc.thePlayer.inventory.getCurrentItem() != null)
            event.isCancelled = true
    }

    override fun onStep(event: StepEvent) {
        if (mc.thePlayer.inventory.getCurrentItem() != null)
            event.stepHeight = 0f
    }

}