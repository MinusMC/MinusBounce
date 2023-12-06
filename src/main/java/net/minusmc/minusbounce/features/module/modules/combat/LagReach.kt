package net.minusmc.minusbounce.features.module.modules.combat

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.features.module.*
import net.minusmc.minusbounce.utils.*
import net.minusmc.minusbounce.utils.timer.MSTimer
import net.minusmc.minusbounce.value.*
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.Packet
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.*
import net.minecraft.network.play.INetHandlerPlayClient
import net.minecraft.world.WorldSettings
import net.minusmc.minusbounce.utils.extensions.getDistanceToEntityBox
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import java.util.concurrent.LinkedBlockingQueue

@ModuleInfo(name = "LagReach", description = "Very Lag reach", category = ModuleCategory.COMBAT)
object LagReach: Module() {
    private val modeValue = ListValue("Mode", arrayOf("MinusBounce", "FakePlayer", "Intave", "AllIncomingPackets", "TargetPackets"), "FakePlayer")
    private val typeValue = ListValue("Type", arrayOf("Normal", "Boost"), "Normal") { modeValue.get().equals("MinusBounce", true) }
    private val pulseDelayValue = IntegerValue("PulseDelay", 200, 50, 500)
    private val onlyAuraValue = BoolValue("OnlyAura", false)
    private val intavetesthurttime = IntegerValue("Packets", 5, 0, 30) { modeValue.get().equals("Intave", true) }
    val ReachMax: FloatValue = object : FloatValue("Max", 3.2f, 3f, 7f, {modeValue.get().equals("MinusBounce", true)}) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = ReachMin.get()
            if (v > newValue) set(v)
        }
    }
    val ReachMin: FloatValue = object : FloatValue("Min", 3.0f, 3f, 7f, {modeValue.get().equals("MinusBounce", true)}) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = ReachMax.get()
            if (v < newValue) set(v)
        }
    }
    private val ticksValue = IntegerValue("Ticks", 3, 1, 10) { typeValue.get().equals("Normal", true) }
    private var ticks = 0

    private val ticksAmount = IntegerValue("BoostTicks", 10, 3, 20) { typeValue.get().equals("Boost", true) }
    private val boostAmount = FloatValue("BoostTimer", 10f, 1f, 50f) { typeValue.get().equals("Boost", true) }
    private val chargeAmount = FloatValue("ChargeTimer", 0.11f, 0.05f, 1f) { typeValue.get().equals("Boost", true) }

    private var counter = -1
    var freezing = false
    var targetTickBase: EntityLivingBase? = null
    var fakePlayer: EntityOtherPlayerMP? = null
    private val pulseTimer = MSTimer()
    var currentTarget: EntityLivingBase? = null
    private var shown = false

    private val packets = LinkedBlockingQueue<Packet<INetHandlerPlayClient>>()
    private lateinit var killAura: KillAura

    override fun onInitialize() {
        killAura = MinusBounce.moduleManager[KillAura::class.java] as KillAura
    }

    override fun onEnable() {
        if (modeValue.get().equals("AllIncomingPackets", true))
            BlinkUtils.setBlinkState(all = true)
        if (modeValue.get().equals("MinusBounce", true))
            counter = -1
        freezing = false
        mc.timer.timerSpeed = 1f
    }

    override fun onDisable() {
        removeFakePlayer()
        clearPackets()
        if (modeValue.get().equals("AllIncomingPackets", true))
            BlinkUtils.setBlinkState(off = true, release = true)
        if (modeValue.get().equals("MinusBounce", true))
            mc.timer.timerSpeed = 1f
    }

    private fun removeFakePlayer() {
        fakePlayer ?: return
        currentTarget = null
        mc.theWorld.removeEntity(fakePlayer)
        fakePlayer = null
    }

    private fun clearPackets() {
        while (!packets.isEmpty())
            PacketUtils.handlePacket(packets.take() as Packet<*>)
        BlinkUtils.releasePacket()
    }


    private fun attackEntity(entity: EntityLivingBase) {
        mc.thePlayer ?: return
        mc.thePlayer.swingItem()
        mc.netHandler.addToSendQueue(C02PacketUseEntity(entity, C02PacketUseEntity.Action.ATTACK))
        if (mc.playerController.currentGameType != WorldSettings.GameType.SPECTATOR)
            mc.thePlayer.attackTargetEntityWithCurrentItem(entity)
    }

    @EventTarget
    fun onAttack(event: AttackEvent) {
        mc.theWorld ?: return
        if (modeValue.get().equals("FakePlayer", true) || modeValue.get().equals("Intave", true)) {
            clearPackets()
            if (fakePlayer == null) {
                currentTarget = event.targetEntity as EntityLivingBase?
                currentTarget ?: return
                val faker = EntityOtherPlayerMP(mc.theWorld, mc.netHandler.getPlayerInfo(currentTarget!!.uniqueID).gameProfile)

                faker.rotationYawHead = currentTarget!!.rotationYawHead
                faker.renderYawOffset = currentTarget!!.renderYawOffset
                faker.copyLocationAndAnglesFrom(currentTarget)
                faker.rotationYawHead = currentTarget!!.rotationYawHead
                faker.health = currentTarget!!.health
                val indices = (0..4).toList().toIntArray()
                for (index in indices) {
                    val equipmentInSlot = currentTarget!!.getEquipmentInSlot(index) ?: continue
                    faker.setCurrentItemOrArmor(index, equipmentInSlot)
                }
                mc.theWorld.addEntityToWorld(-1337, faker)

                fakePlayer = faker
                shown = true
            } else {
                if (event.targetEntity == fakePlayer) {
                    attackEntity(currentTarget!!)
                    event.cancelEvent()
                } else {
                    fakePlayer = null
                    currentTarget = event.targetEntity as EntityLivingBase?
                    shown = false
                }
            }
        } else {
            if (event.targetEntity != currentTarget!!) {
                clearPackets()
                currentTarget = event.targetEntity as EntityLivingBase?
            }
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (!MinusBounce.combatManager.inCombat) {
            removeFakePlayer()
        }
        if (!typeValue.get().equals("Boost", true)) return
        if (ticks == ticksAmount.get()) {
            mc.timer.timerSpeed = chargeAmount.get()
            ticks --
        } else if (ticks > 1) {
            mc.timer.timerSpeed = boostAmount.get()
            ticks --
        } else if (ticks == 1) {
            mc.timer.timerSpeed = 1f
            ticks --
        }
        if (modeValue.get().equals("FakePlayer", true) || modeValue.get().equals("Intave", true) ) {
            if (onlyAuraValue.get() && !MinusBounce.moduleManager[KillAura::class.java]!!.state) {
                removeFakePlayer()
            }
            mc.theWorld ?: return
            mc.thePlayer ?: return
            fakePlayer ?: return
            currentTarget ?: return
            if (EntityUtils.isRendered(fakePlayer!!) && (currentTarget!!.isDead || !EntityUtils.isRendered(currentTarget!!))) {
                removeFakePlayer()
            }

            if (currentTarget != null && fakePlayer != null) {
                fakePlayer!!.health = currentTarget!!.health
                val indices = (0..4).toList().toIntArray()
                for (index in indices) {
                    val equipmentInSlot = currentTarget!!.getEquipmentInSlot(index) ?: continue
                    fakePlayer!!.setCurrentItemOrArmor(index, equipmentInSlot)
                }
            }
            if (modeValue.get().equals("Intave", true) && mc.thePlayer.ticksExisted % intavetesthurttime.get() == 0) {
                if (fakePlayer != null) {
                    fakePlayer!!.rotationYawHead = currentTarget!!.rotationYawHead
                    fakePlayer!!.renderYawOffset = currentTarget!!.renderYawOffset
                    fakePlayer!!.copyLocationAndAnglesFrom(currentTarget!!)
                    fakePlayer!!.rotationYawHead = currentTarget!!.rotationYawHead
                }
                pulseTimer.reset()
            } else if (modeValue.get().equals("FakePlayer", true) && pulseTimer.hasTimePassed(pulseDelayValue.get().toLong())) {
                if (fakePlayer != null) {
                    fakePlayer!!.rotationYawHead = currentTarget!!.rotationYawHead
                    fakePlayer!!.renderYawOffset = currentTarget!!.renderYawOffset
                    fakePlayer!!.copyLocationAndAnglesFrom(currentTarget!!)
                    fakePlayer!!.rotationYawHead = currentTarget!!.rotationYawHead
                }
                pulseTimer.reset()
            }

            if (!shown && currentTarget != null && currentTarget!!.uniqueID != null && mc.netHandler.getPlayerInfo(currentTarget!!.uniqueID) != null && mc.netHandler.getPlayerInfo(currentTarget!!.uniqueID).gameProfile != null) {
                val faker = EntityOtherPlayerMP(mc.theWorld, mc.netHandler.getPlayerInfo(currentTarget!!.uniqueID).gameProfile)

                faker.rotationYawHead = currentTarget!!.rotationYawHead
                faker.renderYawOffset = currentTarget!!.renderYawOffset
                faker.copyLocationAndAnglesFrom(currentTarget!!)
                faker.rotationYawHead = currentTarget!!.rotationYawHead
                faker.health = currentTarget!!.health
                val indices = (0..4).toList().toIntArray()
                for (index in indices) {
                    val equipmentInSlot = currentTarget!!.getEquipmentInSlot(index) ?: continue
                    faker.setCurrentItemOrArmor(index, equipmentInSlot)
                }
                mc.theWorld.addEntityToWorld(-1337, faker)

                fakePlayer = faker
                shown = true
            }
        } else {
            if (pulseTimer.hasTimePassed(pulseDelayValue.get().toLong())) {
                pulseTimer.reset()
                clearPackets()
            }
        }
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (event.eventState == EventState.POST && freezing && typeValue.get().equals("Normal", true)) {
            mc.thePlayer.posX = mc.thePlayer.lastTickPosX
            mc.thePlayer.posY = mc.thePlayer.lastTickPosY
            mc.thePlayer.posZ = mc.thePlayer.lastTickPosZ
        }
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (freezing && typeValue.get().equals("Normal", true)) mc.timer.renderPartialTicks = 0F
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (onlyAuraValue.get() && !MinusBounce.moduleManager[KillAura::class.java]!!.state) return

        if (modeValue.equals("TargetPackets")) {
            if (packet is S14PacketEntity && MinusBounce.combatManager.inCombat) {
                if (packet.getEntity(mc.theWorld) == currentTarget) {
                    event.cancelEvent()
                    packets.add(packet as Packet<INetHandlerPlayClient>)
                }
            }
        } else if (modeValue.equals("AllIncomingPackets")) {
            if (packet.javaClass.simpleName.startsWith("S", ignoreCase = true) && MinusBounce.combatManager.inCombat) {
                if (mc.thePlayer.ticksExisted < 20) return
                event.cancelEvent()
                packets.add(packet as Packet<INetHandlerPlayClient>)
            }
        }
    }
    fun getExtraTicks(): Int {
        if (counter-- > 0) return -1
        freezing = false

        if (killAura.state && (killAura.target == null || mc.thePlayer.getDistanceToEntityBox(killAura.target!!) > killAura.rangeValue.get())) {
            if (targetTickBase != null && mc.thePlayer.hurtTime <= 2) {
                counter = ticksValue.get()
                return counter
            }
        }

        return 0
    }
    fun getReach(): Double {
        val min: Double = Math.min(ReachMin.get(), ReachMax.get()).toDouble()
        val max: Double = Math.max(ReachMin.get(), ReachMax.get()).toDouble()
        return Math.random() * (max - min) + min
    }
    override val tag: String?
        get() = "${ DecimalFormat("0.##", DecimalFormatSymbols(Locale.ENGLISH)).format(ReachMax.get())} - ${ DecimalFormat("0.##", DecimalFormatSymbols(Locale.ENGLISH)).format(ReachMin.get())}"
}