/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.utils.extensions

import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.Vec3
import net.minusmc.minusbounce.utils.MinecraftInstance
import net.minusmc.minusbounce.utils.Rotation
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Allows to get the distance between the current entity and [entity] from the nearest corner of the bounding box
 */
fun Entity.getDistanceToEntityBox(entity: Entity): Double {
    val eyes = this.getPositionEyes(1F)
    val pos = getNearestPointBB(eyes, entity.entityBoundingBox)
    val xDist = abs(pos.xCoord - eyes.xCoord)
    val yDist = abs(pos.yCoord - eyes.yCoord)
    val zDist = abs(pos.zCoord - eyes.zCoord)
    return sqrt(xDist.pow(2) + yDist.pow(2) + zDist.pow(2))
}

fun Entity.getDistanceToBox(box: AxisAlignedBB) = eyes.distanceTo(getNearestPointBB(eyes, box))

val Entity.prevPos: Vec3
    get() = Vec3(this.prevPosX, this.prevPosY, this.prevPosZ)

val Entity.currPos: Vec3
    get() = this.positionVector

val Entity.hitBox: AxisAlignedBB
    get() {
        val borderSize = collisionBorderSize.toDouble()
        return entityBoundingBox.expand(borderSize, borderSize, borderSize)
    }

val Entity?.rotation
    get() = Rotation(this?.rotationYaw ?: 0f, this?.rotationPitch ?: 0f)

fun getNearestPointBB(eye: Vec3, box: AxisAlignedBB): Vec3 {
    val origin = doubleArrayOf(eye.xCoord, eye.yCoord, eye.zCoord)
    val destMins = doubleArrayOf(box.minX, box.minY, box.minZ)
    val destMaxs = doubleArrayOf(box.maxX, box.maxY, box.maxZ)
    for (i in 0..2) {
        if (origin[i] > destMaxs[i]) origin[i] = destMaxs[i] else if (origin[i] < destMins[i]) origin[i] = destMins[i]
    }
    return Vec3(origin[0], origin[1], origin[2])
}

fun EntityPlayer.getPing(): Int {
    val playerInfo = MinecraftInstance.mc.netHandler.getPlayerInfo(uniqueID)
    return playerInfo?.responseTime ?: 0
}

fun EntityPlayerSP.tryJump() {
    if (!MinecraftInstance.mc.gameSettings.keyBindJump.isKeyDown) {
        this.jump()
    }
}