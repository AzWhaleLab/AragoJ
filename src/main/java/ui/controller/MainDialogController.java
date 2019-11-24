package ui.controller;

import com.aragoj.plugins.imagereader.ImageReaderPlugin;
import com.drew.imaging.ImageProcessingException;
import com.jfoenix.controls.JFXAlert;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXColorPicker;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextField;
import equation.model.EquationItem;
import imageprocess.ImageManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.Parent;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import opencv.OpenCVManager;
import opencv.calibration.model.CalibrationModel;
import opencv.calibration.ui.CalibrationDialogController;
import opencv.calibration.ui.UndistortDialog;
import opencv.calibration.ui.UndistortProgressDialogController;
import opencv.filters.Filter;
import opencv.filters.FilterArguments;
import opencv.filters.edgedetection.FilterDialog;
import plugins.PluginLoader;
import session.SessionManager;
import session.export.ExportCSV;
import session.export.ExportPreferences;
import session.model.EditorItem;
import session.model.EditorItemAngle;
import session.model.EditorItemArea;
import session.model.EditorItemLayer;
import session.model.EditorItemSegLine;
import session.model.Session;
import ui.MainApplication;
import ui.cellfactory.ImageListViewCell;
import ui.custom.ImageEditorStackGroup;
import ui.custom.PixelatedImageView;
import ui.custom.ZoomableScrollPane;
import ui.custom.angle.AngleGroup;
import ui.custom.area.AreaGroup;
import ui.custom.segline.SegLineGroup;
import ui.model.ImageItem;
import ui.model.ScaleRatio;
import ui.model.UIEditorItem;
import ui.tasks.ProgressDialog;
import ui.tasks.task.FilterTask;
import ui.tasks.task.ImageImporterTask;
import ui.tasks.task.LoadImageTask;
import utils.Constants;
import utils.Translator;
import utils.Utility;
import utils.jfx.JFXTabPane;

