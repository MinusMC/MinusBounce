/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.minusmc.minusbounce.injection.forge.mixins.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.ServerSelectionList;
import net.minecraft.client.resources.I18n;
import net.minusmc.minusbounce.ui.elements.ToolDropdown;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiMultiplayer.class)
public abstract class MixinGuiMultiplayer extends MixinGuiScreen {
    @Unique
    private GuiButton toolButton;
    @Shadow
    private ServerSelectionList serverListSelector;
    @Shadow
    private GuiButton btnEditServer;
    @Shadow
    private GuiButton btnSelectServer;
    @Shadow
    private GuiButton btnDeleteServer;

    @Shadow
    public abstract void selectServer(int p_selectServer_1_);


    @Inject(method = "initGui", at = @At("RETURN"))
    private void initGui(CallbackInfo callbackInfo) {
        buttonList.add(toolButton = new GuiButton(997, 5, 8, 138, 20, "Tools"));
    }

    /**
     * @author pie
     * @reason fix buttons spacing being funny
     */
    @Overwrite
    public void createButtons() {
        int buttonWidth = 100;
        int buttonWidth2 = 74;
        int buttonHeight = 20;

        this.buttonList.add(this.btnSelectServer = new GuiButton(1, this.width / 2 - 154, this.height - 52, buttonWidth, buttonHeight, I18n.format("selectServer.select")));
        this.buttonList.add(new GuiButton(4, this.width / 2 - 50, this.height - 52, buttonWidth, buttonHeight, I18n.format("selectServer.direct")));
        this.buttonList.add(new GuiButton(3, this.width / 2 + 4 + 50, this.height - 52, buttonWidth, buttonHeight, I18n.format("selectServer.add")));

        this.buttonList.add(new GuiButton(0, this.width / 2 - 154, this.height - 28, buttonWidth2, buttonHeight, I18n.format("gui.cancel")));
        this.buttonList.add(this.btnEditServer = new GuiButton(7, this.width / 2 - 154 + 78, this.height - 28, buttonWidth2, buttonHeight, I18n.format("selectServer.edit")));
        this.buttonList.add(this.btnDeleteServer = new GuiButton(2, this.width / 2 - 154 + 78 * 2, this.height - 28, buttonWidth2, buttonHeight, I18n.format("selectServer.delete")));
        this.buttonList.add(new GuiButton(8, this.width / 2 - 154 + 78 * 3, this.height - 28, buttonWidth2, buttonHeight, I18n.format("selectServer.refresh")));

        this.selectServer(this.serverListSelector.func_148193_k());
    }

    @ModifyConstant(method = "drawScreen", constant = @Constant(intValue = 20))
    private int fixHeadingPosition(int original) {
        return 15;
    }

    @ModifyArg(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/I18n;format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;"))
    private String replaceHeadingText(String original) {
        return String.format("Username: %s", mc.session.getUsername());
    }

    @Inject(method = "drawScreen", at = @At("TAIL"))
    private void injectToolDraw(int mouseX, int mouseY, float partialTicks, CallbackInfo callbackInfo) {
        ToolDropdown.handleDraw(toolButton);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void injectToolClick(int mouseX, int mouseY, int mouseButton, CallbackInfo callbackInfo) {
        if (mouseButton == 0)
            if (ToolDropdown.handleClick(mouseX, mouseY, toolButton))
                callbackInfo.cancel();
    }

    @Inject(method = "actionPerformed", at = @At("HEAD"))
    private void actionPerformed(GuiButton button, CallbackInfo callbackInfo) {
        if (button.id == 997)
            ToolDropdown.toggleState();
    }
}