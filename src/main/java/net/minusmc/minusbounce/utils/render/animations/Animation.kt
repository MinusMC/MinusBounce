package net.minusmc.minusbounce.utils.render.animations

import net.minusmc.minusbounce.utils.timer.TimerUtil
import kotlin.math.max
import kotlin.math.min

/**
 * This animation superclass was made by Foggy and advanced by cedo
 *
 * @author Foggy
 * @author cedo
 * @since 7/21/2020 (yes 2020)
 * @since 7/29/2021
 * @param duration Time in milliseconds of how long you want the animation to take.
 * @param endPoint The desired distance for the animated object to go.
 * @param direction Direction in which the graph is going. If backwards, will start from endPoint and go to 0.
 */
abstract class Animation(var duration: Int, var endPoint: Double, var direction: Direction = Direction.FORWARDS) {
    var timerUtil = TimerUtil()

    fun finished(direction: Direction): Boolean {
        return isDone && this.direction == direction
    }

    val linearOutput: Double
        get() = 1 - timerUtil.time / duration.toDouble() * endPoint

    fun reset() {
        timerUtil.reset()
    }

    val isDone: Boolean
        get() = timerUtil.hasTimeElapsed(duration.toLong())

    fun changeDirection() {
        setDirection(direction.opposite())
    }

    open fun setDirection(direction: Direction): Animation {
        if (this.direction != direction) {
            this.direction = direction
            timerUtil.time = System.currentTimeMillis() - (duration - min(duration, timerUtil.time.toInt()))
        }
        return this
    }



    protected open fun correctOutput(): Boolean {
        return false
    }

    val output: Double
        get() {
            if (direction.forwards()) {
                return if (isDone)
                    endPoint
                else
                    getEquation(timerUtil.time / duration.toDouble()) * endPoint
            } else {
                if (isDone) {
                    return 0.0
                }
                if (correctOutput()) {
                    val revTime: Double = min(duration.toDouble(), max(0, duration - timerUtil.time).toDouble())
                    return getEquation(revTime / duration.toDouble()) * endPoint
                }
                return (1 - getEquation(timerUtil.time / duration.toDouble())) * endPoint
            }
        }

    val outputFloat: Float
        get() = output.toFloat()
    // This is where the animation equation should go, for example, a logistic function. Output should range from 0 to 1.
    // This will take the timer's time as an input, x.
    protected abstract fun getEquation(x: Double): Double
}