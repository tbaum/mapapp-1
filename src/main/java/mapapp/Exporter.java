package mapapp;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author tbaum
 * @since 12.08.2014
 */
public class Exporter {
    private final Provider<VideoGrabber> videoGrabberProvider;

    @Inject
    public Exporter(Provider<VideoGrabber> videoGrabberProvider) {
        this.videoGrabberProvider = videoGrabberProvider;
    }

    public Runnable export(List<MyMapMarkerDot> selection, Consumer<MyMapMarkerDot> notify) {
        List<MyMapMarkerDot> markers = new ArrayList<>(selection);
        return () -> {
            try (VideoGrabber grabber = videoGrabberProvider.get()) {
                long lastUpdate = 0;
                for (MyMapMarkerDot myMapMarkerDot : markers) {
                    grabber.grabVideo(myMapMarkerDot);
                    if (System.currentTimeMillis() - lastUpdate > 100) {
                        notify.accept(myMapMarkerDot);
                        lastUpdate = System.currentTimeMillis();
                    }
                }
            }

        };
    }
}
