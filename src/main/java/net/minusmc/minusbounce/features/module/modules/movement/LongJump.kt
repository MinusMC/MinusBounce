package net.minusmc.minusbounce.features.module.modules.movement

import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.features.module.modules.movement.longjumps.LongJumpMode
import net.minusmc.minusbounce.utils.ClassUtils
import net.minusmc.minusbounce.utils.player.MovementUtils
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.ListValue

@ModuleInfo(name = "LongJump", description = "Allows you to jump further.", category = ModuleCategory.MOVEMENT)
class LongJump: Module() {
    private val modes = ClassUtils.resolvePackage("${this.javaClass.`package`.name}.longjumps", LongJumpMode::class.java)
        .map{it.newInstance() as LongJumpMode}
        .sortedBy{it.modeName}

    val mode: LongJumpMode
        get() = modes.find { modeValue.get().equals(it.modeName, true) } ?: throw NullPointerException()

    private val modeValue: ListValue = object: ListValue("Mode", modes.map{ it.modeName }.toTypedArray(), "MatrixFlag") {
        override fun onPreChange(oldValue: String, newValue: String) {
            if (state) onDisable()
        }
        override fun onPostChange(oldValue: String, newValue: String) {
            if (state) onEnable()
        }
    }

    private val autoJumpValue = BoolValue("AutoJump", false)
    var jumped = false

    override fun onInitialize() {
        modes.map { mode -> mode.values.forEach { value -> value.name = "${mode.modeName}-${value.name}" } }
    }

    override fun onEnable() {
        mode.onEnable()
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1.0F
        mc.thePlayer.speedInAir = 0.02F
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        mode.onUpdate()

        if (jumped) {
            if (mc.thePlayer.onGround || mc.thePlayer.capabilities.isFlying) {
                jumped = false
                return
            }
            mode.onUpdateJumped()
        }

        if (autoJumpValue.get() && mc.thePlayer.onGround && MovementUtils.isMoving) {
            jumped = true
            mc.thePlayer.jump()
        }
    }

    @EventTarget
    fun onSentPacket(event: SentPacketEvent) {
        mode.onSentPacket(event)
    }

    @EventTarget
    fun onReceivedPacket(event: ReceivedPacketEvent) {
        mode.onReceivedPacket(event)
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        mode.onMove(event)
    }

    @EventTarget(ignoreCondition = true)
    fun onJump(event: JumpEvent) {
        jumped = true
        mode.onJump(event)
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