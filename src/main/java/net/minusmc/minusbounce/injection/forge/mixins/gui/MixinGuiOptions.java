/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.injection.forge.mixins.gui;

import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.gui.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.EnumDifficulty;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiOptions.class)
public class MixinGuiOptions extends GuiScreen {

    @Shadow
    @Final
    private static GameSettings.Options[] field_146440_f;

    @Shadow
    private GuiButton field_175357_i;

    @Shadow
    private GuiLockIconButton field_175356_r;

    @Shadow
    protected String field_146442_a;

    @Shadow
    @Final
    private GameSettings game_settings_1;

    @Shadow
    public String func_175355_a(EnumDifficulty p_175355_1_) {
        return null;
    }

    @Redirect(method = "actionPerformed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/stream/GuiStreamUnavailable;func_152321_a(Lnet/minecraft/client/gui/GuiScreen;)V", remap = false))
    private void noGuiStream2(GuiScreen screen) {
        // no-op
    }

    /**
     * @author toidicakhia
     * @reason Reduce button in GuiScreen
     */

    @Overwrite
    public void initGui() {
        int i = 0;
        this.field_146442_a = I18n.format("options.title", new Object[0]);

        for (GameSettings.Options options: this.field_146440_f) {
            if (options.getEnumFloat())
                this.buttonList.add(new GuiOptionSlider(options.returnEnumOrdinal(), this.width / 2 - 155 + i % 2 * 160, this.height / 6 - 12 + 24 * (i >> 1), options));
            else {
                GuiOptionButton guioptionbutton = new GuiOptionButton(options.returnEnumOrdinal(), this.width / 2 - 155 + i % 2 * 160, this.height / 6 - 12 + 24 * (i >> 1), options, this.game_settings_1.getKeyBinding(options));
                this.buttonList.add(guioptionbutton);
            }
            ++i;
        }

        if (this.mc.theWorld != null) {
            EnumDifficulty enumdifficulty = this.mc.theWorld.getDifficulty();
            this.field_175357_i = new GuiButton(108, this.width / 2 - 155 + i % 2 * 160, this.height / 6 - 12 + 24 * (i >> 1), 150, 20, this.func_175355_a(enumdifficulty));
            this.buttonList.add(this.field_175357_i);

            if (this.mc.isSingleplayer() && !this.mc.theWorld.getWorldInfo().isHardcoreModeEnabled()) {
                this.field_175357_i.setWidth(this.field_175357_i.getButtonWidth() - 20);
                this.field_175356_r = new GuiLockIconButton(109, this.field_175357_i.xPosition + this.field_175357_i.getButtonWidth(), this.field_175357_i.yPosition);
                this.buttonList.add(this.field_175356_r);
                this.field_175356_r.func_175229_b(this.mc.theWorld.getWorldInfo().isDifficultyLocked());
                this.field_175356_r.enabled = !this.field_175356_r.func_175230_c();
                this.field_175357_i.enabled = !this.field_175356_r.func_175230_c();
            }
            else
                this.field_175357_i.enabled = false;
        }

        this.buttonList.add(new GuiButton(110, this.width / 2 - 155, this.height / 6 + 48 - 6, 150, 20, I18n.format("options.skinCustomisation", new Object[0])));
        this.buttonList.add(new GuiButton(106, this.width / 2 + 5, this.height / 6 + 48 - 6, 150, 20, I18n.format("options.sounds", new Object[0])));
        this.buttonList.add(new GuiButton(101, this.width / 2 - 155, this.height / 6 + 72 - 6, 150, 20, I18n.format("options.video", new Object[0])));
        this.buttonList.add(new GuiButton(100, this.width / 2 + 5, this.height / 6 + 72 - 6, 150, 20, I18n.format("options.controls", new Object[0])));
        this.buttonList.add(new GuiButton(102, this.width / 2 - 155, this.height / 6 + 96 - 6, 150, 20, I18n.format("options.language", new Object[0])));
        this.buttonList.add(new GuiButton(103, this.width / 2 + 5, this.height / 6 + 96 - 6, 150, 20, I18n.format("options.chat.title", new Object[0])));
        this.buttonList.add(new GuiButton(105, this.width / 2 - 155, this.height / 6 + 120 - 6, 150, 20, I18n.format("options.resourcepack", new Object[0])));
        this.buttonList.add(new GuiButton(104, this.width / 2 + 5, this.height / 6 + 120 - 6, 150, 20, I18n.format("options.snooper.view", new Object[0])));
        this.buttonList.add(new GuiButton(200, this.width / 2 - 100, this.height / 6 + 168, I18n.format("gui.done", new Object[0])));
    }

    @Override
    public void onGuiClosed() {
        mc.gameSettings.saveOptions();
    }
}
