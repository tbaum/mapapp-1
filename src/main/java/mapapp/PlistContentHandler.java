package mapapp;

import org.xml.sax.Attributes;

import java.util.Date;
import java.util.LinkedList;

import static java.util.Arrays.asList;
import static org.joda.time.format.ISODateTimeFormat.dateTimeNoMillis;

/**
 * @author tbaum
 * @since 21.07.2014
 */
public class PlistContentHandler extends ContentHandlerAdapter {

    private final LinkedList<String> keyPath = new LinkedList<>();

    String movie, gpx;
    Date creationDate;
    Long movieTimeScale, movieTimeValue;
    Double playbackFrameRate, recordedFrameRate, trackTimeOffset;

    @Override public void startElement(String uri, String localName, String qName, Attributes atts) {
        if (localName.equals("dict")) {
            keyPath.add(null);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if ("dict".equals(localName)) {
            keyPath.removeLast();
        }

        if ("key".equals(localName)) {
            keyPath.removeLast();
            keyPath.add(currentValue);
        }

        if ("string".equals(localName)) {
            if (asList("MovieFilename").equals(keyPath)) movie = currentValue;
            if (asList("VirbGpxFilePath").equals(keyPath)) gpx = currentValue;
        }
        if ("date".equals(localName)) {
            if ("CreationDate".equals(keyPath.getLast()))
                creationDate = dateTimeNoMillis().parseDateTime(currentValue).toDate();
        }

        if ("integer".equals(localName)) {
            if (asList("MovieDuration", "MovieTimeScale").equals(keyPath))
                movieTimeScale = Long.parseLong(currentValue);
            if (asList("MovieDuration", "MovieTimeValue").equals(keyPath))
                movieTimeValue = Long.parseLong(currentValue);

        }
        if ("real".equals(localName)) {
            if (asList("PlaybackFrameRate").equals(keyPath)) playbackFrameRate = Double.parseDouble(currentValue);
            if (asList("RecordedFrameRate").equals(keyPath)) recordedFrameRate = Double.parseDouble(currentValue);
            if (asList("TrackTimeOffset").equals(keyPath)) trackTimeOffset = Double.parseDouble(currentValue);
        }
    }
}
