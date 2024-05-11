/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.injection.forge.mixins.block;

import net.minecraft.block.BlockGlass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;


@Mixin(BlockGlass.class)
public abstract class MixinBlockGlass extends MixinBlock {

    /**
     * @author toidicakhia
     * @reason Glass is a full block cube but Mojang....
     */

    @Overwrite
    public boolean isFullCube() {
        return true;
    } 
}
