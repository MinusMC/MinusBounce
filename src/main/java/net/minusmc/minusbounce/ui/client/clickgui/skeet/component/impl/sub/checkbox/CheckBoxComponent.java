package net.minusmc.minusbounce.ui.client.clickgui.skeet.component.impl.sub.checkbox;

import net.minusmc.minusbounce.ui.client.clickgui.skeet.LockedResolution;
import net.minusmc.minusbounce.ui.client.clickgui.skeet.SkeetClickGUI;
import net.minusmc.minusbounce.ui.client.clickgui.skeet.SkeetUtils;
import net.minusmc.minusbounce.ui.client.clickgui.skeet.component.ButtonComponent;
import net.minusmc.minusbounce.ui.client.clickgui.skeet.component.Component;
import net.minusmc.minusbounce.ui.client.clickgui.skeet.component.PredicateComponent;
import net.minusmc.minusbounce.utils.render.RenderUtils;

public abstract class CheckBoxComponent extends ButtonComponent implements PredicateComponent {
    public CheckBoxComponent(Component parent, float x, float y, float width, float height) {
        super(parent, x, y, width, height);
    }

    @Override
    public void drawComponent(LockedResolution resolution, int mouseX, int mouseY) {
        float x = this.getX();
        float y = this.getY();
        float width = this.getWidth();
        float height = this.getHeight();
        RenderUtils.INSTANCE.drawRect(x + 0.8f, y + 0.8f, x + width - 0.8f, y + height - 0.8f, SkeetClickGUI.getColor(855309));
        boolean checked = this.isChecked();
        boolean hovered = this.isHovered(mouseX, mouseY);
        SkeetUtils.drawGradientRect(x + 1.2f, y + 1.2f, x + width - 1.2f, y + height - 1.2f, false, checked ? SkeetClickGUI.getColor() : SkeetClickGUI.getColor(hovered ? SkeetUtils.darker(0x494949, 1.4f) : 0x494949), checked ? SkeetUtils.darker(SkeetClickGUI.getColor(), 0.8f) : SkeetClickGUI.getColor(hovered ? SkeetUtils.darker(0x303030, 1.4f) : 0x303030));
    }

    @Override
    public void onPress(int mouseButton) {
        if (mouseButton == 0) {
            this.setChecked(!this.isChecked());
        }
    }

    public abstract boolean isChecked();

    public abstract void setChecked(boolean var1);
}
