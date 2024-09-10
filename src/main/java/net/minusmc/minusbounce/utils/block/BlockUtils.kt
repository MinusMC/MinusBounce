/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.utils.block

import net.minecraft.block.*
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.util.*
import net.minecraft.entity.item.EntityFallingBlock
import net.minusmc.minusbounce.injection.access.StaticStorage
import net.minusmc.minusbounce.utils.extensions.*
import net.minusmc.minusbounce.utils.MinecraftInstance
import net.minusmc.minusbounce.utils.PlaceRotation
import net.minusmc.minusbounce.utils.Rotation
import net.minusmc.minusbounce.utils.player.RotationUtils
import kotlin.math.*


object BlockUtils : MinecraftInstance() {

    /**
     * Get block from [blockPos]
     */
    @JvmStatic
    fun getBlock(blockPos: BlockPos?): Block? = mc.theWorld?.getBlockState(blockPos)?.block

    @JvmStatic
    fun getBlock(x: Number, y: Number, z: Number) = getBlock(BlockPos(x.toInt(), y.toInt(), z.toInt()))

    /**
     * Get material from [blockPos]
     */
    @JvmStatic
    fun getMaterial(blockPos: BlockPos?): Material? = getBlock(blockPos)?.material

    /**
     * Check [blockPos] is replaceable
     */
    @JvmStatic
    fun isReplaceable(blockPos: BlockPos?) = getMaterial(blockPos)?.isReplaceable ?: false

    /**
     * Get state from [blockPos]
     */
    @JvmStatic
    fun getState(blockPos: BlockPos?): IBlockState = mc.theWorld.getBlockState(blockPos)

    /**
     * Check if [blockPos] is clickable
     */
    @JvmStatic
    fun canBeClicked(blockPos: BlockPos?): Boolean {
        val state = getState(blockPos) ?: return false
        val block = state.block ?: return false

        return block.canCollideCheck(state, false) && blockPos in mc.theWorld.worldBorder 
            && !block.material.isReplaceable && !block.hasTileEntity(state) && isFullBlock(blockPos)
            && mc.theWorld.loadedEntityList.find { it is EntityFallingBlock && it.position == blockPos } == null
            && block !is BlockContainer && block !is BlockWorkbench
    }

    /**
     * Get block name by [id]
     */
    @JvmStatic
    fun getBlockName(id: Int): String = Block.getBlockById(id).localizedName

    @JvmStatic
    fun isFullBlock(block: Block) = when (block) {
        is BlockGlass, is BlockStainedGlass -> true
        is BlockSoulSand -> false
        else -> block.isFullBlock && block.isBlockNormalCube && block.blockBoundsMaxX == 1.0 && block.blockBoundsMaxY == 1.0 && block.blockBoundsMaxZ == 1.0
    }

    /**
     * Check if block is full block
     */
    @JvmStatic
    fun isFullBlock(blockPos: BlockPos?): Boolean {
        val axisAlignedBB = getBlock(blockPos)?.getCollisionBoundingBox(mc.theWorld, blockPos, getState(blockPos))
                ?: return false
        return axisAlignedBB.maxX - axisAlignedBB.minX == 1.0 && axisAlignedBB.maxY - axisAlignedBB.minY == 1.0 && axisAlignedBB.maxZ - axisAlignedBB.minZ == 1.0
    }

    /**
     * Get distance to center of [blockPos]
     */
    @JvmStatic
    fun getCenterDistance(blockPos: BlockPos) = mc.thePlayer.getDistance(blockPos.x + 0.5, blockPos.y + 0.5, blockPos.z + 0.5)

    /**
     * Search blocks around the player in a specific [radius]
     */
    @JvmStatic
    fun getAllBlocksInRadius(radius: Int): Map<BlockPos, Block> {
        val blocks = mutableMapOf<BlockPos, Block>()

        for (x in radius downTo -radius + 1) {
            for (y in radius downTo -radius + 1) {
                for (z in radius downTo -radius + 1) {
                    val blockPos = BlockPos(mc.thePlayer.posX.toInt() + x, mc.thePlayer.posY.toInt() + y, mc.thePlayer.posZ.toInt() + z)
                    val block = getBlock(blockPos) ?: continue

                    blocks[blockPos] = block
                }
            }
        }

        return blocks
    }

    @JvmStatic
    fun searchBlocks(radius: Int, targetBlock: Block, maxBlockLimit: Int = 256): List<BlockPos> {
        val blocks = mutableListOf<BlockPos>()

        for (x in radius downTo -radius + 1) {
            for (y in radius downTo -radius + 1) {
                for (z in radius downTo -radius + 1) {

                    if (blocks.size >= maxBlockLimit)
                        return blocks

                    val blockPos = BlockPos(mc.thePlayer.posX.toInt() + x, mc.thePlayer.posY.toInt() + y, mc.thePlayer.posZ.toInt() + z)

                    if (getBlock(blockPos) == targetBlock)
                        blocks.add(blockPos)
                }
            }
        }

        return blocks
    }

    /**
     * Check if [axisAlignedBB] has collidable blocks using custom [collide] check
     */
    @JvmStatic
    fun collideBlock(axisAlignedBB: AxisAlignedBB, collide: (Block?) -> Boolean): Boolean {
        val playerBoundingBox = mc.thePlayer.entityBoundingBox

        for (x in floor(playerBoundingBox.minX)..ceil(playerBoundingBox.maxX) step 1.0) {
            for (z in floor(playerBoundingBox.minZ)..ceil(playerBoundingBox.maxZ) step 1.0) {
                val block = getBlock(BlockPos(x, axisAlignedBB.minY, z))

                if (!collide(block))
                    return false
            }
        }

        return true
    }

    /**
     * Check if [axisAlignedBB] has collidable blocks using custom [collide] check
     */
    @JvmStatic
    fun collideBlockIntersects(axisAlignedBB: AxisAlignedBB, collide: (Block?) -> Boolean): Boolean {
        val playerBoundingBox = mc.thePlayer.entityBoundingBox

        for (x in floor(playerBoundingBox.minX)..ceil(playerBoundingBox.maxX) step 1.0) {
            for (z in floor(playerBoundingBox.minZ)..ceil(playerBoundingBox.maxZ) step 1.0) {
                val blockPos = BlockPos(x, axisAlignedBB.minY, z)
                val block = getBlock(blockPos)

                if (!collide(block))
                    continue

                val boundingBox = block?.getCollisionBoundingBox(mc.theWorld, blockPos, getState(blockPos)) ?: continue

                if (playerBoundingBox.intersectsWith(boundingBox))
                    return true
            }
        }
        return false
    }

    @JvmStatic
    fun floorVec3(vec3: Vec3) = Vec3(floor(vec3.xCoord),floor(vec3.yCoord),floor(vec3.zCoord))
}
