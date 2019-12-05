package imageprocess;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPIterator;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.properties.XMPPropertyInfo;
import com.aragoj.plugins.imagereader.SupportedFormat;
import com.aragoj.plugins.imagereader.metadata.Metadata;
import com.aragoj.plugins.imagereader.metadata.MetadataGroup;
import com.aragoj.plugins.imagereader.metadata.MetadataItem;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Directory;
import com.drew.metadata.MetadataException;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.file.FileMetadataDirectory;
import com.drew.metadata.xmp.XmpDirectory;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import utils.Translator;
import utils.Utility;
import utils.scalr.Scalr;

public class DefaultImageParser {

  public AJImage defaultParseImage(String path)
      throws ImageManager.ImageProcessingException, IOException {
    try {
      com.drew.metadata.Metadata metadata = ImageMetadataReader.readMetadata(new File(path));
      String name = Paths.get(path)
          .getFileName()
          .toString();

      Image image = new Image(new File(path).toURI()
          .toString());
      int width = (int) image.getWidth();
      int height = (int) image.getHeight();

      return new AJImage(name, path, width, height, parseMetadata(metadata, width, height),
          loadThumbnail(image));
    } catch (ImageProcessingException e) {
      throw new ImageManager.ImageProcessingException(
          "DefaultImageParser: Couldn't parse metadata");
    }
  }

  public SupportedFormat[] getSupportedFormats(){
    SupportedFormat jpeg = new SupportedFormat(Translator.getString("jpegimagefilechooser"), new String[]{".jpeg", ".jpg"});
    SupportedFormat bmp = new SupportedFormat(Translator.getString("bmpimagefilechooser"), new String[]{".bmp"});
    SupportedFormat png = new SupportedFormat(Translator.getString("pngimagefilechooser"), new String[]{".png"});
    SupportedFormat gif = new SupportedFormat(Translator.getString("gifimagefilechooser"), new String[]{".gif"});
    return new SupportedFormat[]{jpeg, bmp, png, gif};
  }

  public boolean supportsFile(String path) {
    String p = path.toLowerCase();
    String[] parserExts = { "jpeg", "jpg", "bmp", "png", "gif" };
    return Arrays.stream(parserExts)
        .anyMatch(p::endsWith);
  }

