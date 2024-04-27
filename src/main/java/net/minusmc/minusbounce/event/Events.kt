/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.event

import net.minecraft.block.Block
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.model.ModelPlayer
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.Packet
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing


/**
 * Called when player attacks other entity
 *
 * @param targetEntity Attacked entity
 */
class AttackEvent(val targetEntity: Entity?) : CancellableEvent()

class PreAttackEvent(val targetEntity: Entity?) : CancellableEvent()

class PostAttackEvent(val targetEntity: Entity?) : Event()

/**
 * Called when minecraft get bounding box of block
 *
 * @param blockPos block position of block
 * @param block block itself
 * @param boundingBox vanilla bounding box
 */
class BlockBBEvent(blockPos: BlockPos, val block: Block, var boundingBox: AxisAlignedBB?) : Event() {
    val x = blockPos.x
    val y = blockPos.y
    val z = blockPos.z
}

/**
 * Called when player clicks a block
 */
class ClickBlockEvent(val clickedBlock: BlockPos?, val enumFacing: EnumFacing?) : Event()

class ClickEvent : CancellableEvent()

/**
 * Called when client is shutting down
 */
class ClientShutdownEvent : Event()

/**
 * Called when the other entity moves
 */
data class EntityMovementEvent(val movedEntity: Entity) : Event()

/**
 * Called when an entity receives damage
 */
class EntityDamageEvent(val damagedEntity: Entity): Event()

/**
 * Called when a model updates
 */
class UpdateModelEvent(val player: EntityPlayer, val model: ModelPlayer) : Event()

/**
 * Called when world is going to be rendered
 */
class Render3DEvent(val partialTicks: Float) : Event()

/**
 * Called when player jumps
 *
 * @param motion jump motion (y motion)
 * @param yaw
 */
class JumpEvent(var motion: Float, var yaw: Float) : CancellableEvent()

/**
 * interpolated look vector
 *
 * @param yaw
 * @param pitch
 */
class LookEvent(var yaw: Float, var pitch: Float) : Event()

/**
 * Called when player input
 */

class MoveInputEvent(var forward: Float, var strafe: Float, var jump: Boolean, var sneak: Boolean, var sneakMultiplier: Double) : Event()

/**
 * Called when user press a key once
 *
 * @param key Pressed key
 */
class KeyEvent(val key: Int) : Event()

/**
 * Called before motion
 */
class PreMotionEvent(var x: Double, var y: Double, var z: Double, var yaw: Float, var pitch: Float, var onGround: Boolean): Event()

/**
 * Called after motion
 */
class PostMotionEvent: Event()

/**
 * Called when player sprints or sneaks, after pre-motion event
 *
 * @param sprinting player's sprint state
 * @param sneaking player's sneak state
 */
class ActionEvent(var sprinting: Boolean, var sneaking: Boolean) : Event()

/**
 * Called in "onLivingUpdate" when the player is using a use item.
 *
 * @param strafe the applied strafe slow down
 * @param forward the applied forward slow down
 */
class SlowDownEvent(var strafe: Float, var forward: Float) : CancellableEvent()

/**
 * Called in "moveFlying"
 */
class StrafeEvent(var strafe: Float, var forward: Float, var friction: Float, var yaw: Float) : CancellableEvent()

/**
 * Called when player moves
 *
 * @param x motion
 * @param y motion
 * @param z motion
 */
class MoveEvent(var x: Double, var y: Double, var z: Double) : CancellableEvent() {
    var isSafeWalk = false

    fun zero() {
        x = 0.0
        y = 0.0
        z = 0.0
    }

    fun zeroXZ() {
        x = 0.0
        z = 0.0
    }
}

/**
 * Called when receive or send a packet
 */
class PacketEvent(val packet: Packet<*>) : CancellableEvent()

/**
 * Called when a block tries to push you
 */
class PushOutEvent : CancellableEvent()

/**
 * Called when screen is going to be rendered
 */
class Render2DEvent(val partialTicks: Float) : Event()

/**
 * Called when entity is going to be rendered
 */
class RenderEntityEvent(val entity: Entity, val x: Double, val y: Double, val z: Double, val entityYaw: Float,
                        val partialTicks: Float) : Event()

/**
 * Called when the screen changes
 */
class ScreenEvent(val guiScreen: GuiScreen?) : Event()

/**
 * Called when the session changes
 */
class SessionEvent : Event()

/**
 * Called when player is going to step
 */
class StepEvent(var stepHeight: Float) : Event()

/**
 * Called when player step is confirmed
 */
class StepConfirmEvent : Event()

/**
 * Called when a text is going to be rendered
 */
class TextEvent(var text: String?) : Event()

/**
 * tick... tack... tick... tack
 */
class TickEvent : Event()

/**
 * Called when minecraft player will be updated
 */
class UpdateEvent: Event()

/**
 * Called before update
 */
class PreUpdateEvent: CancellableEvent()

/**
 * Called when the world changes
 */
class WorldEvent(val worldClient: WorldClient?) : Event()

/**
 * Called when window clicked
 */
class ClickWindowEvent(val windowId: Int, val slotId: Int, val mouseButtonClicked: Int, val mode: Int) : CancellableEvent()

/**
 * Called when reload client
 */
class ReloadClientEvent : Event()

/**
 * Called when entity except self was killed
 */
class EntityKilledEvent(val targetEntity: EntityLivingBase): Event()

