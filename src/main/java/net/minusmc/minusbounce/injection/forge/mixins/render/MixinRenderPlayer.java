/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.injection.forge.mixins.render;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.util.ResourceLocation;
import net.minusmc.minusbounce.MinusBounce;
import net.minecraft.client.Minecraft;
import net.minusmc.minusbounce.features.module.modules.render.PlayerEdit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(RenderPlayer.class)
public class MixinRenderPlayer {

    private final ResourceLocation rabbit = new ResourceLocation("liquidbounce+/models/rabbit.png");
    private final ResourceLocation freddy = new ResourceLocation("liquidbounce+/models/freddy.png");
    private final ResourceLocation amogus = new ResourceLocation("liquidbounce+/models/amogus.png");
    @Redirect(method = "renderRightArm", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/ModelPlayer;isSneak:Z", ordinal = 0))
    private void resetArmState(ModelPlayer modelPlayer, boolean value) {
        modelPlayer.isRiding = modelPlayer.isSneak = false;
    }

    @Inject(method = {"getEntityTexture"}, at = {@At("HEAD")}, cancellable = true)
    public void getEntityTexture(AbstractClientPlayer entity, CallbackInfoReturnable<ResourceLocation> ci) {
        if (PlayerEdit.customModel.get() && (MinusBounce.moduleManager.getModule(PlayerEdit.class).onlyMe.get() && entity == Minecraft.getMinecraft().thePlayer || MinusBounce.moduleManager.getModule(PlayerEdit.class).onlyOther.get() && entity != Minecraft.getMinecraft().thePlayer) && MinusBounce.moduleManager.getModule(PlayerEdit.class).getState()) {
            if (MinusBounce.moduleManager.getModule(PlayerEdit.class).mode.get().contains("Rabbit")) {
                ci.setReturnValue(rabbit);
            }
            if (MinusBounce.moduleManager.getModule(PlayerEdit.class).mode.get().contains("Freddy")) {
                ci.setReturnValue(freddy);
            }
            if (MinusBounce.moduleManager.getModule(PlayerEdit.class).mode.get().contains("Amogus")) {
                ci.setReturnValue(amogus);
            }
        }
    }

}
