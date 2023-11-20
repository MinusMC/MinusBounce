/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.client

import net.minecraft.util.ResourceLocation
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.utils.ClassUtils
import net.minusmc.minusbounce.value.ListValue

@ModuleInfo(name = "Cape", description = "MinusBounce capes.", category = ModuleCategory.CLIENT)
class Cape : Module() {
    private val styleValue = ListValue("Style", arrayOf("Dark"), "Dark")
    private val capeCache = hashMapOf<String, ResourceLocation>()

    override fun onInitialize() {
        ClassUtils.capeFiles.forEach {
            val name = it.split("/").last().replace(".png", "")
            capeCache[name.lowercase()] = ResourceLocation(it)
        }
<<<<<<< HEAD
        if (capeCache.isEmpty()) return // cape resolver bi loi ko load dc fix tam thoi r ha
=======
        if (capeCache.isEmpty()) return // out of index when running in idea
>>>>>>> 5171d8234aad90ef0471ddaf38ea74a1097ac7e1
        styleValue.changeListValues(capeCache.keys.toTypedArray())
    } // ko

    val cape: ResourceLocation?
        get() = capeCache[styleValue.get().lowercase()] ?: capeCache["minusbounce"]

    override val tag: String
        get() = styleValue.get()

}
