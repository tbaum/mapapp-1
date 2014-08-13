package mapapp;

import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import static javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION;
import static mapapp.GpxContentHandler.parseGpx;
import static mapapp.VirbSource.readVirbSources;

/**
 * @author tbaum
 * @since 12.08.2014
 */
class VirbSourceListPanel extends JPanel {

    private final Comparator<VirbSource> comparator = (a, b) -> a.getCreationDate().compareTo(b.getCreationDate());
    private final SortedListModel<VirbSource> model = new SortedListModel<>(comparator);
    private final JList<VirbSource> list = new JList<>(model);
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    VirbSourceListPanel() {
        super(new BorderLayout());
        setBorder(new EtchedBorder());
        list.setCellRenderer(new VirbListCellRenderer());
        list.setSelectionMode(MULTIPLE_INTERVAL_SELECTION);
        add(new JLabel("Videos from VIRB-Edit"), BorderLayout.NORTH);
        add(new JScrollPane(list), BorderLayout.CENTER);

        load();
    }

    private Future<Integer> load() {
        return executorService.submit(() -> {
            try {
                model.clear();
                readVirbSources()
                        .map((v) -> {
                            try {
                                BufferedImage read = ImageIO.read(new File(v.getFile(), "thumb.png"));
                                int w = 200;
                                int h = w * read.getHeight(null) / read.getWidth(null);
                                v.preview = read.getScaledInstance(w, h, Image.SCALE_FAST);
                            } catch (IOException ignored) {
                            }
                            return v;
                        })
                        .forEachOrdered(model::addElement);
                return model.getSize();
            } catch (IOException | SAXException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    public Stream<MyMapMarkerDot> getSelectedPoints() {
        return list.getSelectedValuesList().stream().flatMap(source -> {
            try {
                Track trk = parseGpx(new File(source.getFile(), source.getGpx())).trk;
                return trk.selected().map((t) -> new MyMapMarkerDot(source, trk, t));
            } catch (IOException | SAXException e) {
                e.printStackTrace();
                return null;
            }
        });
    }


    public void addListSelectionListener(ListSelectionListener listener) {
        list.addListSelectionListener(listener);
    }

    public Collection<VirbSource> getSelectedValuesList() {
        return list.getSelectedValuesList();
    }
}
