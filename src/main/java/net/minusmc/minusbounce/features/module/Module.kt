/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module

import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.util.ResourceLocation
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.Listenable
import net.minusmc.minusbounce.features.module.modules.misc.AutoDisable.DisableEvent
import net.minusmc.minusbounce.ui.client.hud.element.elements.Notification
import net.minusmc.minusbounce.utils.ClientUtils
import net.minusmc.minusbounce.utils.MinecraftInstance
import net.minusmc.minusbounce.value.Value
import org.lwjgl.input.Keyboard

open class Module : MinecraftInstance(), Listenable {

    // Module information
    // TODO: Remove ModuleInfo and change to constructor (#Kotlin)
    var name: String
    var spacedName: String
    var description: String
    var category: ModuleCategory
    var keyBind = Keyboard.CHAR_NONE
        set(keyBind) {
            field = keyBind

            if (!MinusBounce.isStarting)
                MinusBounce.fileManager.saveConfig(MinusBounce.fileManager.modulesConfig)
        }
    var array = true
        set(array) {
            field = array

            if (!MinusBounce.isStarting)
                MinusBounce.fileManager.saveConfig(MinusBounce.fileManager.modulesConfig)
        }
    private val canEnable: Boolean
    val onlyEnable: Boolean
    private val forceNoSound: Boolean

    var slideStep = 0F
    var animation = 0F
    var autoDisables = mutableListOf<DisableEvent>()

    init {
        val moduleInfo = javaClass.getAnnotation(ModuleInfo::class.java)!!

        name = moduleInfo.name
        spacedName = if (moduleInfo.spacedName == "") name else moduleInfo.spacedName
        description = moduleInfo.description
        category = moduleInfo.category
        keyBind = moduleInfo.keyBind
        array = moduleInfo.array
        canEnable = moduleInfo.canEnable
        onlyEnable = moduleInfo.onlyEnable
        forceNoSound = moduleInfo.forceNoSound
    }

    // Current state of module
    var state = false
        set(value) {
            if (field == value || !canEnable) return

            // Call toggle
            onToggle(value)

            // Play sound and add notification
            if (!MinusBounce.isStarting && !forceNoSound) {
                when (MinusBounce.moduleManager.toggleSoundMode) {
                    1 -> mc.soundHandler.playSound(PositionedSoundRecord.create(ResourceLocation("random.click"),
                        1F))
                    2 -> (if (value) MinusBounce.tipSoundManager.enableSound else MinusBounce.tipSoundManager.disableSound).asyncPlay(MinusBounce.moduleManager.toggleVolume)
                }
                if (MinusBounce.moduleManager.shouldNotify)
                    MinusBounce.hud.addNotification(Notification("${if (value) "Enabled" else "Disabled"} §r$name", if (value) Notification.Type.SUCCESS else Notification.Type.ERROR, 1000L))
            }

            // Call on enabled or disabled
            if (value) {
                onEnable()

                if (!onlyEnable)
                    field = true
            } else {
                onDisable()
                field = false
            }

            // Save module state
            MinusBounce.fileManager.saveConfig(MinusBounce.fileManager.modulesConfig)
        }


    // HUD
    val hue = Math.random().toFloat()
    var slide = 0F
    var arrayY = 0F

    // Tag
    open val tag: String?
        get() = null

    /**
     * Toggle module
     */
    fun toggle() {
        state = !state
    }

    /**
     * Print [msg] to chat
     */
    protected fun chat(msg: String) = ClientUtils.displayChatMessage("§8[§9§l${MinusBounce.CLIENT_NAME}§8] §3$msg")

    /**
     * Called when module toggled
     */
    open fun onToggle(state: Boolean) {}

    /**
     * Called when module enabled
     */
    open fun onEnable() {}

    /**
     * Called when module disabled
     */
    open fun onDisable() {}

    /**
     * Called when module initialized
     */
    open fun onInitialize() {}

    open fun onInitModeListValue() {}

    /**
     * Get module by [valueName]
     */
    open fun getValue(valueName: String) = values.find { it.name.equals(valueName, ignoreCase = true) }

    /**
     * Get all values of module
     */
    open val values: List<Value<*>>
        get() = javaClass.declaredFields.map { valueField ->
            valueField.isAccessible = true
            valueField[this]
        }.filterIsInstance<Value<*>>()

    /**
     * Events should be handled when module is enabled
     */
    override fun handleEvents() = state
}
