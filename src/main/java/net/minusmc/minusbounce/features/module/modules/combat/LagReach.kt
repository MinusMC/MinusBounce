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
import java.util.concurrent.LinkedBlockingQueue

@ModuleInfo(name = "LagReach", "Lag Reach", "Very very lag", ModuleCategory.COMBAT)
class LagReach : Module() {

    var fakePlayer: EntityOtherPlayerMP? = null
    private val aura = BoolValue("Aura", false)
    private val mode = ListValue("Mode", arrayOf("FakePlayer", "Intave", "IncomingBlink"), "IncomingBlink")
    private val pulseDelayValue = IntegerValue("Pulse", 200, 50, 1000)
    private val maxDelayValue = IntegerValue("Delay", 500, 50, 2000)
    private val spoof = BoolValue("Spoof", false)
    private val velocityDelay = IntegerValue("Spoof-Delay", 50, 0, 500)
    private val incomingBlink = BoolValue("IncomingBlink", true) { mode.get().equals("IncomingBlink", true) }
    private val velocityValue = BoolValue("StopOnVelocity", true) { mode.get().equals("IncomingBlink", true) }
    private val outgoingBlink = BoolValue("OutgoingBlink", true) { mode.get().equals("IncomingBlink", true) }
    private val attackValue = BoolValue("ReleaseOnAttack", true) { mode.get().equals("IncomingBlink", true) }
    private val intaveHurtTime = IntegerValue("Packets", 5, 0, 30) { mode.get().equals("Intave", true) }

    private val pulseTimer = MSTimer()
    private val maxTimer = MSTimer()
    var currentTarget: EntityLivingBase? = null
    private var shown = false

    private val packets = LinkedBlockingQueue<Packet<INetHandlerPlayClient>>()
    private val times = ArrayList<Long>()

    private var comboCounter = 0
    private var backtrack = false

    private var delay = 0L
    private var targetDelay = 0L


    private var releasing = false

    override fun onEnable() {
        if (spoof.get()) {
            packets.clear()
            times.clear()
        }
        backtrack = false
        releasing = false
        if (mode.equals("IncomingBlink") && outgoingBlink.get()) {
            BlinkUtils.setBlinkState(off = true, release = true)
        }
    }

    override fun onDisable() {
        removeFakePlayer()
        clearPackets()
        if (mode.equals("IncomingBlink") && outgoingBlink.get()) {
            BlinkUtils.setBlinkState(off = true, release = true)
        }
        if (spoof.get()) {
            while (!packets.isEmpty()) {
                PacketUtils.handlePacket(packets.take() as Packet<INetHandlerPlayClient?>)
            }
            times.clear()
        }
    }

    private fun removeFakePlayer() {
        if (fakePlayer == null) return
        currentTarget = null
        mc.theWorld.removeEntity(fakePlayer)
        fakePlayer = null
    }

    private fun clearPackets() {
        releasing = true
        while (!packets.isEmpty()) {
            PacketUtils.handlePacket(packets.take() as Packet<INetHandlerPlayClient?>)
        }
        releasing = false
        if (outgoingBlink.get())  {
            BlinkUtils.releasePacket()
            if (!backtrack) BlinkUtils.setBlinkState(off = true, release = true)
        }
    }



    private fun attackEntity(entity: EntityLivingBase) {
        val thePlayer = mc.thePlayer ?: return
        thePlayer.swingItem()
        mc.netHandler.addToSendQueue(C02PacketUseEntity(entity, C02PacketUseEntity.Action.ATTACK))
        if (mc.playerController.currentGameType != WorldSettings.GameType.SPECTATOR)
            thePlayer.attackTargetEntityWithCurrentItem(entity)
    }

