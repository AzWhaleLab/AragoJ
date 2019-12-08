package opencv.calibration.ui;

import com.jfoenix.controls.JFXAlert;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextField;
import opencv.calibration.CalibrationImageManager;
import opencv.calibration.model.CalibImageItem;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import opencv.calibration.file.CalibrationFileManager;
import opencv.calibration.model.CalibrationConfig;
import opencv.calibration.model.CalibrationImage;
import opencv.calibration.model.CalibrationModel;
import opencv.calibration.model.CalibrationResults;
import ui.MainApplication;
import ui.custom.ImageEditorStackGroup;
import ui.custom.PixelatedImageView;
import ui.custom.ZoomableScrollPane;
import utils.Constants;
import utils.Translator;
import utils.Utility;

public class CalibrationDialogController implements ImageEditorStackGroup.ModeListener,
    CalibrationProgressDialogController.ProgressListener,
    CalibConfigDialogController.OnActionListener {

  private Stage stage;
  private Preferences prefs;
  private int currentItem = -1;

  private CalibrationSession session;

  @FXML public JFXListView<CalibImageItem> imageListView;
  @FXML public AnchorPane imageEditorAnchorPane;
  @FXML public StackPane stackPane;

  @FXML private JFXButton runCalibrationButton;
  @FXML private JFXButton handButton;
  @FXML private JFXButton zoomButton;

  @FXML private Label currentConfigLabel;
  @FXML private TextArea outputTextArea;

  @FXML private MenuItem saveCalibrationMenuItem;
  @FXML private MenuItem exportCalibrationMenuItem;
  @FXML private MenuItem runCalibrationMenuItem;

  private ImageEditorStackGroup imageEditorStackGroup;
  private ZoomableScrollPane imageEditorScrollPane;
  private PixelatedImageView img;
  private CalibrationResults calibrationResults;
  private CalibrationConfig calibrationConfig;

  public void init(Window owner) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fmxl/CalibrationDialog.fxml"),
          Translator.getBundle());
      loader.setController(this);
      Parent root = loader.load();
      Scene scene = new Scene(root, 700, 500);
      final ObservableList<String> stylesheets = scene.getStylesheets();
      stylesheets.addAll(getClass().getResource("/css/Calibration.css")
          .toExternalForm());
      Stage stage = new Stage();
      stage.getIcons()
          .add(new Image(MainApplication.class.getResourceAsStream("/images/icon.png")));
      stage.setTitle(Translator.getString("cameraCalibration"));
      stage.setScene(scene);
      //            stage.initOwner(owner);
      //            stage.initModality(Modality.NONE);
      //            stage.setResizable(false);
      this.stage = stage;
      stage.show();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @FXML private void initialize() {
    prefs = Preferences.userNodeForPackage(MainApplication.class);
    initializePanes();
    loadPreferences();
    setUpImageList();
    setEditorButtonListeners();
    loadSession(new CalibrationSession());
  }

  private void loadPreferences() {
    Preferences calibPrefs = Preferences.userNodeForPackage(CalibrationConfig.class);
    String sPattern =
        calibPrefs.get(Constants.CALIB_PATTERN, CalibrationConfig.Pattern.CHESSBOARD.toString());
    CalibrationConfig.Pattern pattern = CalibrationConfig.Pattern.CHESSBOARD;
    if (sPattern.equalsIgnoreCase(CalibrationConfig.Pattern.CIRCLE_GRID.toString())) {
      pattern = CalibrationConfig.Pattern.CIRCLE_GRID;
    }
    int hrzPoints = calibPrefs.getInt(Constants.CALIB_HRZPOINTS, 11);
    int vertPoints = calibPrefs.getInt(Constants.CALIB_VERTPOINTS, 7);
    String toggle = calibPrefs.get(Constants.CALIB_LENS, "RECTILINEAR");

    CalibrationConfig calibrationConfig = new CalibrationConfig(pattern, hrzPoints, vertPoints, 1,
        CalibrationConfig.Lens.fromString(toggle));
    onConfigurationSave(calibrationConfig);
  }

  private void initializePanes() {
    // Initalize editor panes + groups.
    imageEditorStackGroup = new ImageEditorStackGroup(this, null, null, 0, null, null);
    imageEditorScrollPane = new ZoomableScrollPane(imageEditorStackGroup, null);
    AnchorPane.setBottomAnchor(imageEditorScrollPane, 0.0);
    AnchorPane.setTopAnchor(imageEditorScrollPane, 0.0);
    AnchorPane.setLeftAnchor(imageEditorScrollPane, 0.0);
    AnchorPane.setRightAnchor(imageEditorScrollPane, 0.0);
    imageEditorAnchorPane.getChildren()
        .setAll(imageEditorScrollPane);
  }

  private void loadSession(CalibrationSession session) {
    // Reset everything
    imageListView.getItems()
        .clear();
    imageEditorStackGroup.clearList();
    imageEditorStackGroup.getChildren()
        .clear();
    currentItem = -1;

    // Set the new session
    ArrayList<CalibrationImageItem> items = session.getItems();
    for (CalibrationImageItem item : items) {
      File file = new File(item.getSourceImagePath());
      if (file.exists()) {
        addImage(file);
      } else {
        //TODO: Show error / locate missing file
      }
    }
    this.session = session;
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

  private void setUpImageList() {
    // Padding adjustments
    imageListView.setCellFactory(param -> {
      CalibrationImageListViewCell cell = new CalibrationImageListViewCell();
      cell.setPadding(new Insets(1, 4, 1, 0));
      return cell;
    });

    // On selection, prepare editor and load scaled image
    imageListView.getSelectionModel()
        .selectedItemProperty()
        .addListener((observable, oldValue, newValue) -> {
          if (newValue != null) {
            int index = imageListView.getSelectionModel()
                .getSelectedIndex();

            // Save the current item before switching
            if (index != currentItem && currentItem < imageListView.getItems()
                .size()) {
              saveCurrentItem();
            }
            // Load the base image and clear the StackGroup
            img = new PixelatedImageView(newValue.getImage());
            imageEditorStackGroup.clearList();
            imageEditorStackGroup.setImage(img);
            // It doesn't exist in session, so add it and set default scale
            if (index > session.getLastIndex()) {
              imageEditorStackGroup.setLineCount(1);
              imageEditorStackGroup.setCurrentScale(null);
              CalibrationImageItem item = new CalibrationImageItem(imageEditorScrollPane);
              item.setSourceImagePath(imageListView.getSelectionModel()
                  .getSelectedItem()
                  .getPath());
              session.addItem(item);
              currentItem = session.getLastIndex();
              imageEditorScrollPane.setDefaultScale();
            }
            // It already exists in session, so load it
            else {
              loadEditorItem(index);
            }
            imageEditorStackGroup.setBounds(
                imageEditorStackGroup.parentToLocal(imageEditorStackGroup.getBoundsInParent()));
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

  private void saveCurrentItem() {
    if (currentItem != -1) {
      CalibrationImageItem item = new CalibrationImageItem(imageEditorScrollPane);
      item.setSourceImagePath(session.getItem(currentItem)
          .getSourceImagePath());
      session.setItem(currentItem, item);
    }
  }

  private void loadEditorItem(int index) {
    CalibrationImageItem item = session.getItem(index);
    imageEditorScrollPane.loadCalibrationItem(item);
    currentItem = index;
  }

  private void addImage(File file) {
    Task task = new Task<CalibImageItem>() {
      @Override protected CalibImageItem call() throws Exception {
        CalibImageItem item = CalibrationImageManager.retrieveImage(file.getAbsolutePath());
        item.preloadThumbnail();
        return item;
      }

      @Override protected void succeeded() {
        try {
          imageListView.getItems()
              .add(get());
          runCalibrationButton.setDisable(false);
          runCalibrationMenuItem.setDisable(false);
          if (imageListView.getItems()
              .size() == 1) {
            imageListView.getSelectionModel()
                .select(0);
          }
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

  private void removeCurrentEditorItem() {
    int index = imageListView.getSelectionModel()
        .getSelectedIndex();
    imageListView.getItems()
        .remove(index);
    currentItem = imageListView.getSelectionModel()
        .getSelectedIndex();
    if (imageListView.getItems()
        .size() == 0) {
      imageEditorStackGroup.clearList();
      imageEditorStackGroup.getChildren()
          .clear();
      runCalibrationButton.setDisable(true);
      runCalibrationMenuItem.setDisable(true);
    }
  }

  private void setEditorButtonListeners() {
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
  }

  private void setInitialButtonStates() {
    exportCalibrationMenuItem.setDisable(true);
    saveCalibrationMenuItem.setDisable(true);
    runCalibrationButton.setDisable(true);
    runCalibrationMenuItem.setDisable(true);
  }

  public void onRunCalibration(ActionEvent actionEvent)
      throws InterruptedException, ExecutionException, IOException {
    FXMLLoader loader =
        new FXMLLoader(getClass().getResource("/fmxl/ProgressDialog.fxml"), Translator.getBundle());
    CalibrationProgressDialogController calibrationProgressDialogController =
        new CalibrationProgressDialogController(this);
    loader.setController(calibrationProgressDialogController);
    Parent root = loader.load();

    calibrationProgressDialogController.startCalibration(calibrationConfig,
        imageListView.getItems(), stackPane);
  }

  public void onCalibrationOptions(ActionEvent actionEvent) {
    CalibConfigDialogController calibConfigDialogController = new CalibConfigDialogController();
    calibConfigDialogController.init(imageEditorStackGroup.getScene()
        .getWindow(), this);
  }

  /**
   * ImageList
   */

  public void onImageDropped(DragEvent dragEvent) {
    Dragboard board = dragEvent.getDragboard();
    List<File> files = board.getFiles();
    for (File file : files) {
      String extension = Utility.getFilePathExtension(file.getPath());
      if (extension != null && Utility.isImageExtensionSupported(extension)) {
        addImage(file);
      }
    }
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

  /**
   * File Menu
   */
  public void newCalibration(ActionEvent actionEvent) {
    setInitialButtonStates();
    this.calibrationResults = null;
    imageListView.getItems()
        .clear();
    imageEditorStackGroup.clearList();
    imageEditorStackGroup.getChildren()
        .clear();
  }

  public void onSaveCalibration(ActionEvent actionEvent) {
    String dialogMessage = "Set calibration name";
    JFXAlert alert = new JFXAlert((Stage) zoomButton.getScene()
        .getWindow());
    alert.initModality(Modality.APPLICATION_MODAL);
    alert.setOverlayClose(false);
    JFXDialogLayout layout = new JFXDialogLayout();
    layout.setHeading(new Label(dialogMessage));
    JFXButton saveButton = new JFXButton("Save");
    JFXButton cancelButton = new JFXButton("Cancel");
    JFXTextField tf = new JFXTextField(calibrationResults.getExtractedCamera());
    cancelButton.getStyleClass()
        .add("dialog-accept");
    cancelButton.setOnAction(event -> alert.hideWithAnimation());
    saveButton.setOnAction(event -> {
      saveCalibrationModel(tf.getText());
      alert.hideWithAnimation();
    });
    layout.setBody(tf);
    layout.setActions(saveButton, cancelButton);
    alert.setContent(layout);
    alert.show();
  }

  public void onExportCalibration(ActionEvent actionEvent) {
    FileChooser ch = new FileChooser();
    String path = prefs.get(Constants.FILECHOOSER_CALIB_FILECHOOSER_SAVEAS, "");
    if (path.length() > 0) {
      File folder = new File(path);
      if (folder.exists() && folder.isDirectory()) {
        ch.setInitialDirectory(new File(path));
      }
    }
    ch.getExtensionFilters()
        .add(
            new FileChooser.ExtensionFilter(Translator.getString("acalibfilechooser"), "*.acalib"));
    ch.setTitle(Translator.getString("exportTo"));
    File file = ch.showSaveDialog(imageListView.getScene()
        .getWindow());
    if (file != null) {
      prefs.put(Constants.FILECHOOSER_CALIB_FILECHOOSER_SAVEAS, file.getParent());
      String name = file.getName();
      saveCalibrationModel(file, name.substring(0, name.indexOf(".acalib")));
    }
  }

  public void onImportImages(ActionEvent actionEvent) {
    if (session == null) return;
    FileChooser ch = new FileChooser();
    String path = prefs.get(Constants.FILECHOOSER_CALIB_IMPORT_IMG_LASTOPENED, "");
    if (path.length() > 0) {
      File folder = new File(path);
      if (folder.exists() && folder.isDirectory()) {
        ch.setInitialDirectory(new File(path));
      }
    }
    ch.getExtensionFilters()
        .add(new FileChooser.ExtensionFilter(Translator.getString("allimagefilechooser"), "*.jpeg",
            "*.jpg", "*.bmp", "*.png", "*.gif"));
    ch.getExtensionFilters()
        .add(new FileChooser.ExtensionFilter(Translator.getString("jpegimagefilechooser"), "*.jpeg",
            "*.jpg"));
    ch.getExtensionFilters()
        .add(new FileChooser.ExtensionFilter(Translator.getString("bmpimagefilechooser"), "*.bmp"));
    ch.getExtensionFilters()
        .add(new FileChooser.ExtensionFilter(Translator.getString("pngimagefilechooser"), "*.png"));
    ch.getExtensionFilters()
        .add(new FileChooser.ExtensionFilter(Translator.getString("gifimagefilechooser"), "*.gif"));
    ch.setTitle(Translator.getString("chooseimages"));

    List<File> files = ch.showOpenMultipleDialog(imageListView.getScene()
        .getWindow());
    if (files != null && files.size() > 0) {
      prefs.put(Constants.FILECHOOSER_CALIB_IMPORT_IMG_LASTOPENED, files.get(0)
          .getParent());
      for (File file : files) {
        addImage(file);
      }
    }
  }

  public void onExitClick(ActionEvent actionEvent) {
    stage.close();
  }

  @Override public void onModeChange(ImageEditorStackGroup.Mode mode) {
    imageEditorScrollPane.setCurrentMode(mode);
    setLineEditorVisual(mode);
  }

  private void setLineEditorVisual(ImageEditorStackGroup.Mode mode) {
    handButton.setBackground(null);
    zoomButton.setBackground(null);
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
    }
  }

  private void saveCalibrationModel(String name) {
    File folder = new File("./calibs");
    if (!folder.exists()) folder.mkdirs();
    File file = new File("./calibs/" + name + ".acalib");
    saveCalibrationModel(file, name);
  }

  private void saveCalibrationModel(File file, String name) {
    if (calibrationResults == null || calibrationResults.getCalibrationModel() == null) return;
    CalibrationModel model = calibrationResults.getCalibrationModel();
    model.setName(name);
    CalibrationFileManager.saveCalibration(file, model);
  }

  @Override public void onCalibrationResult(CalibrationResults results) {
    this.calibrationResults = results;
    List<CalibrationImage> imageResults = results.getImagesResults();
    ObservableList<CalibImageItem> imageItems = imageListView.getItems();
    int selectedIndex = imageListView.getSelectionModel()
        .getSelectedIndex();
    for (int i = 0; i < imageResults.size(); i++) {
      CalibImageItem listImageItem = imageItems.get(i);
      CalibImageItem imageItem = null;
      try {
        Image imageResultItem = imageResults.get(i)
            .getImage();
        boolean found = imageResults.get(i)
            .isFound();
        String color = "#C41E3A";
        if (found) color = "#00A550";
        imageItem = new CalibImageItem(listImageItem.getName(), listImageItem.getPath(), imageResultItem,
            listImageItem.getImageIcon(50), 50, color);
        CalibrationImageItem item = new CalibrationImageItem(imageEditorScrollPane);
        item.setSourceImagePath(imageItem.getPath());
        if (session.size() > i) session.setItem(i, item);
        imageItems.set(i, imageItem);
      } catch (IOException | ExecutionException | InterruptedException e) {
        // Just do nothing
      }
    }
      if (!outputTextArea.getText()
          .isEmpty()) {
          outputTextArea.appendText("\n\n");
      }
    outputTextArea.appendText(results.getFormattedOutput(CalibrationResults.OutputDetail.FULL));
    imageListView.getSelectionModel()
        .select(selectedIndex);
    exportCalibrationMenuItem.setDisable(false);
    saveCalibrationMenuItem.setDisable(false);
    stage.toFront();
  }

  @Override public void onConfigurationSave(CalibrationConfig calibrationConfig) {
    currentConfigLabel.setText("Current: " + calibrationConfig.toString());
    this.calibrationConfig = calibrationConfig;
  }
}