  private Metadata parseMetadata(com.drew.metadata.Metadata inputMetadata, int width, int height) {
    // Directory order: File -> Exif -> GPS -> XMP
    Directory fileDirectory = inputMetadata.getFirstDirectoryOfType(FileMetadataDirectory.class);
    Directory exifIFD0Directory = inputMetadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
    Directory exifSubIFDDirectory =
        inputMetadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
    GpsDirectory gpsDirectory = inputMetadata.getFirstDirectoryOfType(GpsDirectory.class);

    ArrayList<MetadataItem> metadataItems = new ArrayList<>();

    // File directory
    if (fileDirectory != null) {
      if (fileDirectory.containsTag(FileMetadataDirectory.TAG_FILE_MODIFIED_DATE)) {
        Date date = fileDirectory.getDate(FileMetadataDirectory.TAG_FILE_MODIFIED_DATE);
        MetadataItem item = new MetadataItem("Modified date", getDate(date));
        metadataItems.add(item);
      }
      if (fileDirectory.containsTag(FileMetadataDirectory.TAG_FILE_SIZE)) {
        try {
          MetadataItem item = new MetadataItem("Size",
              Utility.formatFileSize(fileDirectory.getLong(FileMetadataDirectory.TAG_FILE_SIZE)));
          metadataItems.add(item);
        } catch (MetadataException e) {
          // Just don't add
        }
      }
    }
    MetadataItem tWidth = new MetadataItem("Width", String.valueOf(width) + " pixels");
    MetadataItem tHeight = new MetadataItem("Height", String.valueOf(height) + " pixels");
    metadataItems.add(tWidth);
    metadataItems.add(tHeight);

    // Exif
    double focalLength = 0;
    double eqFocalLength = 0;
    double aperture = 0;
    double shutterSpeed = 0;
    double iso = 0;
    if (exifIFD0Directory != null && exifSubIFDDirectory != null) {
      MetadataGroup exif = new MetadataGroup("Exif", "");
      if (exifIFD0Directory.containsTag(ExifIFD0Directory.TAG_MAKE)) {
        MetadataItem row =
            new MetadataItem("Make", exifIFD0Directory.getString(ExifIFD0Directory.TAG_MAKE));
        exif.addItem(row);
      }
      if (exifIFD0Directory.containsTag(ExifIFD0Directory.TAG_MODEL)) {
        MetadataItem row =
            new MetadataItem("Model", exifIFD0Directory.getString(ExifIFD0Directory.TAG_MODEL));
        exif.addItem(row);
      }
      if (exifIFD0Directory.containsTag(ExifIFD0Directory.TAG_X_RESOLUTION)) {
        MetadataItem row = new MetadataItem("X Resolution",
            exifIFD0Directory.getString(ExifIFD0Directory.TAG_X_RESOLUTION));
        exif.addItem(row);
      }
      if (exifIFD0Directory.containsTag(ExifIFD0Directory.TAG_Y_RESOLUTION)) {
        MetadataItem row = new MetadataItem("Y Resolution",
            exifIFD0Directory.getString(ExifIFD0Directory.TAG_Y_RESOLUTION));
        exif.addItem(row);
      }

      try {
        if (exifSubIFDDirectory.containsTag(ExifSubIFDDirectory.TAG_35MM_FILM_EQUIV_FOCAL_LENGTH)) {
          eqFocalLength =
              exifSubIFDDirectory.getDouble(ExifSubIFDDirectory.TAG_35MM_FILM_EQUIV_FOCAL_LENGTH);
        }
        if (exifSubIFDDirectory.containsTag(ExifSubIFDDirectory.TAG_FOCAL_LENGTH)) {
          focalLength = exifSubIFDDirectory.getDouble(ExifSubIFDDirectory.TAG_FOCAL_LENGTH);
        }
        if (exifSubIFDDirectory.containsTag(ExifSubIFDDirectory.TAG_SHUTTER_SPEED)) {
          String desc = exifSubIFDDirectory.getDescription(ExifSubIFDDirectory.TAG_SHUTTER_SPEED);
          String[] split = desc.substring(0, desc.length() - 4)
              .split("/");
          shutterSpeed = Double.parseDouble(split[0]) / Double.parseDouble(split[1]);
        } else if (exifSubIFDDirectory.containsTag(ExifSubIFDDirectory.TAG_EXPOSURE_TIME)) {
          shutterSpeed = exifSubIFDDirectory.getDouble(ExifSubIFDDirectory.TAG_EXPOSURE_TIME);
        }
        if (exifSubIFDDirectory.containsTag(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT)) {
          iso = exifSubIFDDirectory.getDouble(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT);
        }
        if (exifSubIFDDirectory.containsTag(ExifSubIFDDirectory.TAG_FNUMBER)) {
          aperture = exifSubIFDDirectory.getDouble(ExifSubIFDDirectory.TAG_FNUMBER);
        } else if (exifSubIFDDirectory.containsTag(ExifSubIFDDirectory.TAG_APERTURE)) {
          aperture = exifSubIFDDirectory.getDouble(ExifSubIFDDirectory.TAG_APERTURE);
        }
      } catch (MetadataException e) {
        // Do nothing
      }

      for (Tag tag : exifSubIFDDirectory.getTags()) {
        MetadataItem row = new MetadataItem(tag.getTagName(), tag.getDescription());
        exif.addItem(row);
      }
      metadataItems.add(exif);
    }

    // GPS
    MetadataGroup gps = new MetadataGroup("GPS", "");
    if (gpsDirectory != null) {
      GeoLocation location = gpsDirectory.getGeoLocation();
      if (location != null) {
        MetadataItem latRow = new MetadataItem("Latitude", GeoLocation.decimalToDegreesMinutesSecondsString(location.getLatitude()));
        MetadataItem longRow =
            new MetadataItem("Longitude", GeoLocation.decimalToDegreesMinutesSecondsString(location.getLongitude()));
        gps.addItem(latRow);
        gps.addItem(longRow);
        if (gpsDirectory.containsTag(GpsDirectory.TAG_ALTITUDE_REF)) {
          MetadataItem row = new MetadataItem("Altitude Ref",
              gpsDirectory.getDescription(GpsDirectory.TAG_ALTITUDE_REF));
          gps.addItem(row);
        }
        if (gpsDirectory.containsTag(GpsDirectory.TAG_ALTITUDE)) {
          MetadataItem row =
              new MetadataItem("Altitude", gpsDirectory.getDescription(GpsDirectory.TAG_ALTITUDE));
          gps.addItem(row);
        }
        metadataItems.add(gps);
      }
    }
    // XMP
    MetadataGroup xmp = new MetadataGroup("XMP", "");
    Collection<XmpDirectory> xmpDirectories =
        inputMetadata.getDirectoriesOfType(XmpDirectory.class);
    for (XmpDirectory xmpDirectory : xmpDirectories) {
      try {
        XMPMeta xmpMeta = xmpDirectory.getXMPMeta();
        XMPIterator iterator = xmpMeta.iterator();
        while (iterator.hasNext()) {
          XMPPropertyInfo xmpPropertyInfo = (XMPPropertyInfo) iterator.next();
          String path = xmpPropertyInfo.getPath();
          if (path != null) {
            String tag = ImageUtility.getXMPTagOfInterest(xmpPropertyInfo.getPath());
            if (tag != null) {
              MetadataItem row = new MetadataItem(tag, xmpPropertyInfo.getValue());
              xmp.addItem(row);
            }
          }
        }
      } catch (XMPException e) {
        e.printStackTrace();
      }
    }
    if (xmp.getData()
        .size() != 0) {
      metadataItems.add(xmp);
    }

    // Derived values
    MetadataGroup derived = new MetadataGroup("Derived", "");
    if (eqFocalLength != 0 && focalLength != 0) {
      double eqFactor = ImageUtility.getScaleFactor(eqFocalLength, focalLength);
      double coc = ImageUtility.getCircleOfConfusion(eqFactor);
      double fovValue = ImageUtility.getFOV(focalLength, eqFactor);
      MetadataItem scaleFactor =
          new MetadataItem(("Scale Factor to 35mm"), String.format("%.1f", eqFactor));
      MetadataItem cocRow =
          new MetadataItem(("Circle of Confusion"), String.format("%.3f", coc) + " mm");
      MetadataItem fov =
          new MetadataItem(("Field of View"), String.format("%.1f", fovValue) + " deg");
      derived.addItem(scaleFactor);
      derived.addItem(cocRow);
      derived.addItem(fov);

      if (aperture != 0) {
        double hyperfocalDistance =
            ImageUtility.getHyperfocalDistance(eqFactor, focalLength, aperture);
        MetadataItem hyperFocal = new MetadataItem(("Hyperfocal Distance"),
            String.format("%.2f", hyperfocalDistance) + " m");
        derived.addItem(hyperFocal);
      }
    }
    if (aperture != 0 && iso != 0 && shutterSpeed != 0) {
      double lightValue = ImageUtility.getLightValue(aperture, shutterSpeed, iso);
      MetadataItem lightV = new MetadataItem(("Light Value"), String.format("%.1f", lightValue));
      derived.addItem(lightV);
    }
    if (derived.getData()
        .size() != 0) {
      metadataItems.add(derived);
    }

    return new Metadata(metadataItems);
  }

  private String getDate(Date date) {
    DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    return df.format(date);
  }

  private Image loadThumbnail(Image image) {
    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
    Scalr.Method method = Scalr.Method.ULTRA_QUALITY;
    if (image.getWidth() * image.getHeight() > 3000000) {
      method = Scalr.Method.BALANCED;
    }

    if (image.getHeight() > image.getWidth()) {
      return SwingFXUtils.toFXImage(
          Scalr.resize(bufferedImage, method, Scalr.Mode.FIT_TO_HEIGHT, 50, (BufferedImageOp) null),
          null);
    }
    return SwingFXUtils.toFXImage(
        Scalr.resize(bufferedImage, method, Scalr.Mode.FIT_TO_WIDTH, 50, (BufferedImageOp) null),
        null);
  }
}
