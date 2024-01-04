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
    fun isSelected(entity: Entity, canAttackCheck: Boolean): Boolean {
        val targetsModule = MinusBounce.moduleManager[Target::class.java]!!
        val teams = MinusBounce.moduleManager[Teams::class.java]!!
        if (entity is EntityLivingBase) {
            // From augustus
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

            if (entity.deathTime > 1) return false
            
            if (!canAttackCheck)
                return false

            if (entity.ticksExisted < 1)
                return false

            if (isFriend(entity))
                return false

            if (!(!teams.state || !teams.isInYourTeam(entity)))
                return false

            if (isBot(entity))
                return false
        }

        return entity is EntityLivingBase && entity != mc.thePlayer
    }

    fun isEnemy(entity: Entity?): Boolean {
        val targetsModule = MinusBounce.moduleManager[Target::class.java]!!
        val teams = MinusBounce.moduleManager[Teams::class.java] as Teams
        if (entity is EntityLivingBase) {
            // From augustus
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

            if (entity.deathTime > 1) return false

            if (!targetsModule.players.get() && entity is EntityPlayer)
                return false

            if (entity.ticksExisted < 1)
                return false

            if (!(!teams.state || !teams.isInYourTeam(entity)))
                return false

            if (isBot(entity))
                return false
        }

        return entity is EntityLivingBase && entity != mc.thePlayer && mc.thePlayer.getDistanceToEntity(entity) < MinusBounce.moduleManager[KillAura::class.java]!!.rangeValue.get()
    }

    fun closestPerson(): EntityLivingBase? {
        val targets = mc.theWorld.loadedEntityList.filter {
            it is EntityLivingBase && it != mc.thePlayer && isSelected(it, true) &&
                    mc.thePlayer.canEntityBeSeen(it) && isEnemy(it)
        }
        val entity = targets.minByOrNull { mc.thePlayer.getDistanceToEntity(it) }
        return entity as EntityLivingBase?
    }

    fun isAnimal(entity: Entity?): Boolean {
        return entity is EntityAnimal || entity is EntitySquid || entity is EntityGolem ||
                entity is EntityBat
    }

    fun isMob(entity: Entity?): Boolean {
        return entity is EntityMob || entity is EntityVillager || entity is EntitySlime ||
                entity is EntityGhast || entity is EntityDragon
    }

    fun isAlive(entity: EntityLivingBase) = entity.isEntityAlive && entity.health > 0 || MinusBounce.moduleManager[KillAura::class.java]!!.aacValue.get() && entity.hurtTime > 5

    fun getName(networkPlayerInfoIn: NetworkPlayerInfo): String {
        return if (networkPlayerInfoIn.displayName != null) networkPlayerInfoIn.displayName.formattedText else ScorePlayerTeam.formatPlayerName(
            networkPlayerInfoIn.playerTeam,
            networkPlayerInfoIn.gameProfile.name
        )
    }

    fun getPing(entityPlayer: EntityPlayer?): Int {
        if (entityPlayer == null) return 0
        val networkPlayerInfo = mc.netHandler.getPlayerInfo(entityPlayer.uniqueID)
        return networkPlayerInfo?.responseTime ?: 0
    }

    fun isRendered(entityToCheck: Entity?): Boolean {
        return mc.theWorld != null && mc.theWorld.getLoadedEntityList().contains(entityToCheck)
    }

    fun isFriend(entity: EntityLivingBase?): Boolean {
        if (entity !is EntityPlayer) return false
        val name = ColorUtils.stripColor(entity.name ?: return false) ?: return false
        return MinusBounce.fileManager.friendsConfig.isFriend(name)
    }
}