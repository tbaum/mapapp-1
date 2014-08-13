package mapapp;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static org.joda.time.format.ISODateTimeFormat.dateTimeNoMillis;

/**
 * @author tbaum
 * @since 21.07.2014
 */
public class GpxContentHandler extends ContentHandlerAdapter {

    Track trk = null;
    private TrackPoint tp = null;

    static GpxContentHandler parseGpx(File gpxFile) throws IOException, SAXException {

        XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        FileReader reader = new FileReader(gpxFile);
        InputSource inputSource = new InputSource(reader);
        GpxContentHandler handler = new GpxContentHandler();
        xmlReader.setContentHandler(handler);
        xmlReader.parse(inputSource);
        return handler;
    }

    public void startElement(String uri, String localName, String qName, Attributes atts) {
        if ("trk".equals(localName)) trk = new Track();
        if ("link".equals(localName) && trk != null && tp == null) trk.link = atts.getValue("href");
        if ("trkpt".equals(localName)) tp = new TrackPoint(atts);
//        if ("accel".equals(localName)) tp.addAccel(atts);
    }

    public void endElement(String uri, String localName, String qName) {
        if ("time".equals(localName) && tp != null)
            tp.time = dateTimeNoMillis().parseDateTime(currentValue).getMillis();

        if ("trkpt".equals(localName)) trk.add(tp);
    }
}
