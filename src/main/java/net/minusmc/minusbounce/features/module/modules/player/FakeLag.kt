package net.minusmc.minusbounce.features.module.modules.player

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.UpdateEvent
import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.utils.BlinkUtils
import net.minusmc.minusbounce.utils.extensions.getDistanceToEntityBox
import net.minusmc.minusbounce.utils.timer.MSTimer
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.INetHandler
import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayClient
import net.minecraft.network.play.INetHandlerPlayServer
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.event.WorldEvent
import net.minusmc.minusbounce.utils.MovementUtils
import net.minusmc.minusbounce.utils.PacketUtils
import net.minusmc.minusbounce.utils.misc.RandomUtils
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.IntegerValue
import net.minusmc.minusbounce.value.ListValue
import java.util.*
import kotlin.random.Random

@ModuleInfo(name = "FakeLag", description = "Make you teleport infrontof entity.", category = ModuleCategory.PLAYER)
object FakeLag : Module() {
    private val modeValue = ListValue("Mode", arrayOf("Dynamic", "Latency"), "Dynamic")
    private val latencyType = ListValue("Type", arrayOf("All", "InBound", "OutBound"), "All") { modeValue.get().equals("Latency", true)}
    private val latencyMoveOnly = BoolValue("MoveOnly", false) { modeValue.get().equals("Latency", true)}

