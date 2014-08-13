package org.openstreetmap.gui.jmapviewer.tiles;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/**
 * @author tbaum
 * @since 04.08.2014
 */
@Singleton class TileEventSource {

    private final List<TileLoaderListener> listener = new ArrayList<>();

    void tileLoadingFinished(Tile tile) {
        listener.forEach((l) -> l.loadingFinished(tile));
    }

    void addListener(TileLoaderListener loaderListener) {
        listener.add(loaderListener);
    }

    void removeListener(TileLoaderListener loaderListener) {
        listener.remove(loaderListener);
    }

}
