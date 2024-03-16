/*package net.minusmc.minusbounce.features.module.modules.world.scaffold

import net.minecraft.client.settings.GameSettings
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.features.module.modules.world.Scaffold
import net.minusmc.minusbounce.features.module.modules.world.Scaffold2
import net.minusmc.minusbounce.utils.ClassUtils
import net.minusmc.minusbounce.utils.MinecraftInstance
import net.minusmc.minusbounce.value.Value

abstract class ModeScaffold(val modeName: String): MinecraftInstance() {
    private var fw = false
    private var bw = false
    private var left = false
    private var right = false

    protected val scaffold: Scaffold
		get() = MinusBounce.moduleManager[Scaffold::class.java]!!

    open val values: List<Value<*>>
		get() = ClassUtils.getValues(this.javaClass, this)


    open fun onEnable() {}
    open fun onDisable() {}
    open fun onUpdate() {}
    open fun onPreMotion(event: PreMotionEvent) {}
    open fun onPostMotion(event: PostMotionEvent) {}
    open fun onPacket(event: PacketEvent) {}
    open fun onClick(event: ClickEvent) {}
    open fun onMove(event: MoveEvent) {}
    open fun onJump(event: JumpEvent) {}

    open fun correctControls(type: Int) {
        fw =  GameSettings.isKeyDown(mc.gameSettings.keyBindForward)
        bw = GameSettings.isKeyDown(mc.gameSettings.keyBindBack)
        right = GameSettings.isKeyDown(mc.gameSettings.keyBindRight)
        left = GameSettings.isKeyDown(mc.gameSettings.keyBindLeft)
        when (type) {
            0 -> {
                mc.gameSettings.keyBindForward.pressed = fw
                mc.gameSettings.keyBindBack.pressed = bw
                mc.gameSettings.keyBindRight.pressed = right
                mc.gameSettings.keyBindLeft.pressed = left
            }
            1 -> {
                mc.gameSettings.keyBindForward.pressed = bw
                mc.gameSettings.keyBindBack.pressed = fw
                mc.gameSettings.keyBindRight.pressed = left
                mc.gameSettings.keyBindLeft.pressed = right
            }
            2 -> {
                mc.gameSettings.keyBindForward.pressed = fw || right
                mc.gameSettings.keyBindBack.pressed = left || bw
                mc.gameSettings.keyBindRight.pressed = right || bw
                mc.gameSettings.keyBindLeft.pressed = fw || left
            }
            3 -> {
                mc.gameSettings.keyBindForward.pressed = left || bw
                mc.gameSettings.keyBindBack.pressed = fw || right
                mc.gameSettings.keyBindRight.pressed = fw || left
                mc.gameSettings.keyBindLeft.pressed = right || bw
            }
        }
    }
}*/