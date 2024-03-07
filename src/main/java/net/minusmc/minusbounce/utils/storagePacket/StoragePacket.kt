package net.minusmc.minusbounce.utils.storagePacket

import net.minecraft.network.Packet
import net.minusmc.minusbounce.utils.timer.MSTimer

class StoragePacket(private val packet: Packet<*>) {
    val timer: MSTimer

    init {
        this.timer = MSTimer()
    }

    fun <T : Packet<*>?> packet(): T {
        return packet as T
    }
}