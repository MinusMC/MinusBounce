package net.minusmc.minusbounce.ui.client.clickgui.skeet.component;

public interface ExpandableComponent {
    public float getExpandedX();
    public float getExpandedY();
    public float getExpandedWidth();
    public float getExpandedHeight();
    public void setExpanded(boolean var1);
    public boolean isExpanded();
}
