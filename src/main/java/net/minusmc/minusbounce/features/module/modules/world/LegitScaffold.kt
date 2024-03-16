package net.minusmc.minusbounce.features.module.modules.world

import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.MoveEvent
import net.minusmc.minusbounce.event.UpdateEvent
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minecraft.client.settings.GameSettings
import net.minusmc.minusbounce.features.module.modules.render.FreeLook
import net.minusmc.minusbounce.utils.InventoryUtils
import net.minusmc.minusbounce.utils.Rotation
import net.minusmc.minusbounce.utils.RotationUtils
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.TickEvent
import net.minusmc.minusbounce.utils.misc.RandomUtils
import net.minusmc.minusbounce.value.*
import kotlin.math.roundToInt

@ModuleInfo(name = "LegitScaffold", description = "Automatically places blocks beneath your feet.", category = ModuleCategory.WORLD)
object LegitScaffold : Module() {
    val modeValue = ListValue("Mode", arrayOf("GodBridge", "SpeedBridge", "Breezily", "JitterBridge", "TellyBridge"), "GodBridge")

    val safewalkValue = ListValue("SafewalkType", arrayOf("Sneak", "Safewalk", "None"), "Safewalk").displayable { modeValue.get().equals("GodBridge", true) }
    val derpValue = BoolValue("GodDerpBridge", false).displayable { modeValue.get().equals("GodBridge", true) }
    val silentRotation = BoolValue("SilentRotation", false)
    val jump = BoolValue("Jump", false)
    val delay = IntegerValue("Delay", 1, 0, 6)
    
    private var playerRot = Rotation(0f, 0f)
    private var oldPlayerRot = Rotation(0f, 0f)
    private var lockRotation = Rotation(0f, 0f)
    private var camYaw = 0f
    private var camPitch = 0f

    private var prevSlot = 0

    private var fw = false
    private var bw = false
    private var left = false
    private var right = false

    private var breezily = false

    override fun onEnable() {
        MinusBounce.moduleManager[FreeLook::class.java]!!.enable()
        prevSlot = mc.thePlayer.inventory.currentItem
    }

    override fun onDisable() {
        MinusBounce.moduleManager[FreeLook::class.java]!!.disable()
        mc.thePlayer.inventory.currentItem = prevSlot

        correctControls(0)
        mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
        mc.gameSettings.keyBindUseItem.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindUseItem)
        mc.gameSettings.keyBindSneak.pressed = (GameSettings.isKeyDown(mc.gameSettings.keyBindSneak))
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        if (modeValue.equals("GodBridge")) {
            if (safewalkValue.equals("Safewalk")) {
                event.isSafeWalk = true
            }
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val blockSlot = InventoryUtils.findAutoBlockBlock()
        if (blockSlot != -1) {
            mc.thePlayer.inventory.currentItem = blockSlot - 36
            mc.rightClickDelayTimer = delay.get()
            mc.gameSettings.keyBindUseItem.pressed = true
        }
        if (silentRotation.get()) {
            camYaw = FreeLook.cameraYaw
            camPitch = FreeLook.cameraPitch
        }

        oldPlayerRot = Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)

