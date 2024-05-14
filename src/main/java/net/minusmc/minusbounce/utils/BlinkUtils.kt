package net.minusmc.minusbounce.utils

import net.minusmc.minusbounce.event.*
import net.minecraft.network.Packet
import net.minecraft.network.handshake.client.*
import net.minecraft.network.play.INetHandlerPlayServer
import net.minecraft.network.play.client.*
import net.minecraft.network.play.client.C03PacketPlayer.*
import net.minecraft.network.status.client.*
import net.minecraft.network.status.server.*
import net.minecraft.network.login.client.*
import net.minecraft.network.login.server.*

object BlinkUtils : MinecraftInstance(), Listenable {
    private val packets = mutableListOf<Packet<INetHandlerPlayServer>>()
    private var movingPacketState = false
    private var transactionState = false
    private var keepAliveState = false
    private var actionState = false
    private var abilitiesState = false
    private var invState = false
    private var interactState = false
    private var otherPacket = false

    private val packetToggleState = hashMapOf<Class<out Packet<INetHandlerPlayServer>>, Boolean>()

    init {
        Constants.clientPacketClasses.forEach {
            packetToggleState[it] = false
        }

        setBlinkState(off = true, release = true)
        clearPacket()
    }


    @EventTarget(priority = -100)
    fun onPacket(event: PacketEvent) {
        if (pushPacket(event.packet))
            event.cancelEvent()
    }

    override fun handleEvents() = true

    fun releasePacket(onlySelected: Boolean = false, amount: Int = -1, minBuff: Int = 0) {
        for (packet in packets) {
            if (packetToggleState[packet.javaClass] ?: false || !onlySelected)
                PacketUtils.sendPacketNoEvent(packet)
        }

        clearPacket(onlySelected, -1)
    }


    fun releasePacket(packetType: Class<Packet<INetHandlerPlayServer>>, onlySelected: Boolean = false, amount: Int = -1, minBuff: Int = 0) {
        var count = 0
        val filteredPackets = packets.filter {it.javaClass == packetType}.toMutableList()
        
        while (filteredPackets.size > minBuff && (count < amount || amount <= 0)) {
            PacketUtils.sendPacketNoEvent(filteredPackets.removeFirst()) 
            count++
        }
        
        clearPacket(packetType, onlySelected, count)
    }

    fun clearPacket(onlySelected: Boolean = false, amount: Int = -1) {
        val filteredPackets = packets.filter {!(packetToggleState[it.javaClass] ?: false) && onlySelected}
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
        if (packetToggleState[packet::class.java] ?: false && !isBlacklisted(packet)) {
            packets.add(packet as Packet<INetHandlerPlayServer>)
            return true
        }

        return false
    }

    private fun isBlacklisted(packet: Packet<*>): Boolean {
        return packet is C00Handshake || packet is C00PacketLoginStart ||
            packet is C00PacketServerQuery || packet is C01PacketChatMessage ||
            packet is C01PacketEncryptionResponse || packet is C01PacketPing
    }

    fun setBlinkState() {
        setBlinkState(off = true)
        clearPacket()
    }

    fun setBlinkState(off: Boolean = false, release: Boolean = false, all: Boolean = false,
        packetMoving: Boolean = movingPacketState, packetTransaction: Boolean = transactionState, 
        packetKeepAlive: Boolean = keepAliveState, packetAction: Boolean = actionState, 
        packetAbilities: Boolean = abilitiesState, packetInventory: Boolean = invState, 
        packetInteract: Boolean = interactState, other: Boolean = otherPacket
    ) {
        if (release)
            releasePacket()

        movingPacketState = (packetMoving && !off) || all
        transactionState = (packetTransaction && !off) || all
        keepAliveState = (packetKeepAlive && !off) || all
        actionState = (packetAction && !off) || all
        abilitiesState = (packetAbilities && !off) || all
        invState = (packetInventory && !off ) || all
        interactState = (packetInteract && !off) || all
        otherPacket = (other && !off) || all

        if (all) {
            packetToggleState.keys.forEach {
                packetToggleState[it] = true
            }

            return
        }

        packetToggleState.keys.forEach {

            if (it == C00PacketKeepAlive::class.java)
                packetToggleState[it] = keepAliveState

            else if (it == C01PacketChatMessage::class.java || it == C11PacketEnchantItem::class.java ||
                it == C12PacketUpdateSign::class.java || it == C14PacketTabComplete::class.java ||
                it == C15PacketClientSettings::class.java || it == C17PacketCustomPayload::class.java ||
                it == C18PacketSpectate::class.java || it == C19PacketResourcePackStatus::class.java)
                packetToggleState[it] = otherPacket

            else if (it == C03PacketPlayer::class.java || it == C04PacketPlayerPosition::class.java ||
                it == C05PacketPlayerLook::class.java || it == C06PacketPlayerPosLook::class.java)
                packetToggleState[it] = movingPacketState

            else if (it == C0FPacketConfirmTransaction::class.java)
                packetToggleState[it] = transactionState

            else if (it == C02PacketUseEntity::class.java || it == C09PacketHeldItemChange::class.java ||
                it == C0APacketAnimation::class.java || it == C0BPacketEntityAction::class.java)
                packetToggleState[it] = actionState

            else if (it == C0CPacketInput::class.java || it == C13PacketPlayerAbilities::class.java)
                packetToggleState[it] == abilitiesState

            else if (it == C0DPacketCloseWindow::class.java || it == C0EPacketClickWindow::class.java ||
                it == C10PacketCreativeInventoryAction::class.java || it == C16PacketClientStatus::class.java)
                packetToggleState[it] = invState

            else if (it == C07PacketPlayerDigging::class.java || it == C08PacketPlayerBlockPlacement::class.java)
                packetToggleState[it] = interactState
        }
    }

    val totalPackets: Int
        get() = packets.size

    fun getTotalPackets(packetType: Class<Packet<INetHandlerPlayServer>>): Int {
        val packetCount = packets.count {it::class.java == packetType}
        return if (packetCount > 0) packetCount else -302
    }
}
