/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.injection.forge.mixins.client;

import net.minecraft.util.MovementInput;
import net.minusmc.minusbounce.event.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MovementInput.class)
public class MixinMovementInput {

    @Shadow 
    public float moveStrafe;

    @Shadow
    public float moveForward;

    @Shadow
    public boolean jump;

    @Shadow
    public boolean sneak;

    @Shadow
    public void updatePlayerMoveState() {
    }

}