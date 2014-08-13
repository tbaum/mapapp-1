package mapapp;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.openstreetmap.gui.jmapviewer.tilesources.Lyrk;
import org.openstreetmap.gui.jmapviewer.tilesources.TileSource;

import java.io.File;
import java.util.prefs.Preferences;

/**
 * @author tbaum
 * @since 04.08.2014
 */
public class Main {

    public static final String VIDEO_STORE = "/Volumes/VIBR/Garmin VIRB";
    public static final String VIDEO_CACHE = "/Volumes/VIBR/Cache";
    public static final String LYRK_API_KEY = "20007e16b609415ba6fcff7a38437e17";
    public static final String OSM_CACHE = "/Volumes/VIBR/OSM-Cache";

    public static void main(String[] args) throws Exception {
//        Preferences mapApp = Preferences.userRoot().node("MapApp");
//        mapApp.get("videoStore");
        try {
            Injector injector = Guice.createInjector(
                    new AbstractModule() {
                        @Override protected void configure() {
                            bind(TileSource.class).toInstance(new Lyrk(LYRK_API_KEY));
                            bind(File.class).annotatedWith(CacheDir.class).toInstance(new File(OSM_CACHE));
                            bind(File.class).annotatedWith(ExportVideoStore.class).toInstance(new File(VIDEO_CACHE));
                            bind(File.class).annotatedWith(VirbVideoStore.class).toInstance(new File(VIDEO_STORE));
                        }
                    }
            );
            injector.getInstance(AppFrame.class).setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
