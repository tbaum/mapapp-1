package org.openstreetmap.gui.jmapviewer.objects;

import org.openstreetmap.gui.jmapviewer.OsmMercator;
import org.openstreetmap.gui.jmapviewer.Style;

import java.awt.*;

import static org.openstreetmap.gui.jmapviewer.objects.MapMarkerCircle.STYLE.FIXED;

public class MapMarkerCircle extends MapObject implements Coordinate {

    private Coordinate coord;
    private double radius;
    private STYLE markerStyle = FIXED;

    public MapMarkerCircle(String name, Coordinate coord, double radius, Style style) {
        super(name, style);
        this.coord = coord;
        this.radius = radius;
    }

    public static Style getDefaultStyle() {
        return new Style(Color.ORANGE, new Color(200, 200, 200, 200), null, getDefaultFont());
    }

    public Coordinate getCoordinate() {
        return coord;
    }

    public double getLat() {
        return coord.getLat();
    }

//    @Override
//    public void setLat(double lat) {
//        if (coord == null) coord = new CoordinateImpl(lat, 0);
//        else coord.setLat(lat);
//    }

    public double getLon() {
        return coord.getLon();
    }

//    @Override
//    public void setLon(double lon) {
//        if (coord == null) coord = new CoordinateImpl(0, lon);
//        else coord.setLon(lon);
//    }

    public double getRadius() {
        return radius;
    }

    public STYLE getMarkerStyle() {
        return markerStyle;
    }

    public void paint(Graphics2D g, Point position, int radio) {
        int size = radio * 2;
        if (getBackColor() != null) {
            Composite oldComposite = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            g.setPaint(getBackColor());
            g.fillOval(position.x - radio, position.y - radio, size, size);
            g.setComposite(oldComposite);
        }
        g.setColor(getColor());
        g.drawOval(position.x - radio, position.y - radio, size, size);

        paintText(g, position);
    }

    /**
     * Calculates the position on the map of a given coordinate
     *
     * @return Integer the radius in pixels
     */
    public Integer getRadius(Point p, int zoom, int width) {
        if (getMarkerStyle() == STYLE.FIXED)
            return (int) getRadius();
        else if (p != null) {
            return p.y - ((int) OsmMercator.LatToY(getLat() + getRadius(), zoom) - width);
        } else return null;
    }

    public boolean isHit(Point point, Point mapPosition, int zoom, int width) {
        if (mapPosition == null) return false;

        Integer radius = getRadius(mapPosition, zoom, width);
        return radius != null && point.distance(mapPosition) <= radius;
    }

    public static enum STYLE {FIXED, VARIABLE}
}
