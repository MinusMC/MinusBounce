package net.minusmc.minusbounce.features.module.modules.movement

import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedMode
import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedType
import net.minusmc.minusbounce.utils.ClassUtils
import net.minusmc.minusbounce.utils.LateinitValue
import net.minusmc.minusbounce.utils.player.MovementUtils
import net.minusmc.minusbounce.value.*

@ModuleInfo(name = "Speed", description = "Run faster.", category = ModuleCategory.MOVEMENT)
class Speed: Module() {
	private val modes = ClassUtils.resolvePackage("${this.javaClass.`package`.name}.speeds", SpeedMode::class.java)
		.map{it.newInstance() as SpeedMode}
		.sortedBy{it.modeName}

	val mode: SpeedMode
        get() = modes.find { modeValue.get().equals(it.modeName, true) } ?: throw NullPointerException()

	private val typeValue: ListValue = object: ListValue("Type", SpeedType.values().map{it.typeName}.toTypedArray()) {
		override fun onPostChange(oldValue: String, newValue: String) {
			modeValue.changeListValues(modes.filter{it.typeName.typeName == newValue}.map{it.modeName}.toTypedArray())
		}
		override fun onPreChange(oldValue: String, newValue: String) {
			modeValue.changeListValues(modes.filter{it.typeName.typeName == newValue}.map{it.modeName}.toTypedArray())
		}
	}

	val modesForType: Array<String>
		get() = modes.filter{it.typeName.typeName == typeValue.get()}.map{it.modeName}.toTypedArray()

	private var modeValue: ListValue = object: ListValue("Mode", modesForType) {
		override fun onPreChange(oldValue: String, newValue: String) {
			if (state) onDisable()
		}
		override fun onPostChange(oldValue: String, newValue: String) {
			if (state) onEnable()
		}
	}

	private val noWater = BoolValue("NoWater", false)
	private val alwaysSprint = BoolValue("AlwaysSprint", true)

	override fun onInitialize() {
		modes.map {mode -> mode.values.forEach {
			value -> value.name = "${mode.modeName}-${value.name}"
		}}
	}

	override fun onInitModeListValue() {
		modeValue.changeListValues(modesForType)
		modeValue.set(LateinitValue.speedModeValue)
	}

	override fun onEnable() {
		mode.onEnable()
	}

	override fun onDisable() {
		mode.onDisable()
	}

	@EventTarget
	fun onTick(event: TickEvent) {
		mode.onTick()
	}

	@EventTarget
	fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer.isSneaking || (noWater.get() && mc.thePlayer.isInWater))
        	return

		mode.onUpdate()
	}

	@EventTarget
	fun onReceivedPacket(event: ReceivedPacketEvent) {
		if (mc.thePlayer.isSneaking || (noWater.get() && mc.thePlayer.isInWater))
        	return

		mode.onReceivedPacket(event)
	}

	@EventTarget
	fun onPreMotion(event: PreMotionEvent) {
        if (mc.thePlayer.isSneaking || (noWater.get() && mc.thePlayer.isInWater))
        	return

		mode.onPreMotion(event)
	}

	@EventTarget
	fun onPostMotion(event: PostMotionEvent) {
        if (mc.thePlayer.isSneaking || (noWater.get() && mc.thePlayer.isInWater))
        	return

		mode.onPostMotion(event)
	}

	@EventTarget
	fun onMove(event: MoveEvent) {
		if (mc.thePlayer.isSneaking || (noWater.get() && mc.thePlayer.isInWater))
        	return

		mode.onMove(event)
	}

	@EventTarget
	fun onJump(event: JumpEvent) {
		if (mc.thePlayer.isSneaking || (noWater.get() && mc.thePlayer.isInWater))
        	return

		mode.onJump(event)
	}

	@EventTarget
	fun onStrafe(event: StrafeEvent) {
		if (mc.thePlayer.isSneaking || (noWater.get() && mc.thePlayer.isInWater))
        	return

		mode.onStrafe(event)
	}

	@EventTarget
	fun onPreUpdate(event: PreUpdateEvent) {
		if (mc.thePlayer.isSneaking || (noWater.get() && mc.thePlayer.isInWater))
        	return
        	
		mode.onPreUpdate()
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
