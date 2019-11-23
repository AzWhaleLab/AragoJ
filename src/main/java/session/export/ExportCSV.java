package session.export;

import equation.Equation;
import equation.model.EquationItem;
import imageprocess.ImageManager;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import session.model.EditorItem;
import session.model.EditorItemAngle;
import session.model.EditorItemArea;
import session.model.EditorItemLayer;
import session.model.EditorItemSegLine;
import session.model.Session;
import ui.custom.angle.AngleGroup;
import ui.custom.area.AreaGroup;
import ui.custom.segline.SegLineGroup;
import ui.model.ImageItem;
import ui.model.TagRow;
import ui.model.UIEditorItem;

public class ExportCSV {

  public static void export(File file, Session session, List<UIEditorItem> uiEditorItems) {
    ImageManager imageManager = new ImageManager();
    ArrayList<String> metadataNameList = new ArrayList<>();
    metadataNameList.add("Source");
    ArrayList<String> layerNameList = new ArrayList<>();

    // To avoid having to load the images two times
    HashMap<String, List<TagRow>> metadataMap = new HashMap<>();

    for (EditorItem item : session.getItems()) {
      // Retrieve metadata
      ImageItem imageItem = findImageItem(uiEditorItems, item.getSourceImagePath());
      if (imageItem != null) {
        List<TagRow> metadataList = imageItem.getMetadata();
        metadataMap.put(item.getSourceImagePath(), metadataList);
        for (TagRow tag : metadataList) {
          if (ExportPreferences.containsPreference(tag.getTag()) && tag.isToExportToFile()) {
            if (!metadataNameList.contains(tag.getTag())) {
              metadataNameList.add(tag.getTag()
                  .replaceAll(",", "."));
            }
          }
          List<TagRow> tagList = tag.getChildren();
          for (TagRow tag2 : tagList) {
            if (ExportPreferences.containsPreference(tag2.getTag()) && tag2.isToExportToFile()) {
              if (!metadataNameList.contains(tag2.getTag())) {
                metadataNameList.add(tag2.getTag()
                    .replaceAll(",", "."));
              }
            }
          }
        }
      }

      // Retrieve layers
      for (EditorItemLayer layer : item.getLayers()) {
        StringBuilder layerNameSB = new StringBuilder();
        StringBuilder layerUnitNameSB = new StringBuilder();
        String baseName = "";
        if (layer instanceof EditorItemSegLine) {
          SegLineGroup lineGroup = new SegLineGroup((EditorItemSegLine) layer, null, null);
          layerNameSB.append(lineGroup.getName())
              .append(" (px)");
          baseName = lineGroup.getName();
          if (item.hasScaleRatio()) {
            layerUnitNameSB.append(lineGroup.getName())
                .append(" (")
                .append(item.getScaleRatio()
                    .getUnits())
                .append(")");
          }
        } else if (layer instanceof EquationItem) {
          EquationItem equationItem = (EquationItem) layer;
          baseName = equationItem.getName();
          layerNameSB.append(equationItem.getName());
        } else if (layer instanceof EditorItemArea) {
          EditorItemArea editorItemArea = (EditorItemArea) layer;
          layerNameSB.append(editorItemArea.getName())
              .append(" (px\u00B2)");
          baseName = editorItemArea.getName();
          if (item.hasScaleRatio()) {
            layerUnitNameSB.append(editorItemArea.getName())
                .append(" (")
                .append(item.getScaleRatio()
                    .getSquaredUnits())
                .append(")");
          }
        } else if (layer instanceof EditorItemAngle) {
          EditorItemAngle editorItemAngle = (EditorItemAngle) layer;
          layerNameSB.append(editorItemAngle.getName())
              .append(" (\u00B0)");
        }

        // Add our layers
        String layerName = layerNameSB.toString();
        if (!layerName.isEmpty()) {
          if (!layerNameList.contains(layerName)) {
            layerNameList.add(layerName);
          }
        }

        // If there's scale applied, then add it to our map (LineGroup units)
        String layerUnitName = layerUnitNameSB.toString();
        if (!layerUnitName.isEmpty()) {
          if (!layerNameList.contains(layerUnitName)) {
            layerNameList.add(layerUnitName);
          }
        }

        // See if there's other layers in other items with the same name in other units and add them
        for (EditorItem item2 : session.getItems()) {
          for (EditorItemLayer layer2 : item2.getLayers()) {
            layerUnitNameSB = new StringBuilder();
            if (layer2 instanceof EditorItemSegLine) {
              SegLineGroup lineGroup = new SegLineGroup((EditorItemSegLine) layer2, null, null);
              if (lineGroup.getName()
                  .equals(baseName)) {
                if (item2.hasScaleRatio()) {
                  layerUnitNameSB.append(lineGroup.getName())
                      .append(" (")
                      .append(item2.getScaleRatio()
                          .getUnits())
                      .append(")");
                }
              }
            }
            if (layer2 instanceof EditorItemArea) {
              EditorItemArea editorItemArea = (EditorItemArea) layer2;
              if (editorItemArea.getName()
                  .equals(baseName)) {
                if (item2.hasScaleRatio()) {
                  layerUnitNameSB.append(editorItemArea.getName())
                      .append(" (")
                      .append(item2.getScaleRatio()
                          .getSquaredUnits())
                      .append(")");
                }
              }
            }
            layerUnitName = layerUnitNameSB.toString();
            if (!layerUnitName.isEmpty()) {
              if (!layerNameList.contains(layerUnitName)) {
                layerNameList.add(layerUnitName);
              }
            }
          }
        }
      }
    }
    writeMapsToFile(file, session.getItems(), metadataMap, metadataNameList, layerNameList);
  }

