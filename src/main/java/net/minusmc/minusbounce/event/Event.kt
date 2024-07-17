/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.event

open class Event {
    var stopRunEvent: Boolean = false
}

open class CancellableEvent : Event() {

    /**
     * Let you know if the event is cancelled
     *
     * @return state of cancel
     */
    var isCancelled: Boolean = false
}

enum class EventPriority(val priority: Int) {
    LOWEST(-100),
    LOW(-50),
    MEDIUM(0),
    HIGH(50),
    HIGHEST(100)
}