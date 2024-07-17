/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.event

class EventManager {

    private val registry = hashMapOf<Class<out Event>, MutableList<EventHook>>()

    /**
     * Register [listener]
     */
    fun registerListener(listener: Listenable) {
        for (method in listener.javaClass.declaredMethods) {
            if (method.isAnnotationPresent(EventTarget::class.java) && method.parameterTypes.size == 1) {
                if (!method.isAccessible)
                    method.isAccessible = true

                val eventClass = method.parameterTypes[0] as Class<out Event>
                val eventTarget = method.getAnnotation(EventTarget::class.java)

                val invokableEventTargets = registry.getOrElse(eventClass) { arrayListOf<EventHook>() }
                try {
                    invokableEventTargets.add(EventHook(listener, method, eventTarget.priority, eventTarget))
                } catch (e: Exception) {
                    e.printStackTrace()
                    // switch to normal handler
                    invokableEventTargets.add(EventHook(listener, method, eventTarget))
                }
                registry[eventClass] = invokableEventTargets.sortedByDescending { it.priority }.toMutableList()
            }
        }
    }

    /**
     * Unregister listener
     *
     * @param listenable for unregister
     */
    fun unregisterListener(listenable: Listenable) {
        for ((key, targets) in registry) {
            targets.removeIf { it.eventClass == listenable }

            registry.put(key, targets)
        }
    }

    /**
     * Call event to listeners
     *
     * @param event to call
     */
    fun callEvent(event: Event) {
        val targets = registry[event.javaClass] ?: return

        targets.filter { it.eventClass.handleEvents() || it.isIgnoreCondition }.forEach {
            try {
                it.method.invoke(it.eventClass, event)
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
            }

            if (event.stopRunEvent)
                return
        }
    }
}
