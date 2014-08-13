package mapapp;

import com.googlecode.javacv.FFmpegFrameGrabber;
import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.cpp.opencv_core;
import org.apache.sanselan.ImageWriteException;
import org.apache.sanselan.SanselanException;
import org.apache.sanselan.formats.jpeg.exifRewrite.ExifRewriter;
import org.apache.sanselan.formats.tiff.constants.TagInfo;
import org.apache.sanselan.formats.tiff.fieldtypes.FieldType;
import org.apache.sanselan.formats.tiff.fieldtypes.FieldTypeASCII;
import org.apache.sanselan.formats.tiff.write.TiffOutputDirectory;
import org.apache.sanselan.formats.tiff.write.TiffOutputField;
import org.apache.sanselan.formats.tiff.write.TiffOutputSet;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.lang.Math.round;
import static mapapp.Helpers.map;
import static mapapp.Helpers.toJson;
import static org.apache.sanselan.formats.tiff.constants.ExifTagConstants.*;
import static org.apache.sanselan.formats.tiff.constants.GPSTagConstants.GPS_TAG_GPS_IMG_DIRECTION;

/**
 * @author tbaum
 * @since 12.08.2014
 */
class VideoGrabber implements AutoCloseable {

    private final File virbStore;
    private final File exportDir;
    FFmpegFrameGrabber grabber = null;
    String openVideo = null;
    long lastpos = 0;
    private String projectId = "sZBncLZJmbq4nAHmwHoO2Q";

    @Inject VideoGrabber(@VirbVideoStore File virbStore, @ExportVideoStore File exportDir) {
        this.virbStore = virbStore;
        this.exportDir = exportDir;
    }

    private void writeTaggedImage(MyMapMarkerDot marker, File file, BufferedImage bufferedImage, String projectId)
            throws SanselanException, IOException {

        TiffOutputSet outputSet = new TiffOutputSet();
        outputSet.setGPSInDegrees(marker.point.lon, marker.point.lat);
        addField(outputSet, GPS_TAG_GPS_IMG_DIRECTION, round(marker.point.ankle));
        addField(outputSet, EXIF_TAG_DATE_TIME_ORIGINAL,
                new SimpleDateFormat("yyyy:MM:dd HH:mm:ss").format(new Date(marker.point.time)));
        if (projectId != null) {
            addField(outputSet, EXIF_TAG_IMAGE_DESCRIPTION, toJson(map("MAPSettingsProject", projectId)));
        }
        addField(outputSet, EXIF_TAG_ORIENTATION, 0);

        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "JPEG", output);
            new ExifRewriter().updateExifMetadataLossless(output.toByteArray(), os, outputSet);
        }
        file.setLastModified(marker.point.time);
    }

    private void addField(TiffOutputSet outputSet, TagInfo tag, Object s) throws ImageWriteException {
        FieldType dataType = tag.dataTypes[0];
        byte[] data = dataType.writeData(s, outputSet.byteOrder);
        TiffOutputDirectory directory = outputSet.findDirectory(tag.directoryType.directoryType);
        if (directory == null) {
            directory = new TiffOutputDirectory(tag.directoryType.directoryType);
            outputSet.addDirectory(directory);
        }
        directory.add(new TiffOutputField(tag, dataType, dataType instanceof FieldTypeASCII ? data.length : 1, data));
    }

    BufferedImage grabVideo(MyMapMarkerDot l) {
        String s = l.source.getFile().getName();
        if (s.endsWith(".localized")) s = s.substring(0, s.length() - ".localized".length());

        File cache = new File(exportDir, s);
        if (!cache.exists() && !cache.mkdirs()) {
            throw new RuntimeException("unable to create export-dir " + cache);
        }

        TrackPoint first = l.track.tps.first();
        long t = l.asVideoTimecode();

        File img = new File(cache, String.format("%08X-%010d.jpeg", first.time / 1000, t));

        if (img.exists()) {
            try {
                return ImageIO.read(img);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        BufferedImage bufferedImage = grabVideoReal(l);
        try {
            writeTaggedImage(l, img, bufferedImage, projectId);
        } catch (IOException | SanselanException e) {
            e.printStackTrace();
        }
        return bufferedImage;
    }

    BufferedImage grabVideoReal(MyMapMarkerDot l) {
        if (!l.source.getMovie().equals(openVideo)) {
            close();

            openVideo = l.source.getMovie();
            grabber = new FFmpegFrameGrabber(new File(virbStore, openVideo));
            try {
                grabber.start();
            } catch (FrameGrabber.Exception e) {
                e.printStackTrace();
                grabber = null;
                openVideo = null;
            }
        }

        try {
            long t = l.asVideoTimecode();
            if (t < lastpos) {
                System.err.println("seek backwards");
                grabber.restart();
            }
            lastpos = t;
            grabber.setTimestamp(t);
            opencv_core.IplImage grab = grabber.grab();
            if (grab != null) {
                return grab.getBufferedImage();
            } else {
                System.err.println("grab null!!");
            }
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override public void close() {
        if (grabber == null) {
            return;
        }

        try {
            grabber.stop();
            grabber = null;
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
    }
}
