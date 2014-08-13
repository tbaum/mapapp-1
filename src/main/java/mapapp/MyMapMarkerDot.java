package mapapp;

import org.openstreetmap.gui.jmapviewer.objects.CoordinateImpl;
import org.openstreetmap.gui.jmapviewer.objects.MapMarkerDot;

import java.awt.*;

/**
 * @author tbaum
 * @since 12.08.2014
 */
public class MyMapMarkerDot extends MapMarkerDot {
    final VirbSource source;
    final Track track;
    final TrackPoint point;

    public MyMapMarkerDot(VirbSource source, Track track, TrackPoint point) {
        super(new CoordinateImpl(point.lat, point.lon));
        this.source = source;
        this.track = track;
        this.point = point;
    }

    @Override public void setMarked(boolean marked) {
        super.setMarked(marked);
        setBackColor(marked ? Color.RED : Color.YELLOW);
    }
}
