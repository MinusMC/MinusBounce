package net.minusmc.minusbounce.features.module.modules.combat

import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.Packet
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.*
import net.minecraft.network.play.INetHandlerPlayClient
import net.minecraft.world.WorldSettings
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.utils.BlinkUtils
import net.minusmc.minusbounce.utils.ClientUtils
import net.minusmc.minusbounce.utils.EntityUtils
import net.minusmc.minusbounce.utils.PacketUtils
import net.minusmc.minusbounce.utils.extensions.getDistanceToEntityBox
import net.minusmc.minusbounce.utils.timer.MSTimer
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.IntegerValue
import net.minusmc.minusbounce.value.ListValue

@ModuleInfo(name = "LagReach", "Lag Reach", "Very very lag", ModuleCategory.COMBAT)
class LagReach : Module() {
    private val modeValue = ListValue("Mode", arrayOf("FakePlayer", "Intave", "IncomingBlink"), "IncomingBlink")
    private val pulseDelayValue = IntegerValue("Pulse", 200, 50, 1000)
    private val maxDelayValue = IntegerValue("Delay", 500, 50, 2000)
    private val spoof = BoolValue("Spoof", false)
    private val spoofDelay = IntegerValue("SpoofDelay", 50, 0, 500) {spoof.get()}
    private val incomingBlink = BoolValue("IncomingBlink", true) { modeValue.get().equals("IncomingBlink", true) }
    private val velocityValue = BoolValue("PauseOnVelocity", true) { modeValue.get().equals("IncomingBlink", true) }
    private val outgoingBlink = BoolValue("OutgoingBlink", true) { modeValue.get().equals("IncomingBlink", true) }
    private val attackValue = BoolValue("ReleaseOnAttack", true) { modeValue.get().equals("IncomingBlink", true) }
    private val intaveHurtTime = IntegerValue("Packets", 5, 0, 30) { modeValue.get().equals("Intave", true) }
    private val aura = BoolValue("OnlyAura", false)

    private var fakePlayer: EntityOtherPlayerMP? = null
    private val pulseTimer = MSTimer()
    private val maxTimer = MSTimer()
    private var currentTarget: EntityLivingBase? = null
    private var shown = false

    private val packets = mutableListOf<Packet<INetHandlerPlayClient>>()
    private val times = ArrayList<Long>()

    private var comboCounter = 0
    private var backtrack = false

    private var delay = 0L
    private var targetDelay = 0L

    override fun onEnable() {
        if (spoof.get()) {
            packets.clear()
            times.clear()
        }

        backtrack = false

        if (modeValue.get().equals("IncomingBlink", true) && outgoingBlink.get())
            BlinkUtils.setBlinkState(off = true, release = true)
    }

    override fun onDisable() {
        removeFakePlayer()
        clearPackets()

        if (modeValue.get().equals("imcomingblink", true) && outgoingBlink.get())
            BlinkUtils.setBlinkState(off = true, release = true)

        if (spoof.get()) {
            packets.map {it}.forEach { PacketUtils.handlePacketNoEvent(it) }
            packets.clear()
            times.clear()
        }
    }

    private fun removeFakePlayer() {
        fakePlayer ?: return
        currentTarget = null
        mc.theWorld.removeEntity(fakePlayer)
        fakePlayer = null
    }

    private fun clearPackets() {
        packets.map {it}.forEach { PacketUtils.handlePacketNoEvent(it) }
        packets.clear()

        if (outgoingBlink.get()) {
            BlinkUtils.releasePacket()
            if (!backtrack) 
                BlinkUtils.setBlinkState(off = true, release = true)
        }
    }

