package net.minusmc.minusbounce.ui.client.clickgui.skeet;

public class MathUtils {
    public static float clampValue(final float value, final float floor, final float cap) {
        if (value < floor) {
            return floor;
        }
        return Math.min(value, cap);
    }

}