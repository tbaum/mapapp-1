package org.openstreetmap.gui.jmapviewer.tiles;

import mapapp.CacheDir;

import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * @author tbaum
 * @since 04.08.2014
 */
class TileFactory {

    private final File tileCacheDir;

    @Inject TileFactory(@CacheDir File tileCacheDir) {
        this.tileCacheDir = tileCacheDir;
    }

    Tile createTile(Integer xtile, Integer ytile, Integer zoom) {
        File cacheDir = new File(tileCacheDir, String.valueOf(zoom));
        if ((!cacheDir.exists() && !cacheDir.mkdirs())) {
            throw new RuntimeException("unable to create cache-dir " + cacheDir);
        }
        return new Tile(xtile, ytile, zoom, new File(cacheDir, xtile + "_" + ytile));
    }

    Tile createTile(int xtile, int ytile, int zoom, BufferedImage tmpImage) {
        Tile tile = createTile(xtile, ytile, zoom);
        tile.setImage(tmpImage);
        return tile;
    }
}
