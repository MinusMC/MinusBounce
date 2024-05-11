/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.injection.forge.mixins.render;

import net.minecraft.entity.item.*;
import net.minecraft.client.renderer.entity.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import net.minusmc.minusbounce.features.module.modules.render.*;
import net.minusmc.minusbounce.*;
import org.lwjgl.opengl.*;
import org.spongepowered.asm.mixin.injection.*;
import net.minecraft.client.resources.model.*;
import net.minecraft.util.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.*;
import net.minecraft.item.*;
import org.spongepowered.asm.mixin.*;

@Mixin(RenderEntityItem.class)
public abstract class MixinRenderEntityItem extends Render<EntityItem> {
    protected MixinRenderEntityItem(final RenderManager renderManager) {
        super(renderManager);
    }

    @Shadow
    protected abstract int func_177078_a(final ItemStack p0);
    
    @Shadow
    protected abstract boolean shouldBob();
    
    @Overwrite
    private int func_177077_a(EntityItem itemIn, double x, double y, double z, float yaw, IBakedModel model) {
        final ItemPhysics itemPhysics = MinusBounce.moduleManager.getModule(ItemPhysics.class);
        ItemStack itemstack = itemIn.getEntityItem();
        Item item = itemstack.getItem();

        if (item == null || itemPhysics == null)
        {
            return 0;
        }
        else
        {
            boolean flag = model.isGui3d();
            int i = this.func_177078_a(itemstack);
            float f = 0.25F;
            float f1 = MathHelper.sin(((float)itemIn.getAge() + yaw) / 10.0F + itemIn.hoverStart) * 0.1F + 0.1F;
            if (itemPhysics.getState()) {
                f1 = 0.0f;
            }
            float f2 = model.getItemCameraTransforms().getTransform(ItemCameraTransforms.TransformType.GROUND).scale.y;
            GlStateManager.translate((float)x, (float)y + f1 + 0.25F * f2, (float)z);

            if (flag || this.renderManager.options != null)
            {
                float f3 = (((float)itemIn.getAge() + yaw) / 20.0F + itemIn.hoverStart) * (180F / (float)Math.PI);
                if (itemPhysics.getState()) {
                    if (itemIn.onGround) {
                        GL11.glRotatef(itemIn.rotationYaw, 0.0f, 1.0f, 0.0f);
                        GL11.glRotatef(itemIn.rotationPitch + 90.0f, 1.0f, 0.0f, 0.0f);
                    } else {
                        for (int a = 0; a < 10; ++a) {
                            GL11.glRotatef(f3, itemPhysics.getItemWeight().get(), itemPhysics.getItemWeight().get(), 0.0f);
                        }
                    }
                } else {
                    GlStateManager.rotate(f3, 0.0F, 1.0F, 0.0F);
                }
            }

            if (!flag)
            {
                float f6 = -0.0F * (float)(i - 1) * 0.5F;
                float f4 = -0.0F * (float)(i - 1) * 0.5F;
                float f5 = -0.046875F * (float)(i - 1) * 0.5F;
                GlStateManager.translate(f6, f4, f5);
            }

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            return i;
        }
    }
}