  private static ImageItem findImageItem(List<UIEditorItem> uiEditorItems, String sourceImagePath) {
    return uiEditorItems.stream()
        .filter(uiEditorItem -> uiEditorItem.getImageItem()
            .getOriginalPath()
            .equals(sourceImagePath))
        .map(UIEditorItem::getImageItem)
        .findFirst()
        .orElse(null);
  }

  private static void writeMapsToFile(File file, ArrayList<EditorItem> items,
      HashMap<String, List<TagRow>> metadataMap, ArrayList<String> metadataNameList,
      ArrayList<String> layerNameList) {
    try {
      BufferedWriter out = new BufferedWriter(new FileWriter(file));
      ArrayList<String> idList = new ArrayList<>(metadataNameList);
      idList.addAll(layerNameList);

      // Write name
      StringBuilder idStringBuilder = new StringBuilder();
      for (String name : idList) {
        idStringBuilder.append(name)
            .append(",");
      }
      idStringBuilder.deleteCharAt(idStringBuilder.length() - 1);
      out.write(idStringBuilder.toString());
      out.newLine();

      for (EditorItem item : items) {
        StringBuilder valueStringBuilder = new StringBuilder();

        for (String name : idList) {
          // Metadata
          if (name.equalsIgnoreCase("Source")) {
            valueStringBuilder.append(item.getSourceImagePath());
          }
          List<TagRow> metadataList = metadataMap.get(item.getSourceImagePath());
          TagRow row = getMetadataFromTag(metadataList, name);
          if (row != null) {
            valueStringBuilder.append(row.getValue()
                .replaceAll(",", "."));
          }
          // Layer
          ArrayList<EditorItemLayer> layers = item.getLayers();
          for (EditorItemLayer layer : layers) {
            if (layer instanceof EditorItemSegLine) {
              SegLineGroup lineGroup = new SegLineGroup((EditorItemSegLine) layer, null, null);
              String lineName = lineGroup.getName() + " (px)";
              if (name.equals(lineName)) {
                valueStringBuilder.append(lineGroup.getLength());
              }
              if (item.hasScaleRatio()) {
                String lineUnitName = lineGroup.getName() + " (" + item.getScaleRatio()
                    .getUnits() + ")";
                if (name.equals(lineUnitName)) {
                  valueStringBuilder.append(item.getScaleRatio()
                      .getRoundedScaledValue(lineGroup.getLength()));
                }
              }
            } else if (layer instanceof EditorItemArea) {
              EditorItemArea editorItemArea = (EditorItemArea) layer;
              AreaGroup areaGroup = new AreaGroup(editorItemArea, null);
              String areaName = editorItemArea.getName() + " (px\u00B2)";

              if (name.equals(areaName)) {
                valueStringBuilder.append(areaGroup.calculateArea());
              }
              if (item.hasScaleRatio()) {
                String lineUnitName = areaGroup.getPrimaryText() + " (" + item.getScaleRatio()
                    .getSquaredUnits() + ")";
                if (name.equals(lineUnitName)) {
                  valueStringBuilder.append(item.getScaleRatio()
                      .getSquaredRoundedScaledValue(areaGroup.calculateArea()));
                }
              }
            } else if (layer instanceof EditorItemAngle) {
              EditorItemAngle editorItemAngle = (EditorItemAngle) layer;
              AngleGroup angleGroup = new AngleGroup(editorItemAngle, null, null);
              String angleName = angleGroup.getName() + " (\u00B0)";

              if (name.equals(angleName)) {
                valueStringBuilder.append(Math.toDegrees(angleGroup.getAngle()));
              }
            } else if (layer instanceof EquationItem) {
              EquationItem equationItem = (EquationItem) layer;
              try {
                double result = equationItem.getResult();
                String layerName = equationItem.getName();
                if (name.equals(layerName)) {
                  valueStringBuilder.append(result);
                }
              } catch (Equation.ValidationError validationError) {
                // Ignore
              }
            }
          }
          valueStringBuilder.append(",");
        }
        valueStringBuilder.deleteCharAt(valueStringBuilder.length() - 1);
        out.write(valueStringBuilder.toString());
        out.newLine();
      }

      out.close();
    } catch (IOException e) {
      //TODO: Handle exception
    }
  }

  private static TagRow getMetadataFromTag(List<TagRow> metadata, String tag) {
    for (TagRow row : metadata) {
      if (row.getTag()
          .equalsIgnoreCase(tag)) {
        return row;
      } else {
        List<TagRow> tagList = row.getChildren();
        for (TagRow row2 : tagList) {
          if (row2.getTag()
              .equalsIgnoreCase(tag)) {
            return row2;
          }
        }
      }
    }
    return null;
  }
}
