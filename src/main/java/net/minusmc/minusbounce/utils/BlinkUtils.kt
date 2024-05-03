package net.minusmc.minusbounce.utils

import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayServer
import net.minecraft.network.play.client.*
import net.minecraft.network.play.client.C03PacketPlayer.*
import java.math.BigInteger
import java.util.*

object BlinkUtils : MinecraftInstance() {
    private val packets = LinkedList<Packet<INetHandlerPlayServer>>()
    private var movingPacketStat = false
    private var transactionStat = false
    private var keepAliveStat = false
    private var actionStat = false
    private var abilitiesStat = false
    private var invStat = false
    private var interactStat = false
    private var otherPacket = false

    private val packetToggleState = hashMapOf<Class<Packet<INetHandlerPlayServer>>, Boolean>()
    private var packetToggleStat = BooleanArray(20)

    init {
        Constants.clientPacketClasses.forEach {
            packetToggleState[it] = false
        }

        setBlinkState(off = true, release = true)
        clearPacket()
    }

    fun releasePacket(onlySelected: Boolean = false, amount: Int = -1, minBuff: Int = 0) {
        for (packet in packets) {
            if (packetToggleState[packet.javaClass] || !onlySelected)
                PacketUtils.sendPacketNoEvent(packet)
        }

        clearPacket(onlySelected, -1)
    }


    fun releasePacket(packetType: Class<Packet<INetHandlerPlayServer>>, onlySelected: Boolean = false, amount: Int = -1, minBuff: Int = 0) {
        var count = 0
        val filteredPackets = packets.filter {it.javaClass == packetType}
        
        while (tempBuffer.size > minBuff && (count < amount || amount <= 0)) {
            PacketUtils.sendPacketNoEvent(filteredPackets.removeAt(0)) 
            count++
        }
        
        clearPacket(packetType, onlySelected, count)
    }

    fun clearPacket(onlySelected: Boolean = false, amount: Int = -1) {
        val filteredPackets = packets.filter {!packetToggleState[it.javaClass] && onlySelected}
        packets.clear()

        filteredPackets.forEach {packets.add(it)}
    }

    fun clearPacket(packetType: Class<Packet<INetHandlerPlayServer>>, onlySelected: Boolean = false, amount: Int = -1) {
        var count = 0
        val filteredPackets = mutableListOf<Packet<INetHandlerPlayServer>>()

        for (packet in packets) {
            if (packet.javaClass != packetType)
                filteredPackets.add(packet)
            else {
                count++
                if (count > amount)
                    filteredPackets.add(packet)
            }
        }

        packets.clear()
        filteredPackets.forEach {packets.add(it)}
    }

    fun pushPacket(packet: Packet<*>): Boolean {
        if (packetToggleStat[packet.javaClass] && !isBlacklisted(packet.javaClass)) {
            packets.add(packet as Packet<INetHandlerPlayServer>)
            return true
        }

        return false
    }

    private fun isBlacklisted(packet: Packet<*>): Boolean {
        return packetType is C00Handshake || packetType is C00PacketLoginStart ||
            packetType is C00PacketServerQuery || packetType is C01PacketChatMessage ||
            packetType is C01PacketEncryptionResponse || packetType is C01PacketPing
    }

    fun setBlinkState() {
        setBlinkState(off = true)
        clearPacket()
    }

    fun setBlinkState(off: Boolean = false, release: Boolean = false, all: Boolean = false,
        packetMoving: Boolean = movingPacketStat, packetTransaction: Boolean = transactionStat, 
        packetKeepAlive: Boolean = keepAliveStat, packetAction: Boolean = actionStat, 
        packetAbilities: Boolean = abilitiesStat, packetInventory: Boolean = invStat, 
        packetInteract: Boolean = interactStat, other: Boolean = otherPacket
    ) {
        if (release)
            releasePacket()

        movingPacketStat = (packetMoving && !off) || all
        transactionStat = (packetTransaction && !off) || all
        keepAliveStat = (packetKeepAlive && !off) || all
        actionStat = (packetAction && !off) || all
        abilitiesStat = (packetAbilities && !off) || all
        invStat = (packetInventory && !off ) || all
        interactStat = (packetInteract && !off) || all
        otherPacket = (other && !off) || all

        if (all) {
            packetToggleState.keys.forEach {
                packetToggleState[it] = true
            }

            return
        }

        packetToggleState.keys.forEach {

            if (it == C00PacketKeepAlive::class.java)
                packetToggleState[it] = keepAliveStat

            else if (it == C01PacketChatMessage::class.java || it == C11PacketEnchantItem::class.java ||
                it == C12PacketUpdateSign::class.java || it == C14PacketTabComplete::class.java ||
                it == C15PacketClientSettings::class.java || it == C17PacketCustomPayload::class.java ||
                it == C18PacketSpectate::class.java || it == C19PacketResourcePackStatus::class.java)
                packetToggleState[it] = otherPacket

            else if (it == C03PacketPlayer::class.java || it == C04PacketPlayerPosition::class.java
                it == C05PacketPlayerLook::class.java || it == C06PacketPlayerPosLook::class.java)
                packetToggleState[it] = movingPacketStat

            else if (it == C0FPacketConfirmTransaction::class.java)
                packetToggleState[it] = transactionStat

            else if (it == C02PacketUseEntity::class.java || it == C09PacketHeldItemChange::class.java ||
                it == C0APacketAnimation::class.java || it == C0BPacketEntityAction::class.java)
                packetToggleState[it] = actionStat

            else if (it == C0CPacketInput::class.java || it == C13PacketPlayerAbilities::class.java)
                packetToggleStat[it] == abilitiesStat

            else if (it == C0DPacketCloseWindow::class.java || it == C0EPacketClickWindow::class.java ||
                it == C10PacketCreativeInventoryAction::class.java || it == C16PacketClientStatus::class.java)
                packetToggleState[it] = invStat

            else if (it == C07PacketPlayerDigging::class.java || it == C08PacketPlayerBlockPlacement::class.java)
                packetToggleState[it] = interactStat
        }
    }

    val totalPackets: Int
        get() = packets.size

    fun getTotalPackets(packetType: Class<Packet<INetHandlerPlayServer>>): Int {
        var packetCount = 0
        for (packet in packets) {
            if (packet::class.java == packetType)
                packetCount++
        }

        return if (packetCount > 0) packetCount else -302
    }
}
