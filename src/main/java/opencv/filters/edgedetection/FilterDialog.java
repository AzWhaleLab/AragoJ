package opencv.filters.edgedetection;

import com.jfoenix.controls.JFXSpinner;
import imageprocess.ImageItem;
import java.io.IOException;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import opencv.filters.Filter;
import opencv.filters.FilterArguments;
import ui.tasks.task.FilterTask;
import opencv.filters.edgedetection.sobel.SobelEdgeFilter;
import ui.MainApplication;
import ui.custom.PixelatedImageView;
import utils.Translator;

public class FilterDialog implements FilterTask.ResultListener {

  private static final int HEIGHT_NO_IMAGE = 60;
  private static final int HEIGHT_IMAGE = 280;

  @FXML public Pane previewPane;
  @FXML public Button applyButton;

  private OnActionListener listener;
  private Stage stage;

  private ImageItem imageItem;
  private PixelatedImageView pixelatedImageView;
  private JFXSpinner loadingSpinner;

  private FilterTask filterTask;
  private Filter filter;
  private FilterArguments filterArguments;

  public void init(Window owner, OnActionListener listener, ImageItem imageItem, String title){
    this.listener = listener;
    this.imageItem = imageItem;
    filterTask = new FilterTask(this, new SobelEdgeFilter(), null, imageItem.getImage());
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fmxl/FilterDialogNoArgs.fxml"), Translator.getBundle());
      loader.setController(this);
      Parent root = loader.load();
      int width = calculateStageWidth(imageItem);
      int height = calculateStageHeight(imageItem, width);
      Scene scene = new Scene(root, width, height);
      final ObservableList<String> stylesheets = scene.getStylesheets();
      stylesheets.addAll(getClass().getResource("/css/MainApplication.css").toExternalForm());
      stage = new Stage();
      stage.getIcons().add(new Image(MainApplication.class.getResourceAsStream("/images/icon.png")));
      stage.setTitle(title);
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
    //thresholdSlider.setOrientation(Orientation.HORIZONTAL);
    //thresholdSlider.setIndicatorPosition(JFXSlider.IndicatorPosition.RIGHT);
    //thresholdSlider.setMax(300);
    //thresholdSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
    //  edgeDetectionManager.applyCannyEdgeDetection(this, imageItem.getImage(), newValue.intValue());
    //});
    loadingSpinner = new JFXSpinner();
    loadingSpinner.setRadius(15);
    loadingSpinner.layoutXProperty().bind(previewPane.widthProperty().subtract(loadingSpinner.widthProperty()).divide(2));
    loadingSpinner.layoutYProperty().bind(previewPane.heightProperty().subtract(loadingSpinner.heightProperty()).divide(2));
    pixelatedImageView = new PixelatedImageView();
    pixelatedImageView.setVisible(false);
    pixelatedImageView.fitWidthProperty().bind(previewPane.widthProperty());
    pixelatedImageView.fitHeightProperty().bind(previewPane.heightProperty());
    pixelatedImageView.setPreserveRatio(true);
    filterTask.startTask();
    previewPane.getChildren().addAll(loadingSpinner, pixelatedImageView);
  }

  public void onApplyAction(ActionEvent actionEvent) {
    if(filter != null){
      listener.onApplyFilter(filter, filterArguments);
    }
    stage.close();
  }

  public void onCloseAction(ActionEvent actionEvent) {
    filterTask.cancelTask();
    stage.close();
  }

  @Override
  public void onFilterFinished(Filter filter, FilterArguments filterArguments, Image image) {
    this.filter = filter;
    this.filterArguments = filterArguments;
    applyButton.setDisable(false);
    pixelatedImageView.setVisible(true);
    loadingSpinner.setVisible(false);
    pixelatedImageView.setImage(image);
  }

  @Override
  public void onFilterFailed(Filter filter, FilterArguments filterArguments, Image image) {

  }

  public interface OnActionListener{
    void onApplyFilter(Filter filter, FilterArguments filterArguments);
  }
}