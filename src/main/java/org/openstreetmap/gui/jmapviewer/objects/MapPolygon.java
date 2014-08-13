// License: GPL. For details, see Readme.txt file.
package org.openstreetmap.gui.jmapviewer.objects;

import org.openstreetmap.gui.jmapviewer.Style;

import java.awt.*;
import java.util.List;

public class MapPolygon extends MapObject {

    private final List<? extends Coordinate> points;

    public MapPolygon(String name, List<? extends Coordinate> points, Style style) {
        super(name, style);
        this.points = points;
    }

    public static Style getDefaultStyle() {
        return new Style(Color.BLUE, new Color(100, 100, 100, 50), new BasicStroke(2), getDefaultFont());
    }

    public List<? extends Coordinate> getPoints() {
        return this.points;
    }

    public void paint(Graphics2D g, List<Point> points) {
        Polygon polygon = new Polygon();
        for (Point p : points) {
            polygon.addPoint(p.x, p.y);
        }
        paint(g, polygon);
    }

    public void paint(Graphics2D g, Polygon polygon) {
        // Prepare graphics
        Color oldColor = g.getColor();
        g.setColor(getColor());

        Stroke oldStroke = g.getStroke();
        g.setStroke(getStroke());
        // Draw
        g.drawPolygon(polygon);
        if (getBackColor() != null) {
            Composite oldComposite = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            g.setPaint(getBackColor());
            g.fillPolygon(polygon);
            g.setComposite(oldComposite);
        }
        // Restore graphics
        g.setColor(oldColor);
        g.setStroke(oldStroke);

        Rectangle rec = polygon.getBounds();
        Point corner = rec.getLocation();
        Point p = new Point(corner.x + (rec.width / 2), corner.y + (rec.height / 2));
        paintText(g, p);
    }

}
