package ui.custom.preferences;

import java.util.prefs.Preferences;
import ui.MainApplication;
import utils.Constants;

public class ViewPreferencesManager {

  private final Preferences prefs;

  private PixelGridManager pixelGridManager = new PixelGridManager();
  private ColorLinesManager colorLinesManager = new ColorLinesManager();

  public ViewPreferencesManager(){
    prefs = Preferences.userNodeForPackage(ViewPreferencesManager.class);
    double step = prefs.getDouble(Constants.STTGS_VIEW_PIXEL_GRID, -1);
    pixelGridManager.setStep(step);
    boolean showIdShape = prefs.getBoolean(Constants.STTGS_VIEW_IDENTIFIER_SHAPE, true);
    colorLinesManager.setColorShapesVisible(showIdShape);
  }

  public void savePixelGridPreferences(double step){
    pixelGridManager.setStep(step);
    prefs.putDouble(Constants.STTGS_VIEW_PIXEL_GRID, step);
  }

  public PixelGridManager getPixelGridManager() {
    return pixelGridManager;
  }

  public void saveColorShapesPreferences(boolean show){
    colorLinesManager.setColorShapesVisible(show);
    prefs.putBoolean(Constants.STTGS_VIEW_IDENTIFIER_SHAPE, show);
  }

  public ColorLinesManager getColorLinesManager() {
    return colorLinesManager;
  }
}