    private val minRand: IntegerValue = object : IntegerValue("MinDelay", 170, 0, 20000, "ms", {modeValue.get().equals("Latency", true)}) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val v = maxRand.get()
            if (v < newValue) set(v)
        }
    }
    private val maxRand: IntegerValue = object : IntegerValue("MaxDelay", 500, 0, 20000, "ms", {modeValue.get().equals("Latency", true)}) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val v = minRand.get()
            if (v > newValue) set(v)
        }
    }

    private val fakeLagInclude = BoolValue("Include", true) { modeValue.get().equals("Latency", true) }
    private val fakeLagExclude = BoolValue("Exclude", false) { modeValue.get().equals("Latency", true) }

    // variables
    private val outBus = LinkedList<Packet<INetHandlerPlayServer>>()
    private val inBus = LinkedList<Packet<INetHandlerPlayClient>>()

    private val ignoreBus = LinkedList<Packet<out INetHandler>>()

    private val inTimer = MSTimer()
    private val outTimer = MSTimer()

    private var inDelay = 0
    private var outDelay = 0
    private val lagDelay = MSTimer()
    private val lagDuration = MSTimer()

    private var delayLength = 0L
    private var durationLength = 0L

    private var currentState = 0
    private var closestEntity = 0f

    private var resetTimers = false


    override fun onEnable() {
        if (modeValue.get().equals("Dynamic", true)) {
            if(!MinusBounce.moduleManager[Blink::class.java]!!.state) {
                BlinkUtils.clearPacket()
            }
            lagDuration.reset()
            lagDelay.reset()
            resetTimers = true
        }
        if (modeValue.get().equals("Latency", true)) {
            inBus.clear()
            outBus.clear()
            ignoreBus.clear()

            inTimer.reset()
            outTimer.reset()
        }
    }

    override fun onDisable() {
        if (modeValue.get().equals("Dynamic", true)) {
            if (mc.thePlayer == null) return
            BlinkUtils.setBlinkState(off = true, release = true)
        }
        if (modeValue.get().equals("Latency", true)) {
            while (inBus.size > 0)
                inBus.poll()?.processPacket(mc.netHandler)

            while (outBus.size > 0) {
                val upPacket = outBus.poll() ?: continue
                PacketUtils.sendPacketNoEvent(upPacket)
            }

            inBus.clear()
            outBus.clear()
            ignoreBus.clear()
        }
    }

    @EventTarget(priority = -100)
    fun onPacket(event: PacketEvent) {
        mc.thePlayer ?: return
        mc.theWorld ?: return
        val packet = event.packet
        if (ignoreBus.remove(packet)) return

        if ((latencyType.get().equals("outbound", true) || latencyType.get().equals("all", true))
            && packet::class.java.simpleName.startsWith("C", true)
            && (!fakeLagInclude.get() || "c0f,confirmtransaction,packetplayer,c17".split(",")
                .find { packet::class.java.simpleName.contains(it, true) } != null)
            && (!fakeLagExclude.get() || "c0f,confirmtransaction,packetplayer,c17".split(",")
                .find { packet::class.java.simpleName.contains(it, true) } == null)
        ) {
            outBus.add(packet as Packet<INetHandlerPlayServer>)
            ignoreBus.add(packet)
            event.cancelEvent()
        }

        if ((latencyType.get().equals("inbound", true) || latencyType.get().equals("all", true))
            && packet::class.java.simpleName.startsWith("S", true)
            && (!fakeLagInclude.get() || "c0f,confirmtransaction,packetplayer,c17".split(",")
                .find { packet::class.java.simpleName.contains(it, true) } != null)
            && (!fakeLagExclude.get() || "c0f,confirmtransaction,packetplayer,c17".split(",")
                .find { packet::class.java.simpleName.contains(it, true) } == null)
        ) {
            inBus.add(packet as Packet<INetHandlerPlayClient>)
            ignoreBus.add(packet)
            event.cancelEvent()
        }
    }


    @EventTarget
    fun onWorld(event: WorldEvent) {
        inBus.clear()
        outBus.clear()
        ignoreBus.clear()

        inTimer.reset()
        outTimer.reset()
    }

    @EventTarget(priority = -5)
    fun onUpdate(event: UpdateEvent) {
        if (modeValue.get().equals("Latency", true)) {
            mc.netHandler ?: return

            if (!inBus.isEmpty() && ((latencyMoveOnly.get() && !MovementUtils.isMoving) || inTimer.hasTimePassed(inDelay.toLong()))) {
                while (inBus.size > 0)
                    inBus.poll()?.processPacket(mc.netHandler)
                inDelay = RandomUtils.nextInt(minRand.get(), maxRand.get())
                inTimer.reset()
            }
            if (!outBus.isEmpty() && ((latencyMoveOnly.get() && !MovementUtils.isMoving) || outTimer.hasTimePassed(
                    outDelay.toLong()
                ))
            ) {
                while (outBus.size > 0) {
                    val upPacket = outBus.poll() ?: continue
                    PacketUtils.sendPacketNoEvent(upPacket)
                }
                outDelay = RandomUtils.nextInt(minRand.get(), maxRand.get())
                outTimer.reset()
            }
        }
        if (modeValue.get().equals("Dynamic", true)) {
            if (MinusBounce.moduleManager[Blink::class.java]!!.state) {
                lagDelay.reset()
                lagDuration.reset()
                return
            }
            if (mc.thePlayer.isDead) {
                BlinkUtils.setBlinkState(off = true, release = true)
                return
            }
            closestEntity = 1000f

            for (entity in mc.theWorld.loadedEntityList) {
                val it = entity as EntityLivingBase
                if (mc.thePlayer.getDistanceToEntityBox(it) < closestEntity) {
                    closestEntity = mc.thePlayer.getDistanceToEntityBox(it).toFloat()
                }
            }

            val prevState = currentState
            if (closestEntity > 30f) {
                currentState = 1
            } else if (closestEntity > 6f) {
                currentState = 2
            } else if (closestEntity > 4f) {
                currentState = 3
            } else if (closestEntity > 2.8f) {
                currentState = 4
            } else {
                currentState = 5
            }

            if (prevState != currentState) {
                lagDelay.reset()
                lagDuration.reset()
                resetTimers = true
            }

            if (lagDelay.hasTimePassed(durationLength + delayLength)) {
                lagDelay.reset()
                lagDuration.reset()
                resetTimers = true
                BlinkUtils.setBlinkState(all = true)
            }

            if (lagDuration.hasTimePassed(durationLength)) {
                BlinkUtils.setBlinkState(off = true, release = true)
            }

            if (resetTimers) {
                when (currentState) {
                    1 -> {
                        durationLength = 300L + Random.nextInt(0, 153).toLong()
                        delayLength = 1500L + Random.nextInt(0, 500).toLong()
                    }

                    2 -> {
                        durationLength = 320L + Random.nextInt(0, 83).toLong()
                        delayLength = 1000L + Random.nextInt(0, 120).toLong()
                    }

                    3 -> {
                        durationLength = 300L + Random.nextInt(0, 60).toLong()
                        delayLength = 250L + Random.nextInt(0, 45).toLong()
                    }

                    4 -> {
                        durationLength = 750L + Random.nextInt(0, 120).toLong()
                        delayLength = 120L + Random.nextInt(0, 40).toLong()
                    }

                    5 -> {
                        durationLength = 200L + Random.nextInt(0, 100).toLong()
                        delayLength = 100L + Random.nextInt(0, 30).toLong()
                    }

                    else -> {
                        durationLength = 1L
                        delayLength = 1L
                    }
                }
            }
        }
    }
}
