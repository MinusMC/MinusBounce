package net.minusmc.minusbounce.ui.client.clickgui.skeet.component.impl.sub.text;

import net.minusmc.minusbounce.ui.client.clickgui.skeet.LockedResolution;
import net.minusmc.minusbounce.ui.client.clickgui.skeet.SkeetClickGUI;
import net.minusmc.minusbounce.ui.client.clickgui.skeet.component.Component;
import net.minusmc.minusbounce.ui.font.TTFFontRenderer;

public final class TextComponent extends Component {
    private static final TTFFontRenderer FONT_RENDERER = SkeetClickGUI.FONT_RENDERER;
    private final String text;

    public TextComponent(Component parent, String text, float x, float y) {
        super(parent, x, y, FONT_RENDERER.getWidth(text), FONT_RENDERER.getHeight(text));
        this.text = text;
    }

    @Override
    public void drawComponent(LockedResolution resolution, int mouseX, int mouseY) {
        if (SkeetClickGUI.shouldRenderText()) {
            FONT_RENDERER.drawString(this.text, this.getX(), this.getY(), SkeetClickGUI.getColor(0xE6E6E6));
        }
    }
}
