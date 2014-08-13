package org.openstreetmap.gui.jmapviewer.tiles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;

import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 * @author tbaum
 * @since 04.08.2014
 */
@Singleton class TileCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(TileCache.class);
    private final LinkedHashMap<Tile, Future<Tile>> memCache = new LinkedHashMap<>();
    private final TileEventSource tileEventSource;

    @Inject TileCache(TileEventSource tileEventSource) {
        this.tileEventSource = tileEventSource;
    }

    synchronized Tile getCachedTile(Tile tile) {
        Future<Tile> future = memCache.get(tile);
        if (future != null && future.isDone()) {
            try {
                Tile loadedTile = future.get();
                memCache.remove(loadedTile);
                memCache.put(loadedTile, future);
                if (loadedTile.isLoaded()) {
                    return loadedTile;
                }
            } catch (InterruptedException | ExecutionException ignored) {
            }
        }
        return null;
    }

    synchronized void addToCache(Tile tile, Function<Tile, Future<Tile>> fetchTile) {
        if (tile.isLoadingOrMark()) {
            return;
        }
        File tileFile = tile.getCacheFile();

        if (tile.isCacheValid()) {
            LOGGER.debug("using cache {} -> {}", tile, tile.getCacheFile());
            try {
                tile.loadImage(new FileInputStream(tileFile));
                tileEventSource.tileLoadingFinished(tile);
                memCache.put(tile, completedFuture(tile));
                return;
            } catch (IOException ignored) {
                LOGGER.debug(ignored.getMessage(), ignored);
            }
        }

        Future<Tile> future = memCache.computeIfAbsent(tile, fetchTile);

        memCache.remove(tile);
        memCache.put(tile, future);

        Iterator<Tile> iterator = memCache.keySet().iterator();
        while (memCache.size() > TileLoader.CACHE_SIZE && iterator.hasNext()) {
            Tile next = iterator.next();
            LOGGER.debug("drop from mem-cache {}", next);
            iterator.remove();
        }
    }
}
