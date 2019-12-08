package opencv.filters;

import javafx.scene.image.Image;

public interface Filter {

  Image applyFilter(Image image, FilterArguments arguments, String path) throws FilterArguments.NoArgumentFound;
}
