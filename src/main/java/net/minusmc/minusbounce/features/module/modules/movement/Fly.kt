package net.minusmc.minusbounce.features.module.modules.movement

import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyMode
import net.minusmc.minusbounce.features.module.modules.movement.flys.FlyType
import net.minusmc.minusbounce.utils.render.RenderUtils
import net.minusmc.minusbounce.utils.ClassUtils
import net.minusmc.minusbounce.utils.LateinitValue
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.value.ListValue
import org.lwjgl.input.Keyboard
import java.awt.Color

@ModuleInfo(name = "Fly", description = "Allows you to fly in survival mode.", category = ModuleCategory.MOVEMENT, keyBind = Keyboard.KEY_F)
class Fly: Module() {
	private val modes = ClassUtils.resolvePackage("${this.javaClass.`package`.name}.flys", FlyMode::class.java)
		.map{it.newInstance() as FlyMode}
		.sortedBy{it.modeName}

	val mode: FlyMode
        get() = modes.find { modeValue.get().equals(it.modeName, true) } ?: throw NullPointerException()

	private val typeValue: ListValue = object: ListValue("Type", FlyType.values().map{it.typeName}.toTypedArray(), "AAC") {
		override fun onPostChange(oldValue: String, newValue: String) {
			modeValue.changeListValues(modes.filter{it.typeName.typeName == newValue}.map{it.modeName}.toTypedArray())
		}
		override fun onPreChange(oldValue: String, newValue: String) {
			modeValue.changeListValues(modes.filter{it.typeName.typeName == newValue}.map{it.modeName}.toTypedArray())
		}
	}

	val modesForType: Array<String>
		get() = modes.filter{it.typeName.typeName == typeValue.get()}.map{it.modeName}.toTypedArray()

	private val modeValue: ListValue = object: ListValue("Mode", modesForType) {
		override fun onPreChange(oldValue: String, newValue: String) {
			if (state) onDisable()
		}
		override fun onPostChange(oldValue: String, newValue: String) {
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
		mode.onEnable()
		
		if (fakeDmgValue.get())
			mc.thePlayer.handleStatusUpdate(2.toByte())
	}

	override fun onDisable() {
        mc.thePlayer.capabilities.isFlying = false
        
        if (resetMotionValue.get()) {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionY = 0.0
            mc.thePlayer.motionZ = 0.0
        }

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
	fun onSentPacket(event: SentPacketEvent) {
		mode.onSentPacket(event)
	}

	@EventTarget
	fun onReceivedPacket(event: ReceivedPacketEvent) {
		mode.onReceivedPacket(event)
	}

	@EventTarget
	fun onPreMotion(event: PreMotionEvent) {
		if (bobbingValue.get()) {
            mc.thePlayer.cameraYaw = bobbingAmountValue.get()
            mc.thePlayer.prevCameraYaw = bobbingAmountValue.get()
        }

		mode.onPreMotion(event)
	}

	@EventTarget
	fun onPostMotion(event: PostMotionEvent) {
		mode.onPostMotion(event)
	}

	@EventTarget
	fun onRender3D(event: Render3DEvent) {
		val boundingBox = mc.thePlayer.entityBoundingBox ?: return

		if (markValue.get()) {
            val y = mode.startY + 2
            val color = if (boundingBox.maxY < y) Color(0, 255, 0, 90) else Color(255, 0, 0, 90)
            RenderUtils.drawPlatform(y, color, 1.0)

            mode.onRender3D()
        }
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
