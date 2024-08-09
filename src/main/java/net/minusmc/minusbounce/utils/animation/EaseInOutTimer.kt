package net.minusmc.minusbounce.utils.animation

class EaseInOutTimer(
	var delay: Long = 500L, 
	var duringDelay: Long = 3000L, 
	var state: State = State.DURING_OUT,
	var hover: Boolean = false
) {
    var progress = 0f
    private var lastMS = System.currentTimeMillis()

    fun update() {
    	when (state) {
	    	State.IN -> {
	    		val deltaTime = System.currentTimeMillis() - lastMS
	    		val progress = deltaTime.toFloat() / delay

	    		this.progress = if (progress < 1f) progress else {
	    			if (!hover) {
	    				state = State.DURING_IN
	    				lastMS = System.currentTimeMillis()
	    			}
	    			1f
	    		}
	    	}
	    	State.OUT -> {
	    		val deltaTime = System.currentTimeMillis() - lastMS
	    		val progress = 1 - deltaTime.toFloat() / delay

	    		this.progress = if (progress > 0f) progress else {
	    			if (!hover) {
	    				state = State.DURING_OUT
	    				lastMS = System.currentTimeMillis()
	    			}
	    			0f
	    		}
	    	}

	    	State.DURING_IN -> if (!hover && System.currentTimeMillis() - lastMS > duringDelay) {
	    		state = State.OUT
	    		lastMS = System.currentTimeMillis()
	    	}
	    	
	    	State.DURING_OUT -> if (!hover && System.currentTimeMillis() - lastMS > duringDelay) {
	    		state = State.IN
	    		lastMS = System.currentTimeMillis()
	    	}
		}
	}

	fun resetTime() {
		lastMS = System.currentTimeMillis()
	}

	val timePassed: Boolean
		get() = System.currentTimeMillis() - lastMS > delay

    enum class State {
    	IN, DURING_IN, OUT, DURING_OUT
    }

}