    @EventTarget
    fun onAttack(event: AttackEvent) {
        comboCounter ++
        if ( mode.equals("FakePlayer") || mode.equals("Intave") ) {
            clearPackets()
            if (fakePlayer == null) {
                currentTarget = event.targetEntity as EntityLivingBase?
                val faker = EntityOtherPlayerMP(
                    mc.theWorld ?: return,
                    mc.netHandler.getPlayerInfo((currentTarget ?: return).uniqueID ?: return).gameProfile ?: return
                )

                faker.rotationYawHead = (currentTarget ?: return).rotationYawHead
                faker.renderYawOffset = (currentTarget ?: return).renderYawOffset
                faker.copyLocationAndAnglesFrom(currentTarget ?: return)
                faker.rotationYawHead = (currentTarget ?: return).rotationYawHead
                faker.health = (currentTarget ?: return).health
                val indices = (0..4).toList().toIntArray()
                for (index in indices) {
                    val equipmentInSlot = (currentTarget ?: return).getEquipmentInSlot(index) ?: continue
                    faker.setCurrentItemOrArmor(index, equipmentInSlot)
                }
                (mc.theWorld ?: return).addEntityToWorld(-1337, faker)

                fakePlayer = faker
                shown = true
            } else {
                if (event.targetEntity == fakePlayer) {
                    attackEntity(currentTarget ?: return)
                    event.cancelEvent()
                } else {
                    fakePlayer = null
                    currentTarget = event.targetEntity as EntityLivingBase?
                    shown = false
                }
            }
        } else {
            if (event.targetEntity != currentTarget) {
                clearPackets()
                currentTarget = event.targetEntity as EntityLivingBase?
            }
            currentTarget?.let {
                if (mc.thePlayer.getDistanceToEntityBox(it) > 2.6f) {
                    if (comboCounter >= 2) {
                        if (outgoingBlink.get()) BlinkUtils.setBlinkState(all = true)
                        backtrack = true
                        maxTimer.reset()
                    }
                }
            }
            if (attackValue.get() && outgoingBlink.get()) {
                BlinkUtils.releasePacket()
            }
        }
        if (spoof.get()) {
            if (event.targetEntity?.let { mc.thePlayer.getDistanceToEntityBox(it) }!! > 2.6f) {
                targetDelay = maxDelayValue.get().toLong()
            }
        }
    }

