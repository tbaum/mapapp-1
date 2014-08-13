// License: GPL. For details, see Readme.txt file.
package org.openstreetmap.gui.jmapviewer.tiles;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newFixedThreadPool;

@Singleton
public class TileController {

    private final ExecutorService executorService = newFixedThreadPool(4, (r) -> {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        return thread;
    });

    private final TileLoader tileLoader;
    private final TileCache tileCache;
    private final TileEventSource tileEventSource;
    private final TileFactory tileFactory;
    private final TileScaleLoader scaleLoader;

    @Inject TileController(TileLoader tileLoader, TileCache tileCache, TileEventSource tileEventSource,
                           TileFactory tileFactory, TileScaleLoader scaleLoader) {
        this.tileLoader = tileLoader;
        this.tileCache = tileCache;
        this.tileEventSource = tileEventSource;
        this.tileFactory = tileFactory;
        this.scaleLoader = scaleLoader;
    }

    public Tile getTile(int tilex, int tiley, int zoom) {
        int max = (1 << zoom);
        if (tilex < 0 || tilex >= max || tiley < 0 || tiley >= max) {
            return null;
        }

        Tile tile = tileCache.getCachedTile(tileFactory.createTile(tilex, tiley, zoom));
        if (tile == null || tile.isError()) {
            tile = scaleLoader.createWithPlaceholder(tilex, tiley, zoom);
        } else if (tile.isLoaded()) {
            return tile;
        }

        tileCache.addToCache(tile, (missingTile) -> executorService.submit(tileLoader.createLoaderJob(missingTile), missingTile));
        return tile;
    }


    public void addListener(TileLoaderListener listener) {
        tileEventSource.addListener(listener);
    }
}
