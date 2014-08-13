// License: GPL. For details, see Readme.txt file.
package org.openstreetmap.gui.jmapviewer.tiles;

import org.openstreetmap.gui.jmapviewer.tilesources.TileSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.Random;

@Singleton class TileLoader {
    public static final long FILE_AGE_ONE_DAY = 1000 * 60 * 60 * 24;
    public static final long FILE_AGE_ONE_WEEK = FILE_AGE_ONE_DAY * 7;
    public static final int CACHE_SIZE = 200;
    private static final Logger LOGGER = LoggerFactory.getLogger(TileLoader.class);
    private final TileSource tileSource;
    private final TileEventSource tileEventSource;

    @Inject TileLoader(TileSource tileSource, TileEventSource tileEventSource) {
        this.tileSource = tileSource;
        this.tileEventSource = tileEventSource;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    byte[] loadTileInBuffer(URLConnection urlConn) throws IOException {
        try (InputStream input = urlConn.getInputStream()) {
            ByteArrayOutputStream bout = new ByteArrayOutputStream(input.available());
            byte[] buffer = new byte[2048];
            int read;
            while ((read = input.read(buffer)) > 0) {
                bout.write(buffer, 0, read);
            }
            return bout.toByteArray();
        }
    }


    Runnable createLoaderJob(Tile tile) {
        return () -> {
            final String url = tileSource.getUrl(tile);
            final File tileCacheFile = tile.getCacheFile();

            final long start = System.currentTimeMillis();
            LOGGER.debug("{} start loading from {}", tile, url);
            try {
                HttpURLConnection urlConn = (HttpURLConnection) new URL(url).openConnection();
                urlConn.setRequestProperty("Accept", "text/html, image/png, image/jpeg, image/gif, */*");
                if (tileCacheFile.exists()) {
                    urlConn.setIfModifiedSince(tileCacheFile.lastModified());
                }
                urlConn.setReadTimeout(10000);
                urlConn.setConnectTimeout(5000);

                long time = new Date().getTime();

                if (urlConn.getResponseCode() == 304) {
                    LOGGER.debug("{} not modified", tile);
                    tile.loadImage(new FileInputStream(tileCacheFile));
                    if (!tileCacheFile.setLastModified(time)) {
                        LOGGER.debug("unable to set file mtime for {} / {}", tileCacheFile, time);

                    }
                } else if (urlConn.getHeaderField("X-VE-Tile-Info") != null) {
                    LOGGER.debug("{} no tile at this zoom level", tile);
                    tile.setError();
                    saveTileToFile(tileCacheFile, new byte[0], 0);
                } else {
                    for (int i = 0; i < 5; ++i) {
                        if (urlConn.getResponseCode() != 200) {
                            LOGGER.debug("{} unexpected response-status={}", tile, urlConn.getResponseCode());
                            Thread.sleep(5000 + (new Random()).nextInt(5000));
                        } else {
                            LOGGER.debug("{} succeeded loading in {}ms", tile, System.currentTimeMillis() - start);

                            byte[] buffer = loadTileInBuffer(urlConn);
                            tile.loadImage(new ByteArrayInputStream(buffer));
                            saveTileToFile(tileCacheFile, buffer, time);
                            return;
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.debug(e.getMessage(), e);
                tile.setError();
            } finally {
                tileEventSource.tileLoadingFinished(tile);
            }
        };
    }

    void saveTileToFile(File file, byte[] buffer, long time) {
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(buffer);
        } catch (IOException e) {
            LOGGER.debug(e.getMessage(), e);
        }
        if (!file.setLastModified(time)) {
            LOGGER.debug("unable to set file mtime for {} / {}", file, time);
        }
    }
}