        when (modeValue.get().lowercase()) {
            "breezily" -> {
                var rpitch = 0f
                if (((camYaw / 45).roundToInt()) % 2 == 0) {
                    rpitch = 79.6f
                } else {
                    rpitch = 76.3f
                }

                playerRot = Rotation(camYaw + 180f, rpitch)
                lockRotation = RotationUtils.limitAngleChange(oldPlayerRot, playerRot, 60f)

                correctControls(1)
                mc.gameSettings.keyBindRight.pressed = false
                mc.gameSettings.keyBindLeft.pressed = false

                if (mc.theWorld.getBlockState(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)).block == Blocks.air && ((camYaw / 45).roundToInt()) % 2 == 0) {
                    breezily = !breezily
                    mc.gameSettings.keyBindRight.pressed = breezily
                    mc.gameSettings.keyBindLeft.pressed = !breezily
                }
            }
            "godbridge" -> {
                var rpitch = 0f
                if (((camYaw / 45).roundToInt()) % 2 == 0) {
                    if (safewalkValue.equals("None")) {
                        rpitch = 79f
                    } else {
                        rpitch = 83.2f
                    }
                } else {
                    if (safewalkValue.equals("None")) {
                        rpitch = 76.3f
                    } else {
                        rpitch = 78.1f
                    }
                }

                // Applying rotations
                if (derpValue.get()) {
                    if (mc.theWorld.getBlockState(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)).block == Blocks.air) {
                        playerRot = Rotation(camYaw + 180f, rpitch)
                    } else {
                        if (mc.thePlayer.onGround && mc.gameSettings.keyBindJump.pressed) {
                            playerRot = Rotation(camYaw + 31, rpitch) // jump correction
                        } else {
                            playerRot = Rotation(camYaw + 45, rpitch) // normal derp
                        }
                    }

                    lockRotation = RotationUtils.limitAngleChange(oldPlayerRot, playerRot, 180f)
                } else {
                    playerRot = Rotation(camYaw + 180f, rpitch)
                    lockRotation = RotationUtils.limitAngleChange(oldPlayerRot, playerRot, 90f)
                }
                if (derpValue.get()) {
                    if (mc.theWorld.getBlockState(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)).block == Blocks.air) {
                        correctControls(1)
                    } else {
                        correctControls(2)
                    }
                } else {
                    correctControls(1)
                }
                if (safewalkValue.equals("Sneak")) {
                    mc.gameSettings.keyBindSneak.pressed = (GameSettings.isKeyDown(mc.gameSettings.keyBindSneak) || mc.theWorld.getBlockState(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)).block == Blocks.air)
                }
            }
            "speedbridge" -> {
                var rpitch = 0.0
                if (((camYaw / 15).roundToInt()) % 6 == 0) {
                    rpitch = 78.7
                } else  {
                    rpitch = 78.9
                }

                if (rpitch == 78.7) {
                    playerRot = Rotation(camYaw - 135, rpitch.toFloat())
                    correctControls(3)
                } else {
                    playerRot = Rotation(camYaw - 180, rpitch.toFloat())
                    correctControls(1)
                }

                lockRotation = RotationUtils.limitAngleChange(oldPlayerRot, playerRot, 90f)

                mc.gameSettings.keyBindSneak.pressed = (GameSettings.isKeyDown(mc.gameSettings.keyBindSneak) || mc.theWorld.getBlockState(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)).block == Blocks.air)
            }
            "jitterbridge" -> {
                var rpitch = 0f
                if (((camYaw / 45).roundToInt()) % 2 == 0) {
                    rpitch = 77.4f
                } else  {
                    rpitch = 77.1f
                }

                playerRot = Rotation(camYaw + 180, rpitch)
                lockRotation = RotationUtils.limitAngleChange(oldPlayerRot, playerRot, 80f)

                correctControls(1)

                if (jump.get()) mc.gameSettings.keyBindJump.pressed = true
            }
            "tellybridge" -> {
                var rpitch = 0f
                if (((camYaw / 45).roundToInt()) % 2 == 0) {
                    rpitch = 75.1f
                } else  {
                    rpitch = 75.5f
                }

                if (mc.thePlayer.onGround) {
                    playerRot = Rotation(camYaw, rpitch)
                    correctControls(0)
                } else {
                    playerRot = Rotation(camYaw + 180, rpitch)
                    correctControls(1)
                }
                lockRotation = RotationUtils.limitAngleChange(oldPlayerRot, playerRot, 180f)
                if (jump.get()) mc.gameSettings.keyBindJump.pressed = true
            }
        }

        lockRotation.toPlayer(mc.thePlayer)
    }

    private fun correctControls(type: Int) {
        fw =  GameSettings.isKeyDown(mc.gameSettings.keyBindForward)
        bw = GameSettings.isKeyDown(mc.gameSettings.keyBindBack)
        right = GameSettings.isKeyDown(mc.gameSettings.keyBindRight)
        left = GameSettings.isKeyDown(mc.gameSettings.keyBindLeft)
        when (type) {
            0 -> {
                mc.gameSettings.keyBindForward.pressed = fw
                mc.gameSettings.keyBindBack.pressed = bw
                mc.gameSettings.keyBindRight.pressed = right
                mc.gameSettings.keyBindLeft.pressed = left
            }
            1 -> {
                mc.gameSettings.keyBindForward.pressed = bw
                mc.gameSettings.keyBindBack.pressed = fw
                mc.gameSettings.keyBindRight.pressed = left
                mc.gameSettings.keyBindLeft.pressed = right
            }
            2 -> {
                mc.gameSettings.keyBindForward.pressed = fw || right
                mc.gameSettings.keyBindBack.pressed = left || bw
                mc.gameSettings.keyBindRight.pressed = right || bw
                mc.gameSettings.keyBindLeft.pressed = fw || left
            }
            3 -> {
                mc.gameSettings.keyBindForward.pressed = left || bw
                mc.gameSettings.keyBindBack.pressed = fw || right
                mc.gameSettings.keyBindRight.pressed = fw || left
                mc.gameSettings.keyBindLeft.pressed = right || bw
            }
        }
    }
}