package opencv.filters.edgedetection;

import com.jfoenix.controls.JFXSlider;
import imageprocess.ImageItem;
import java.io.IOException;
import java.util.prefs.Preferences;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import ui.MainApplication;
import ui.custom.PixelatedImageView;
import utils.Translator;

public class EdgeDetectionDialog implements EdgeDetectionManager.ResultListener {

  private static final int HEIGHT_NO_IMAGE = 89;
  private static final int HEIGHT_IMAGE = 280;

  @FXML public Pane previewPane;
  @FXML public JFXSlider thresholdSlider;

  private OnActionListener listener;
  private Stage stage;
  private Preferences prefs;

  private ImageItem imageItem;
  private PixelatedImageView pixelatedImageView;
  private EdgeDetectionManager edgeDetectionManager = new EdgeDetectionManager();

  public void init(Window owner, OnActionListener listener, ImageItem imageItem){
    this.listener = listener;
    this.imageItem = imageItem;
    this.prefs = Preferences.userNodeForPackage(EdgeDetectionDialog.class);
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fmxl/EdgeDetectionDialog.fxml"), Translator.getBundle());
      loader.setController(this);
      Parent root = loader.load();
      int width = calculateStageWidth(imageItem);
      int height = calculateStageHeight(imageItem, width);
      Scene scene = new Scene(root, width, height);
      final ObservableList<String> stylesheets = scene.getStylesheets();
      stylesheets.addAll(getClass().getResource("/css/MainApplication.css").toExternalForm());
      stage = new Stage();
      stage.getIcons().add(new Image(MainApplication.class.getResourceAsStream("/images/icon.png")));
      stage.setTitle(Translator.getString("edgeDetection"));
      stage.setScene(scene);
      stage.initOwner(owner);
      stage.initModality(Modality.APPLICATION_MODAL);
      stage.setResizable(false);
      stage.showAndWait();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  private int calculateStageWidth(ImageItem imageItem){
    double imageRatio = (double) imageItem.getWidth() / imageItem.getHeight();
    return (int) Math.max(Math.min(600, HEIGHT_IMAGE*imageRatio), 220);
  }

  private int calculateStageHeight(ImageItem imageItem, int width){
    double imageRatio = (double) imageItem.getHeight() / imageItem.getWidth();
    return (int) Math.min(Math.max(imageRatio*width+HEIGHT_NO_IMAGE, HEIGHT_NO_IMAGE), 600);
  }

  @FXML
  private void initialize(){
    thresholdSlider.setOrientation(Orientation.HORIZONTAL);
    thresholdSlider.setIndicatorPosition(JFXSlider.IndicatorPosition.RIGHT);
    thresholdSlider.setMax(300);
    thresholdSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
      edgeDetectionManager.applyCannyEdgeDetection(this, imageItem.getImage(), newValue.intValue());
    });
    pixelatedImageView = new PixelatedImageView(edgeDetectionManager.applyCannyEdgeDetectionSync(imageItem.getImage(), (int) thresholdSlider.getValue()));
    pixelatedImageView.fitWidthProperty().bind(previewPane.widthProperty());
    pixelatedImageView.fitHeightProperty().bind(previewPane.heightProperty());
    pixelatedImageView.setPreserveRatio(true);
    previewPane.getChildren().addAll(pixelatedImageView);
  }

  public void onApplyAction(ActionEvent actionEvent) {
    listener.onApplyEdgeDetection(new EdgeDetectionFilter((int) thresholdSlider.getValue()));
    stage.close();
  }


  public void onCloseAction(ActionEvent actionEvent) {
    stage.close();
  }

  @Override public void onEdgeDetectionFinished(Image image) {
    pixelatedImageView.setImage(image);
  }

  public interface OnActionListener{
    void onApplyEdgeDetection(EdgeDetectionFilter edgeDetectionFilter);
  }
}
