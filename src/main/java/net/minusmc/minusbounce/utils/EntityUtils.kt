/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.utils

import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.boss.EntityDragon
import net.minecraft.entity.monster.EntityGhast
import net.minecraft.entity.monster.EntityGolem
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.monster.EntitySlime
import net.minecraft.entity.passive.EntityAnimal
import net.minecraft.entity.passive.EntityBat
import net.minecraft.entity.passive.EntitySquid
import net.minecraft.entity.passive.EntityVillager
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.scoreboard.ScorePlayerTeam
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.features.module.modules.client.Target
import net.minusmc.minusbounce.features.module.modules.combat.AntiBot.isBot
import net.minusmc.minusbounce.features.module.modules.combat.KillAura
import net.minusmc.minusbounce.features.module.modules.misc.Teams
import net.minusmc.minusbounce.utils.render.ColorUtils

object EntityUtils : MinecraftInstance() {

    private val teamsModule: Teams
        get() = MinusBounce.moduleManager[Teams::class.java]!!

    private val targetsModule: Target
        get() = MinusBounce.moduleManager[Target::class.java]!!

    fun isSelected(entity: Entity, canAttackCheck: Boolean): Boolean {
        if (entity is EntityLivingBase) {
            if (entity is EntityArmorStand)
                return false
            
            if (!targetsModule.dead.get() && entity.isDead)
                return false

            if (!targetsModule.invisible.get() && entity.isInvisible())
                return false

            if (!targetsModule.mobs.get() && isMob(entity))
                return false

            if (!targetsModule.animals.get() && isAnimal(entity))
                return false

            if (!targetsModule.villager.get() && entity is EntityVillager)
                return false

            if (!targetsModule.players.get() && entity is EntityPlayer)
                return false

            if (isFriend(entity))
                return false

            if (!canAttackCheck)
                return false

            if (entity.deathTime > 1)
                return false

            if (entity.ticksExisted < 1)
                return false

            if (entity is EntityPlayer) {
                if (!(!teamsModule.state || !teamsModule.isInYourTeam(entity)))
                    return false

                if (isBot(entity))
                    return false
            }
        }

        return entity is EntityLivingBase && entity != mc.thePlayer
    }

    fun isAnimal(entity: Entity?) = entity is EntityAnimal || entity is EntitySquid ||
        entity is EntityGolem || entity is EntityBat

    fun isMob(entity: Entity?) = entity is EntityMob || entity is EntitySlime || 
        entity is EntityGhast || entity is EntityDragon

    fun isAlive(entity: EntityLivingBase) = entity.isEntityAlive && entity.health > 0

    fun getName(networkPlayerInfoIn: NetworkPlayerInfo) = networkPlayerInfoIn.displayName?.formattedText ?: ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.playerTeam, networkPlayerInfoIn.gameProfile.name)

    fun getPing(entityPlayer: EntityPlayer?): Int {
        entityPlayer ?: return 0

        val networkPlayerInfo = mc.netHandler.getPlayerInfo(entityPlayer.uniqueID)
        return networkPlayerInfo?.responseTime ?: 0
    }

    fun isFriend(entity: EntityLivingBase?): Boolean {
        if (entity !is EntityPlayer)
            return false

        val name = ColorUtils.stripColor(entity.name ?: return false) ?: return false
        return MinusBounce.fileManager.friendsConfig.isFriend(name)
    }

    fun isRendered(entity: Entity?) = mc.theWorld != null && mc.theWorld.loadedEntityList.contains(entity)
}
