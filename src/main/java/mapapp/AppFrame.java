package mapapp;

import org.openstreetmap.gui.jmapviewer.JMapViewer;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.awt.BorderLayout.*;
import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.NORTHEAST;
import static java.awt.event.MouseEvent.BUTTON1;
import static javax.swing.SwingUtilities.invokeLater;
import static org.openstreetmap.gui.jmapviewer.OsmMercator.LatToY;
import static org.openstreetmap.gui.jmapviewer.OsmMercator.LonToX;

/**
 * @author tbaum
 * @since 04.08.2014
 */
public class AppFrame extends JFrame {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final JButton exportButton = new JButton("Export Selected Tracks");
    private final List<MyMapMarkerDot> selection = new ArrayList<>();
    private final PreviewPanel previewPanel;
    private final JMapViewer mapViewer;
    private MyMapMarkerDot activeMarker = null;

    @Inject
    public AppFrame(JMapViewer mapViewer, PreviewPanel previewPanel, Exporter exporter) {
        this.mapViewer = mapViewer;
        this.previewPanel = previewPanel;
        setSize(1700, 1000);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);


        VirbSourceListPanel virbList = new VirbSourceListPanel();

        virbList.addListSelectionListener((listModel) -> {
            if (listModel.getValueIsAdjusting()) {
                return;
            }

            selection.clear();
            mapViewer.clearMap();

            virbList.getSelectedPoints().forEach((t) -> {
                mapViewer.addMapObject(t);
                selection.add(t);
            });
            mapViewer.setDisplayToFitMapElements();
            mapViewer.updateUI();
            exportButton.setEnabled(!selection.isEmpty());
        });


        mapViewer.addMouseMotionListener(new MouseAdapter() {

            @Override
            public void mouseMoved(MouseEvent e) {
                MyMapMarkerDot first = (MyMapMarkerDot) mapViewer.getMapMarkerNearTo(e.getPoint());

                selectMapPoint(first);
            }
        });

        mapViewer.addMouseWheelListener(e -> mapViewer.setZoom(mapViewer.getZoom() - e.getWheelRotation(), e.getPoint()));


        mapViewer.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getButton() == BUTTON1) {
                    if (mapViewer.getAttribution().handleAttribution(e.getPoint(), true)) {
                        return;
                    }

                    if (e.getClickCount() > 1) {
                        mapViewer.setZoom(mapViewer.getZoom() + 1, e.getPoint());
                    }
                }
            }

            @Override public void mouseMoved(MouseEvent e) {
                boolean handCursor = mapViewer.getAttribution().handleAttribution(e.getPoint(), false);
                mapViewer.setCursor(new Cursor(handCursor ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        });

        MapMouseMoveListener mapMouseMoveListener = new MapMouseMoveListener() {
            @Override public void moveMap(int x, int y) {
                mapViewer.moveMap(x, y);
            }
        };
        mapViewer.addMouseListener(mapMouseMoveListener);
        mapViewer.addMouseMotionListener(mapMouseMoveListener);

        mapViewer.setDisplayPosition(new Point(0, 0), (int) LonToX(13.74, 10), (int) LatToY(51.0, 10), 10);

        exportButton.setEnabled(false);
        exportButton.addActionListener((x) -> executor.submit(exporter.export(selection, this::selectMapPoint)));
        previewPanel.add(exportButton, new GridBagConstraints(0, 4, 2, 1, 1, 0, NORTHEAST, NONE, new Insets(2, 2, 2, 2), 0, 0));

        setExtendedState(MAXIMIZED_BOTH);

        JPanel panelRight = new JPanel(new BorderLayout());
        panelRight.add(virbList, CENTER);
        panelRight.add(previewPanel, SOUTH);

        add(panelRight, EAST);
        add(mapViewer, CENTER);
    }

    public void selectMapPoint(MyMapMarkerDot marker) {
        if (marker != null) {
            if (activeMarker != marker) {
                if (activeMarker != null) {
                    activeMarker.setMarked(false);
                }
                activeMarker = marker;
                activeMarker.setMarked(true);

                invokeLater(() -> {
                    previewPanel.setPreviewImage(activeMarker);
                    mapViewer.updateUI();
                });
            }
        }
    }

}
