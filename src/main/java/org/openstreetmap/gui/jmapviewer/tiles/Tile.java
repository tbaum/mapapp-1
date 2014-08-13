package org.openstreetmap.gui.jmapviewer.tiles;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static java.lang.System.currentTimeMillis;
import static mapapp.Helpers.loadStaticImage;
import static org.openstreetmap.gui.jmapviewer.tiles.Tile.State.*;

/**
 * @author tbaum
 * @since 04.08.2014
 */
public class Tile {

    private static final BufferedImage LOADING_IMAGE = loadStaticImage("/images/hourglass.png");
    private static final BufferedImage ERROR_IMAGE = loadStaticImage("/images/error.png");
    private final int xtile;
    private final int ytile;
    private final int zoom;
    private final File cacheFile;
    private BufferedImage image;
    private State state = UNKNOWN;

    Tile(int xtile, int ytile, int zoom, File cacheFile) {
        this.xtile = xtile;
        this.ytile = ytile;
        this.zoom = zoom;
        this.image = LOADING_IMAGE;
        this.cacheFile = cacheFile;
    }

    public int getX() {
        return xtile;
    }

    public int getY() {
        return ytile;
    }

    public int getZoom() {
        return zoom;
    }

    public BufferedImage getImage() {
        return image;
    }

    void setImage(BufferedImage image) {
        this.image = image;
    }

    void loadImage(InputStream input) {
        try {
            image = ImageIO.read(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        state = State.LOADED;
    }

    boolean isLoaded() {
        return state == LOADED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tile)) return false;

        Tile tile = (Tile) o;

        if (xtile != tile.xtile) return false;
        if (ytile != tile.ytile) return false;
        if (zoom != tile.zoom) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = xtile;
        result = 31 * result + ytile;
        result = 31 * result + zoom;
        return result;
    }

    boolean isError() {
        return state == ERROR;
    }

    void setError() {
        state = ERROR;
        image = ERROR_IMAGE;
    }

    synchronized boolean isLoadingOrMark() {
        if (state != UNKNOWN) {
            return true;
        }
        state = LOADING;
        return false;
    }

    File getCacheFile() {
        return cacheFile;
    }

    boolean isCacheValid() {
        return cacheFile.exists() &&
                cacheFile.length() > 0 &&
                cacheFile.lastModified() + TileLoader.FILE_AGE_ONE_WEEK > currentTimeMillis();
    }

    @Override public String toString() {
        return "Tile{zoom=" + zoom + ", xtile=" + xtile + ", ytile=" + ytile + "}";
    }

    static enum State {
        UNKNOWN, LOADING, ERROR, LOADED
    }
}
