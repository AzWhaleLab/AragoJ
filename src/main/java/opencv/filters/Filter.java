package opencv.filters;

import javafx.scene.image.Image;

public interface Filter {

  Image applyFilter(Image image, FilterArguments arguments) throws FilterArguments.NoArgumentFound;
}
