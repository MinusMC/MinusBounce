/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.injection.forge.mixins.gui;

import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Gui.class)
public abstract class MixinGui {

    @Shadow public abstract void drawTexturedModalRect(float xCoord, float yCoord, int minU, int minV, int maxU, int maxV);

    @Shadow protected float zLevel;
    
}
