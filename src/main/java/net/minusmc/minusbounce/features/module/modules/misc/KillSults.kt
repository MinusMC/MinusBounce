package net.minusmc.minusbounce.features.module.modules.misc

import net.minecraft.entity.player.EntityPlayer
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.EntityKilledEvent
import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.utils.ClassUtils
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.ListValue
import net.minusmc.minusbounce.value.TextValue


@ModuleInfo(name = "Killsults", description = "Insults people when you kill them.", category = ModuleCategory.MISC)
class KillSults: Module() {
	private val modeValue = ListValue("Mode", arrayOf("Custom"), "Custom")
	private val directMessage = BoolValue("DirectMessage", false)
    private val customTextValue = TextValue("CustomTextValue", "L ") { modeValue.get().equals("Custom", true) }

	private val texts = hashMapOf<String, ArrayList<String>>()

	override fun onInitialize() {
        for (path in ClassUtils.killSultFiles) {
			val inputStream = MinusBounce::class.java.getResourceAsStream(path)!!
			val dataText = inputStream.bufferedReader().use { it.readText() }
			val name = path.split("/").last().replace(".txt", "")
			val text = ArrayList(dataText.lines())
            texts[name.lowercase()] = text
		}
        texts["custom"] = ArrayList()
		modeValue.changeListValues(texts.keys.toTypedArray())
	}

    private val textsForMode: ArrayList<String>
        get() = if (modeValue.get().equals("custom", true))
            arrayListOf(customTextValue.get())
        else
            texts[modeValue.get().lowercase()] ?: throw NullPointerException()

	@EventTarget
	fun onEntityKilled(event: EntityKilledEvent) {
		val target = event.targetEntity
		if (target !is EntityPlayer) return
        val message = textsForMode.random().replace("%name%", target.getName())

		if (directMessage.get())
            mc.thePlayer.sendChatMessage("/msg ${target.getName()} $message")
		else
			mc.thePlayer.sendChatMessage(message)
	}

    override val tag: String
		get() = modeValue.get()
}