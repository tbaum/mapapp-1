package org.openstreetmap.gui.jmapviewer.tilesources;

import org.openstreetmap.gui.jmapviewer.tiles.Tile;

public interface TileSource {

    int getMaxZoom();

    String getUrl(Tile tile);

    String getAttributionText();

    String getAttributionLUrl();

    String getTermsOfUseText();

    String getTermsOfUseUrl();
}
