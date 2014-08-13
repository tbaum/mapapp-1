package mapapp;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.awt.GridBagConstraints.*;
import static java.lang.String.format;
import static java.util.Locale.ENGLISH;

/**
 * @author tbaum
 * @since 12.08.2014
 */
class PreviewPanel extends JPanel {
    private final JLabel previewImage = new JLabel();
    private final JLabel previewImageInfo1 = new JLabel();
    private final JLabel previewImageInfo2 = new JLabel();
    private final JLabel previewImageInfo3 = new JLabel();
    private final VideoGrabber previewGrabber;// = new VideoGrabber(new File(Main.VIDEO_STORE), new File(Main.VIDEO_CACHE));
    private final JLabel previewImageInfo0 = new JLabel();

    @Inject
    public PreviewPanel(VideoGrabber previewGrabber) {
        super(new GridBagLayout());
        setBorder(new EtchedBorder());
        this.previewGrabber = previewGrabber;

        add(new JLabel("Preview"), new GridBagConstraints(0, 0, 2, 1, 0, 0, NORTHEAST, HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));

        add(previewImage, new GridBagConstraints(0, 1, 2, 1, 0, 0, CENTER, BOTH, new Insets(2, 2, 2, 2), 0, 0));
        add(new Label("File"), new GridBagConstraints(0, 2, 1, 1, 1, 0, NORTHEAST, HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
        add(new Label("Position"), new GridBagConstraints(0, 3, 1, 1, 1, 0, NORTHEAST, HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
        add(new Label("Direction"), new GridBagConstraints(0, 4, 1, 1, 1, 0, NORTHEAST, HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
        add(new Label("Date"), new GridBagConstraints(0, 5, 1, 1, 1, 0, NORTHEAST, HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));

        add(previewImageInfo0, new GridBagConstraints(1, 2, 1, 1, 5, 0, NORTHEAST, HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
        add(previewImageInfo1, new GridBagConstraints(1, 3, 1, 1, 5, 0, NORTHEAST, HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
        add(previewImageInfo2, new GridBagConstraints(1, 4, 1, 1, 5, 0, NORTHEAST, HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
        add(previewImageInfo3, new GridBagConstraints(1, 5, 1, 1, 5, 0, NORTHEAST, HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
    }

    void setPreviewImage(MyMapMarkerDot l) {

        BufferedImage bufferedImage = previewGrabber.grabVideo(l);

        if (bufferedImage == null) {
            previewImage.setIcon(new ImageIcon());
        } else {
            Image scaledInstance = bufferedImage.getScaledInstance(300, 300 * bufferedImage.getHeight() / bufferedImage.getWidth(), Image.SCALE_FAST);
            ImageIcon icon = new ImageIcon(scaledInstance);
            previewImage.setIcon(icon);
        }

        previewImageInfo0.setText(l.source.getMovie());
        previewImageInfo1.setText(format(ENGLISH, "lat:%2.7f lon:%2.7f", l.point.lat, l.point.lon));
        previewImageInfo2.setText(format(ENGLISH, "%3.0f", l.point.ankle));
        previewImageInfo3.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(l.point.time)));
    }


}
