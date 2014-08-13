// License: GPL. For details, see Readme.txt file.
package org.openstreetmap.gui.jmapviewer;

import org.openstreetmap.gui.jmapviewer.objects.*;
import org.openstreetmap.gui.jmapviewer.tiles.Tile;
import org.openstreetmap.gui.jmapviewer.tiles.TileController;
import org.openstreetmap.gui.jmapviewer.tiles.TileLoaderListener;
import org.openstreetmap.gui.jmapviewer.tilesources.TileSource;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;
import static java.lang.Math.max;
import static javax.swing.SwingConstants.VERTICAL;
import static org.openstreetmap.gui.jmapviewer.OsmMercator.*;

/**
 * Provides a simple panel that displays pre-rendered map tiles loaded from the
 * OpenStreetMap project.
 *
 * @author Jan Peter Stotz
 * @author Jason Huntley
 */
public class JMapViewer extends JPanel implements TileLoaderListener {

    public static final int MAX_ZOOM = 22;
    public static final int MIN_ZOOM = 0;
    private static final Point[] move = {new Point(1, 0), new Point(0, 1), new Point(-1, 0), new Point(0, -1)};
    private final List<MapObject> mapObject = new ArrayList<>();
    private final TileController tileController;
    private final JSlider zoomSlider;
    private final TileSource tileSource;
    private final AttributionSupport attribution;
    private final JButton zoomInButton, zoomOutButton;
    private int zoom = 0;
    private Point center = new Point();

    @Inject
    public JMapViewer(TileSource tileSource, TileController tileController, AttributionSupport attribution) {
        super(null);
        this.attribution = attribution;
        this.tileSource = tileSource;
        this.tileController = tileController;

        setMinimumSize(new Dimension(TILE_SIZE, TILE_SIZE));
        setPreferredSize(new Dimension(400, 400));

        add(zoomSlider = createZoomSlider());
        add(zoomInButton = createZoomButton("/images/plus.png", 4, e -> setZoom(zoom + 1, getCenter())));
        add(zoomOutButton = createZoomButton("/images/minus.png", 26, e -> setZoom(zoom - 1, getCenter())));

        tileController.addListener(this);
    }

    public MapMarkerDot getMapMarkerNearTo(Point p) {
        Optional<MapMarkerDot> first = mapObject.stream()
                .filter(o -> o instanceof MapMarkerDot)
                .map(o -> (MapMarkerDot) o)
                .filter(o -> {
                    Point mapPosition = getMapPosition(o.getLat(), o.getLon(), true);
                    return o.isHit(p, mapPosition, zoom, center.y + getHeight() / 2);
                }).findFirst();
        return first.isPresent() ? first.get() : null;
    }

    private JButton createZoomButton(String image, int ofsX, ActionListener l) {
        JButton button = new JButton(new ImageIcon(JMapViewer.class.getResource(image)));
        button.setBounds(ofsX, 155, 18, 18);
        button.setFocusable(false);
        button.addActionListener(l);
        return button;
    }

    private JSlider createZoomSlider() {
        JSlider zoomSlider = new JSlider(VERTICAL, MIN_ZOOM, tileSource.getMaxZoom(), 1);
        zoomSlider.setBounds(10, 10, 30, 150);
        zoomSlider.setOpaque(false);
        zoomSlider.addChangeListener(e -> setZoom(zoomSlider.getValue(), getCenter()));
        zoomSlider.setFocusable(false);
        return zoomSlider;
    }

    public void setDisplayPosition(Point mapPoint, int x, int y, int zoom) {
        if (zoom > tileSource.getMaxZoom() || zoom < MIN_ZOOM)
            return;

        // Get the plain tile number
        Point p = new Point();
        p.x = x - mapPoint.x + getWidth() / 2;
        p.y = y - mapPoint.y + getHeight() / 2;
        center = p;
        setIgnoreRepaint(true);
        try {
            this.zoom = zoom;
            zoomSlider.setValue(zoom);
        } finally {
            setIgnoreRepaint(false);
            repaint();
        }
        updateZoom();
    }

