/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.utils.asm

import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnList

/**
 * A bytecode node util
 *
 * @author CCBlueX
 */
object NodeUtils {

    /**
     * Lazy.
     */
    fun toNodes(vararg nodes : AbstractInsnNode) : InsnList {
        val insnList = InsnList()
        for(node in nodes)
            insnList.add(node)
        return insnList
    }

}