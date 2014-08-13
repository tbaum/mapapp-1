package org.openstreetmap.gui.jmapviewer.tilesources;

import org.openstreetmap.gui.jmapviewer.tiles.Tile;

import static java.lang.String.format;

/**
 * @author tbaum
 * @since 01.08.2014
 */
public class Lyrk implements TileSource {

    private final String apiKey;

    public Lyrk(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override public int getMaxZoom() {
        return 18;
    }

    @Override public String getUrl(Tile tile) {
        return format("http://tiles.lyrk.org/ls/%d/%d/%d?apikey=%s", tile.getZoom(), tile.getX(), tile.getY(), apiKey);
    }

    @Override public String getAttributionText() {
        return "Lizenzinformation, Tiles by Lyrk";
    }

    @Override public String getAttributionLUrl() {
        return "https://geodienste.lyrk.de/copyright";
    }

    @Override public String getTermsOfUseText() {
        return "Background Terms of Use";
    }

    @Override public String getTermsOfUseUrl() {
        return "https://www.openstreetmap.org/copyright";
    }
}
