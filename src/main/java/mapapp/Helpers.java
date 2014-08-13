package mapapp;

import com.google.gson.Gson;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

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

    @SuppressWarnings("unchecked") public static <K, V> Map<K, V> map(Object... objects) {
        Map<K, V> targetMap = new LinkedHashMap<>();
        int i = 0;
        while (i < objects.length) {
            targetMap.put((K) objects[i++], (V) objects[i++]);
        }
        return targetMap;
    }

    public static String toJson(Object mapSettingsProject) {
        return new Gson().toJson(mapSettingsProject);
    }
}
