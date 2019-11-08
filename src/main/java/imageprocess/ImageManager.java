package imageprocess;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import javafx.scene.image.Image;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.ImagingException;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.jpeg.xmp.JpegXmpRewriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;

import static opencv.calibration.tools.undistort.UndistortManager.TMP_DIR;

public class ImageManager {

    public static ImageItem retrieveImage(String path) throws ImageProcessingException, IOException {
        Metadata metadata = ImageMetadataReader.readMetadata(new File(path));
        return new ImageItem(metadata, Paths.get(path).getFileName().toString(), new Image(new File(path).toURI().toString()), path);
    }

    public static void copyMetadata(File src, File undistorted, File dst) {
        File result = copyXMpData(src, copyExifData(src, undistorted));
        result.renameTo(dst);
    }

    private static File copyExifData(File srcFile, File to) {
        try {
            final JpegImageMetadata metadata = (JpegImageMetadata) Imaging.getMetadata(srcFile);
            if(metadata != null && metadata.getExif() != null && metadata.getExif().getOutputSet() != null){
                File outputFile = new File(TMP_DIR + srcFile.getName() + "_e");
                FileOutputStream fos = new FileOutputStream(outputFile);
                new ExifRewriter().updateExifMetadataLossless(to, fos, metadata.getExif().getOutputSet());
                to.delete();
                return outputFile;
            }
        } catch (ImageReadException | IOException | ImageWriteException e) {
            // Ignore
        }
        return to;
    }

    private static File copyXMpData(File srcFile, File to)  {
        try {
            String xml = Imaging.getXmpXml(srcFile);
            if (xml != null) {
                File outputFile = new File(TMP_DIR + srcFile.getName() + "_x");
                FileOutputStream fos = new FileOutputStream(outputFile);
                new JpegXmpRewriter().updateXmpXml(to, fos, xml);
                to.delete();
                return outputFile;
            }
        } catch (ImagingException | IOException e) {
            // Ignore
        }
        return to;
    }
}