public class MainDialogController
    implements ImageEditorStackGroup.ModeListener, ImageEditorStackGroup.ElementListener,
    LayerTabPageController.LineChangeListener, ScaleDialogController.OnActionListener,
    ConvertUnitsDialogController.OnActionListener, UndistortDialog.OnActionListener,
    UndistortProgressDialogController.UndistortCallback, ZoomableScrollPane.ZoomChangeListener,
    FilterDialog.OnActionListener {

  private Preferences prefs;

  //// Menu Items
  // View items
  @FXML private CheckMenuItem precisionLinesCheckItem;
  @FXML private CheckMenuItem identifierLinesCheckItem;

  @FXML public MenuBar menuBar;
  private Menu pluginImportImagesMenuItem;

  @FXML public Menu filtersMenu;
  @FXML private MenuItem convertUnitsMenuItem;
  @FXML public MenuItem newSessionMenuItem;
  @FXML public MenuItem openSessionMenuItem;
  @FXML public MenuItem saveSessionMenuItem;
  @FXML public MenuItem saveSessionAsMenuItem;
  @FXML public MenuItem importImagesMenuItem;
  @FXML public MenuItem exportCSVMenuItem;
  @FXML public MenuItem undistortMenuItem;
  @FXML public MenuItem autoUndistortCheckMenuItem;

  // Module items
  @FXML private Menu conversionMenu;

  // Left side controllers & views
  @FXML private JFXListView<UIEditorItem> imageListView;

  // Right side controllers & views
  @FXML public JFXTabPane miscTabPane;
  @FXML private MetaTreeTableController metaTabPageController;
  @FXML private LayerTabPageController layerTabPageController;

  private SessionManager sessionManager;
  // Main Editor
  private ImageEditorStackGroup imageEditorStackGroup;
  private ZoomableScrollPane imageEditorScrollPane;

  private ImageManager imageManager = new ImageManager();

  @FXML private StackPane stackPane;

  @FXML public HBox imageEditorToolsSecondaryPane;
  @FXML public StackPane imageEditorStackPane;
  @FXML public Label ieDegreeLabel;
  @FXML public JFXColorPicker ieColorPicker;
  @FXML public JFXTextField ieDegreePicker;
  @FXML private JFXButton editorCursorBtn;
  @FXML private JFXButton editorLineBtn;
  @FXML private JFXButton editorAngBtn;
  @FXML private JFXButton editorAreaBtn;
  @FXML private JFXButton angleBtn;
  @FXML private JFXButton handButton;
  @FXML private JFXButton zoomButton;

  @FXML private Label statusLabel;

  private ProgressDialog imageLoadProgressDialog;

  public MainDialogController() {
    this.sessionManager = new SessionManager();
  }

  @FXML private void initialize() {
    imageManager.clearTmpFolder();
    prefs = Preferences.userNodeForPackage(MainApplication.class);
    ExportPreferences.importExportPreferences();

    loadPreferencesAndPanes();

    // Default visual settings
    loadInitialState();
    ieColorPicker.managedProperty()
        .bind(ieColorPicker.visibleProperty());
    ieDegreePicker.managedProperty()
        .bind(ieDegreePicker.visibleProperty());
    ieDegreeLabel.managedProperty()
        .bind(ieDegreeLabel.visibleProperty());

    setUpImageList();
    setEditorButtonListeners();

    handButton.sceneProperty()
        .addListener((observable, oldValue, newValue) -> {
          if (oldValue == null && newValue != null) {
            newValue.getWindow()
                .setOnCloseRequest(event -> {
                  ExportPreferences.exportExportPreferences();
                  Session session = sessionManager.getSession();
                  if (session != null) {
                    event.consume();
                    closeAppWithSureDialog(session);
                  }
                });
          }
        });
    importPlugins();
  }

  private void importPlugins() {
    try {
      HashMap<Integer, ImageReaderPlugin> imageImportPlugins =
          imageManager.loadImageImportPlugins();
      if (!imageImportPlugins.isEmpty()) {
        Menu menuParent = new Menu(Translator.getString("plugins"));
        Menu menu = new Menu(Translator.getString("pluginImportImages"));
        for (Map.Entry<Integer, ImageReaderPlugin> set : imageImportPlugins.entrySet()) {
          MenuItem menuItem = new MenuItem(set.getValue()
              .getPluginName());
          menuItem.setUserData(set.getKey());
          menuItem.setOnAction(event -> {
            try {
              showImportImagesDialog((Integer) menuItem.getUserData(), false);
            } catch (PluginLoader.PluginNotFoundException e) {
              showPluginErrorDialog(e.getMessage());
            }
          });
          menu.getItems()
              .add(menuItem);
        }
        menuParent.getItems()
            .add(menu);
        menu.setDisable(sessionManager.getSession() == null);
        pluginImportImagesMenuItem = menu;
        menuBar.getMenus()
            .add(menuParent);
      }
    } catch (PluginLoader.CouldNotLoadPluginException e) {
      showPluginErrorDialog(e.getMessage());
    }
  }

  private void showPluginErrorDialog(String message) {
    JFXAlert alert = new JFXAlert((Stage) zoomButton.getScene()
        .getWindow());
    alert.initModality(Modality.APPLICATION_MODAL);
    alert.setOverlayClose(false);
    JFXDialogLayout layout = new JFXDialogLayout();
    layout.setHeading(new Label(message));
    JFXButton okButton = new JFXButton("OK");
    okButton.getStyleClass()
        .add("dialog-accept");
    okButton.setOnAction(event -> alert.hideWithAnimation());
    layout.setActions(okButton);
    alert.setContent(layout);
    alert.show();
  }

  /**
   * Right side panel methods (image list)
   */
  private void setUpImageList() {
    // Padding adjustments
    imageListView.setCellFactory(param -> {
      ImageListViewCell cell = new ImageListViewCell();
      cell.setPadding(new Insets(1, 4, 1, 0));
      return cell;
    });

    // On selection, prepare editor and load scaled image
    imageListView.getSelectionModel()
        .selectedItemProperty()
        .addListener((observable, oldValue, newValue) -> {
          if (newValue != null) {
            switchImage(newValue, oldValue);
          }
        });

    ContextMenu listItemContextMenu = new ContextMenu();
    MenuItem removeContextItem = new MenuItem(Translator.getString("delete"));

    removeContextItem.setOnAction(event -> {
      removeCurrentEditorItem();
    });
    listItemContextMenu.getItems()
        .setAll(removeContextItem);
    imageListView.setContextMenu(listItemContextMenu);
    imageListView.setOnKeyPressed(event -> {
      if (event.getCode() == KeyCode.DELETE) {
        removeCurrentEditorItem();
      }
    });
  }

  private void switchImage(UIEditorItem newValue, UIEditorItem oldValue) {
    ImageItem imageItem = newValue.getImageItem();
    EditorItem editorItem = newValue.getEditorItem();
    if (imageLoadProgressDialog != null) {
      imageLoadProgressDialog.close();
    }
    imageLoadProgressDialog = new ProgressDialog();
    imageLoadProgressDialog.setFailAlternative("Find image", () -> {
      int plugin = imageListView.getSelectionModel()
          .getSelectedItem()
          .getImageItem()
          .getOpenerSourceId();
      try {
        showImportImagesDialog(plugin, true);
      } catch (PluginLoader.PluginNotFoundException e) {
        showPluginErrorDialog("There was an error loading the plugin that opened this file.");
      }
    });
    imageLoadProgressDialog.show(
        new LoadImageTask(imageItem.getOpenerSourceId(), imageItem.getActivePath(),
            new LoadImageTask.ResultListener() {
              @Override public void onImageLoaded(Image image) {
                Platform.runLater(() -> {
                  metaTabPageController.addRootTreeItem(imageItem.getMetadata());
                  imageEditorStackGroup.clearList();
                  layerTabPageController.clearList();
                  imageEditorStackGroup.setImage(new PixelatedImageView(image));

                  imageEditorStackGroup.setCurrentScale(editorItem.getScaleRatio());
                  imageEditorScrollPane.loadEditorItem(editorItem, imageEditorStackGroup,
                      layerTabPageController);
                  if (editorItem.hasScaleRatio()) {
                    convertUnitsMenuItem.setDisable(false);
                  } else {
                    convertUnitsMenuItem.setDisable(true);
                  }
                  layerTabPageController.setListener(MainDialogController.this);
                  layerTabPageController.setCurrentScale(imageEditorStackGroup.getCurrentScale());
                  imageEditorStackGroup.setBounds(imageEditorStackGroup.parentToLocal(
                      imageEditorStackGroup.getBoundsInParent()));
                  setEditorEnable(true);

                  if (oldValue == null && !editorItem.getLayers()
                      .isEmpty()) {
                    miscTabPane.getSelectionModel()
                        .select(0);
                  }
                });
              }

              @Override public void onImageLoadFail() {
                imageEditorStackGroup.clearList();
                metaTabPageController.clearRootTreeItem();
                layerTabPageController.clearList();
                setEditorEnable(false);
              }
            }, imageManager), false, true, imageEditorStackPane);
  }

  private void addImageFiles(int pluginId, List<File> files) {
    if (files == null || files.size() <= 0) return;

    ProgressDialog progressDialog = new ProgressDialog();
    progressDialog.show(new ImageImporterTask(imageManager, files, pluginId, imageItems -> {
      Platform.runLater(() -> {
        for (ImageItem item : imageItems) {
          imageListView.getItems()
              .add(new UIEditorItem(item));
        }
        sessionManager.syncSession(new ArrayList<>(imageListView.getItems()));
        exportCSVMenuItem.setDisable(false);
        System.out.println("ImageListView size:" + imageListView.getItems()
            .size());
        System.out.println("Free Memory: " + Runtime.getRuntime()
            .freeMemory());
      });
    }), true, false, stackPane);
  }

  private void replaceCurrentImageFile(int pluginId, int index, File file) {
    if (file == null) return;

    ProgressDialog progressDialog = new ProgressDialog();
    progressDialog.show(
        new ImageImporterTask(imageManager, Arrays.asList(file), pluginId, imageItems -> {
          Platform.runLater(() -> {
            for (ImageItem item : imageItems) {
              UIEditorItem uiEditorItem = imageListView.getItems()
                  .get(index);
              if (uiEditorItem != null) {
                ImageItem imageItem = uiEditorItem.getImageItem();
                imageItem.setPath(item.getOriginalPath());
                imageItem.setActivePath(item.getActivePath());
                sessionManager.syncSession(new ArrayList<>(imageListView.getItems()));
                exportCSVMenuItem.setDisable(false);
                imageListView.getItems()
                    .set(index, uiEditorItem);
                switchImage(uiEditorItem, null);
              }
            }
          });
        }), true, false, stackPane);
  }

  private void addImages(List<EditorItem> items) {
    Task task = new Task<List<UIEditorItem>>() {
      @Override protected List<UIEditorItem> call() throws Exception {
        ArrayList<UIEditorItem> uiEditorItems = new ArrayList<>();
        for (EditorItem item : items) {
          File file = new File(item.getSourceImagePath());
          if (file.exists()) {
            ImageItem imageItem = new ImageItem(
                imageManager.retrieveImage(item.getOpenedWith(), file.getAbsolutePath()));
            uiEditorItems.add(new UIEditorItem(item, imageItem));
          }
        }
        return uiEditorItems;
      }

      @Override protected void succeeded() {
        try {
          List<UIEditorItem> editorItems = get();
          for (UIEditorItem item : editorItems) {
            imageListView.getItems()
                .add(item);
          }
          exportCSVMenuItem.setDisable(false);
          System.out.println("ImageListView size:" + imageListView.getItems()
              .size());
          System.out.println("Free Memory: " + Runtime.getRuntime()
              .freeMemory());
        } catch (InterruptedException e) {
          //e.printStackTrace();
          //TODO: Catch exception
        } catch (ExecutionException e) {
          //e.printStackTrace();
          //TODO: Catch exception
        }
        super.succeeded();
      }
    };
    new Thread(task).start();
  }

  /**
   * Main editor methods
   */
  private void loadPreferencesAndPanes() {
    // Preferences
    Color currentPickedColor = ImageEditorStackGroup.DEFAULT_COLOR;
    String pickedColor = prefs.get(Constants.STTGS_COLOR_PICKER, "RED");
    double angle = prefs.getDouble(Constants.STTGS_ANGLE_PICKER, 90.0);
    if (!pickedColor.equals("RED")) currentPickedColor = Color.valueOf(pickedColor);
    ieColorPicker.setValue(currentPickedColor);
    ieDegreePicker.setText(NumberFormat.getInstance()
        .format(angle));

    // Initalize editor panes + groups.
    imageEditorStackGroup = new ImageEditorStackGroup(this, this, currentPickedColor, angle,
        statusLabel.textProperty());
    imageEditorScrollPane = new ZoomableScrollPane(imageEditorStackGroup, this);
    AnchorPane.setBottomAnchor(imageEditorScrollPane, 0.0);
    AnchorPane.setTopAnchor(imageEditorScrollPane, 0.0);
    AnchorPane.setLeftAnchor(imageEditorScrollPane, 0.0);
    AnchorPane.setRightAnchor(imageEditorScrollPane, 0.0);
    imageEditorStackPane.getChildren()
        .setAll(imageEditorScrollPane);
  }

  private void setEditorButtonListeners() {
    DecimalFormat format = new DecimalFormat("#.##");
    ieDegreePicker.setTextFormatter(new TextFormatter<>(c -> {
      if (c.getControlNewText()
          .isEmpty()) {
        imageEditorStackGroup.setAddedLineAngle(0);
        return c;
      }
      ParsePosition parsePosition = new ParsePosition(0);
      Object object = format.parse(c.getControlNewText(), parsePosition);

      if (object == null || parsePosition.getIndex() < c.getControlNewText()
          .length()) {
        return null;
      } else {
        if (((Number) object).doubleValue() > 360) {
          return null;
        }
        imageEditorStackGroup.setAddedLineAngle(((Number) object).doubleValue());
        return c;
      }
    }));

    zoomButton.setOnAction(event -> {
      if (imageEditorStackGroup != null) {
        imageEditorStackGroup.setCurrentMode(ImageEditorStackGroup.Mode.ZOOM);
      }
    });

    handButton.setOnAction(event -> {
      if (imageEditorStackGroup != null) {
        imageEditorStackGroup.setCurrentMode(ImageEditorStackGroup.Mode.PAN);
      }
    });

    ieColorPicker.setOnAction(
        event -> imageEditorStackGroup.setCurrentPickedColor(ieColorPicker.getValue()));

    editorCursorBtn.setOnMouseClicked(event -> {
      if (imageEditorStackGroup != null) {
        imageEditorStackGroup.setCurrentMode(ImageEditorStackGroup.Mode.SELECT);
      }
    });

    editorLineBtn.setOnMouseClicked(event -> {
      if (imageEditorStackGroup != null) {
        imageEditorStackGroup.setCurrentMode(ImageEditorStackGroup.Mode.LINE_CREATION);
      }
    });

    editorAngBtn.setOnMouseClicked(event -> {
      if (imageEditorStackGroup != null) {
        imageEditorStackGroup.setCurrentMode(ImageEditorStackGroup.Mode.LINE_ANG_SEL);
      }
    });

    editorAreaBtn.setOnAction(event -> {
      if (imageEditorStackGroup != null) {
        imageEditorStackGroup.setCurrentMode(ImageEditorStackGroup.Mode.AREA_CREATION);
      }
    });

    angleBtn.setOnAction(event -> {
      if (imageEditorStackGroup != null) {
        imageEditorStackGroup.setCurrentMode(ImageEditorStackGroup.Mode.ANGLE_CREATION);
      }
    });
  }

  private void setLineEditorVisual(ImageEditorStackGroup.Mode mode) {
    editorCursorBtn.setBackground(null);
    editorLineBtn.setBackground(null);
    editorAngBtn.setBackground(null);
    editorAreaBtn.setBackground(null);
    handButton.setBackground(null);
    zoomButton.setBackground(null);
    angleBtn.setBackground(null);
    ieColorPicker.setVisible(false);
    ieDegreeLabel.setVisible(false);
    ieDegreePicker.setVisible(false);
    imageEditorToolsSecondaryPane.setVisible(false);
    imageEditorStackGroup.setCursor(Cursor.DEFAULT);
    if (mode == null) return;
    switch (mode) {
      case PAN:
        handButton.setBackground(new Background(
            new BackgroundFill(Color.valueOf("#cceaf4"), CornerRadii.EMPTY, Insets.EMPTY)));
        Image imageCursor = new Image("images/hand_cursor.png");
        // Can't use urls in setStyle (bugged) - https://bugs.openjdk.java.net/browse/JDK-8089191
        imageEditorStackGroup.setCursor(
            new ImageCursor(imageCursor, imageCursor.getWidth() / 2, imageCursor.getHeight() / 2));
        break;
      case ZOOM:
        zoomButton.setBackground(new Background(
            new BackgroundFill(Color.valueOf("#cceaf4"), CornerRadii.EMPTY, Insets.EMPTY)));
        Image image = new Image("images/magnifier_cursor.png");
        imageEditorStackGroup.setCursor(
            new ImageCursor(image, image.getWidth() / 2, image.getHeight() / 2));
        break;
      case SELECT:
        editorCursorBtn.setBackground(new Background(
            new BackgroundFill(Color.valueOf("#cceaf4"), CornerRadii.EMPTY, Insets.EMPTY)));
        break;
      case LINE_ANG:
        editorLineBtn.setBackground(new Background(
            new BackgroundFill(Color.valueOf("#cceaf4"), CornerRadii.EMPTY, Insets.EMPTY)));
        imageEditorStackGroup.setCursor(Cursor.CROSSHAIR);
        imageEditorToolsSecondaryPane.setVisible(true);
        ieColorPicker.setVisible(true);
        break;
      case LINE_CREATION:
        editorLineBtn.setBackground(new Background(
            new BackgroundFill(Color.valueOf("#cceaf4"), CornerRadii.EMPTY, Insets.EMPTY)));
        imageEditorStackGroup.setCursor(Cursor.CROSSHAIR);
        imageEditorToolsSecondaryPane.setVisible(true);
        ieColorPicker.setVisible(true);
        break;
      case LINE_ANG_SEL:
        editorAngBtn.setBackground(new Background(
            new BackgroundFill(Color.valueOf("#cceaf4"), CornerRadii.EMPTY, Insets.EMPTY)));
        imageEditorToolsSecondaryPane.setVisible(true);
        ieDegreeLabel.setVisible(true);
        ieDegreePicker.setVisible(true);
        break;
      case AREA_CREATION:
        editorAreaBtn.setBackground(new Background(
            new BackgroundFill(Color.valueOf("#cceaf4"), CornerRadii.EMPTY, Insets.EMPTY)));
        imageEditorStackGroup.setCursor(Cursor.CROSSHAIR);
        imageEditorToolsSecondaryPane.setVisible(true);
        ieColorPicker.setVisible(true);
        break;
      case ANGLE_CREATION:
        angleBtn.setBackground(new Background(
            new BackgroundFill(Color.valueOf("#cceaf4"), CornerRadii.EMPTY, Insets.EMPTY)));
        imageEditorStackGroup.setCursor(Cursor.CROSSHAIR);
        break;
    }
  }

  private void loadInitialState() {
    saveSessionAsMenuItem.setDisable(true);
    saveSessionMenuItem.setDisable(true);
    importImagesMenuItem.setDisable(true);
    exportCSVMenuItem.setDisable(true);
    imageListView.setDisable(true);
    setEditorEnable(false);
  }

  public void setEditorEnable(boolean editorEnable) {
    if (!editorEnable) {
      editorCursorBtn.setDisable(true);
      editorLineBtn.setDisable(true);
      editorAngBtn.setDisable(true);
      editorAreaBtn.setDisable(true);
      angleBtn.setDisable(true);
      handButton.setDisable(true);
      zoomButton.setDisable(true);
      conversionMenu.setDisable(true);
      miscTabPane.getSelectionModel()
          .select(1);
      miscTabPane.setDisable(true);
      setLineEditorVisual(null);
    } else {
      editorCursorBtn.setDisable(false);
      editorLineBtn.setDisable(false);
      editorAngBtn.setDisable(false);
      editorAreaBtn.setDisable(false);
      angleBtn.setDisable(false);
      handButton.setDisable(false);
      zoomButton.setDisable(false);
      miscTabPane.setDisable(false);
      conversionMenu.setDisable(false);
    }
    if (sessionManager.getSession() != null) {
      imageListView.setDisable(false);
      saveSessionAsMenuItem.setDisable(false);
      saveSessionMenuItem.setDisable(false);
      importImagesMenuItem.setDisable(false);
      if (pluginImportImagesMenuItem != null) pluginImportImagesMenuItem.setDisable(false);
      if (imageListView.getItems()
          .size() > 0) {
        undistortMenuItem.setDisable(false);
        exportCSVMenuItem.setDisable(false);
        filtersMenu.setDisable(false);
      } else {
        exportCSVMenuItem.setDisable(true);
        undistortMenuItem.setDisable(true);
        filtersMenu.setDisable(true);
      }
    }
  }

  /**
   * ImageEditorStackGroup listener callbacks
   */
  @Override public void onModeChange(ImageEditorStackGroup.Mode mode) {
    imageEditorScrollPane.setCurrentMode(mode);
    setLineEditorVisual(mode);
  }

  @Override public void onLineAdd(SegLineGroup line, boolean editorItemLoad) {
    if (!editorItemLoad) {
      getSelectedEditorItem().getEditorItem()
          .getLayers()
          .add(new EditorItemSegLine(line));
      miscTabPane.getSelectionModel()
          .select(0);
    }
    layerTabPageController.addLayer(line);

    imageEditorStackGroup.setColorHelperLinesVisible(identifierLinesCheckItem.isSelected());
    imageEditorStackGroup.setLayerHelperLinesVisible(precisionLinesCheckItem.isSelected());
  }

  @Override public void onAngleAdd(AngleGroup angle, boolean editorItemLoad) {
    if (!editorItemLoad) {
      getSelectedEditorItem().getEditorItem()
          .getLayers()
          .add(new EditorItemAngle(angle));
      miscTabPane.getSelectionModel()
          .select(0);
    }
    layerTabPageController.addLayer(angle);
  }

  @Override public void onAreaAdd(AreaGroup area, boolean sessionLoad) {
    if (!sessionLoad) {
      getSelectedEditorItem().getEditorItem()
          .getLayers()
          .add(new EditorItemArea(area));
      miscTabPane.getSelectionModel()
          .select(0);
    }
    layerTabPageController.addLayer(area);
  }

  @Override public void onLineChange(SegLineGroup lineGroup) {
    ArrayList<EditorItemLayer> list = getSelectedEditorItem().getEditorItem()
        .getLayers();
    for (int i = 0; i < list.size(); i++) {
      EditorItemLayer item = list.get(i);
      if (item.getIdentifier()
          .equals(lineGroup.getName())) {
        list.set(i, new EditorItemSegLine(lineGroup));
        break;
      }
    }

    layerTabPageController.refreshList();
  }

  @Override public void onAreaChange(AreaGroup areaGroup) {
    ArrayList<EditorItemLayer> list = getSelectedEditorItem().getEditorItem()
        .getLayers();
    for (int i = 0; i < list.size(); i++) {
      if (list.get(i) instanceof AreaGroup) {
        AreaGroup item = (AreaGroup) list.get(i);
        if (item.getPrimaryText()
            .equals(areaGroup.getPrimaryText())) {
          list.set(i, new EditorItemArea(areaGroup));
        }
      }
    }
    layerTabPageController.refreshList();
  }

  @Override public void onAngleChange(AngleGroup angleGroup) {
    ArrayList<EditorItemLayer> list = getSelectedEditorItem().getEditorItem()
        .getLayers();
    for (int i = 0; i < list.size(); i++) {
      EditorItemLayer item = list.get(i);
      if (item.getIdentifier()
          .equals(angleGroup.getName())) {
        list.set(i, new EditorItemAngle(angleGroup));
        break;
      }
    }

    layerTabPageController.refreshList();
  }

  /**
   * Menu Items
   */
  @FXML public void onIdentifierLinesToggle(ActionEvent actionEvent) {
    CheckMenuItem item = (CheckMenuItem) actionEvent.getSource();
    if (item.isSelected()) {
      imageEditorStackGroup.setColorHelperLinesVisible(true);
    } else {
      imageEditorStackGroup.setColorHelperLinesVisible(false);
    }
  }

  @FXML public void onPrecisionLinesToggle(ActionEvent actionEvent) {
    CheckMenuItem item = (CheckMenuItem) actionEvent.getSource();
    if (item.isSelected()) {
      imageEditorStackGroup.setLayerHelperLinesVisible(true);
    } else {
      imageEditorStackGroup.setLayerHelperLinesVisible(false);
    }
  }

  @FXML public void convertViaScale(ActionEvent actionEvent) {
    ScaleDialogController scaleDialog = new ScaleDialogController();
    scaleDialog.init(editorCursorBtn.getScene()
        .getWindow(), this, imageEditorStackGroup.getLines());
  }

  @FXML public void convertViaRatioDf(ActionEvent actionEvent) {
    DistanceScaleDialogController scaleDialog = new DistanceScaleDialogController();
    scaleDialog.init(editorCursorBtn.getScene()
        .getWindow(), this, getSelectedEditorItem().getImageItem());
  }

  @FXML public void convertUnits(ActionEvent actionEvent) {
    ConvertUnitsDialogController converUnitsDialog = new ConvertUnitsDialogController();

    converUnitsDialog.init(editorCursorBtn.getScene()
            .getWindow(), this, imageEditorStackGroup.getLines(),
        imageEditorStackGroup.getCurrentScale());
  }

  private UIEditorItem getSelectedEditorItem() {
    return imageListView.getSelectionModel()
        .getSelectedItem();
  }

  /**
   * For reference scaling
   */
  @Override public void onApplyScale(ScaleRatio scaleRatio, boolean allImages) {
    imageEditorStackGroup.setCurrentScale(scaleRatio);
    layerTabPageController.setCurrentScale(scaleRatio);

    if (allImages) {
      imageListView.getItems()
          .forEach((item) -> {
            item.getEditorItem()
                .setScaleRatio(scaleRatio);
          });
    } else {
      getSelectedEditorItem().getEditorItem()
          .setScaleRatio(scaleRatio);
    }
    convertUnitsMenuItem.setDisable(false);
  }

  @Override public void onChangeScale(ScaleRatio oldScale, ScaleRatio newScale, boolean allImages) {
    imageEditorStackGroup.setCurrentScale(newScale);
    layerTabPageController.setCurrentScale(newScale);
    if (allImages) {
      imageListView.getItems()
          .forEach((item) -> {
            ScaleRatio itemScaleRatio = item.getEditorItem()
                .getScaleRatio();
            if (itemScaleRatio != null && itemScaleRatio.getUnits() != null && item.getEditorItem()
                .getScaleRatio()
                .getUnits()
                .equals(oldScale.getUnits())) {
              item.getEditorItem()
                  .setScaleRatio(newScale);
            }
          });
    } else {
      getSelectedEditorItem().getEditorItem()
          .setScaleRatio(newScale);
    }
    convertUnitsMenuItem.setDisable(false);
  }

  /**
   * For camera opencv.calibration
   */

  @FXML public void onCalibrateCamera(ActionEvent actionEvent) {
    try {
      OpenCVManager.loadOpenCV();
      CalibrationDialogController calibrationDialogController = new CalibrationDialogController();
      calibrationDialogController.init(editorCursorBtn.getScene()
          .getWindow());
    } catch (SecurityException | UnsatisfiedLinkError e) {
      System.err.println("Could not locate dll");
    }
  }

  /**
   * LayerTabPageController listener callbacks
   */

  @Override public void onRemoveLayer(String name) {
    ArrayList<EditorItemLayer> list = getSelectedEditorItem().getEditorItem()
        .getLayers();
    for (int i = 0; i < list.size(); i++) {
      EditorItemLayer item = list.get(i);
      if (item.getIdentifier()
          .equals(name)) {
        list.remove(i);
        break;
      }
    }
    imageEditorStackGroup.removeLineGroup(name);
  }

  @Override public void onRenameLayer(String oldName, String newName) {
    ArrayList<EditorItemLayer> list = getSelectedEditorItem().getEditorItem()
        .getLayers();
    for (int i = 0; i < list.size(); i++) {
      EditorItemLayer item = list.get(i);
      if (item.getIdentifier()
          .equals(oldName)) {
        item.setIdentifier(newName);
        break;
      }
    }
    imageEditorStackGroup.renameLineGroup(oldName, newName);
  }

  @Override public void onEquationAdd(EquationItem equationItem) {
    getSelectedEditorItem().getEditorItem()
        .getLayers()
        .add(equationItem);
  }

  @Override public void onZoomChange(double hValue, double vValue) {
    getSelectedEditorItem().getEditorItem()
        .updateZoomAndScale(imageEditorScrollPane);
  }

  /**
   * File MenuItems
   */
  @FXML public void onNewSession(ActionEvent actionEvent) {
    if (sessionManager.getSession() != null) {
      loadSessionWithSureDialog(sessionManager.getSession(), sessionManager.newSession());
    } else {
      loadSession(sessionManager.newSession());
    }
  }

  //TODO: Refactor this so we dont repeat code
  private void loadSessionWithSureDialog(Session oldSession, Session newSession) {
    String sessionName = oldSession.getName();
    if (sessionName == null) sessionName = "Unnamed";
    String dialogMessage = "Do you want to save changes to " + sessionName + "?";
    JFXAlert alert = new JFXAlert((Stage) zoomButton.getScene()
        .getWindow());
    alert.initModality(Modality.APPLICATION_MODAL);
    alert.setOverlayClose(false);
    JFXDialogLayout layout = new JFXDialogLayout();
    layout.setHeading(new Label(dialogMessage));
    JFXButton saveButton = new JFXButton("Save");
    JFXButton dontSaveButton = new JFXButton("Don't Save");
    JFXButton cancelButton = new JFXButton("Cancel");
    cancelButton.getStyleClass()
        .add("dialog-accept");
    cancelButton.setOnAction(event -> alert.hideWithAnimation());
    dontSaveButton.setOnAction(event -> {
      loadSession(newSession);
      alert.hideWithAnimation();
    });
    saveButton.setOnAction(event -> {
      boolean saved = saveCurrentSession();
      if (saved) loadSession(newSession);
      alert.hideWithAnimation();
    });
    layout.setActions(saveButton, dontSaveButton, cancelButton);
    alert.setContent(layout);
    alert.show();
  }

  private void closeAppWithSureDialog(Session session) {
    String sessionName = session.getName();
    if (sessionName == null) sessionName = "Unnamed";
    String dialogMessage = "Do you want to save changes to " + sessionName + "?";
    JFXAlert alert = new JFXAlert((Stage) zoomButton.getScene()
        .getWindow());
    alert.initModality(Modality.APPLICATION_MODAL);
    alert.setOverlayClose(false);
    JFXDialogLayout layout = new JFXDialogLayout();
    layout.setHeading(new Label(dialogMessage));
    JFXButton saveButton = new JFXButton("Save");
    JFXButton dontSaveButton = new JFXButton("Don't Save");
    JFXButton cancelButton = new JFXButton("Cancel");
    cancelButton.getStyleClass()
        .add("dialog-accept");
    cancelButton.setOnAction(event -> alert.hideWithAnimation());
    dontSaveButton.setOnAction(event -> {
      alert.hideWithAnimation();
      imageManager.clearTmpFolder();
      Platform.exit();
    });
    saveButton.setOnAction(event -> {
      boolean saved = saveCurrentSession();
      alert.hideWithAnimation();
      if (saved) {
        imageManager.clearTmpFolder();
        Platform.exit();
      }
    });
    layout.setActions(saveButton, dontSaveButton, cancelButton);
    alert.setContent(layout);
    alert.show();
  }

  @FXML public void onOpenSession(ActionEvent actionEvent) {
    FileChooser ch = new FileChooser();
    String path = prefs.get(Constants.STTGS_FILECHOOSER_OPEN, "");
    if (path.length() > 0) {
      File folder = new File(path);
      if (folder.exists() && folder.isDirectory()) {
        ch.setInitialDirectory(new File(path));
      }
    }
    ch.getExtensionFilters()
        .add(new FileChooser.ExtensionFilter(Translator.getString("axmlfilechooser"), "*.axml"));
    ch.setTitle(Translator.getString("open"));

    File file = ch.showOpenDialog(imageListView.getScene()
        .getWindow());
    if (file != null && file.exists()) {
      prefs.put(Constants.STTGS_FILECHOOSER_OPEN, file.getParent());
      try {
        if (sessionManager.getSession() != null) {
          loadSessionWithSureDialog(sessionManager.getSession(), sessionManager.openSession(file));
        } else {
          loadSession(sessionManager.openSession(file));
        }
      } catch (FileNotFoundException e) {
        //TODO
        e.printStackTrace();
      }
    }
  }

  private void removeCurrentEditorItem() {
    imageListView.getItems()
        .remove(imageListView.getSelectionModel()
            .getSelectedIndex());
    if (imageListView.getItems()
        .size() == 0) {
      imageEditorStackGroup.clearList();
      imageEditorStackGroup.getChildren()
          .clear();
      metaTabPageController.clearRootTreeItem();
      layerTabPageController.clearList();
    }
  }

  private void loadSession(Session session) {
    // Reset everything
    imageListView.getItems()
        .clear();
    imageEditorStackGroup.clearList();
    imageEditorStackGroup.getChildren()
        .clear();
    metaTabPageController.clearRootTreeItem();
    layerTabPageController.clearList();
    if (imageLoadProgressDialog != null) imageLoadProgressDialog.close();

    // Set the new session
    ArrayList<EditorItem> items = session.getItems();
    addImages(items);
    setEditorEnable(false);
    if (session.getPath() != null && !session.getPath()
        .isEmpty()) {
      File file = new File(session.getPath());
      if (file.exists()) {
        MainApplication.setStageName("AragoJ - " + session.getName());
      } else {
        MainApplication.setStageName("AragoJ - Unnamed");
      }
    } else {
      MainApplication.setStageName("AragoJ - Unnamed");
    }
  }

  @FXML public void onSaveSession(ActionEvent actionEvent) {
    saveCurrentSession();
  }

  @FXML public void onSaveSessionAs(ActionEvent actionEvent) {
    saveCurrentSessionAs();
  }

  private boolean saveCurrentSession() {
    sessionManager.syncSession(new ArrayList<>(imageListView.getItems()));
    if (!sessionManager.saveSession()) {
      return saveCurrentSessionAs();
    }
    return true;
  }

  private boolean saveCurrentSessionAs() {
    if (sessionManager.getSession() == null) return false;

    FileChooser ch = new FileChooser();
    String path = prefs.get(Constants.STTGS_FILECHOOSER_SAVEAS, "");
    if (path.length() > 0) {
      File folder = new File(path);
      if (folder.exists() && folder.isDirectory()) {
        ch.setInitialDirectory(new File(path));
      }
    }
    ch.getExtensionFilters()
        .add(new FileChooser.ExtensionFilter(Translator.getString("axmlfilechooser"), "*.axml"));
    ch.setTitle(Translator.getString("saveAs"));
    File file = ch.showSaveDialog(imageListView.getScene()
        .getWindow());
    if (file != null) {
      prefs.put(Constants.STTGS_FILECHOOSER_SAVEAS, file.getParent());
      sessionManager.getSession()
          .setPath(file.getPath());
      MainApplication.setStageName("AragoJ - " + sessionManager.getSession()
          .getName());
      sessionManager.syncSession(new ArrayList<>(imageListView.getItems()));
      return sessionManager.saveSession();
    }
    return false;
  }

  @FXML private void onImportImages() {
    try {
      showImportImagesDialog(-1, false);
    } catch (PluginLoader.PluginNotFoundException e) {
      // Not really possible
    }
  }

  private void showImportImagesDialog(int pluginId, boolean singleImageReplaceCurrent)
      throws PluginLoader.PluginNotFoundException {
    if (sessionManager.getSession() == null) return;
    FileChooser ch = new FileChooser();
    String path = prefs.get(Constants.STTGS_FILECHOOSER_LASTOPENED, "");
    if (path.length() > 0) {
      File folder = new File(path);
      if (folder.exists() && folder.isDirectory()) {
        ch.setInitialDirectory(new File(path));
      }
    }
    if (pluginId == -1) {
      ch.getExtensionFilters()
          .addAll(imageManager.getDefaultExtensionFilters());
    } else {
      ch.getExtensionFilters()
          .addAll(imageManager.getPluginExtensionFilters(pluginId));
    }

    if (singleImageReplaceCurrent) {
      ch.setTitle(Translator.getString("chooseimage"));
      File file = ch.showOpenDialog(imageListView.getScene()
          .getWindow());
      if (file != null) {
        prefs.put(Constants.STTGS_FILECHOOSER_LASTOPENED, file.getParent());
        replaceCurrentImageFile(pluginId, imageListView.getSelectionModel()
            .getSelectedIndex(), file);
      }
    } else {
      ch.setTitle(Translator.getString("chooseimages"));
      List<File> files = ch.showOpenMultipleDialog(imageListView.getScene()
          .getWindow());
      if (files != null && files.size() > 0) {
        prefs.put(Constants.STTGS_FILECHOOSER_LASTOPENED, files.get(0)
            .getParent());
        addImageFiles(pluginId, files);
      }
    }
  }

  @FXML public void onExportCSV(ActionEvent actionEvent) {
    if (sessionManager.getSession() == null) return;
    FileChooser ch = new FileChooser();
    String path = prefs.get(Constants.STTGS_FILECHOOSER_EXPORTCSV_LASTOPENED, "");
    if (path.length() > 0) {
      File folder = new File(path);
      if (folder.exists() && folder.isDirectory()) {
        ch.setInitialDirectory(new File(path));
      }
    }
    ch.getExtensionFilters()
        .add(new FileChooser.ExtensionFilter(Translator.getString("csvfilechooser"), "*.csv"));
    ch.setTitle(Translator.getString("exportcsv"));

    File file = ch.showSaveDialog(imageListView.getScene()
        .getWindow());
    if (file != null) {
      prefs.put(Constants.STTGS_FILECHOOSER_EXPORTCSV_LASTOPENED, file.getParent());
      sessionManager.syncSession(new ArrayList<>(imageListView.getItems()));
      ExportCSV.export(file, sessionManager.getSession(), imageListView.getItems());
    }
  }

  @FXML public void onExitClick(ActionEvent actionEvent) {
    ExportPreferences.exportExportPreferences();
    if (sessionManager.getSession() != null) {
      closeAppWithSureDialog(sessionManager.getSession());
    } else {
      Platform.exit();
    }
  }

  public void onImageDropped(DragEvent dragEvent) {
    Dragboard board = dragEvent.getDragboard();
    List<File> files = board.getFiles();
    List<File> finalFiles = new ArrayList<>();
    for (File file : files) {
      String extension = Utility.getFilePathExtension(file.getPath());
      if (extension != null && Utility.isImageExtensionSupported(extension)) {
        finalFiles.add(file);
      }
    }
    Collections.sort(finalFiles);
    addImageFiles(-1, finalFiles);
  }

  public void onImageListDragOver(DragEvent dragEvent) {
    Dragboard board = dragEvent.getDragboard();
    if (board.hasFiles()) {
      for (File file : board.getFiles()) {
        String extension = Utility.getFilePathExtension(file.getPath());
        if (extension != null && Utility.isImageExtensionSupported(extension)) {
          dragEvent.acceptTransferModes(TransferMode.ANY);
          return;
        }
      }
    }
  }

  public void onUndistortClick(ActionEvent actionEvent) {
    try {
      OpenCVManager.loadOpenCV();
      UndistortDialog undistortDialog = new UndistortDialog();
      undistortDialog.init(editorCursorBtn.getScene()
          .getWindow(), this);
    } catch (SecurityException | UnsatisfiedLinkError e) {
      System.err.println("Could not locate dll");
    }
  }

  @Override public void onApplyUndistort(CalibrationModel calibrationModel, boolean applyToAll) {
    FXMLLoader loader =
        new FXMLLoader(getClass().getResource("/fmxl/ProgressDialog.fxml"), Translator.getBundle());
    UndistortProgressDialogController controller = new UndistortProgressDialogController(this);
    loader.setController(controller);
    try {
      Parent root = loader.load();
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }
    final List<ImageItem> imageItems = new ArrayList<>();
    if (applyToAll) {
      imageListView.getItems()
          .forEach((item) -> {
            imageItems.add(item.getImageItem());
          });
    } else {
      imageItems.add(imageListView.getSelectionModel()
          .getSelectedItem()
          .getImageItem());
    }
    controller.undistortImages(calibrationModel, imageItems, imageListView.getSelectionModel()
        .getSelectedIndices()
        .get(0), stackPane);
  }

  @Override public void onImageItemUndistorted(int index, String newPath, boolean select) {
    try {
      UIEditorItem uiEditorItem = imageListView.getItems()
          .get(index);
      imageListView.getItems()
          .get(index)
          .setImageItem(new ImageItem(imageManager.retrieveImage(uiEditorItem.getImageItem()
              .getOpenerSourceId(), newPath)));
      if (select) {
        imageListView.getSelectionModel()
            .select(index);
      }
    } catch (IOException | ImageManager.ImageProcessingException | ImageReaderPlugin.FormatNotSupported e) {
      // Do nothing
    }
  }

  public void onClearFilters(ActionEvent actionEvent) throws ImageProcessingException, IOException {
    try {
      ImageItem imageItem = imageListView.getSelectionModel()
          .getSelectedItem()
          .getImageItem();
      imageItem.setActivePath(imageItem.getOriginalPath());
      Image thumbnail =
          imageManager.loadThumbnail(imageItem.getOpenerSourceId(), imageItem.getActivePath());
      if (thumbnail != null) {
        imageItem.setThumbnail(thumbnail);
      }
      changeCurrentImageItem(imageItem,
          imageManager.loadImage(imageItem.getOpenerSourceId(), imageItem.getActivePath()));
    } catch (ImageManager.ImageProcessingException | ImageReaderPlugin.FormatNotSupported e) {
      e.printStackTrace();
    }
  }

  public void onEdgeDetection(ActionEvent actionEvent) {
    try {
      OpenCVManager.loadOpenCV();
      FilterDialog filterDialog = new FilterDialog();
      filterDialog.init(editorCursorBtn.getScene()
          .getWindow(), this, imageManager.getCachedImage(), Translator.getString("edgeDetection"));
    } catch (SecurityException | UnsatisfiedLinkError e) {
      System.err.println("Could not locate dll");
    }
  }

  private void changeCurrentImageItem(ImageItem imageItem, Image image) {
    imageListView.getSelectionModel()
        .getSelectedItem()
        .setImageItem(imageItem);
    imageEditorStackGroup.setImage(new PixelatedImageView(image));
    imageListView.refresh();
  }

  @Override public void onApplyFilter(Filter filter, FilterArguments filterArguments) {
    ImageItem imageItem = imageListView.getSelectionModel()
        .getSelectedItem()
        .getImageItem();
    ProgressDialog progressDialog = new ProgressDialog();
    progressDialog.show(new FilterTask(new FilterTask.ResultListener() {
          @Override
          public void onFilterFinished(Filter filter, FilterArguments filterArguments, Image image,
              String path) {
            Platform.runLater(() -> {
              ImageItem imageItem = imageListView.getSelectionModel()
                  .getSelectedItem()
                  .getImageItem();
              imageItem.setActivePath(path);
              Image thumbnail =
                  imageManager.loadThumbnail(imageItem.getOpenerSourceId(), imageItem.getActivePath());
              if (thumbnail != null) {
                imageItem.setThumbnail(thumbnail);
              }
              changeCurrentImageItem(imageItem, image);
            });
          }

          @Override
          public void onFilterFailed(Filter filter, FilterArguments filterArguments, Image image) {

          }
        }, filter, filterArguments, imageManager.getCachedImage(), imageItem.getOriginalPath()), true,
        false, stackPane);
  }
}