    @EventTarget
    fun onUpdate(@Suppress("UNUSED_PARAMETER") event: UpdateEvent?) {
        if (!MinusBounce.combatManager.inCombat) {
            removeFakePlayer()
        }
        if ( mode.equals("FakePlayer") || mode.equals("Intave") ) {
            if (aura.get() && !MinusBounce.moduleManager[KillAura::class.java]!!.state) {
                removeFakePlayer()
            }
            if (mc.thePlayer == null) return
            if (fakePlayer != null && EntityUtils.isRendered(fakePlayer ?: return) && ((currentTarget ?: return).isDead || !EntityUtils.isRendered(
                    currentTarget ?: return
                ))
            ) {
                removeFakePlayer()
            }
            if (currentTarget != null && fakePlayer != null) {
                (fakePlayer ?: return).health = (currentTarget ?: return).health
                val indices = (0..4).toList().toIntArray()
                for (index in indices) {
                    val equipmentInSlot = (currentTarget ?: return).getEquipmentInSlot(index) ?: continue
                    (fakePlayer ?: return).setCurrentItemOrArmor(index, equipmentInSlot)
                }
            }
            if (mode.equals("Intave") && mc.thePlayer.ticksExisted % intaveHurtTime.get() == 0) {
                if (fakePlayer != null) {
                    (fakePlayer ?: return).rotationYawHead = (currentTarget ?: return).rotationYawHead
                    (fakePlayer ?: return).renderYawOffset = (currentTarget ?: return).renderYawOffset
                    (fakePlayer ?: return).copyLocationAndAnglesFrom(currentTarget ?: return)
                    (fakePlayer ?: return).rotationYawHead = (currentTarget ?: return).rotationYawHead
                }
                pulseTimer.reset()
            } else if (mode.equals("FakePlayer") && pulseTimer.hasTimePassed(pulseDelayValue.get().toLong())) {
                if (fakePlayer != null) {
                    (fakePlayer ?: return).rotationYawHead = (currentTarget ?: return).rotationYawHead
                    (fakePlayer ?: return).renderYawOffset = (currentTarget ?: return).renderYawOffset
                    (fakePlayer ?: return).copyLocationAndAnglesFrom(currentTarget ?: return)
                    (fakePlayer ?: return).rotationYawHead = (currentTarget ?: return).rotationYawHead
                }
                pulseTimer.reset()
            }

            if (!shown && currentTarget != null && (currentTarget ?: return).uniqueID != null && mc.netHandler.getPlayerInfo(
                    (currentTarget ?: return).uniqueID ?: return
                ) != null && mc.netHandler.getPlayerInfo((currentTarget ?: return).uniqueID ?: return).gameProfile != null
            ) {
                val faker = EntityOtherPlayerMP(
                    mc.theWorld ?: return,
                    mc.netHandler.getPlayerInfo((currentTarget ?: return).uniqueID ?: return).gameProfile ?: return
                )

                faker.rotationYawHead = (currentTarget ?: return).rotationYawHead
                faker.renderYawOffset = (currentTarget ?: return).renderYawOffset
                faker.copyLocationAndAnglesFrom(currentTarget ?: return)
                faker.rotationYawHead = (currentTarget ?: return).rotationYawHead
                faker.health = (currentTarget ?: return).health
                val indices = (0..4).toList().toIntArray()
                for (index in indices) {
                    val equipmentInSlot = (currentTarget ?: return).getEquipmentInSlot(index) ?: continue
                    faker.setCurrentItemOrArmor(index, equipmentInSlot)
                }
                (mc.theWorld ?: return).addEntityToWorld(-1337, faker)

                fakePlayer = faker
                shown = true
            }
        } else {
            if (pulseTimer.hasTimePassed(pulseDelayValue.get().toLong()) && backtrack) {
                pulseTimer.reset()
                clearPackets()
            }
            if (maxTimer.hasTimePassed(maxDelayValue.get().toLong()) && backtrack) {
                clearPackets()
                backtrack = false
            }
        }
        if (spoof.get()) {
            if (mc.thePlayer.ticksExisted < 20) {
                times.clear()
                packets.clear()
            }
            ClientUtils.displayChatMessage(delay.toString() + ' ' + times.size.toString())
            delay += ((targetDelay - delay) / 3)
            targetDelay = (targetDelay * 0.93).toLong()
            if (!packets.isEmpty()) {
                while (times.first() < System.currentTimeMillis() - delay) {
                    PacketUtils.handlePacket(packets.take() as Packet<INetHandlerPlayClient?>)
                    times.remove(times.first())
                }
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (aura.get() && !MinusBounce.moduleManager[KillAura::class.java]!!.state || !backtrack) {
            clearPackets()
            backtrack = false
            return
        }

        if (mode.equals("IncomingBlink") && backtrack) {
            if (packet.javaClass.simpleName.startsWith("S", ignoreCase = true)) {
                if (mc.thePlayer.ticksExisted < 20) return
                if (incomingBlink.get()) {
                    if (packet is S12PacketEntityVelocity && velocityValue.get()) {
                        comboCounter = 0
                        event.cancelEvent()
                        packets.add(packet as Packet<INetHandlerPlayClient>)
                        clearPackets()
                        return
                    }
                    event.cancelEvent()
                    packets.add(packet as Packet<INetHandlerPlayClient>)
                }
            }
        }
        if (spoof.get()) {
            if (packet.javaClass.simpleName.startsWith("S", ignoreCase = true) && mc.thePlayer.ticksExisted > 20 && targetDelay > 0) {
                event.cancelEvent()
                times.add(System.currentTimeMillis())
                packets.add(packet as Packet<INetHandlerPlayClient>)
                if (packet is S12PacketEntityVelocity) targetDelay = velocityDelay.get().toLong()
                if (packet is S08PacketPlayerPosLook) {
                    targetDelay = 0L
                    while (!packets.isEmpty()) {
                        val packet = packets.take() as Packet<INetHandlerPlayClient?>
                        try {
                            PacketUtils.handlePacket(packet)
                        } catch (ignored: Exception) {
                            chat("Failed to process packet: " + packet.javaClass.name.replace("net.minecraft.network.play.server.", " "))
                        }
                    }
                    times.clear()
                    return
                }
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
