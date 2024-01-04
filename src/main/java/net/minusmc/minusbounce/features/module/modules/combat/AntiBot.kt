/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.combat

import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemArmor
import net.minecraft.network.play.server.*
import net.minecraft.world.WorldSettings
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.features.module.modules.combat.KillAura
import net.minusmc.minusbounce.utils.ClientUtils
import net.minusmc.minusbounce.utils.EntityUtils
import net.minusmc.minusbounce.utils.render.ColorUtils
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.value.IntegerValue
import net.minusmc.minusbounce.value.ListValue
import java.util.*

@ModuleInfo(name = "AntiBot", spacedName = "Anti Bot", description = "revents KillAura from attacking AntiCheat bots.", category = ModuleCategory.COMBAT)
object AntiBot : Module() {
    private val czechHekValue = BoolValue("CzechMatrix", false)
    private val czechHekPingCheckValue = BoolValue("PingCheck", true) { czechHekValue.get() }
    private val czechHekGMCheckValue = BoolValue("GamemodeCheck", true) { czechHekValue.get() }
    private val sameArmorOnBedwars = BoolValue("SameArmorOnBedwars", false)
    private val tabValue = BoolValue("Tab", true)
    private val tabModeValue = ListValue("TabMode", arrayOf("Equals", "Contains"), "Contains")
    private val entityIDValue = BoolValue("EntityID", true)
    private val colorValue = BoolValue("Color", false)
    private val livingTimeValue = BoolValue("LivingTime", false)
    private val livingTimeTicksValue = IntegerValue("LivingTimeTicks", 40, 1, 200)
    private val groundValue = BoolValue("Ground", true)
    private val airValue = BoolValue("Air", false)
    private val invalidGroundValue = BoolValue("InvalidGround", true)
    private val swingValue = BoolValue("Swing", false)
    private val healthValue = BoolValue("Health", false)
    private val minHealthValue = FloatValue("MinHealth", 0F, 0F, 100F)
    private val maxHealthValue = FloatValue("MaxHealth", 20F, 0F, 100F)
    private val derpValue = BoolValue("Derp", true)
    private val wasInvisibleValue = BoolValue("WasInvisible", false)
    private val armorValue = BoolValue("Armor", false)
    private val pingValue = BoolValue("Ping", false)
    private val needHitValue = BoolValue("NeedHit", false)
    private val spawnInCombatValue = BoolValue("SpawnInCombat", false)
    private val duplicateInWorldValue = BoolValue("DuplicateInWorld", false)
    private val duplicateInTabValue = BoolValue("DuplicateInTab", false)
    private val duplicateCompareModeValue = ListValue("DuplicateCompareMode", arrayOf("OnTime", "WhenSpawn"), "OnTime") {
        duplicateInTabValue.get() || duplicateInWorldValue.get()
    }
    private val experimentalNPCDetection = BoolValue("ExperimentalNPCDetection", false)
    private val illegalName = BoolValue("IllegalName", false)
    private val removeFromWorld = BoolValue("RemoveFromWorld", false)
    private val removeIntervalValue = IntegerValue("Remove-Interval", 20, 1, 100, " tick")
    private val debugValue = BoolValue("Debug", false)

