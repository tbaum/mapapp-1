package mapapp;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.xml.sax.Attributes;

import java.util.Locale;

import static java.lang.Double.isNaN;
import static java.lang.Double.parseDouble;
import static java.lang.Math.*;

/**
 * @author tbaum
 * @since 01.08.2014
 */
public class TrackPoint implements Comparable<TrackPoint> {

    final double lat, lon;
    Double dist, ankle;
    long time;
    boolean selected;

    public TrackPoint(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public TrackPoint(Attributes atts) {
        this(parseDouble(atts.getValue("lat")), parseDouble(atts.getValue("lon")));
    }

    public double distance(TrackPoint to) {
        double v = 6378137.0d * acos(sin(toRadians(lat)) * sin(toRadians(to.lat))
                + cos(toRadians(lat)) * cos(toRadians(to.lat)) * cos(toRadians(to.lon - lon)));
        return isNaN(v) ? 0 : v;
    }

    double ankle(TrackPoint to) {
        double v = toDegrees(atan2(
                toRadians(to.lon - lon),
                log(tan(toRadians(to.lat) / 2 + PI / 4) / tan(toRadians(lat) / 2 + PI / 4))
        )) - 180;

        if (v < 0) v += 360;
        if (v > 360) v -= 360;

        return v;
    }

    @Override public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }


    @Override public int compareTo(TrackPoint o) {
        return ((Long) time).compareTo(o.time);
    }

    public boolean isSelected() {
        return selected;
    }

    String toJs() {
        return "  new OpenLayers.Feature.Vector(" +
                "new OpenLayers.Geometry.Point(" + lon + "," + lat + ").transform('EPSG:4326', 'EPSG:3857'), " +
                "{time:" + time + "" +
                (dist != null ? (String.format(Locale.US, ",dist:%.3f, ankl:%.0f", dist, ankle)) : "") + "})";
    }

//    public Map<Integer, Accel> tmpAccel = new TreeMap<>();
//    public Accel accel = null;
//
//    public void finAccel() {
//        accel = tmpAccel.values().stream().reduce(new GpxContentHandler.Accel(0, 0, 0), GpxContentHandler.Accel::add);
//        for (Map.Entry<Integer, GpxContentHandler.Accel> integerEntry : tmpAccel.entrySet()) {
//            System.err.println(integerEntry.getKey() + "\t" + integerEntry.getValue());
//        }
//        System.err.println("");
//    }
//
//    void addAccel(Attributes atts) {
//        tmpAccel.put(parseInt(atts.getValue("offset")), new Accel(atts));
//    }
//
//    public class Accel {
//        private final float x, y, z;
//
//        private Accel(float x, float y, float z) {
//            this.x = x;
//            this.y = y;
//            this.z = z;
//        }
//
//        private Accel(Attributes atts) {
//            this(parseFloat(atts.getValue("x")), parseFloat(atts.getValue("y")), parseFloat(atts.getValue("z")));
//        }
//
//        public Accel add(Accel o) {
//            return new Accel(x + o.x, y + o.y, z + o.z);
//        }
//
//        @Override public String toString() {
//            return String.format("%6.4f\t%6.4f\t%6.4f\t\t%5.4f\t%4.0f\t%4.0f\t%4.0f",
//                    x, y, z,
//                    sqrt(x * x + y * y + z * z),
//                    x != 0 ? atan(z / x) / PI * 180 : Float.NaN,
//                    y != 0 ? atan(z / y) / PI * 180 : Float.NaN,
//                    y != 0 ? atan(x / y) / PI * 180 : Float.NaN);
//        }
//    }
}
