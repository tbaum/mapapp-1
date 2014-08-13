package org.openstreetmap.gui.jmapviewer.tilesources;

import org.openstreetmap.gui.jmapviewer.tiles.Tile;

import static java.lang.String.format;

public class Mapnik implements TileSource {

    private int server = 0;

    @Override public int getMaxZoom() {
        return 19;
    }

    @Override public String getAttributionText() {
        return "\u00a9 OpenStreetMap contributors";
    }

    @Override public String getAttributionLUrl() {
        return "https://openstreetmap.org/";
    }

    @Override public String getTermsOfUseText() {
        return "Background Terms of Use";
    }

    @Override public String getTermsOfUseUrl() {
        return "https://www.openstreetmap.org/copyright";
    }

    @Override
    public String getUrl(Tile tile) {
        String url = format("https://%s.tile.openstreetmap.org/%d/%d/%d.png", 'a' + (char) server, tile.getZoom(), tile.getX(), tile.getY());
        server = (server + 1) % 3;
        return url;
    }
}