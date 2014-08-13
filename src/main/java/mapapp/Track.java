package mapapp;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author tbaum
 * @since 01.08.2014
 */
public class Track {
    public String name;
    public String link;
    public SortedSet<TrackPoint> tps = new TreeSet<>();

    public void add(TrackPoint tp) {
        tps.add(tp);
    }

    void filterDistance(double threshold) {
        if (tps.isEmpty()) return;

        DistanceState state = new DistanceState(threshold, tps.first());
        tps.stream().forEachOrdered(state::updateDist);
    }

    public String dumpJs() {
        filterDistance(20);
        return "overlay.addFeatures([\n" + tps.stream()
                .filter(TrackPoint::isSelected)
                .map(TrackPoint::toJs)
                .collect(Collectors.joining(",\n")) + " ]);";
    }

    public Stream<TrackPoint> selected() {
        filterDistance(20);
        return tps.stream().filter(TrackPoint::isSelected);
    }

    private static class DistanceState {
        private final double threshold;
        TrackPoint last;
        double dst = 0.0;

        DistanceState(double threshold, TrackPoint first) {
            this.threshold = threshold;
            this.last = first;
        }

        //TODO interpolate coordinates
        private void updateDist(TrackPoint trackPoint) {
            trackPoint.dist = trackPoint.distance(last);
            trackPoint.ankle = trackPoint.ankle(last);
            dst += trackPoint.dist;

            trackPoint.selected = (dst) > threshold;
//            System.err.println(trackPoint.lat + "; " + trackPoint.lon + "; dist:" + trackPoint.dist + " " + dst);
            if (trackPoint.selected) dst = 0.0;

            if (trackPoint.dist > 0)
                last = trackPoint;
        }
    }
}