    /**
     * Sets the displayed map pane and zoom level so that all chosen map elements are
     * visible.
     */
    public void setDisplayToFitMapElements() {
        if (mapObject.isEmpty()) return;
        int x_max = MIN_VALUE, x_min = MAX_VALUE, y_max = MIN_VALUE, y_min = MAX_VALUE;
        int mapZoomMax = tileSource.getMaxZoom();

        for (MapObject object : mapObject) {

            if (object instanceof MapMarkerCircle) {
                MapMarkerCircle marker = (MapMarkerCircle) object;
                if (marker.isVisible()) {
                    int x = (int) OsmMercator.LonToX(marker.getLon(), mapZoomMax);
                    int y = (int) OsmMercator.LatToY(marker.getLat(), mapZoomMax);
                    x_max = Math.max(x_max, x);
                    y_max = Math.max(y_max, y);
                    x_min = Math.min(x_min, x);
                    y_min = Math.min(y_min, y);
                }
            }

            if (object instanceof MapRectangle) {
                MapRectangle rectangle = (MapRectangle) object;
                if (rectangle.isVisible()) {
                    x_max = Math.max(x_max, (int) OsmMercator.LonToX(rectangle.getBottomRight().getLon(), mapZoomMax));
                    y_max = Math.max(y_max, (int) OsmMercator.LatToY(rectangle.getTopLeft().getLat(), mapZoomMax));
                    x_min = Math.min(x_min, (int) OsmMercator.LonToX(rectangle.getTopLeft().getLon(), mapZoomMax));
                    y_min = Math.min(y_min, (int) OsmMercator.LatToY(rectangle.getBottomRight().getLat(), mapZoomMax));
                }
            }

            if (object instanceof MapPolygon) {
                MapPolygon polygon = (MapPolygon) object;
                if (polygon.isVisible()) {
                    for (Coordinate c : polygon.getPoints()) {
                        int x = (int) OsmMercator.LonToX(c.getLon(), mapZoomMax);
                        int y = (int) OsmMercator.LatToY(c.getLat(), mapZoomMax);
                        x_max = Math.max(x_max, x);
                        y_max = Math.max(y_max, y);
                        x_min = Math.min(x_min, x);
                        y_min = Math.min(y_min, y);
                    }
                }
            }
        }

        int height = max(0, getHeight());
        int width = max(0, getWidth());
        int newZoom = mapZoomMax;
        int x = x_max - x_min;
        int y = y_max - y_min;
        while (x > width || y > height) {
            newZoom--;
            x >>= 1;
            y >>= 1;
        }
        x = x_min + (x_max - x_min) / 2;
        y = y_min + (y_max - y_min) / 2;
        int z = 1 << (mapZoomMax - newZoom);
        x /= z;
        y /= z;

        setDisplayPosition(getCenter(), x, y, newZoom);
    }

    /**
     * Converts the relative pixel coordinate (regarding the top left corner of
     * the displayed map) into a latitude / longitude coordinate
     *
     * @param mapPoint relative pixel coordinate regarding the top left corner of the displayed map
     * @return latitude / longitude
     */
    public Coordinate getPosition(Point mapPoint) {
        int x = center.x + mapPoint.x - getWidth() / 2;
        int y = center.y + mapPoint.y - getHeight() / 2;
        double lon = OsmMercator.XToLon(x, zoom);
        double lat = OsmMercator.YToLat(y, zoom);
        return new CoordinateImpl(lat, lon);
    }

    /**
     * Calculates the position on the map of a given coordinate
     *
     * @return point on the map or <code>null</code> if the point is not visible and checkOutside set to <code>true</code>
     */
    public Point getMapPosition(double lat, double lon, boolean checkOutside) {
        int x = (int) LonToX(lon, zoom);
        int y = (int) LatToY(lat, zoom);
        x -= center.x - getWidth() / 2;
        y -= center.y - getHeight() / 2;
        if (checkOutside) {
            if (x < 0 || y < 0 || x > getWidth() || y > getHeight())
                return null;
        }
        return new Point(x, y);
    }


    /**
     * Calculates the position on the map of a given coordinate
     *
     * @return point on the map or <code>null</code> if the point is not visible and checkOutside set to <code>true</code>
     */
    public Point getMapPosition(Coordinate coord) {
        if (coord != null)
            return getMapPosition(coord.getLat(), coord.getLon(), false);
        else
            return null;
    }

    public void clearMap() {
        mapObject.clear();
    }

    protected Point getCenter() {
        return new Point(getWidth() / 2, getHeight() / 2);
    }

