package com.aragoj.ui.controller;

import com.aragoj.ui.custom.*;
import com.drew.imaging.ImageProcessingException;
import javafx.fxml.FXMLLoader;
import com.aragoj.opencv.OpenCVManager;
import com.aragoj.opencv.calibration.model.CalibrationModel;
import com.aragoj.opencv.calibration.ui.CalibrationDialogController;
import com.jfoenix.controls.*;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.input.*;
import javafx.stage.*;
import com.aragoj.opencv.calibration.ui.UndistortDialog;
import com.aragoj.opencv.calibration.ui.UndistortProgressDialogController;
import com.aragoj.session.export.ExportCSV;
import com.aragoj.imageprocess.ImageItem;
import com.aragoj.imageprocess.ImageManager;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import com.aragoj.session.SessionUtils;
import com.aragoj.mainscreen.io.preferences.MetadataExportPreferencesManager;
import com.aragoj.ui.MainApplication;
import com.aragoj.ui.cellfactory.ImageListViewCell;
import com.aragoj.session.model.EditorItem;
import com.aragoj.ui.model.ScaleRatio;
import com.aragoj.session.model.Session;
import com.aragoj.utils.Constants;
import com.aragoj.utils.Translator;
import com.aragoj.utils.Utility;
import com.aragoj.utils.jfx.JFXTabPane;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;

public class MainDialogController  implements ImageEditorStackGroup.ModeListener, ImageEditorStackGroup.ElementListener, LayerTabPageController.LineChangeListener, ScaleDialogController.OnActionListener, ConvertUnitsDialogController.OnActionListener, UndistortDialog.OnActionListener, UndistortProgressDialogController.UndistortCallback {


    private Preferences prefs;

    //// Menu Items
    // View items
    @FXML private CheckMenuItem precisionLinesCheckItem;
    @FXML private CheckMenuItem identifierLinesCheckItem;

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

    // Session related variables
    private Session session;
    private int currentItem = -1;

    // Left side controllers & views
    @FXML private JFXListView<ImageItem> imageListView;

    // Right side controllers & views
    @FXML public JFXTabPane miscTabPane;
    @FXML private MetaTreeTableController metaTabPageController;
    @FXML private LayerTabPageController layerTabPageController;

    // Main Editor
    private ImageEditorStackGroup imageEditorStackGroup;
    private ZoomableScrollPane imageEditorScrollPane;
    private PixelatedImageView img;

    @FXML private StackPane stackPane;

    @FXML public HBox imageEditorToolsSecondaryPane;
    @FXML public AnchorPane imageEditorAnchorPane;
    @FXML public Label ieDegreeLabel;
    @FXML public JFXColorPicker ieColorPicker;
    @FXML public JFXTextField ieDegreePicker;
    @FXML private JFXButton editorCursorBtn;
    @FXML private JFXButton editorLineBtn;
    @FXML private JFXButton editorAngBtn;
    @FXML private JFXButton editorAreaBtn;
    @FXML private JFXButton handButton;
    @FXML private JFXButton zoomButton;


    public MainDialogController(){ }

    @FXML
    private void initialize(){
        prefs = Preferences.userNodeForPackage(MainApplication.class);
        MetadataExportPreferencesManager.importExportPreferences();

        loadPreferencesAndPanes();

        // Default visual settings
        loadInitialState();
        ieColorPicker.managedProperty().bind(ieColorPicker.visibleProperty());
        ieDegreePicker.managedProperty().bind(ieDegreePicker.visibleProperty());
        ieDegreeLabel.managedProperty().bind(ieDegreeLabel.visibleProperty());

        setUpImageList();
        setEditorButtonListeners();

        handButton.sceneProperty().addListener((observable, oldValue, newValue) -> {
            if(oldValue == null && newValue != null){
                newValue.getWindow().setOnCloseRequest(new EventHandler<WindowEvent>() {
                    @Override
                    public void handle(WindowEvent event) {
                        MetadataExportPreferencesManager.exportExportPreferences();
                        if(session != null){
                            event.consume();
                            closeAppWithSureDialog(session);
                        }
                    }
                });
            }
        });
    }

