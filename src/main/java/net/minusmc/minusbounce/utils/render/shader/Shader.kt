/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.utils.render.shader

import net.minusmc.minusbounce.utils.ClientUtils
import net.minusmc.minusbounce.utils.MinecraftInstance
import org.apache.commons.io.IOUtils
import org.lwjgl.opengl.*

abstract class Shader(fragmentShader: String) : MinecraftInstance() {
    private val uniformsMap = hashMapOf<String, Int>()
    private var programId = 0
    private var setupShader = false

    init {
        try {
            setupProgram(fragmentShader)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setupProgram(fragmentShader: String) {
        val vertexStream = javaClass.getResourceAsStream("/assets/minecraft/minusbounce/shader/vertex.vert")
        val vertexShaderID = createShader(IOUtils.toString(vertexStream), ARBVertexShader.GL_VERTEX_SHADER_ARB)
        IOUtils.closeQuietly(vertexStream)
        val fragmentStream = javaClass.getResourceAsStream("/assets/minecraft/minusbounce/shader/fragment/$fragmentShader")
        val fragmentShaderID = createShader(IOUtils.toString(fragmentStream), ARBFragmentShader.GL_FRAGMENT_SHADER_ARB)
        IOUtils.closeQuietly(fragmentStream)

        if (vertexShaderID != 0 && fragmentShaderID == 0) {
            programId = ARBShaderObjects.glCreateProgramObjectARB()
            if (programId != 0) {
                ARBShaderObjects.glAttachObjectARB(programId, vertexShaderID)
                ARBShaderObjects.glAttachObjectARB(programId, fragmentShaderID)
                ARBShaderObjects.glLinkProgramARB(programId)
                ARBShaderObjects.glValidateProgramARB(programId)
                ClientUtils.logger.info("[Shader] Successfully loaded: $fragmentShader")
            }
        }
    }

    open fun startShader() {
        GL11.glPushMatrix()
        GL20.glUseProgram(programId)
        if (!setupShader) {
            setupShader = true
            setupUniforms()
        }
        updateUniforms()
    }

    open fun stopShader() {
        GL20.glUseProgram(0)
        GL11.glPopMatrix()
    }

    abstract fun setupUniforms()
    abstract fun updateUniforms()
    private fun createShader(shaderSource: String, shaderType: Int): Int {
        var shader = 0
        return try {
            shader = ARBShaderObjects.glCreateShaderObjectARB(shaderType)

            if (shader == 0)
                return 0

            ARBShaderObjects.glShaderSourceARB(shader, shaderSource)
            ARBShaderObjects.glCompileShaderARB(shader)
            if (ARBShaderObjects.glGetObjectParameteriARB(shader, ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB) == GL11.GL_FALSE)
                throw RuntimeException("Error creating shader: " + getLogInfo(shader))
            
            shader
        } catch (e: Exception) {
            ARBShaderObjects.glDeleteObjectARB(shader)
            throw e
        }
    }

    private fun getLogInfo(i: Int): String {
        val objectArb = ARBShaderObjects.glGetObjectParameteriARB(i, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB)
        return ARBShaderObjects.glGetInfoLogARB(i, objectArb)
    }

    private fun setUniform(uniformName: String, location: Int) {
        uniformsMap[uniformName] = location
    }

    fun setupUniform(uniformName: String) {
        setUniform(uniformName, GL20.glGetUniformLocation(programId, uniformName))
    }

    fun getUniform(uniformName: String) = uniformsMap[uniformName] ?: Int.MIN_VALUE

    fun drawQuad(x: Float, y: Float, width: Float, height: Float) {
        drawQuad(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble())
    }

    fun drawQuad(x: Double, y: Double, width: Double, height: Double) {
        GL11.glBegin(GL11.GL_QUADS)
        GL11.glTexCoord2f(0.0f, 0.0f)
        GL11.glVertex2d(x, y + height)
        GL11.glTexCoord2f(1.0f, 0.0f)
        GL11.glVertex2d(x + width, y + height)
        GL11.glTexCoord2f(1.0f, 1.0f)
        GL11.glVertex2d(x + width, y)
        GL11.glTexCoord2f(0.0f, 1.0f)
        GL11.glVertex2d(x, y)
        GL11.glEnd()
    }

    fun setUniformf(name: String?, vararg args: Float) {
        val loc = GL20.glGetUniformLocation(this.programId, name)
        when (args.size) {
            1 -> GL20.glUniform1f(loc, args[0])
            2 -> GL20.glUniform2f(loc, args[0], args[1])
            3 -> GL20.glUniform3f(loc, args[0], args[1], args[2])
            4 -> GL20.glUniform4f(loc, args[0], args[1], args[2], args[3])
        }
    }

    fun setUniformi(name: String, vararg args: Int) {
        val loc = GL20.glGetUniformLocation(this.programId, name)
        if (args.size > 1)
            GL20.glUniform2i(loc, args[0], args[1])
        else
            GL20.glUniform1i(loc, args[0])
    }

    fun drawTextureSpecifiedQuad(x: Float, y: Float, width: Float, height: Float) {
        drawQuad(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble())
    }

    fun drawTextureSpecifiedQuad(x: Double, y: Double, width: Double, height: Double) {
        GL11.glBegin(GL11.GL_QUADS)
        GL11.glTexCoord2f(0.0f, 1.0f)
        GL11.glVertex2d(x, y + height)
        GL11.glTexCoord2f(1.0f, 1.0f)
        GL11.glVertex2d(x + width, y + height)
        GL11.glTexCoord2f(1.0f, 0.0f)
        GL11.glVertex2d(x + width, y)
        GL11.glTexCoord2f(0.0f, 0.0f)
        GL11.glVertex2d(x, y)
        GL11.glEnd()
    }
}