    private val ground = mutableListOf<Int>()
    private val air = mutableListOf<Int>()
    private val invalidGround = mutableMapOf<Int, Int>()
    private val swing = mutableListOf<Int>()
    private val invisible = mutableListOf<Int>()
    private val hasRemovedEntities = mutableListOf<Int>()
    private val spawnInCombat = mutableListOf<Int>()
    private val hitted = mutableListOf<Int>()
    private val duplicate = mutableListOf<UUID>()
    private var wasAdded = (mc.thePlayer != null)
    override fun onDisable() {
        clearAll()
    }
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        mc.thePlayer ?: return
        mc.theWorld ?: return
        if (removeFromWorld.get() && mc.thePlayer.ticksExisted > 0 && mc.thePlayer.ticksExisted % removeIntervalValue.get() == 0){
            val ent = mutableListOf<EntityPlayer>()
            for (entity in mc.theWorld.playerEntities) {
                if (entity != mc.thePlayer && isBot(entity))
                    ent.add(entity)
            }
            if (ent.isEmpty()) return
            for (e in ent) {
                mc.theWorld.removeEntity(e)
                if (debugValue.get()) ClientUtils.displayChatMessage("§7[§a§lAnti Bot§7] §fRemoved §r${e.name} §fdue to it being a bot.")
            }
        }
    }
    @EventTarget
    fun onPacket(event: PacketEvent) {
        mc.thePlayer ?: return
        mc.theWorld ?: return
        val packet = event.packet
        if (czechHekValue.get()) {
            if (packet is S41PacketServerDifficulty) wasAdded = false
            if (packet is S38PacketPlayerListItem) {
                val dataent = packet.entries[0]
                if (dataent.profile != null && dataent.profile.name != null) {
                    if (!wasAdded)
                        wasAdded = dataent.profile.name.equals(mc.thePlayer.name)
                    else if (!mc.thePlayer.isSpectator && !mc.thePlayer.capabilities.allowFlying && (!czechHekPingCheckValue.get() || dataent.ping != 0) && (!czechHekGMCheckValue.get() || dataent.gameMode != WorldSettings.GameType.NOT_SET)) {
                        event.cancelEvent()
                        if (debugValue.get()) ClientUtils.displayChatMessage("§7[§a§lAnti Bot/§6Matrix§7] §fPrevented §r${dataent.profile.name} §ffrom spawning.")
                    }
                }
            }
        }
        if(packet is S14PacketEntity) {
            val entity = packet.getEntity(mc.theWorld)
            if(entity is EntityPlayer) {
                if(packet.onGround && !ground.contains(entity.entityId))
                    ground.add(entity.entityId)
                if(!packet.onGround && !air.contains(entity.entityId))
                    air.add(entity.entityId)
                if(packet.onGround) {
                    if(entity.prevPosY != entity.posY)
                        invalidGround[entity.entityId] = invalidGround.getOrDefault(entity.entityId, 0) + 1
                }else{
                    val currentVL = invalidGround.getOrDefault(entity.entityId, 0) / 2
                    if (currentVL <= 0)
                        invalidGround.remove(entity.entityId)
                    else
                        invalidGround[entity.entityId] = currentVL
                }
                if(entity.isInvisible() && !invisible.contains(entity.entityId))
                    invisible.add(entity.entityId)
            }
        }
        if(packet is S0BPacketAnimation) {
            val entity = mc.theWorld.getEntityByID(packet.entityID)
            if (entity is EntityLivingBase && packet.animationType == 0 && !swing.contains(entity.entityId))
                swing.add(entity.entityId)
        }
        if (packet is S38PacketPlayerListItem) {
            if (duplicateCompareModeValue.equals("WhenSpawn") && packet.action == S38PacketPlayerListItem.Action.ADD_PLAYER) {
                packet.entries.forEach { entry ->
                    val name = entry.profile.name
                    if (duplicateInWorldValue.get() && mc.theWorld.playerEntities.any { it.name == name } ||
                        duplicateInTabValue.get() && mc.netHandler.playerInfoMap.any { it.gameProfile.name == name }) {
                        duplicate.add(entry.profile.id)
                    }
                }
            }
        } else if (packet is S0CPacketSpawnPlayer) {
            val killAura = MinusBounce.moduleManager[KillAura::class.java]!!
            if (killAura.target != null && !hasRemovedEntities.contains(packet.entityID)) {
                spawnInCombat.add(packet.entityID)
            }
        } else if (packet is S13PacketDestroyEntities) {
            hasRemovedEntities.addAll(packet.entityIDs.toTypedArray())
        }
    }
    @EventTarget
    fun onAttack(event: AttackEvent) {
        val entity = event.targetEntity
        if(entity is EntityLivingBase && !hitted.contains(entity.entityId))
            hitted.add(entity.entityId)
    }
    @EventTarget
    fun onWorld(event: WorldEvent) {
        clearAll()
    }

    private fun clearAll() {
        hitted.clear()
        swing.clear()
        ground.clear()
        invalidGround.clear()
        invisible.clear()
    }
    @JvmStatic
    fun isBot(entity: EntityLivingBase): Boolean {
        if (entity !is EntityPlayer || entity == mc.thePlayer)
            return false

        if (!state) return false

        if (experimentalNPCDetection.get() && (entity.getDisplayName().unformattedText.lowercase().contains("npc") || entity.getDisplayName().unformattedText.lowercase().contains("cit-")))
            return true

        if (illegalName.get() && (entity.getName().contains(" ") || entity.getDisplayName().unformattedText.contains(" ")))
            return true

        if (colorValue.get() && !entity.getDisplayName().formattedText.replace("§r", "").contains("§"))
            return true

        if (livingTimeValue.get() && entity.ticksExisted < livingTimeTicksValue.get())
            return true

        if (groundValue.get() && !ground.contains(entity.entityId))
            return true

        if (airValue.get() && !air.contains(entity.entityId))
            return true

        if (spawnInCombatValue.get() && spawnInCombat.contains(entity.entityId))
            return true

        if(swingValue.get() && !swing.contains(entity.entityId))
            return true

        if(healthValue.get() && (entity.getHealth() > maxHealthValue.get() || entity.getHealth() < minHealthValue.get()))
            return true

        if(entityIDValue.get() && (entity.entityId >= 1000000000 || entity.entityId <= -1))
            return true

        if(derpValue.get() && (entity.rotationPitch > 90F || entity.rotationPitch < -90F))
            return true

        if(wasInvisibleValue.get() && invisible.contains(entity.entityId))
            return true

        if(armorValue.get()) {
            if (entity.inventory.armorInventory[0] == null && entity.inventory.armorInventory[1] == null && entity.inventory.armorInventory[2] == null && entity.inventory.armorInventory[3] == null)
                return true
        }

        if(pingValue.get()) {
            if (mc.netHandler.getPlayerInfo(entity.uniqueID) != null && mc.netHandler.getPlayerInfo(entity.uniqueID).responseTime == 0)
                return true
        }

        if(needHitValue.get() && !hitted.contains(entity.entityId))
            return true

        if(invalidGroundValue.get() && invalidGround.getOrDefault(entity.entityId, 0) >= 10)
            return true

        if(tabValue.get()) {
            val targetName = ColorUtils.stripColor(entity.getDisplayName().formattedText)
            if (targetName != null) {
                for (networkPlayerInfo in mc.netHandler.playerInfoMap) {
                    val networkName = ColorUtils.stripColor(EntityUtils.getName(networkPlayerInfo)) ?: continue
                    if ((tabModeValue.get().equals("Equals", true) && targetName.equals(networkName, true)) || (targetName.contains(networkName)))
                        return false
                }
                return true
            }
        }
        if (duplicateCompareModeValue.equals("WhenSpawn") && duplicate.contains(entity.gameProfile.id)) {
            return true
        }
        if (duplicateInWorldValue.get() && duplicateCompareModeValue.equals("OnTime") && mc.theWorld.loadedEntityList.count { it is EntityPlayer && it.name == it.name } > 1) {
            return true
        }
        if (duplicateInTabValue.get() && duplicateCompareModeValue.equals("OnTime") && mc.netHandler.playerInfoMap.count { entity.name == it.gameProfile.name } > 1) {
            return true
        }

        /* 
        Check armor material in bedwars
        Author: pie, toidicakhia
        */

        if (sameArmorOnBedwars.get()) {
            val helmet = entity.inventory.armorInventory[3]
            val chestplate = entity.inventory.armorInventory[2]
            val leggings = entity.inventory.armorInventory[1]
            val boots = entity.inventory.armorInventory[0]

            if (helmet == null || chestplate == null || helmet.item == null || chestplate.item == null)
                return true

            val helmetMaterial = (helmet.item as ItemArmor).armorMaterial
            val chestplateMaterial = (chestplate.item as ItemArmor).armorMaterial
            val leggingsMaterial = (leggings.item as ItemArmor).armorMaterial
            val bootsMaterial = (boots.item as ItemArmor).armorMaterial
            if (!(helmetMaterial == ItemArmor.ArmorMaterial.LEATHER && chestplateMaterial == ItemArmor.ArmorMaterial.LEATHER && leggingsMaterial == bootsMaterial))
                return true
        }

        return entity.getName().isEmpty() || entity.getName().equals(mc.thePlayer.name)
    }
}