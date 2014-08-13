package org.openstreetmap.gui.jmapviewer.objects;

import org.openstreetmap.gui.jmapviewer.Style;

import java.awt.*;

public class MapMarkerDot extends MapMarkerCircle {

    public static final int DOT_RADIUS = 5;

    public MapMarkerDot(Coordinate coord) {
        super(null, coord, DOT_RADIUS, getDefaultStyle());
    }

    public static Style getDefaultStyle() {
        return new Style(Color.BLACK, Color.YELLOW, null, getDefaultFont());
    }
}
