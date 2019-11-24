package ui.model;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPIterator;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.properties.XMPPropertyInfo;
import com.aragoj.plugins.imagereader.metadata.Metadata;
import com.aragoj.plugins.imagereader.metadata.MetadataGroup;
import com.aragoj.plugins.imagereader.metadata.MetadataItem;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.xmp.XmpDirectory;
import imageprocess.AJImage;
import imageprocess.ImageUtility;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import session.export.ExportPreferences;

public class ImageItem {
  private int openerSourceId = -1;

  private String name;
  private String activePath;
  private String path;

  private int width;
  private int height;

  private ObservableList<TagRow> metadata;
  private Image thumbnail;

  public ImageItem(AJImage ajImage) {
    this.name = ajImage.getName();
    this.path = ajImage.getPath();
    this.activePath = path;
    this.width = ajImage.getWidth();
    this.height = ajImage.getHeight();
    this.metadata = parseMetadata(ajImage.getMetadata());
    this.thumbnail = ajImage.getThumbnail();
    this.openerSourceId = ajImage.getOpenerSourceId();
  }

  private ObservableList<TagRow> parseMetadata(Metadata metadata) {
    ObservableList<TagRow> tags = FXCollections.observableArrayList();
    List<MetadataItem> itemList = metadata.getData();
    for (MetadataItem item : itemList) {
      if (item instanceof MetadataGroup) {
        TagRow tagRow = new TagRow(item.getTag(), item.getValue(), true);
        parseMetadataGroup(tagRow, (MetadataGroup) item);
        tags.add(tagRow);
      } else {
        TagRow tagRow = new TagRow(item.getTag(), item.getValue(), false);
        tagRow.setToExport(ExportPreferences.containsPreference(item.getTag()));
        tags.add(tagRow);
      }
    }
    return tags;
  }

  private void parseMetadataGroup(TagRow tagRow, MetadataGroup group) {
    for (MetadataItem item : group.getData()) {
      if (item instanceof MetadataGroup) {
        TagRow tag = new TagRow(item.getTag(), item.getValue(), true);
        tag.setToExport(ExportPreferences.containsPreference(item.getTag()));
        tagRow.getChildren()
            .add(tag);
        parseMetadataGroup(tag, (MetadataGroup) item);
      } else {
        TagRow tag = new TagRow(item.getTag(), item.getValue(), false);
        tag.setToExport(ExportPreferences.containsPreference(item.getTag()));
        tagRow.getChildren()
            .add(tag);
      }
    }
  }

  public String getCameraModel(){
    try{
      com.drew.metadata.Metadata drewMetadata = ImageMetadataReader.readMetadata(new File(path));
      StringBuilder cameraModel = new StringBuilder();
      if(metadata == null)
        return  cameraModel.toString();
      Directory exifIFD0Directory = drewMetadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
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
    } catch (IOException | ImageProcessingException e) {
      e.printStackTrace();
    }
    return "";
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getActivePath() {
    return activePath;
  }

  public void setActivePath(String activePath){
    this.activePath = activePath;
  }

  public String getOriginalPath(){
    return path;
  }


  public void setPath(String path) {
    this.path = path;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public ObservableList<TagRow> getMetadata() {
    return metadata;
  }

  public void setMetadata(ObservableList<TagRow> metadata) {
    this.metadata = metadata;
  }

  public Image getThumbnail() {
    return thumbnail;
  }

  public void setThumbnail(Image thumbnail) {
    this.thumbnail = thumbnail;
  }

  public int getOpenerSourceId() {
    return openerSourceId;
  }

  public String getPath() {
    return path;
  }
}
