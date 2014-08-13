package mapapp;

import com.googlecode.javacv.FFmpegFrameGrabber;
import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.cpp.opencv_core;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.ImageWriteException;
import org.apache.sanselan.formats.jpeg.exifRewrite.ExifRewriter;
import org.apache.sanselan.formats.tiff.write.TiffOutputField;
import org.apache.sanselan.formats.tiff.write.TiffOutputSet;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.apache.sanselan.formats.tiff.constants.ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL;
import static org.apache.sanselan.formats.tiff.constants.ExifTagConstants.EXIF_TAG_ORIENTATION;
import static org.apache.sanselan.formats.tiff.constants.GPSTagConstants.GPS_TAG_GPS_IMG_DIRECTION;
import static org.apache.sanselan.formats.tiff.constants.TiffFieldTypeConstants.*;

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

    @Inject VideoGrabber(@VirbVideoStore File virbStore, @ExportVideoStore File exportDir) {
        this.virbStore = virbStore;
        this.exportDir = exportDir;
    }

    private void writeTaggedImage(MyMapMarkerDot marker, File file, BufferedImage bufferedImage)
            throws ImageWriteException, IOException, ImageReadException {
        TiffOutputSet outputSet = new TiffOutputSet();
        outputSet.setGPSInDegrees(marker.point.lon, marker.point.lat);
        outputSet.getOrCreateGPSDirectory().add(new TiffOutputField(GPS_TAG_GPS_IMG_DIRECTION, FIELD_TYPE_RATIONAL,
                1, FIELD_TYPE_RATIONAL.writeData(marker.point.ankle, outputSet.byteOrder)));

        Date time = new Date(marker.point.time);
        byte[] dateOriginal = FIELD_TYPE_ASCII.writeData(new SimpleDateFormat("yyyy:MM:dd HH:mm:ss").format(time), outputSet.byteOrder);
        outputSet.getOrCreateExifDirectory().add(new TiffOutputField(EXIF_TAG_DATE_TIME_ORIGINAL, FIELD_TYPE_ASCII,
                dateOriginal.length, dateOriginal));
        //    byte[] bytes1 = FIELD_TYPE_ASCII.writeData("{\"foo\":1246}", outputSet.byteOrder);
        //    outputSet.getOrCreateRootDirectory().add(new TiffOutputField(EXIF_TAG_IMAGE_DESCRIPTION, FIELD_TYPE_ASCII, bytes1.length, bytes1));

        outputSet.getOrCreateRootDirectory().add(new TiffOutputField(EXIF_TAG_ORIENTATION, FIELD_TYPE_SHORT,
                1, FIELD_TYPE_SHORT.writeData(0, outputSet.byteOrder)));

        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "JPEG", output);
            new ExifRewriter().updateExifMetadataLossless(output.toByteArray(), os, outputSet);
        }
    }

    BufferedImage grabVideo(MyMapMarkerDot l) {
        String s = l.source.getFile().getName();//
        if (s.endsWith(".localized")) s = s.substring(0, s.length() - ".localized".length());

        File cache = new File(exportDir, s);
        if (!cache.exists() && !cache.mkdirs()) {
            throw new RuntimeException("unable to create export-dir " + cache);
        }

        TrackPoint first = l.track.tps.first();
        long l1 = calculateVidTimestamp(l);

        File img = new File(cache, String.format("%08X-%010d.jpeg", first.time / 1000, l1));

        if (img.exists()) {
            try {
                return ImageIO.read(img);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // .replace(,"");
        BufferedImage bufferedImage = grabVideoReal(l);
        try {
            writeTaggedImage(l, img, bufferedImage);
        } catch (ImageWriteException | IOException | ImageReadException e) {
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
//                bufferedImage = new BufferedImage(grabber.getImageWidth(), grabber.getImageHeight(), BufferedImage.TYPE_INT_ARGB);
            } catch (FrameGrabber.Exception e) {
                e.printStackTrace();
                grabber = null;
                openVideo = null;
            }
        }


//        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoFile);
//        grabber.start();
//        try {
////            l.source.
////            grabber.setFrameNumber(l.point.time);
//        Date creationDate = first;.getCreationDate();
//        Date creationDate = l.source.getCreationDate();
//        System.out.println("creationDate = " + first.time);
//        System.out.println("creationDate = " + creationDate);
//        System.out.println("creationDate = " + creationDate.getTime());
//        System.out.println("length = " + grabber.getLengthInTime());
//        System.out.println("length-frms = " + grabber.getLengthInFrames());

//        System.out.println("seek to:" + frameDiv);
        try {
            long frameDiv = calculateVidTimestamp(l);
            if (frameDiv < lastpos) {
                System.err.println("seek backwards");
//         grabber.setTimestamp(0);
//         grabber.release();
                grabber.restart();
            }
            lastpos = frameDiv;
            grabber.setTimestamp(frameDiv);
            opencv_core.IplImage grab = grabber.grab();
            if (grab != null) {
//                File snap = createTempFile();
                return grab.getBufferedImage();
//                cvSaveImage(snap.getAbsolutePath(), grab);
//                BufferedImage bufferedImage = ImageIO.read(snap);
//                snap.delete();
//                return snap;
            } else {
                System.err.println("grab null!!");
            }
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private long calculateVidTimestamp(MyMapMarkerDot l) {
        return (long) ((l.point.time - l.track.tps.first().time) * 1000.0 / l.source.playbackFactor);
    }

    private File createTempFile() throws IOException {
        return File.createTempFile("snap", ".jpeg");
    }

    public void close() {
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
