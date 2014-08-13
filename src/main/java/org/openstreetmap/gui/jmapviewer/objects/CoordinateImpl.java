package org.openstreetmap.gui.jmapviewer.objects;

public class CoordinateImpl implements Coordinate {

    private final double lat, lon;

    public CoordinateImpl(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public String toString() {
        return "Coordinate[" + lon + ", " + lat + "]";
    }
}
