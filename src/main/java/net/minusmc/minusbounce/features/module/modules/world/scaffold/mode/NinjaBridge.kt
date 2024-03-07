/*package net.minusmc.minusbounce.features.module.modules.world.scaffold.mode

import net.minecraft.client.settings.GameSettings
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minusmc.minusbounce.features.module.modules.world.Scaffold2
import net.minusmc.minusbounce.features.module.modules.world.scaffold.ModeScaffold
import net.minusmc.minusbounce.utils.Rotation
import net.minusmc.minusbounce.utils.RotationUtils
import kotlin.math.roundToInt

class NinjaBridge: ModeScaffold("Ninja") {
    private var playerRot = Rotation(0f, 0f)
    private var oldPlayerRot = Rotation(0f, 0f)
    private var lockRotation = Rotation(0f, 0f)
    private var camYaw = 0f
    private var camPitch = 0f
    override fun onUpdate() {
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

        mc.gameSettings.keyBindSneak.pressed = (GameSettings.isKeyDown(mc.gameSettings.keyBindSneak) || mc.theWorld.getBlockState(
            BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)
        ).block == Blocks.air)
    }
}*/