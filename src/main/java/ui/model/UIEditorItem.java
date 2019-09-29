package ui.model;

import imageprocess.ImageItem;
import session.model.EditorItem;

public class UIEditorItem {
  private EditorItem editorItem;
  private ImageItem imageItem;

  public UIEditorItem(ImageItem imageItem) {
    this.editorItem = new EditorItem();
    setImageItem(imageItem);
  }

  public UIEditorItem(EditorItem editorItem, ImageItem imageItem) {
    this.editorItem = editorItem;
    this.imageItem = imageItem;
  }

  public EditorItem getEditorItem() {
    return editorItem;
  }

  public void setEditorItem(EditorItem editorItem) {
    this.editorItem = editorItem;
  }

  public ImageItem getImageItem() {
    return imageItem;
  }

  public void setImageItem(ImageItem imageItem) {
    this.imageItem = imageItem;
    editorItem.setSourceImagePath(imageItem.getPath());
  }
}
