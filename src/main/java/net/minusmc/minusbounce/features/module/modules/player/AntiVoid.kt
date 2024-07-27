/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 *
 * This code is skidded from FDPClient.
 */
package net.minusmc.minusbounce.features.module.modules.player

import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.features.module.modules.player.antivoids.AntiVoidMode
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.utils.ClassUtils
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.value.ListValue
import net.minusmc.minusbounce.utils.misc.FallingPlayer

@ModuleInfo(name = "AntiVoid", description = "Anti void", category = ModuleCategory.PLAYER)
object AntiVoid : Module() {
    private val modes = ClassUtils.resolvePackage("${this.javaClass.`package`.name}.antivoids", AntiVoidMode::class.java)
        .map{ it.newInstance() as AntiVoidMode }
        .sortedBy{ it.modeName }

    private val mode: AntiVoidMode
        get() = modes.find{ modeValue.get() == it.modeName } ?: throw NullPointerException()

    val modeValue: ListValue = object: ListValue("Mode", modes.map{ it.modeName }.toTypedArray()) {
        override fun onPreChange(oldValue: String, newValue: String) {
            if (state) onDisable()
        }
        override fun onPostChange(oldValue: String, newValue: String) {
            if (state) onEnable()
        }
    }

    val maxFallDistValue = FloatValue("MaxFallDistance", 10F, 5F, 20F)
    val voidOnlyValue = BoolValue("OnlyVoid", true)

    override fun onInitialize() {
        modes.map {mode -> mode.values.forEach {
            value -> value.name = "${mode.modeName}-${value.name}"
        }}
    }

    override fun onEnable() {
        mode.onEnable()
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        mode.onWorld()
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        mode.onUpdate()

        if (!voidOnlyValue.get() || FallingPlayer(mc.thePlayer).findCollision(60) == null)
            mode.onUpdateVoided()

        mode.onPostVoided()
    }

    @EventTarget
    fun onSentPacket(event: SentPacketEvent) {
        mode.onSentPacket(event)
    }

    @EventTarget
    fun onReceivedPacket(event: ReceivedPacketEvent) {
        mode.onReceivedPacket(event)
    }

    override val tag: String
        get() = modeValue.get()

    override val values = super.values.toMutableList().also {
        modes.map {
            mode -> mode.values.forEach { value ->
                val displayableFunction = value.displayableFunction
                it.add(value.displayable { displayableFunction.invoke() && modeValue.get() == mode.modeName })
            }
        }
    }
}