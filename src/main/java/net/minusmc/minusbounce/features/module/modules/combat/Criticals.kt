package net.minusmc.minusbounce.features.module.modules.combat

import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.AttackEvent
import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.SentPacketEvent
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.features.module.modules.combat.criticals.CriticalMode
import net.minusmc.minusbounce.features.module.modules.movement.Fly
import net.minusmc.minusbounce.utils.ClassUtils
import net.minusmc.minusbounce.utils.player.MovementUtils
import net.minusmc.minusbounce.utils.timer.MSTimer
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.value.IntegerValue
import net.minusmc.minusbounce.value.ListValue

@ModuleInfo(name = "Criticals", description = "Automatically deals critical hits.", category = ModuleCategory.COMBAT)
class Criticals : Module() {

    private val modes = ClassUtils.resolvePackage("${this.javaClass.`package`.name}.criticals", CriticalMode::class.java)
        .map{ it.newInstance() as CriticalMode }
        .sortedBy{ it.modeName }

    private val mode: CriticalMode
        get() = modes.find { modeValue.get().equals(it.modeName, true) } ?: throw NullPointerException()

    val modeValue: ListValue = object: ListValue("Mode", modes.map{ it.modeName }.toTypedArray(), "Jump") {
        override fun onPreChange(oldValue: String, newValue: String) {
            if (state) onDisable()
        }
        override fun onPostChange(oldValue: String, newValue: String) {
            if (state) onEnable()
        }
    }

    val delayValue = IntegerValue("Delay", 0, 0, 500, "ms")
    val jumpHeightValue = FloatValue("JumpHeight", 0.42F, 0.1F, 0.42F)
    val downYValue = FloatValue("DownY", 0f, 0f, 0.1F)
    private val hurtTimeValue = IntegerValue("HurtTime", 10, 0, 10)
    private val onlyAuraValue = BoolValue("OnlyAura", false)

    val msTimer = MSTimer()
    var entity: EntityLivingBase? = null

    @EventTarget
    fun onAttack(event: AttackEvent) {
        if (onlyAuraValue.get() && !MinusBounce.moduleManager[KillAura::class.java]!!.state) return

        if (event.targetEntity is EntityLivingBase) {
            entity = event.targetEntity

            if (!mc.thePlayer.onGround || mc.thePlayer.isOnLadder || mc.thePlayer.isInWeb || mc.thePlayer.isInWater ||
                    mc.thePlayer.isInLava || mc.thePlayer.ridingEntity != null || entity!!.hurtTime > hurtTimeValue.get() ||
                    MinusBounce.moduleManager[Fly::class.java]!!.state || !msTimer.hasTimePassed(delayValue.get()))
                return

            mode.onAttack(event)
            msTimer.reset()
        }
    }

    @EventTarget
    fun onSentPacket(event: SentPacketEvent) {
        val packet = event.packet

        if (onlyAuraValue.get() && !MinusBounce.moduleManager[KillAura::class.java]!!.state)
            return

        mode.onSentPacket(event)
    }

    override val tag: String
        get() = modeValue.get()

    override val values = super.values.toMutableList().also {
        modes.map {
            mode -> mode.values.forEach { value ->
                val displayableFunction = value.displayableFunction
                it.add(value.displayable { displayableFunction.invoke() && modeValue.get().equals(mode.modeName, true) })
            }
        }
    }
}
