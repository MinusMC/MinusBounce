package net.minusmc.minusbounce.features.module.modules.movement

import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedMode
import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedType
import net.minusmc.minusbounce.utils.ClassUtils
import net.minusmc.minusbounce.utils.ListValuesOverride
import net.minusmc.minusbounce.utils.MovementUtils
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.ListValue

@ModuleInfo(name = "Speed", description = "Run faster.", category = ModuleCategory.MOVEMENT)
class Speed: Module() {
	private val modes = ClassUtils.resolvePackage("${this.javaClass.`package`.name}.speeds", SpeedMode::class.java)
		.map{it.newInstance() as SpeedMode}
		.sortedBy{it.modeName}

	val mode: SpeedMode
        get() = modes.find { modeValue.get().equals(it.modeName, true) } ?: throw NullPointerException()

	private val typeValue: ListValue = object: ListValue("Type", SpeedType.values().map{it.typeName}.toTypedArray()) {
		override fun onChanged(oldValue: String, newValue: String) {
			if (firstLoad) return
			modeValue.changeListValues(modes.filter{it.typeName.typeName == newValue}.map{it.modeName}.toTypedArray())
			modeValue.changeValue(modeValue.values[0])
		}
		override fun onChange(oldValue: String, newValue: String) {
			if (firstLoad) return
			modeValue.changeListValues(modes.filter{it.typeName.typeName == newValue}.map{it.modeName}.toTypedArray())
			modeValue.changeValue(modeValue.values[0])
		}
	}

	val modesForType: Array<String>
		get() = modes.filter{it.typeName.typeName == typeValue.get()}.map{it.modeName}.toTypedArray()

	private var modeValue: ListValue = object: ListValue("Mode", modesForType) {
		override fun onChange(oldValue: String, newValue: String) {
			if (state) onDisable()
		}
		override fun onChanged(oldValue: String, newValue: String) {
			if (state) onEnable()
		}
	}

	private val noWater = BoolValue("NoWater", false)
	private val alwaysSprint = BoolValue("AlwaysSprint", true)

	var firstLoad = true

	override fun onInitialize() {
		modes.map {mode -> mode.values.forEach {
			value -> value.name = "${mode.modeName}-${value.name}"
		}}
	}

	override fun onInitModeListValue() {
		modeValue.changeListValues(modesForType)
		modeValue.changeValue(ListValuesOverride.oldSpeedMode)
		firstLoad = false
	}

	override fun onEnable() {mode.onEnable()}

	override fun onDisable() {mode.onDisable()}

	@EventTarget
	fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer.isSneaking || (noWater.get() && mc.thePlayer.isInWater)) return
		if (MovementUtils.isMoving && alwaysSprint.get()) mc.thePlayer.isSprinting = true
		mode.onUpdate()
	}

	@EventTarget
	fun onPacket(event: PacketEvent) {
		mode.onPacket(event)
	}

	@EventTarget
	fun onMotion(event: MotionEvent) {
        if (mc.thePlayer.isSneaking || event.eventState != EventState.PRE) return
		if (MovementUtils.isMoving && alwaysSprint.get()) mc.thePlayer.isSprinting = true
		mode.onMotion(event)
	}

	@EventTarget
	fun onMove(event: MoveEvent) {
		if (mc.thePlayer.isSneaking) return
		mode.onMove(event)
	}

	@EventTarget
	fun onJump(event: JumpEvent) {
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
