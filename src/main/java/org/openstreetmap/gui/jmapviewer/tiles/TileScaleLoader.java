package org.openstreetmap.gui.jmapviewer.tiles;

import org.openstreetmap.gui.jmapviewer.JMapViewer;

import javax.inject.Inject;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import static org.openstreetmap.gui.jmapviewer.OsmMercator.TILE_SIZE;

class TileScaleLoader {
    private final TileFactory tileFactory;
    private final TileCache tileCache;

    @Inject TileScaleLoader(TileFactory tileFactory, TileCache tileCache) {
        this.tileFactory = tileFactory;
        this.tileCache = tileCache;
    }

    Tile createWithPlaceholder(int xtile, int ytile, int zoom) {
        BufferedImage tmpImage = new BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) tmpImage.getGraphics();
        for (int zoomDiff = 1; zoomDiff < 5; zoomDiff++) {
            // first we check if there are already the 2^x tiles
            // of a higher detail level
            int zoom_high = zoom + zoomDiff;
            if (zoomDiff < 3 && zoom_high <= JMapViewer.MAX_ZOOM) {
                int factor = 1 << zoomDiff;
                int xtile_high = xtile << zoomDiff;
                int ytile_high = ytile << zoomDiff;
                double scale = 1.0 / factor;
                g.setTransform(AffineTransform.getScaleInstance(scale, scale));
                int paintedTileCount = 0;
                for (int x = 0; x < factor; x++) {
                    for (int y = 0; y < factor; y++) {
                        Tile zTile = tileCache.getCachedTile(tileFactory.createTile(xtile_high + x, ytile_high + y, zoom_high));
                        if (zTile != null && zTile.isLoaded() && zTile.getImage() != null) {
                            paintedTileCount++;
                            g.drawImage(zTile.getImage(), x * TILE_SIZE, y * TILE_SIZE, null);
                        }
                    }
                }
                if (paintedTileCount == factor * factor) {
                    return tileFactory.createTile(xtile, ytile, zoom, tmpImage);
                }
            }

            int zoom_low = zoom - zoomDiff;
            if (zoom_low >= JMapViewer.MIN_ZOOM) {
                int xtile_low = xtile >> zoomDiff;
                int ytile_low = ytile >> zoomDiff;
                int factor = (1 << zoomDiff);
                AffineTransform at = new AffineTransform();
                int translate_x = (xtile % factor) * TILE_SIZE;
                int translate_y = (ytile % factor) * TILE_SIZE;
                at.setTransform((double) factor, 0, 0, (double) factor, -translate_x, -translate_y);
                g.setTransform(at);
                Tile zTile = tileCache.getCachedTile(tileFactory.createTile(xtile_low, ytile_low, zoom_low));
                if (zTile != null && zTile.isLoaded() && zTile.getImage() != null) {
                    g.drawImage(zTile.getImage(), 0, 0, null);
                    g.setColor(new Color(128, 128, 128, 128));
                    for (int x = 0; x < TILE_SIZE * 2; x += 10) g.drawLine(x, 0, 0, x);
                    return tileFactory.createTile(xtile, ytile, zoom, tmpImage);
                }
            }
        }
        return tileFactory.createTile(xtile, ytile, zoom);
    }
}