    @Override
    protected void paintComponent(Graphics g1) {
        super.paintComponent(g1);
        Graphics2D g = (Graphics2D) g1;


        int tilesize = TILE_SIZE;
        int tilex = center.x / tilesize;
        int tiley = center.y / tilesize;
        int off_x = (center.x % tilesize);
        int off_y = (center.y % tilesize);

        int w2 = getWidth() / 2;
        int h2 = getHeight() / 2;
        int posx = w2 - off_x;
        int posy = h2 - off_y;

        boolean start_left = off_x < tilesize - off_x;
        boolean start_top = off_y < tilesize - off_y;
        int iMove = start_top
                ? start_left ? 2 : 3
                : start_left ? 1 : 0;

        // calculate the visibility borders

        int x_min = -tilesize;
        int y_min = -tilesize;
        int x_max = getWidth();
        int y_max = getHeight();

        // paint the tiles in a spiral, starting from center of the map
        boolean painted = true;
        int x = 0;
        while (painted) {
            painted = false;
            for (int i = 0; i < 4; i++) {
                if (i % 2 == 0) {
                    x++;
                }
                for (int j = 0; j < x; j++) {
                    if (x_min <= posx && posx <= x_max && y_min <= posy && posy <= y_max) {
                        // tile is visible
                        Tile tile = tileController.getTile(tilex, tiley, zoom);
                        if (tile != null && tile.getImage() != null) {
                            g.drawImage(tile.getImage(), posx, posy, null);
                        }
                        painted = true;
                    }
                    Point p = move[iMove];
                    posx += p.x * tilesize;
                    posy += p.y * tilesize;
                    tilex += p.x;
                    tiley += p.y;
                }
                iMove = (iMove + 1) % move.length;
            }
        }
        // outer border of the map
        int mapSize = tilesize << zoom;
        g.drawRect(w2 - center.x, h2 - center.y, mapSize, mapSize);

        mapObject.stream()
                .filter(MapObject::isVisible)
                .sorted((a, b) -> ((Boolean) a.isMarked()).compareTo(b.isMarked()))
                .forEachOrdered(object -> {
                    if (object instanceof MapPolygon) paintMapPolygon(g, (MapPolygon) object);
                    if (object instanceof MapRectangle) paintMapRectangle(g, (MapRectangle) object);
                    if (object instanceof MapMarkerCircle) paintMapMarker(g, (MapMarkerCircle) object);
                });

        attribution.paintAttribution(g, getWidth(), getHeight());
    }


    private void paintMapMarker(Graphics2D g, MapMarkerCircle marker) {
        Point p = getMapPosition(marker.getLat(), marker.getLon(), true);
        if (p != null) {
            Integer radius = marker.getRadius(p, zoom, center.y + getHeight() / 2);
            marker.paint(g, p, radius);
        }
    }

    private void paintMapRectangle(Graphics2D g, MapRectangle rectangle) {
        Coordinate topLeft = rectangle.getTopLeft();
        Coordinate bottomRight = rectangle.getBottomRight();
        if (topLeft != null && bottomRight != null) {
            Point pTopLeft = getMapPosition(topLeft);
            Point pBottomRight = getMapPosition(bottomRight);
            if (pTopLeft != null && pBottomRight != null) {
                rectangle.paint(g, pTopLeft, pBottomRight);
            }
        }
    }

    private void paintMapPolygon(Graphics2D g, MapPolygon polygon) {
        List<? extends Coordinate> coords = polygon.getPoints();
        if (coords != null && coords.size() >= 3) {
            List<Point> points = new LinkedList<>();
            for (Coordinate c : coords) {
                Point p = getMapPosition(c);
                if (p == null) {
                    return;
                }
                points.add(p);
            }
            polygon.paint(g, points);
        }
    }

    /**
     * Moves the visible map pane.
     *
     * @param x horizontal movement in pixel.
     * @param y vertical movement in pixel
     */
    public void moveMap(int x, int y) {
        center.x += x;
        center.y += y;
        repaint();
    }

    /**
     * @return the current zoom level
     */
    public int getZoom() {
        return zoom;
    }

    /**
     * Set the zoom level and center point for display
     *
     * @param zoom     new zoom level
     * @param mapPoint point to choose as center for new zoom level
     */
    public void setZoom(int zoom, Point mapPoint) {
        if (zoom > tileSource.getMaxZoom() || zoom < 0 || zoom == this.zoom)
            return;
        Coordinate zoomPos = getPosition(mapPoint);

        int x = (int) LonToX(zoomPos.getLon(), zoom);
        int y = (int) LatToY(zoomPos.getLat(), zoom);
        setDisplayPosition(mapPoint, x, y, zoom);

        updateZoom();
    }

    public void addMapObject(MapObject marker) {
        mapObject.add(marker);
    }

    public void loadingFinished(Tile tile) {
        repaint();
    }

    public AttributionSupport getAttribution() {
        return attribution;
    }

    private void updateZoom() {
        zoomSlider.setToolTipText("Zoom level " + zoom);
        zoomInButton.setToolTipText("Zoom to level " + (zoom + 1));
        zoomOutButton.setToolTipText("Zoom to level " + (zoom - 1));
        zoomOutButton.setEnabled(zoom > 0);
        zoomInButton.setEnabled(zoom < tileSource.getMaxZoom());
    }

}
