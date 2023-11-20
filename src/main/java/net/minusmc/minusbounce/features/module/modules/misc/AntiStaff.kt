package net.minusmc.minusbounce.features.module.modules.misc

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.event.UpdateEvent
import net.minusmc.minusbounce.event.WorldEvent
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.ui.client.hud.element.elements.Notification
import net.minusmc.minusbounce.utils.ClientUtils
import net.minusmc.minusbounce.utils.EntityUtils
import net.minusmc.minusbounce.utils.ServerUtils
import net.minusmc.minusbounce.utils.misc.HttpUtils
import net.minusmc.minusbounce.utils.render.ColorUtils
import net.minusmc.minusbounce.value.BoolValue
import net.minecraft.entity.Entity
import net.minecraft.network.play.server.*
import kotlin.concurrent.thread

@ModuleInfo(name = "AntiStaff", spacedName = "Anti Staff", description = "Anti staff on BlocksMC. Automatically leaves a map if detected known staffs.", category = ModuleCategory.MISC)

class AntiStaff : Module() {
    private var staffs = mutableListOf<String>()
    private var staffsInWorld = mutableListOf<String>()

    private var bmcstaffList: String = "${MinusBounce.CLIENT_CLOUD}/staffs.txt"

    private val notify = BoolValue("Notify", true)
    private val chat = BoolValue("Chat", true)
    private val leave = BoolValue("Leave", true)

    private val onBMC: Boolean
        get() = ServerUtils.serverData!!.serverIP.contains("blocksmc.com")

    override fun onInitialize() {
        thread {
            staffs.addAll(HttpUtils.get(bmcstaffList).split(","))

            ClientUtils.logger.info("[Staff/main] $staffs")
        }
    }

    override fun onEnable() {
        staffsInWorld.clear()
    }

    @EventTarget
    fun onWorld(e: WorldEvent) {
        staffsInWorld.clear()
    }

    private fun warn(name: String) {
        if (name in staffsInWorld)
            return

        val msg = if (leave.get()) ", leaving" else ""
        if (chat.get())
            chat("[AntiStaff] Detected staff: $name$msg")
        if (notify.get())
            MinusBounce.hud.addNotification(Notification("Detected staff: $name$msg", Notification.Type.ERROR, 4000L))
        if (leave.get())
            mc.thePlayer.sendChatMessage("/leave")

        staffsInWorld.add(name)
        // sao ko có syntax highlight
    }

    private fun isStaff(entity: Entity): Boolean {
        if (onBMC) {
            return entity.name in staffs || entity.displayName.unformattedText in staffs
        }

        return false
    }

    // ai cho tu bo de v
    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (mc.theWorld == null || mc.thePlayer == null) return
        if (onBMC)
            when (val packet = event.packet) {
                is S0CPacketSpawnPlayer -> {
                    val entity = mc.theWorld.getEntityByID(packet.entityID) ?: return
                    if (isStaff(entity))
                        warn(entity.name)
                }
                is S1EPacketRemoveEntityEffect -> {
                    val entity = mc.theWorld.getEntityByID(packet.entityId) ?: return
                    if (isStaff(entity))
                        warn(entity.name)
                }
                is S01PacketJoinGame -> {
                    val entity = mc.theWorld.getEntityByID(packet.entityId) ?: return
                    if (isStaff(entity))
                        warn(entity.name)
                }
                is S04PacketEntityEquipment -> {
                    val entity = mc.theWorld.getEntityByID(packet.entityID) ?: return
                    if (isStaff(entity))
                        warn(entity.name)
                }
                is S1CPacketEntityMetadata -> {
                    val entity = mc.theWorld.getEntityByID(packet.entityId) ?: return
                    if (isStaff(entity))
                        warn(entity.name)
                }
                is S1DPacketEntityEffect -> {
                    val entity = mc.theWorld.getEntityByID(packet.entityId) ?: return
                    if (isStaff(entity))
                        warn(entity.name)
                }
                is S18PacketEntityTeleport -> {
                    val entity = mc.theWorld.getEntityByID(packet.entityId) ?: return
                    if (isStaff(entity))
                        warn(entity.name)
                }
                is S20PacketEntityProperties -> {
                    val entity = mc.theWorld.getEntityByID(packet.entityId) ?: return
                    if (isStaff(entity))
                        warn(entity.name)
                }
                is S0BPacketAnimation -> {
                    val entity = mc.theWorld.getEntityByID(packet.entityID) ?: return
                    if (isStaff(entity))
                        warn(entity.name)
                }
                is S14PacketEntity -> {
                    val entity = packet.getEntity(mc.theWorld) ?: return
                    if (isStaff(entity))
                        warn(entity.name)
                }
                is S19PacketEntityStatus -> {
                    val entity = packet.getEntity(mc.theWorld) ?: return
                    if (isStaff(entity))
                        warn(entity.name)
                }
                is S19PacketEntityHeadLook -> {
                    val entity = packet.getEntity(mc.theWorld) ?: return
                    if (isStaff(entity))
                        warn(entity.name)
                }
                is S49PacketUpdateEntityNBT -> {
                    val entity = packet.getEntity(mc.theWorld) ?: return
                    if (isStaff(entity))
                        warn(entity.name)
                }
            }

    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (mc.theWorld == null || mc.thePlayer == null || !onBMC) return

        mc.netHandler.playerInfoMap.forEach {
            val networkName = ColorUtils.stripColor(EntityUtils.getName(it))!!.split(" ")[0]
            if (networkName in staffs)
                warn(networkName)
        }

        mc.theWorld.loadedEntityList.forEach {
            if (it.name in staffs)
                warn(it.name)
        }
    }
}