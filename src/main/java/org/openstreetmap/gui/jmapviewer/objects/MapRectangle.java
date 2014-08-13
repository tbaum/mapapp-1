// License: GPL. For details, see Readme.txt file.
package org.openstreetmap.gui.jmapviewer.objects;

import org.openstreetmap.gui.jmapviewer.Style;

import java.awt.*;

public class MapRectangle extends MapObject {

    private final Coordinate topLeft;
    private final Coordinate bottomRight;

    public MapRectangle(String name, Coordinate topLeft, Coordinate bottomRight, Style style) {
        super(name, style);
        this.topLeft = topLeft;
        this.bottomRight = bottomRight;
    }

    public static Style getDefaultStyle() {
        return new Style(Color.BLUE, null, new BasicStroke(2), getDefaultFont());
    }

    public Coordinate getTopLeft() {
        return topLeft;
    }

    public Coordinate getBottomRight() {
        return bottomRight;
    }

    public void paint(Graphics2D g, Point topLeft, Point bottomRight) {
        Color oldColor = g.getColor();
        g.setColor(getColor());
        Stroke oldStroke = g.getStroke();
        g.setStroke(getStroke());
        g.drawRect(topLeft.x, topLeft.y, bottomRight.x - topLeft.x, bottomRight.y - topLeft.y);
        g.setColor(oldColor);
        g.setStroke(oldStroke);
        Point p = new Point(topLeft.x + ((bottomRight.x - topLeft.x) / 2), topLeft.y + ((bottomRight.y - topLeft.y) / 2));
        paintText(g, p);
    }
}
