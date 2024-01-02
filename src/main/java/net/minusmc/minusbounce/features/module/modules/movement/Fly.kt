package net.minusmc.minusbounce.features.module.modules.movement

import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyMode
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyType
import net.minusmc.minusbounce.utils.ClassUtils
import net.minusmc.minusbounce.utils.LateinitValue
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.value.ListValue
import org.lwjgl.input.Keyboard

@ModuleInfo(name = "Fly", description = "Allows you to fly in survival mode.", category = ModuleCategory.MOVEMENT, keyBind = Keyboard.KEY_F)
class Fly: Module() {
	private val modes = ClassUtils.resolvePackage("${this.javaClass.`package`.name}.flys", FlyMode::class.java)
		.map{it.newInstance() as FlyMode}
		.sortedBy{it.modeName}

	val mode: FlyMode
        get() = modes.find { modeValue.get().equals(it.modeName, true) } ?: throw NullPointerException()

	private val typeValue: ListValue = object: ListValue("Type", FlyType.values().map{it.typeName}.toTypedArray(), "AAC") {
		override fun onChanged(oldValue: String, newValue: String) {
			modeValue.changeListValues(modes.filter{it.typeName.typeName == newValue}.map{it.modeName}.toTypedArray())
		}
		override fun onChange(oldValue: String, newValue: String) {
			modeValue.changeListValues(modes.filter{it.typeName.typeName == newValue}.map{it.modeName}.toTypedArray())
		}
	}

	val modesForType: Array<String>
		get() = modes.filter{it.typeName.typeName == typeValue.get()}.map{it.modeName}.toTypedArray()

	private val modeValue: ListValue = object: ListValue("Mode", modesForType) {
		override fun onChange(oldValue: String, newValue: String) {
			if (state) onDisable()
		}
		override fun onChanged(oldValue: String, newValue: String) {
			if (state) onEnable()
		}
	}	
    val resetMotionValue = BoolValue("ResetMotion", true)

    val fakeDmgValue = BoolValue("FakeDamage", true)
    private val bobbingValue = BoolValue("Bobbing", true)
    private val bobbingAmountValue = FloatValue("BobbingAmount", 0.2F, 0F, 1F) { bobbingValue.get() }
    val markValue = BoolValue("Mark", true)

	override fun onInitialize() {
		modes.map { mode -> mode.values.forEach { value -> value.name = "${mode.modeName}-${value.name}" } }
	}

    override fun onInitModeListValue() {
    	modeValue.changeListValues(modesForType)
		modeValue.set(LateinitValue.flyModeValue)
    }

	override fun onEnable() {
		mode.initEnable()
		mode.onEnable()
		mode.handleUpdate()
	}

	override fun onDisable() {
        mc.thePlayer.capabilities.isFlying = false

        mode.resetMotion()
		mode.onDisable()

        mc.timer.timerSpeed = 1f
        mc.thePlayer.speedInAir = 0.02f
	}

	@EventTarget
	fun onUpdate(event: UpdateEvent) {
		mc.thePlayer.noClip = false
		mode.onUpdate()
	}
	
	@EventTarget
	fun onPacket(event: PacketEvent) {
		mode.onPacket(event)
	}

	@EventTarget
	fun onMotion(event: MotionEvent) {
		if (bobbingValue.get()) {
            mc.thePlayer.cameraYaw = bobbingAmountValue.get()
            mc.thePlayer.prevCameraYaw = bobbingAmountValue.get()
        }
		mode.onMotion(event)
	}

	@EventTarget
	fun onRender3D(event: Render3DEvent) {
		mode.onRender3D()
	}

	@EventTarget
	fun onRender2D(event: Render2DEvent) {
		mode.onRender2D()
	}

	@EventTarget
	fun onMove(event: MoveEvent) {
		mode.onMove(event)
	}

	@EventTarget
	fun onBlockBB(event: BlockBBEvent) {
		mode.onBlockBB(event)
	}

	@EventTarget
	fun onJump(event: JumpEvent) {
		mode.onJump(event)
	}

	@EventTarget
	fun onStep(event: StepEvent) {
		mode.onStep(event)
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
