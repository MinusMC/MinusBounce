/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.player

import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.enchantment.Enchantment
import net.minecraft.init.Blocks
import net.minecraft.item.*
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.PreMotionEvent
import net.minusmc.minusbounce.event.PostMotionEvent
import net.minusmc.minusbounce.event.UpdateEvent
import net.minusmc.minusbounce.event.WorldEvent
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.features.module.modules.world.Scaffold
import net.minusmc.minusbounce.injection.implementations.IItemStack
import net.minusmc.minusbounce.utils.ClientUtils
import net.minusmc.minusbounce.utils.InventoryUtils
import net.minusmc.minusbounce.utils.player.MovementUtils
import net.minusmc.minusbounce.utils.item.ArmorPart
import net.minusmc.minusbounce.utils.item.ItemHelper
import net.minusmc.minusbounce.utils.misc.RandomUtils
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.value.IntegerValue
import net.minusmc.minusbounce.value.ListValue


@ModuleInfo(name = "InvManager", spacedName = "InvManager", description = "Automatically throws away useless items, and also equips armors for you.", category = ModuleCategory.PLAYER)
class InvManager : Module() {
    private val delayValue = IntRangeValue("Delay", 50, 50, 0, 1000, "ms")
    private val modeValue = ListValue("Mode", arrayOf("OpenInventory", "Spoof"), "OpenInventory")

    private val itemDelayValue = IntegerValue("ItemDelay", 0, 0, 5000, "ms")

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        mc.thePlayer ?: return

        if (!canOperateInv())
            return

        // check armor

    }

    private fun runFindBestArmor() {
        val groupArmorParts = (0..36).map { mc.thePlayer.inventory.getStackInSlot(it) }.filter {
            itemStack != null && itemStack.item is ItemArmor && (i < 9 || System.currentTimeMillis() - (itemStack as IItemStack).itemDelay >= itemDelayValue.get())
        }
    }

    private fun canOperateInv(): Boolean {
        if (!mc.playerController.currentGameType.isSurvivalOrAdventure())
            return false

        if (mc.thePlayer.openContainer.windowId != 0)
            return false

        if (modeValue.get().equals("openinventory", true) && mc.currentScreen !is GuiInventory)
            return false

        return true
    }
}
