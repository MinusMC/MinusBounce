/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.utils.render

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Matrix4f
import org.lwjgl.util.vector.Vector2f
import org.lwjgl.util.vector.Vector3f
import org.lwjgl.util.vector.Vector4f

object WorldToScreen {
    fun getMatrix(matrix: Int): Matrix4f {
        val floatBuffer = BufferUtils.createFloatBuffer(16)
        GL11.glGetFloat(matrix, floatBuffer)
        return Matrix4f().load(floatBuffer) as Matrix4f
    }

    fun worldToScreen(pointInWorld: Vector3f, screenWidth: Int, screenHeight: Int) = 
        worldToScreen(pointInWorld, getMatrix(GL11.GL_MODELVIEW_MATRIX), getMatrix(GL11.GL_PROJECTION_MATRIX), screenWidth, screenHeight)

    fun worldToScreen(pointInWorld: Vector3f, view: Matrix4f, projection: Matrix4f, screenWidth: Int, screenHeight: Int): Vector2f? {
        val pointedWorldVector = Vector4f(pointInWorld.x, pointInWorld.y, pointInWorld.z, 1.0f)
        val clipSpacePos = pointedWorldVector * view * projection

        val ndcSpacePos = Vector3f(clipSpacePos.x, clipSpacePos.y, clipSpacePos.z) / clipSpacePos.w

        val screenX = (ndcSpacePos.x + 1.0f) / 2.0f * screenWidth
        val screenY = (1.0f - ndcSpacePos.y) / 2.0f * screenHeight

        return if (ndcSpacePos.z < -1.0 || ndcSpacePos.z > 1.0) null else Vector2f(screenX, screenY)
    }

    operator fun Vector4f.times(mat: Matrix4f) = Vector4f(
        x * mat.m00 + y * mat.m10 + z * mat.m20 + w * mat.m30,
        x * mat.m01 + y * mat.m11 + z * mat.m21 + w * mat.m31,
        x * mat.m02 + y * mat.m12 + z * mat.m22 + w * mat.m32,
        x * mat.m03 + y * mat.m13 + z * mat.m23 + w * mat.m33
    )

    operator fun Vector3f.div(b: Float) = Vector3f(x / b, y / b, z / b)
}
