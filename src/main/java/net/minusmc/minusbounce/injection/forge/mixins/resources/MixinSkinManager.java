/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.injection.forge.mixins.resources;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minusmc.minusbounce.MinusBounce;
import net.minusmc.minusbounce.features.module.modules.misc.NameProtect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.SkinManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Mixin(SkinManager.class)
public class MixinSkinManager {

    @Inject(method = "loadSkinFromCache", cancellable = true, at = @At("HEAD"))
    private void injectSkinProtect(GameProfile gameProfile, CallbackInfoReturnable<Map<MinecraftProfileTexture.Type, MinecraftProfileTexture>> cir) {
        if (gameProfile == null)
            return;
        
        NameProtect nameProtect = MinusBounce.moduleManager.getModule(NameProtect.class);

        if (nameProtect.getState() && nameProtect.getSkinProtectValue().get()) {
            if (nameProtect.getAllPlayersValue().get() || Objects.equals(gameProfile.getId(), Minecraft.getMinecraft().getSession().getProfile().getId())) {
                cir.setReturnValue(new HashMap<>());
                cir.cancel();
            }
        }
    }

}
