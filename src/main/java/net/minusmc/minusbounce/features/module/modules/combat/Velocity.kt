/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.combat

import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.value.ListValue
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.utils.ClassUtils
import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minecraft.network.play.server.S27PacketExplosion
import net.minusmc.minusbounce.value.IntegerValue


@ModuleInfo(name = "Velocity", description = "Allows you to modify the amount of knockback you take.", category = ModuleCategory.COMBAT)
class Velocity : Module() {
    private val modes = ClassUtils.resolvePackage("${this.javaClass.`package`.name}.velocitys", VelocityMode::class.java)
        .map { it.newInstance() as VelocityMode }
        .sortedBy { it.modeName }

    private val mode: VelocityMode
        get() = modes.find { modeValue.get().equals(it.modeName, true) }
            ?: throw NullPointerException() // this should not happen

    private val modeValue: ListValue = object : ListValue("Mode", modes.map { it.modeName }.toTypedArray(), "Cancel") {
        override fun onChange(oldValue: String, newValue: String) {
            if (state) onDisable()
        }

        override fun onChanged(oldValue: String, newValue: String) {
            if (state) onEnable()
        }
    }

    private val onExplosionValue = BoolValue("OnExplosion", true)
    private val horizontalExplosionValue = FloatValue("HorizontalExplosion", 0F, 0F, 1F) { onExplosionValue.get() }
    private val verticalExplosionValue = FloatValue("VerticalExplosion", 0F, 0F, 1F) { onExplosionValue.get() }

    private val reduceChance = FloatValue("Reduce-Chance", 100F, 0F, 100F, "%")
    private var shouldAffect: Boolean = true

    override fun onInitialize() {
        modes.map { mode -> mode.values.forEach { value -> value.name = "${mode.modeName}-${value.name}" } }
    }

    override fun onDisable() {
        mc.thePlayer?.speedInAir = 0.02F
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        mode.onPacket(event)
        val packet = event.packet
        if (onExplosionValue.get() && packet is S27PacketExplosion) {
            mc.thePlayer.motionX += packet.func_149149_c() * horizontalExplosionValue.get()
            mc.thePlayer.motionY += packet.func_149144_d() * verticalExplosionValue.get()
            mc.thePlayer.motionZ += packet.func_149147_e() * horizontalExplosionValue.get()
            event.cancelEvent()
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer.hurtTime <= 0) shouldAffect = (Math.random().toFloat() < reduceChance.get() / 100F)
        if (mc.thePlayer.isInWater || mc.thePlayer.isInLava || mc.thePlayer.isInWeb || !shouldAffect)
            return
        mode.onUpdate()
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        if (mc.thePlayer == null || mc.thePlayer.isInWater || mc.thePlayer.isInLava || mc.thePlayer.isInWeb || !shouldAffect)
            return
        mode.onJump(event)
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        mode.onMotion(event)
    }

    @EventTarget
    fun onTick(event: TickEvent) {
        mode.onTick(event)
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