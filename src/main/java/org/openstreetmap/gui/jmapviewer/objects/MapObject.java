// License: GPL. For details, see Readme.txt file.
package org.openstreetmap.gui.jmapviewer.objects;

import org.openstreetmap.gui.jmapviewer.Style;

import javax.swing.*;
import java.awt.*;

public abstract class MapObject {
    private String name;
    private Style style;
    private boolean visible = true;
    private volatile boolean marked;

    public MapObject(String name, Style style) {
        this.name = name;
        this.style = style;
    }

    public static Font getDefaultFont() {
        Font f = UIManager.getDefaults().getFont("TextField.font");
        return new Font(f.getName(), Font.BOLD, f.getSize());
    }

    public Style getStyle() {
        return style;
    }

    public void setStyle(Style style) {
        this.style = style;
    }

    public Style getStyleAssigned() {
        return style;
    }

    public Color getColor() {
        Style styleAssigned = getStyleAssigned();
        return styleAssigned == null ? null : getStyleAssigned().getColor();
    }

    public void setColor(Color color) {
        if (style == null && color != null) style = new Style();
        if (style != null) style.setColor(color);
    }

    public Color getBackColor() {
        Style styleAssigned = getStyleAssigned();
        return styleAssigned == null ? null : getStyleAssigned().getBackColor();
    }

    public void setBackColor(Color backColor) {
        if (style == null && backColor != null) style = new Style();
        if (style != null) style.setBackColor(backColor);
    }

    public Stroke getStroke() {
        Style styleAssigned = getStyleAssigned();
        return styleAssigned == null ? null : getStyleAssigned().getStroke();
    }

    public void setStroke(Stroke stroke) {
        if (style == null && stroke != null) style = new Style();
        if (style != null) style.setStroke(stroke);
    }

    public Font getFont() {
        Style styleAssigned = getStyleAssigned();
        return styleAssigned == null ? null : getStyleAssigned().getFont();
    }

    public void setFont(Font font) {
        if (style == null && font != null) style = new Style();
        if (style != null) style.setFont(font);
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isMarked() {
        return marked;
    }

    public void setMarked(boolean marked) {
        this.marked = marked;
    }

    public String getName() {
        return name;
    }

    public void setName(String txt) {
        this.name = txt;
    }

    public void paintText(Graphics2D g, Point position) {
        if (name != null && g != null && position != null) {
            if (getFont() == null) {
                setFont(getDefaultFont());
            }
            g.setColor(Color.DARK_GRAY);
            g.setFont(getFont());
            g.drawString(name, position.x + MapMarkerDot.DOT_RADIUS + 2, position.y + MapMarkerDot.DOT_RADIUS);
        }
    }
}