    /**
     * Right side panel methods (image list)
     */
    private void setUpImageList() {
        // Padding adjustments
        imageListView.setCellFactory(param -> {
            ImageListViewCell cell = new ImageListViewCell();
            cell.setPadding(new Insets(1,4,1,0));
            return cell;
        });

        // On selection, prepare editor and load scaled image
        imageListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ImageItem>() {
            @Override
            public void changed(ObservableValue<? extends ImageItem> observable, ImageItem oldValue, ImageItem newValue) {
                if(newValue != null){
                    metaTabPageController.addRootTreeItem(newValue.getMetadata());
                    try {
                        int index = imageListView.getSelectionModel().getSelectedIndex();

                        // Save the current item before switching
                        if(index != currentItem && currentItem < imageListView.getItems().size()){
                            saveCurrentItem();
                        }
                        // Load the base image and clear the StackGroup
                        img = new PixelatedImageView(newValue.getImage());
                        imageEditorStackGroup.getChildren().setAll(img);
                        imageEditorStackGroup.clearList();
                        layerTabPageController.clearList();
                        // It doesn't exist in session, so add it and set default scale
                        if(index > session.getLastIndex() || oldValue == null){
                            imageEditorStackGroup.setLineCount(1);
                            imageEditorStackGroup.setCurrentScale(null);
                            EditorItem item = new EditorItem(imageEditorScrollPane, layerTabPageController.getLayers());
                            item.setSourceImagePath(imageListView.getSelectionModel().getSelectedItem().getPath());
                            session.addItem(item);
                            currentItem = session.getLastIndex();
                            imageEditorScrollPane.setDefaultScale();
                        }
                        // It already exists in session, so load it
                        else{
                            loadEditorItem(index);
                        }
                        layerTabPageController.setListener(MainDialogController.this);
                        layerTabPageController.setCurrentScale(imageEditorStackGroup.getCurrentScale());
                        imageEditorStackGroup.setBounds(imageEditorStackGroup.parentToLocal(imageEditorStackGroup.getBoundsInParent()));
                        setEditorEnable(true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        ContextMenu listItemContextMenu = new ContextMenu();
        MenuItem removeContextItem = new MenuItem(Translator.getString("delete"));

        removeContextItem.setOnAction(event -> {
            removeCurrentEditorItem();
        });
        listItemContextMenu.getItems().setAll(removeContextItem);
        imageListView.setContextMenu(listItemContextMenu);
        imageListView.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.DELETE){
                removeCurrentEditorItem();
            }
        });
    }

    private void loadEditorItem(int index) {
        EditorItem item = session.getItem(index);
        imageEditorStackGroup.clearList();
        imageEditorScrollPane.loadEditorItem(item, imageEditorStackGroup, layerTabPageController);

        ScaleRatio scaleRatio = item.getScaleRatio();
        if(scaleRatio != null){
            convertUnitsMenuItem.setDisable(false);
        } else{
            convertUnitsMenuItem.setDisable(true);
        }
        currentItem = index;
    }


    private void addImage(File file) {
        Task task = new Task<ImageItem>() {
            @Override
            protected ImageItem call() throws Exception {
                ImageItem item = ImageManager.retrieveImage(file.getAbsolutePath());
                item.preloadThumbnail();
                return item;
            }

            @Override
            protected void succeeded() {
                try {
                    imageListView.getItems().add(get());
                    exportCSVMenuItem.setDisable(false);
                    System.out.println("ImageListView size:" + imageListView.getItems().size());
                    System.out.println("Free Memory: " + Runtime.getRuntime().freeMemory());
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
        if(!pickedColor.equals("RED"))
            currentPickedColor = Color.valueOf(pickedColor);
        ieColorPicker.setValue(currentPickedColor);
        ieDegreePicker.setText(NumberFormat.getInstance().format(angle));

        // Initalize editor panes + groups.
        imageEditorStackGroup = new ImageEditorStackGroup(this, this, currentPickedColor, angle);
        imageEditorScrollPane = new ZoomableScrollPane(imageEditorStackGroup);
        AnchorPane.setBottomAnchor(imageEditorScrollPane, 0.0);
        AnchorPane.setTopAnchor(imageEditorScrollPane, 0.0);
        AnchorPane.setLeftAnchor(imageEditorScrollPane, 0.0);
        AnchorPane.setRightAnchor(imageEditorScrollPane, 0.0);
        imageEditorAnchorPane.getChildren().setAll(imageEditorScrollPane);
    }

    private void setEditorButtonListeners() {
        DecimalFormat format = new DecimalFormat( "#.##" );
        ieDegreePicker.setTextFormatter(new TextFormatter<>(c ->{
            if (c.getControlNewText().isEmpty()) {
                imageEditorStackGroup.setAddedLineAngle(0);
                return c;
            }
            ParsePosition parsePosition = new ParsePosition( 0 );
            Object object = format.parse( c.getControlNewText(), parsePosition );

            if ( object == null || parsePosition.getIndex() < c.getControlNewText().length() ) {
                return null;
            }
            else {
                if(((Number)object).doubleValue() > 360){
                    return  null;
                }
                imageEditorStackGroup.setAddedLineAngle(((Number)object).doubleValue());
                return c;
            }
        }));

        zoomButton.setOnAction(event -> {
            if(imageEditorStackGroup != null){
                imageEditorStackGroup.setCurrentMode(ImageEditorStackGroup.Mode.ZOOM);
            }
        });

        handButton.setOnAction(event -> {
            if(imageEditorStackGroup != null){
                imageEditorStackGroup.setCurrentMode(ImageEditorStackGroup.Mode.PAN);
            }
        });

        ieColorPicker.setOnAction(event -> imageEditorStackGroup.setCurrentPickedColor(ieColorPicker.getValue()));

        editorCursorBtn.setOnMouseClicked(event -> {
            if(imageEditorStackGroup != null){
                imageEditorStackGroup.setCurrentMode(ImageEditorStackGroup.Mode.SELECT);
            }
        });

        editorLineBtn.setOnMouseClicked(event -> {
            if(imageEditorStackGroup != null){
                imageEditorStackGroup.setCurrentMode(ImageEditorStackGroup.Mode.LINE);
            }
        });

        editorAngBtn.setOnMouseClicked(event -> {
            if(imageEditorStackGroup != null){
                imageEditorStackGroup.setCurrentMode(ImageEditorStackGroup.Mode.ANG_SEL);
            }
        });

        editorAreaBtn.setOnAction(event -> {
            if (imageEditorStackGroup != null) {
                imageEditorStackGroup.setCurrentMode(ImageEditorStackGroup.Mode.AREA_CREATION);
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
        ieColorPicker.setVisible(false);
        ieDegreeLabel.setVisible(false);
        ieDegreePicker.setVisible(false);
        imageEditorToolsSecondaryPane.setVisible(false);
        imageEditorStackGroup.setCursor(Cursor.DEFAULT);
        if(mode == null ) return;
        switch (mode){
            case PAN:
                handButton.setBackground(new Background(new BackgroundFill(Color.valueOf("#cceaf4"), CornerRadii.EMPTY, Insets.EMPTY)));
                Image imageCursor = new Image("images/hand_cursor.png");
                // Can't use urls in setStyle (bugged) - https://bugs.openjdk.java.net/browse/JDK-8089191
                imageEditorStackGroup.setCursor(new ImageCursor(imageCursor, imageCursor.getWidth()/2, imageCursor.getHeight()/2));
                break;
            case ZOOM:
                zoomButton.setBackground(new Background(new BackgroundFill(Color.valueOf("#cceaf4"), CornerRadii.EMPTY, Insets.EMPTY)));
                Image image = new Image("images/magnifier_cursor.png");
                imageEditorStackGroup.setCursor(new ImageCursor(image, image.getWidth()/2, image.getHeight()/2));
                break;
            case SELECT:
                editorCursorBtn.setBackground(new Background(new BackgroundFill(Color.valueOf("#cceaf4"), CornerRadii.EMPTY, Insets.EMPTY)));
                break;
            case ANG:
                editorLineBtn.setBackground(new Background(new BackgroundFill(Color.valueOf("#cceaf4"), CornerRadii.EMPTY, Insets.EMPTY)));
                imageEditorStackGroup.setCursor(Cursor.CROSSHAIR);
                imageEditorToolsSecondaryPane.setVisible(true);
                ieColorPicker.setVisible(true);
                break;
            case LINE:
                editorLineBtn.setBackground(new Background(new BackgroundFill(Color.valueOf("#cceaf4"), CornerRadii.EMPTY, Insets.EMPTY)));
                imageEditorStackGroup.setCursor(Cursor.CROSSHAIR);
                imageEditorToolsSecondaryPane.setVisible(true);
                ieColorPicker.setVisible(true);
                break;
            case ANG_SEL:
                editorAngBtn.setBackground(new Background(new BackgroundFill(Color.valueOf("#cceaf4"), CornerRadii.EMPTY, Insets.EMPTY)));
                imageEditorToolsSecondaryPane.setVisible(true);
                ieDegreeLabel.setVisible(true);
                ieDegreePicker.setVisible(true);
                break;
            case AREA_CREATION:
                editorAreaBtn.setBackground(new Background(new BackgroundFill(Color.valueOf("#cceaf4"), CornerRadii.EMPTY, Insets.EMPTY)));
                imageEditorStackGroup.setCursor(Cursor.CROSSHAIR);
                imageEditorToolsSecondaryPane.setVisible(true);
                ieColorPicker.setVisible(true);
                break;
        }
    }

    private void loadInitialState(){
        saveSessionAsMenuItem.setDisable(true);
        saveSessionMenuItem.setDisable(true);
        importImagesMenuItem.setDisable(true);
        exportCSVMenuItem.setDisable(true);
        imageListView.setDisable(true);
        setEditorEnable(false);
    }

    public void setEditorEnable(boolean editorEnable) {
        if(!editorEnable){
            editorCursorBtn.setDisable(true);
            editorLineBtn.setDisable(true);
            editorAngBtn.setDisable(true);
            editorAreaBtn.setDisable(true);
            handButton.setDisable(true);
            zoomButton.setDisable(true);
            conversionMenu.setDisable(true);
            miscTabPane.getSelectionModel().select(1);
            miscTabPane.setDisable(true);
            setLineEditorVisual(null);
        }
        else{
            editorCursorBtn.setDisable(false);
            editorLineBtn.setDisable(false);
            editorAngBtn.setDisable(false);
            editorAreaBtn.setDisable(false);
            handButton.setDisable(false);
            zoomButton.setDisable(false);
            miscTabPane.setDisable(false);
            conversionMenu.setDisable(false);
        }
        if(session != null){
            imageListView.setDisable(false);
            saveSessionAsMenuItem.setDisable(false);
            saveSessionMenuItem.setDisable(false);
            importImagesMenuItem.setDisable(false);
            if(imageListView.getItems().size() > 0){
                undistortMenuItem.setDisable(false);
                exportCSVMenuItem.setDisable(false);
            } else {
                exportCSVMenuItem.setDisable(true);
                undistortMenuItem.setDisable(true);
            }
        }
    }

    /**
     * ImageEditorStackGroup listener callbacks
     */
    @Override
    public void onModeChange(ImageEditorStackGroup.Mode mode) {
        imageEditorScrollPane.setCurrentMode(mode);
        setLineEditorVisual(mode);
    }

    @Override
    public void onLineAdd(LineGroup line, boolean editorItemLoad) {
        if(!editorItemLoad) miscTabPane.getSelectionModel().select(0);
        layerTabPageController.addLayer(line);

        imageEditorStackGroup.setColorHelperLinesVisible(identifierLinesCheckItem.isSelected());
        imageEditorStackGroup.setLayerHelperLinesVisible(precisionLinesCheckItem.isSelected());

    }

    @Override
    public void onAreaAdd(AreaGroup area, boolean sessionLoad) {
        if(!sessionLoad) miscTabPane.getSelectionModel().select(0);
        layerTabPageController.addLayer(area);
    }

    @Override
    public void onLineChange() {
        layerTabPageController.refreshList();
    }

    @Override
    public void onAreaChange() {
        layerTabPageController.refreshList();
    }


    /**
     * Menu Items
     */
    @FXML
    public void onIdentifierLinesToggle(ActionEvent actionEvent) {
        CheckMenuItem item = (CheckMenuItem) actionEvent.getSource();
        if(item.isSelected()){
            imageEditorStackGroup.setColorHelperLinesVisible(true);
        }
        else{
            imageEditorStackGroup.setColorHelperLinesVisible(false);
        }
    }

    @FXML
    public void onPrecisionLinesToggle(ActionEvent actionEvent) {
        CheckMenuItem item = (CheckMenuItem) actionEvent.getSource();
        if(item.isSelected()){
            imageEditorStackGroup.setLayerHelperLinesVisible(true);
        }
        else{
            imageEditorStackGroup.setLayerHelperLinesVisible(false);
        }
    }

    @FXML
    public void convertViaScale(ActionEvent actionEvent) {
        ScaleDialogController scaleDialog = new ScaleDialogController();
        scaleDialog.init(editorCursorBtn.getScene().getWindow(), this, imageEditorStackGroup.getLines());
    }

    @FXML
    public void convertViaRatioDf(ActionEvent actionEvent) {
        DistanceScaleDialogController scaleDialog = new DistanceScaleDialogController();
        scaleDialog.init(editorCursorBtn.getScene().getWindow(), this, imageListView.getSelectionModel().getSelectedItem());
    }

    @FXML
    public void convertUnits(ActionEvent actionEvent) {
        ConvertUnitsDialogController converUnitsDialog = new ConvertUnitsDialogController();

        converUnitsDialog.init(editorCursorBtn.getScene().getWindow(), this, imageEditorStackGroup.getLines(), imageEditorStackGroup.getCurrentScale());
    }


    /**
     * For reference scaling
     */
    @Override
    public void onApplyScale(ScaleRatio scaleRatio, boolean allImages) {
        imageEditorStackGroup.setCurrentScale(scaleRatio);
        layerTabPageController.setCurrentScale(scaleRatio);

        if(allImages){
            for(EditorItem item : session.getItems()){
                item.setScaleRatio(scaleRatio);
            }
        }
        convertUnitsMenuItem.setDisable(false);
    }

    @Override
    public void onChangeScale(ScaleRatio oldScale, ScaleRatio newScale, boolean allImages) {
        imageEditorStackGroup.setCurrentScale(newScale);
        layerTabPageController.setCurrentScale(newScale);
        if(allImages){
            for(EditorItem item : session.getItems()){
                ScaleRatio itemScaleRatio = item.getScaleRatio();
                if(itemScaleRatio != null && itemScaleRatio.getUnits() != null && item.getScaleRatio().getUnits().equals(oldScale.getUnits())){
                    item.setScaleRatio(newScale);
                }
            }
        }
        convertUnitsMenuItem.setDisable(false);
    }

    /**
     * For camera opencv.calibration
     */

    @FXML
    public void onCalibrateCamera(ActionEvent actionEvent) {
        try{
            OpenCVManager.loadOpenCV();
            CalibrationDialogController calibrationDialogController = new CalibrationDialogController();
            calibrationDialogController.init(editorCursorBtn.getScene().getWindow());
        } catch (SecurityException | UnsatisfiedLinkError e){
            System.err.println("Could not locate dll");
        }
    }

    /**
     * LayerTabPageController listener callbacks
     */

    @Override
    public void onRemoveLine(String name) {
        imageEditorStackGroup.removeLineGroup(name);
    }

    @Override
    public void onRenameLine(String oldName, String newName) {
        imageEditorStackGroup.renameLineGroup(oldName, newName);
    }

    /**
     * File MenuItems
     */
    @FXML
    public void onNewSession(ActionEvent actionEvent) {
        if(session != null){
            loadSessionWithSureDialog(new Session());
        } else {
            loadSession(new Session());
        }
    }

    //TODO: Refactor this so we dont repeat code
    private void loadSessionWithSureDialog(Session session) {
        String sessionName = this.session.getName();
        if(sessionName == null) sessionName = "Unnamed";
        String dialogMessage = "Do you want to save changes to "+ sessionName+ "?";
        JFXAlert alert = new JFXAlert((Stage) zoomButton.getScene().getWindow());
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setOverlayClose(false);
        JFXDialogLayout layout = new JFXDialogLayout();
        layout.setHeading(new Label(dialogMessage));
        JFXButton saveButton = new JFXButton("Save");
        JFXButton dontSaveButton = new JFXButton("Don't Save");
        JFXButton cancelButton = new JFXButton("Cancel");
        cancelButton.getStyleClass().add("dialog-accept");
        cancelButton.setOnAction(event -> alert.hideWithAnimation());
        dontSaveButton.setOnAction(event -> {
            loadSession(session);
            alert.hideWithAnimation();
        });
        saveButton.setOnAction(event -> {
            boolean saved = saveCurrentSession();
            if(saved) loadSession(session);
            alert.hideWithAnimation();
        });
        layout.setActions(saveButton, dontSaveButton, cancelButton);
        alert.setContent(layout);
        alert.show();
    }

    private void closeAppWithSureDialog(Session session){
        String sessionName = this.session.getName();
        if(sessionName == null) sessionName = "Unnamed";
        String dialogMessage = "Do you want to save changes to "+ sessionName+ "?";
        JFXAlert alert = new JFXAlert((Stage) zoomButton.getScene().getWindow());
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setOverlayClose(false);
        JFXDialogLayout layout = new JFXDialogLayout();
        layout.setHeading(new Label(dialogMessage));
        JFXButton saveButton = new JFXButton("Save");
        JFXButton dontSaveButton = new JFXButton("Don't Save");
        JFXButton cancelButton = new JFXButton("Cancel");
        cancelButton.getStyleClass().add("dialog-accept");
        cancelButton.setOnAction(event -> alert.hideWithAnimation());
        dontSaveButton.setOnAction(event -> {
            alert.hideWithAnimation();
            Platform.exit();
        });
        saveButton.setOnAction(event -> {
            boolean saved = saveCurrentSession();
            alert.hideWithAnimation();
            if(saved){
                Platform.exit();
            }
        });
        layout.setActions(saveButton, dontSaveButton, cancelButton);
        alert.setContent(layout);
        alert.show();

    }


    @FXML
    public void onOpenSession(ActionEvent actionEvent) {
        FileChooser ch = new FileChooser();
        String path = prefs.get(Constants.STTGS_FILECHOOSER_OPEN, "");
        if (path.length() > 0) {
            File folder = new File(path);
            if(folder.exists() && folder.isDirectory()){
                ch.setInitialDirectory(new File(path));
            }
        }
        ch.getExtensionFilters().add(new FileChooser.ExtensionFilter(Translator.getString("axmlfilechooser"), "*.axml"));
        ch.setTitle(Translator.getString("open"));

        File file = ch.showOpenDialog(imageListView.getScene().getWindow());
        if (file != null && file.exists()) {
            prefs.put(Constants.STTGS_FILECHOOSER_OPEN, file.getParent());
            try {
                Session session = SessionUtils.openSession(file);
                if(this.session != null){
                    loadSessionWithSureDialog(session);
                } else {
                    loadSession(session);
                }
            } catch (FileNotFoundException e) {
                //TODO
                e.printStackTrace();
            }
        }

    }
    private void removeCurrentEditorItem(){
        int index = imageListView.getSelectionModel().getSelectedIndex();
        ImageItem item = imageListView.getItems().get(index);
        session.removeItemByPath(item.getPath());
        imageListView.getItems().remove(index);
        currentItem = imageListView.getSelectionModel().getSelectedIndex();
        if(imageListView.getItems().size() == 0){
            imageEditorStackGroup.clearList();
            imageEditorStackGroup.getChildren().clear();
            metaTabPageController.clearRootTreeItem();
            layerTabPageController.clearList();
        }
//        if(imageListView.getItems().size() > 0){
//            if(index > 0){
//                imageListView.getSelectionModel().select(index-1);
//            } else if (index == 0){
//                imageListView.getSelectionModel().select(index);
//            }
//        }
    }

    private void loadSession(Session session) {
        // Reset everything
        imageListView.getItems().clear();
        imageEditorStackGroup.clearList();
        imageEditorStackGroup.getChildren().clear();
        metaTabPageController.clearRootTreeItem();
        layerTabPageController.clearList();
        currentItem = -1;

        // Set the new session
        ArrayList<EditorItem> items = session.getItems();
        for(EditorItem item : items){
            File file = new File(item.getSourceImagePath());
            if(file.exists()){
                addImage(file);
            }
            else{
                //TODO: Show error / locate missing file
            }
        }
        this.session = session;
        setEditorEnable(false);
        if(session.getPath() != null && !session.getPath().isEmpty()){
            File file = new File(session.getPath());
            if(file.exists()){
                MainApplication.setStageName("AragoJ - " + session.getName());
            } else {
                MainApplication.setStageName("AragoJ - Unnamed");
            }
        } else{
            MainApplication.setStageName("AragoJ - Unnamed");
        }

    }

    @FXML
    public void onSaveSession(ActionEvent actionEvent) {
        saveCurrentSession();
    }

    @FXML
    public void onSaveSessionAs(ActionEvent actionEvent) {
        saveCurrentSessionAs();
    }

    private boolean saveCurrentSession() {
        if(session == null) return false;
        if(session.getPath() == null || !new File(session.getPath()).exists()){
            return saveCurrentSessionAs();
        }
        else{
            saveCurrentItem();
            return SessionUtils.saveSession(session);
        }
    }

    private boolean saveCurrentSessionAs() {
        if(session == null) return false;
        saveCurrentItem();

        FileChooser ch = new FileChooser();
        String path = prefs.get(Constants.STTGS_FILECHOOSER_SAVEAS, "");
        if (path.length() > 0) {
            File folder = new File(path);
            if(folder.exists() && folder.isDirectory()){
                ch.setInitialDirectory(new File(path));
            }
        }
        ch.getExtensionFilters().add(new FileChooser.ExtensionFilter(Translator.getString("axmlfilechooser"), "*.axml"));
        ch.setTitle(Translator.getString("saveAs"));
        File file = ch.showSaveDialog(imageListView.getScene().getWindow());
        if(file != null){
            prefs.put(Constants.STTGS_FILECHOOSER_SAVEAS, file.getParent());
            session.setPath(file.getPath());
            MainApplication.setStageName("AragoJ - " + session.getName());
            return SessionUtils.saveSession(session);
        }
        return false;
    }

    @FXML
    private void onImportImages(){
        if(session == null) return;
        FileChooser ch = new FileChooser();
        String path = prefs.get(Constants.STTGS_FILECHOOSER_LASTOPENED, "");
        if (path.length() > 0) {
            File folder = new File(path);
            if(folder.exists() && folder.isDirectory()){
                ch.setInitialDirectory(new File(path));
            }
        }
        ch.getExtensionFilters().add(new FileChooser.ExtensionFilter(Translator.getString("allimagefilechooser"), "*.jpeg", "*.jpg", "*.bmp", "*.png", "*.gif"));
        ch.getExtensionFilters().add(new FileChooser.ExtensionFilter(Translator.getString("jpegimagefilechooser"), "*.jpeg", "*.jpg"));
        ch.getExtensionFilters().add(new FileChooser.ExtensionFilter(Translator.getString("bmpimagefilechooser"), "*.bmp"));
        ch.getExtensionFilters().add(new FileChooser.ExtensionFilter(Translator.getString("pngimagefilechooser"), "*.png"));
        ch.getExtensionFilters().add(new FileChooser.ExtensionFilter(Translator.getString("gifimagefilechooser"), "*.gif"));
        ch.setTitle(Translator.getString("chooseimages"));

        List<File> files = ch.showOpenMultipleDialog(imageListView.getScene().getWindow());
        if (files != null && files.size() > 0) {
            prefs.put(Constants.STTGS_FILECHOOSER_LASTOPENED, files.get(0).getParent());
            for (File file : files) {
                addImage(file);
            }
        }
    }

    @FXML
    public void onExportCSV(ActionEvent actionEvent) {
        if(session == null) return;
        FileChooser ch = new FileChooser();
        String path = prefs.get(Constants.STTGS_FILECHOOSER_EXPORTCSV_LASTOPENED, "");
        if (path.length() > 0) {
            File folder = new File(path);
            if(folder.exists() && folder.isDirectory()){
                ch.setInitialDirectory(new File(path));
            }
        }
        ch.getExtensionFilters().add(new FileChooser.ExtensionFilter(Translator.getString("csvfilechooser"), "*.csv"));
        ch.setTitle(Translator.getString("exportcsv"));

        File file = ch.showSaveDialog(imageListView.getScene().getWindow());
        if(file != null){
            // Save the current item before exporting
            saveCurrentItem();

            prefs.put(Constants.STTGS_FILECHOOSER_EXPORTCSV_LASTOPENED, file.getParent());
            ExportCSV.export(file, session);
        }
    }


    @FXML
    public void onExitClick(ActionEvent actionEvent) {
        MetadataExportPreferencesManager.exportExportPreferences();
        if(session != null){
            closeAppWithSureDialog(session);
        } else {
            Platform.exit();
        }
    }

    private void saveCurrentItem() {
        if(currentItem != -1){
            EditorItem item = new EditorItem(imageEditorScrollPane, layerTabPageController.getLayers());
            item.setSourceImagePath(session.getItem(currentItem).getSourceImagePath());
            session.setItem(currentItem, item);
        }
    }


    public void onImageDropped(DragEvent dragEvent) {
        Dragboard board = dragEvent.getDragboard();
        List<File> files = board.getFiles();
        for(File file : files){
            String extension = Utility.getFilePathExtension(file.getPath());
            if(extension != null && Utility.isImageExtensionSupported(extension)){
                addImage(file);
            }
        }

    }

    public void onImageListDragOver(DragEvent dragEvent) {
        Dragboard board = dragEvent.getDragboard();
        if(board.hasFiles()){
            for(File file : board.getFiles()){
                String extension = Utility.getFilePathExtension(file.getPath());
                if(extension != null && Utility.isImageExtensionSupported(extension)){
                    dragEvent.acceptTransferModes(TransferMode.ANY);
                    return;
                }
            }
        }
    }


    public void onUndistortClick(ActionEvent actionEvent) {
        try{
            OpenCVManager.loadOpenCV();
            UndistortDialog undistortDialog = new UndistortDialog();
            undistortDialog.init(editorCursorBtn.getScene().getWindow(), this);
        } catch (SecurityException | UnsatisfiedLinkError e){
            System.err.println("Could not locate dll");
        }
    }

    @Override
    public void onApplyUndistort(CalibrationModel calibrationModel, boolean applyToAll) {
        saveCurrentItem();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fmxl/ProgressDialog.fxml"), Translator.getBundle());
        UndistortProgressDialogController controller = new UndistortProgressDialogController(this);
        loader.setController(controller);
        try {
            Parent root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
           return;
        }
        List<ImageItem> imageItems;
        if(applyToAll){
            imageItems = imageListView.getItems();
        } else {
            imageItems = new ArrayList<>();
            imageItems.add(imageListView.getSelectionModel().getSelectedItem());
        }
        controller.undistortImages(calibrationModel, imageItems, imageListView.getSelectionModel().getSelectedIndices().get(0), stackPane);

    }

    @Override
    public void onImageItemUndistorted(int index, String newPath, boolean select) {
        try {
            imageListView.getItems().set(index, ImageManager.retrieveImage(newPath));
            EditorItem item = session.getItem(index);
            item.setSourceImagePath(newPath);
            session.setItem(index, item);
            if(select){
                imageListView.getSelectionModel().select(index);
            }
        } catch (ImageProcessingException | IOException e) {
            // Do nothing
        }
    }


}