    @EventTarget
    fun onAttack(event: AttackEvent) {
        mc.theWorld ?: return

        val target = event.targetEntity as EntityLivingBase
        val targetId = target.uniqueID ?: return
        val gameProfile = mc.netHandler.getPlayerInfo(targetId).gameProfile ?: return

        comboCounter++
        if (modeValue.get().equals("fakeplayer", true) || modeValue.get().equals("intave", true)) {
            clearPackets()

            if (fakePlayer == null) {
                currentTarget = target

                val faker = EntityOtherPlayerMP(mc.theWorld, gameProfile)
                faker.rotationYawHead = target.rotationYawHead
                faker.renderYawOffset = target.renderYawOffset
                faker.copyLocationAndAnglesFrom(target)
                faker.rotationYawHead = target.rotationYawHead
                faker.health = target.health

                for (idx in 0..4) {
                    val equipmentInSlot = target.getEquipmentInSlot(idx) ?: continue
                    faker.setCurrentItemOrArmor(idx, equipmentInSlot)
                }

                mc.theWorld.addEntityToWorld(-1337, faker)
                fakePlayer = faker
                shown = true
            } else {
                if (target == fakePlayer) {
                    mc.playerController.attackEntity(mc.thePlayer, target)
                    event.cancelEvent()
                } else {
                    fakePlayer = null
                    currentTarget = target
                    shown = false
                }
            }
        } else {
            if (target != currentTarget) {
                clearPackets()
                currentTarget = target
            }

            if (mc.thePlayer.getDistanceToEntityBox(target) > 2.6f && comboCounter >= 2) {
                if (outgoingBlink.get()) 
                    BlinkUtils.setBlinkState(all = true)
                backtrack = true
                maxTimer.reset()
            }

            if (attackValue.get() && outgoingBlink.get())
                BlinkUtils.releasePacket()

        }

        if (spoof.get() && mc.thePlayer.getDistanceToEntityBox(target) > 2.6) {
            targetDelay = maxDelayValue.get().toLong()
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {

        mc.thePlayer ?: return
        mc.theWorld ?: return

        if (!MinusBounce.combatManager.inCombat)
            removeFakePlayer()


        if (modeValue.get().equals("fakeplayer", true) || modeValue.get().equals("intave", true)) {
            val killAura = MinusBounce.moduleManager[KillAura::class.java]

            if (aura.get() && killAura != null && !killAura.state)
                removeFakePlayer()

            currentTarget?.let { current -> 
                fakePlayer?.let { currentFaker ->
                    if (EntityUtils.isRendered(currentFaker) && (current.isDead || EntityUtils.isRendered(current)))
                        removeFakePlayer()

                    currentFaker.health = current.health
                    for (idx in 0..4) {
                        val equipmentInSlot = current.getEquipmentInSlot(idx) ?: continue
                        currentFaker.setCurrentItemOrArmor(idx, equipmentInSlot)
                    }

                    when (modeValue.get().lowercase()) {
                        "intave" -> if (mc.thePlayer.ticksExisted % intaveHurtTime.get() == 0) {
                            currentFaker.rotationYawHead = current.rotationYawHead
                            currentFaker.renderYawOffset = current.renderYawOffset
                            currentFaker.copyLocationAndAnglesFrom(current)
                            currentFaker.rotationYawHead = current.rotationYawHead
                        }
                        "fakeplayer" -> if (pulseTimer.hasTimePassed(pulseDelayValue.get())) {
                            currentFaker.rotationYawHead = current.rotationYawHead
                            currentFaker.renderYawOffset = current.renderYawOffset
                            currentFaker.copyLocationAndAnglesFrom(current)
                            currentFaker.rotationYawHead = current.rotationYawHead

                            pulseTimer.reset()
                        }
                    }
                } ?: return

                val targetId = current.uniqueID ?: return
                val playerInfo = mc.netHandler.getPlayerInfo(targetId) ?: return
                val gameProfile = playerInfo.gameProfile ?: return

                if (!shown) {
                    val faker = EntityOtherPlayerMP(mc.theWorld, gameProfile)

                    faker.rotationYawHead = current.rotationYawHead
                    faker.renderYawOffset = current.renderYawOffset
                    faker.copyLocationAndAnglesFrom(current)
                    faker.rotationYawHead = current.rotationYawHead
                    faker.health = faker.health

                    for (idx in 0..4) {
                        val equipmentInSlot = current.getEquipmentInSlot(idx) ?: continue
                        faker.setCurrentItemOrArmor(idx, equipmentInSlot)
                    }

                    mc.theWorld.addEntityToWorld(-1337, faker)

                    fakePlayer = faker
                    shown = true
                }

            } ?: return

        } else if (backtrack) {
            if (pulseTimer.hasTimePassed(pulseDelayValue.get())) {
                pulseTimer.reset()
                clearPackets()
            }
            if (maxTimer.hasTimePassed(maxDelayValue.get())) {
                clearPackets()
                backtrack = false
            }
        }

        if (spoof.get()) {
            if (mc.thePlayer.ticksExisted < 20) {
                times.clear()
                packets.clear()
            }

            delay += (targetDelay - delay) / 3
            targetDelay *= 93
            targetDelay /= 100

            if (packets.isEmpty()) 
                return

            while (times.first() < System.currentTimeMillis() - delay) {
                PacketUtils.handlePacketNoEvent(packets.removeFirst())
                times.removeFirst()
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        val killAura = MinusBounce.moduleManager[KillAura::class.java]

        if (aura.get() && (killAura != null && killAura.state) || !backtrack) {
            clearPackets()
            backtrack = false
            return
        }

        if (modeValue.get().equals("incomingblink") && packet.javaClass.name.contains("play.server.", true) && backtrack) {
            if (mc.thePlayer.ticksExisted < 20) 
                return

            if (incomingBlink.get()) {

                event.cancelEvent()
                packets.add(packet as Packet<INetHandlerPlayClient>)

                if (packet is S12PacketEntityVelocity && velocityValue.get()) {
                    comboCounter = 0
                    clearPackets()
                    return
                }
            }
        }

        if (spoof.get() && packet.javaClass.name.contains("play.server.", true) && mc.thePlayer.ticksExisted > 20 && targetDelay > 0) {
            event.cancelEvent()
            times.add(System.currentTimeMillis())
            packets.add(packet as Packet<INetHandlerPlayClient>)

            if (packet is S12PacketEntityVelocity) 
                targetDelay = spoofDelay.get().toLong()

            if (packet is S08PacketPlayerPosLook) {
                targetDelay = 0L

                packets.map {it}.forEach { PacketUtils.handlePacketNoEvent(it) }
                packets.clear()

                times.clear()
                return
            }
        }
    }
    
    @EventTarget
    fun onWorld(event: WorldEvent) {
        if (spoof.get()) {
            times.clear()
            packets.clear()
        }
    }
}
