package mapapp;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.lang.System.getenv;

/**
 * @author tbaum
 * @since 02.08.2014
 */
public class VirbSource {
    private static final Pattern dirPattern = Pattern.compile("^........-....-....-....-............(\\..*)?");
    public final double playbackFactor;
    private final File file;
    private final String gpx;
    private final String movie;
    private final Date creationDate;
    private final double length;
    Image preview;

    public VirbSource(File file, String gpx, String movie, Date creationDate, double length, double playbackFactor) {
        this.file = file;
        this.gpx = gpx;
        this.movie = movie;
        this.creationDate = creationDate;
        this.length = length;
        this.playbackFactor = playbackFactor;
    }

    public static Stream<VirbSource> readVirbSources() throws SAXException, IOException {
        return readVirbSources(new File(getenv("HOME"), "/Library/Application Support/Garmin/VIRB Edit/Database"));
    }

    public static Stream<VirbSource> readVirbSources(File f) throws SAXException, IOException {
        List<File> files = new ArrayList<>();
        File[] databases = f.listFiles();
        if (databases != null) for (File database : databases) {
            File rawMoviesDir = new File(database, "RawMovies");
            File[] rawMovies = rawMoviesDir.listFiles((dir, name) -> dirPattern.matcher(name).matches());
            if (rawMovies != null) Collections.addAll(files, rawMovies);
        }

        return files.stream()
//                .filter((v) -> v.getName().startsWith("77282A09-2E0D")
//                        || v.getName().startsWith("E08A0F68")
//                        || v.getName().startsWith("270D0C21")
//                )
                .map((rawMovie) -> {
                            try (FileReader reader = new FileReader(new File(rawMovie, "movie.plist"))) {
                                InputSource inputSource = new InputSource(reader);
                                PlistContentHandler handler = new PlistContentHandler();
                                XMLReader xmlReader = XMLReaderFactory.createXMLReader();
                                xmlReader.setContentHandler(handler);
                                xmlReader.parse(inputSource);

                                if (handler.gpx != null && handler.movie != null) {
                                    return new VirbSource(rawMovie, handler.gpx, handler.movie, handler.creationDate,
                                            handler.playbackFrameRate * handler.movieTimeValue /
                                                    (handler.recordedFrameRate * handler.movieTimeScale),
                                            handler.playbackFrameRate / handler.recordedFrameRate);
                                }
                            } catch (IOException | SAXException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }
                ).filter((v) -> v != null);
    }

    public double getLength() {
        return length;
    }

    public File getFile() {
        return file;
    }

    public String getGpx() {
        return gpx;
    }

    public String getMovie() {
        return movie;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    @Override public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
