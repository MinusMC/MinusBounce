/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.misc

import net.minecraft.network.play.client.C01PacketChatMessage
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S45PacketTitle
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.ReceivedPacketEvent
import net.minusmc.minusbounce.event.UpdateEvent
import net.minusmc.minusbounce.event.WorldEvent
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.ui.client.hud.element.elements.Notification
import net.minusmc.minusbounce.utils.PacketUtils
import net.minusmc.minusbounce.utils.timer.MSTimer
import net.minusmc.minusbounce.value.IntegerValue
import net.minusmc.minusbounce.value.TextValue

@ModuleInfo(name = "AutoLogin", spacedName = "Auto Login", description = "Automatically login into some servers for you.", category = ModuleCategory.MISC)
class AutoLogin : Module() {

	private val password = TextValue("Password", "example@01")
	private val regRegex = TextValue("Register-Regex", "/register")
	private val loginRegex = TextValue("Login-Regex", "/login")
	private val registerCmdValue = TextValue("Register-Cmd", "/register %p %p")
	private val loginCmdValue = TextValue("Login-Cmd", "/login %p")

	private val delayValue = IntegerValue("Delay", 5000, 0, 5000, "ms")

	private var loginState = false
	private var registerState = false

	private val regTimer = MSTimer()
	private val logTimer = MSTimer()

	private val loginCmd: String
		get() = loginCmdValue.get().replace("%p", password.get(), true)

	private val registerCmd: String
		get() = registerCmdValue.get().replace("%p", password.get(), true)

	override fun onEnable() {
		loginState = false
		registerState = false
		regTimer.reset()
		logTimer.reset()
	}

	@EventTarget
	fun onWorld(event: WorldEvent) {
		loginState = false
		registerState = false
		regTimer.reset()
		logTimer.reset()
	}

	@EventTarget
	fun onUpdate(event: UpdateEvent) {

		if (!registerState)
			regTimer.reset()
		else if (regTimer.hasTimePassed(delayValue.get())) {
			PacketUtils.sendPacketNoEvent(C01PacketChatMessage(registerCmd))
			MinusBounce.hud.addNotification(Notification("AutoLogin", "Successfully registered.", Notification.Type.SUCCESS))
			registerState = false
			regTimer.reset()
		}

		if (!loginState)
			logTimer.reset()
		else if (logTimer.hasTimePassed(delayValue.get())) {
			PacketUtils.sendPacketNoEvent(C01PacketChatMessage(loginCmd))
			MinusBounce.hud.addNotification(Notification("AutoLogin", "Successfully logined.", Notification.Type.SUCCESS))
			loginState = false
			logTimer.reset()
		}
	}

    @EventTarget
    fun onReceivedPacket(event: ReceivedPacketEvent) {
    	mc.thePlayer ?: return

		val packet = event.packet

    	if (packet is S45PacketTitle) {
            val messageOrigin = packet.message ?: return
            val message = messageOrigin.unformattedText

    		if (message.contains(loginRegex.get(), true))
    			loginState = true

    		if (message.contains(regRegex.get(), true))
    			registerState = true
    	}

    	if (packet is S02PacketChat) {
            val message = packet.chatComponent.unformattedText

    		if (message.contains(loginRegex.get(), true))
    			loginState = true

    		if (message.contains(regRegex.get(), true))
    			registerState = true
    	}
    }
}
