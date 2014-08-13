package org.openstreetmap.gui.jmapviewer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author tbaum
 * @since 04.08.2014
 */
public class Helpers {

    static BufferedImage loadStaticImage(InputStream stream) {
        try {
            return ImageIO.read(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static BufferedImage loadStaticImage(String name) {
        return loadStaticImage(Helpers.class.getResourceAsStream(name));
    }
}
