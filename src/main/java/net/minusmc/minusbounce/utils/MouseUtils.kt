package net.minusmc.minusbounce.utils

import net.minecraftforge.client.event.MouseEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.ObfuscationReflectionHelper
import org.lwjgl.input.Mouse
import java.nio.ByteBuffer

object MouseUtils {
    @JvmStatic
    fun mouseWithinBounds(mouseX: Int, mouseY: Int, x: Float, y: Float, x2: Float, y2: Float) = mouseX >= x && mouseX < x2 && mouseY >= y && mouseY < y2
}