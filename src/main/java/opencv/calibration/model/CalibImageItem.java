package opencv.calibration.model;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPIterator;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.properties.XMPPropertyInfo;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.file.FileMetadataDirectory;
import com.drew.metadata.xmp.XmpDirectory;
import imageprocess.ImageUtility;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import session.export.ExportPreferences;
import ui.model.LabeledComboOption;
import ui.model.TagRow;
import utils.Utility;
import utils.scalr.Scalr;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class CalibImageItem {
  private Metadata metadata;

  private String name;
  private String path;

  private int width;
  private int height;

  private Image thumbnail;
  private int thumbnailWidth;

  private Image img;
  private String statusColor;

  public CalibImageItem(Metadata metadata, String name, Image image, String path) {
    this.metadata = metadata;
    this.name = name;
    this.path = path;
    this.statusColor = "#00000000";
    this.img = image;
  }

  public CalibImageItem(String name, String path, Image img, Image thumbnail, int thumbnailWidth, String statusColor) {
    this.name = name;
    this.img = img;
    this.thumbnail = thumbnail;
    this.thumbnailWidth = thumbnailWidth;
    this.path = path;
    this.statusColor = statusColor;
  }

  public Image getImageIcon(int width) throws IOException, ExecutionException, InterruptedException {
    if(thumbnail != null && thumbnailWidth == width){
      return thumbnail;
    }

    BufferedImage image = SwingFXUtils.fromFXImage(img, null);
    this.width = image.getWidth();
    this.height = image.getHeight();

    Scalr.Method method = Scalr.Method.ULTRA_QUALITY;
    if(this.width*this.height > 3000000){
      method = Scalr.Method.BALANCED;
    }

    if(height > this.width){
      return SwingFXUtils.toFXImage(Scalr.resize(image, method, Scalr.Mode.FIT_TO_HEIGHT, width, (BufferedImageOp) null), null);
    }
    return SwingFXUtils.toFXImage(Scalr.resize(image, method, Scalr.Mode.FIT_TO_WIDTH, width, (BufferedImageOp) null), null);
  }

  public Image getImage() {
    return img;
  }

  public void setImage(Image image){
    thumbnail = null;
    img = image;
  }

  public String getName() {
    return name;
  }

  public String getPath() {
    return path;
  }


  public String getDate(Date date){
    DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    return df.format(date);
  }

  public String getFormattedResolution(){
    if(width != 0 && height != 0){
      return width + "x" + height;
    }
    return "N/A";
  }

  public void preloadThumbnail() {
    try {
      thumbnail = getImageIcon(50);
      thumbnailWidth = 50;
    } catch (Exception e) {
      e.printStackTrace();
      // Since it's a preload, do nothing
    }
  }


  public ObservableList<TagRow> getMetadata(){
    if(metadata == null)
      return  null;

    // Directory order: File -> Exif -> GPS -> XMP
    Directory fileDirectory = metadata.getFirstDirectoryOfType(FileMetadataDirectory.class);
    Directory exifIFD0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
    Directory exifSubIFDDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
    GpsDirectory gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);

    ObservableList<TagRow> tags = FXCollections.observableArrayList();

    // File directory
    if(fileDirectory != null){
      if(fileDirectory.containsTag(FileMetadataDirectory.TAG_FILE_MODIFIED_DATE)){
        Date date = fileDirectory.getDate(FileMetadataDirectory.TAG_FILE_MODIFIED_DATE);
        TagRow tModDate = new TagRow("Modified date", getDate(date), false);
        tModDate.setToExport(ExportPreferences.containsPreference("Modified date"));
        tags.add(tModDate);
      }
      if(fileDirectory.containsTag(FileMetadataDirectory.TAG_FILE_SIZE)){
        try {
          TagRow tSize = new TagRow("Size", Utility.formatFileSize(fileDirectory.getLong(FileMetadataDirectory.TAG_FILE_SIZE)), false);
          tSize.setToExport(ExportPreferences.containsPreference("Size"));
          tags.add(tSize);
        } catch (MetadataException e) {
          // Just don't add
        }
      }
    }
    TagRow tWidth = new TagRow("Width", String.valueOf(width) + " pixels", false);
    tWidth.setToExport(ExportPreferences.containsPreference("Width"));
    TagRow tHeight = new TagRow("Height", String.valueOf(height) + " pixels", false);
    tHeight.setToExport(ExportPreferences.containsPreference("Height"));
    tags.addAll(tWidth, tHeight);

    // Exif
    double focalLength = 0;
    double eqFocalLength = 0;
    double aperture = 0;
    double shutterSpeed = 0;
    double iso = 0;
    if(exifIFD0Directory != null && exifSubIFDDirectory != null){
      TagRow exif = new TagRow("Exif", "", true);
      if(exifIFD0Directory.containsTag(ExifIFD0Directory.TAG_MAKE)) {
        TagRow row = new TagRow("Make", exifIFD0Directory.getString(ExifIFD0Directory.TAG_MAKE), false);
        row.setToExport(ExportPreferences.containsPreference("Make"));
        exif.getChildren().add(row);
      }
      if(exifIFD0Directory.containsTag(ExifIFD0Directory.TAG_MODEL)) {
        TagRow row = new TagRow("Model", exifIFD0Directory.getString(ExifIFD0Directory.TAG_MODEL), false);
        row.setToExport(ExportPreferences.containsPreference("Model"));
        exif.getChildren().add(row);
      }
      if(exifIFD0Directory.containsTag(ExifIFD0Directory.TAG_X_RESOLUTION)) {
        TagRow row = new TagRow("X Resolution", exifIFD0Directory.getString(ExifIFD0Directory.TAG_X_RESOLUTION), false);
        row.setToExport(ExportPreferences.containsPreference("X Resolution"));
        exif.getChildren().add(row);
      }
      if(exifIFD0Directory.containsTag(ExifIFD0Directory.TAG_Y_RESOLUTION)) {
        TagRow row = new TagRow("Y Resolution", exifIFD0Directory.getString(ExifIFD0Directory.TAG_Y_RESOLUTION), false);
        row.setToExport(ExportPreferences.containsPreference("Y Resolution"));
        exif.getChildren().add(row);
      }

      try {
        if(exifSubIFDDirectory.containsTag(ExifSubIFDDirectory.TAG_35MM_FILM_EQUIV_FOCAL_LENGTH)) {
          eqFocalLength = exifSubIFDDirectory.getDouble(ExifSubIFDDirectory.TAG_35MM_FILM_EQUIV_FOCAL_LENGTH);
        }
        if(exifSubIFDDirectory.containsTag(ExifSubIFDDirectory.TAG_FOCAL_LENGTH)) {
          focalLength = exifSubIFDDirectory.getDouble(ExifSubIFDDirectory.TAG_FOCAL_LENGTH);
        }
        if(exifSubIFDDirectory.containsTag(ExifSubIFDDirectory.TAG_SHUTTER_SPEED)) {
          String desc = exifSubIFDDirectory.getDescription(ExifSubIFDDirectory.TAG_SHUTTER_SPEED);
          String[] split = desc.substring(0, desc.length()-4).split("/");
          shutterSpeed = Double.parseDouble(split[0]) / Double.parseDouble(split[1]);
        }
        else if(exifSubIFDDirectory.containsTag(ExifSubIFDDirectory.TAG_EXPOSURE_TIME)) {
          shutterSpeed = exifSubIFDDirectory.getDouble(ExifSubIFDDirectory.TAG_EXPOSURE_TIME);
        }
        if(exifSubIFDDirectory.containsTag(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT)) {
          iso = exifSubIFDDirectory.getDouble(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT);
        }
        if(exifSubIFDDirectory.containsTag(ExifSubIFDDirectory.TAG_FNUMBER)) {
          aperture = exifSubIFDDirectory.getDouble(ExifSubIFDDirectory.TAG_FNUMBER);
        }
        else if(exifSubIFDDirectory.containsTag(ExifSubIFDDirectory.TAG_APERTURE)) {
          aperture = exifSubIFDDirectory.getDouble(ExifSubIFDDirectory.TAG_APERTURE);
        }
      } catch (MetadataException e) {
        // Do nothing
      }

      for(Tag tag: exifSubIFDDirectory.getTags()){
        TagRow row = new TagRow(tag.getTagName(), tag.getDescription(), false);
        row.setToExport(ExportPreferences.containsPreference(tag.getTagName()));
        exif.getChildren().add(row);
      }
      tags.add(exif);
    }

    // GPS
    TagRow gps = new TagRow("GPS", "", true);
    if(gpsDirectory != null){
      GeoLocation location = gpsDirectory.getGeoLocation();
      if(location != null){
        TagRow latRow = new TagRow("Latitude", String.valueOf(location.getLatitude()), false);
        latRow.setToExport(ExportPreferences.containsPreference("Latitude"));
        TagRow longRow = new TagRow("Longitude", String.valueOf(location.getLongitude()), false);
        longRow.setToExport(ExportPreferences.containsPreference("Longitude"));
        gps.getChildren().addAll(latRow, longRow);

        if(gpsDirectory.containsTag(GpsDirectory.TAG_ALTITUDE_REF)){
          TagRow row = new TagRow("Altitude Ref", gpsDirectory.getDescription(GpsDirectory.TAG_ALTITUDE_REF), false);
          row.setToExport(ExportPreferences.containsPreference("Altitude Ref"));
          gps.getChildren().add(row);
        }
        if(gpsDirectory.containsTag(GpsDirectory.TAG_ALTITUDE)){
          TagRow row = new TagRow("Altitude", gpsDirectory.getDescription(GpsDirectory.TAG_ALTITUDE), false);
          row.setToExport(ExportPreferences.containsPreference("Altitude"));
          gps.getChildren().add(row);
        }
        tags.add(gps);
      }
    }
    // XMP
    TagRow xmp = new TagRow("XMP", "", true);
    Collection<XmpDirectory> xmpDirectories = metadata.getDirectoriesOfType(XmpDirectory.class);
    for (XmpDirectory xmpDirectory : xmpDirectories) {
      try {
        XMPMeta xmpMeta = xmpDirectory.getXMPMeta();
        XMPIterator iterator = xmpMeta.iterator();
        while (iterator.hasNext()) {
          XMPPropertyInfo xmpPropertyInfo = (XMPPropertyInfo)iterator.next();
          String path = xmpPropertyInfo.getPath();
          if(path != null){
            String tag = ImageUtility.getXMPTagOfInterest(xmpPropertyInfo.getPath());
            if(tag != null){
              TagRow row = new TagRow(tag, xmpPropertyInfo.getValue(), false);
              row.setToExport(ExportPreferences.containsPreference(tag));
              xmp.getChildren().add(row);
            }
          }
        }
      } catch (XMPException e) {
        e.printStackTrace();
      }
    }
    if(xmp.getChildren().size() != 0){
      tags.add(xmp);
    }

    // Derived values
    TagRow derived = new TagRow("Derived", "", true);
    if(eqFocalLength != 0 && focalLength != 0){
      double eqFactor = ImageUtility.getScaleFactor(eqFocalLength, focalLength);
      double coc = ImageUtility.getCircleOfConfusion(eqFactor);
      double fovValue = ImageUtility.getFOV(focalLength, eqFactor);
      TagRow scaleFactor = new TagRow(("Scale Factor to 35mm"), String.format("%.1f", eqFactor), false);
      TagRow cocRow = new TagRow(("Circle of Confusion"), String.format("%.3f", coc) + " mm", false);
      TagRow fov = new TagRow(("Field of View"), String.format("%.1f", fovValue) + " deg", false);
      scaleFactor.setToExport(ExportPreferences.containsPreference("Scale Factor to 35mm"));
      cocRow.setToExport(ExportPreferences.containsPreference("Circle of Confusion"));
      fov.setToExport(ExportPreferences.containsPreference("Field of View"));
      derived.getChildren().addAll(scaleFactor, cocRow, fov);

      if(aperture != 0){
        double hyperfocalDistance = ImageUtility.getHyperfocalDistance(eqFactor, focalLength, aperture);
        TagRow hyperFocal = new TagRow(("Hyperfocal Distance"), String.format("%.2f", hyperfocalDistance) + " m", false);
        hyperFocal.setToExport(ExportPreferences.containsPreference("Hyperfocal Distance"));
        derived.getChildren().add(hyperFocal);
      }
    }
    if(aperture != 0 && iso != 0 && shutterSpeed != 0){
      double lightValue = ImageUtility.getLightValue(aperture, shutterSpeed, iso);
      TagRow lightV = new TagRow(("Light Value"), String.format("%.1f", lightValue) , false);
      lightV.setToExport(ExportPreferences.containsPreference("Light Value"));
      derived.getChildren().add(lightV);
    }
    if(derived.getChildren().size() != 0){
      tags.add(derived);
    }

    return tags;
  }

  public List<LabeledComboOption> getTargetDistanceSuggestions(){
    List<LabeledComboOption> list = new ArrayList<>();
    Collection<XmpDirectory> xmpDirectories = metadata.getDirectoriesOfType(XmpDirectory.class);
    for (XmpDirectory xmpDirectory : xmpDirectories) {
      try {
        XMPMeta xmpMeta = xmpDirectory.getXMPMeta();
        XMPIterator iterator = xmpMeta.iterator();
        while (iterator.hasNext()) {
          XMPPropertyInfo xmpPropertyInfo = (XMPPropertyInfo)iterator.next();
          String path = xmpPropertyInfo.getPath();
          if(path != null){
            String tag = ImageUtility.getXMPTagOfInterest(xmpPropertyInfo.getPath());
            if(tag != null){
              if(tag.equals("RelativeAltitude")){
                String value = xmpPropertyInfo.getValue();
                value = value.replaceAll("\\+", "");
                list.add(new LabeledComboOption("Relative Altitude: " + value, value));
              }
            }
          }
        }
      } catch (XMPException e) {
        e.printStackTrace();
      }
    }
    return list;
  }

  public String getCameraModel(){
    StringBuilder cameraModel = new StringBuilder();
    if(metadata == null)
      return  cameraModel.toString();
    Directory exifIFD0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
    if(exifIFD0Directory != null) {
      if (exifIFD0Directory.containsTag(ExifIFD0Directory.TAG_MAKE)) {
        cameraModel.append(exifIFD0Directory.getString(ExifIFD0Directory.TAG_MAKE));
      }
      if (exifIFD0Directory.containsTag(ExifIFD0Directory.TAG_MODEL)) {
        if(cameraModel.length() > 0){
          cameraModel.append(" ");
        }
        cameraModel.append(exifIFD0Directory.getString(ExifIFD0Directory.TAG_MODEL));
      }
    }
    return cameraModel.toString();
  }

  public LabeledComboOption getFocalLengthSuggestion(){
    Directory exifSubIFDDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
    if(exifSubIFDDirectory == null) return null;
    double focalLength = 0;
    try {
      if(exifSubIFDDirectory.containsTag(ExifSubIFDDirectory.TAG_FOCAL_LENGTH)) {
        focalLength = exifSubIFDDirectory.getDouble(ExifSubIFDDirectory.TAG_FOCAL_LENGTH);
      }
    } catch (MetadataException e) {
      // Do nothing
    }
    if(focalLength != 0){
      return new LabeledComboOption("EXIF Focal Length: " +focalLength + " mm", String.valueOf(focalLength));
    }
    return null;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }


  public String getStatusColor() {
    return statusColor;
  }

  public void setStatusColor(String statusColor) {
    this.statusColor = statusColor;
  }
}
