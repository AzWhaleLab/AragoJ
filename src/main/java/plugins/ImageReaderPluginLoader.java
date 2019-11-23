package plugins;

import com.aragoj.plugins.Plugin;
import com.aragoj.plugins.imagereader.ImageReaderPlugin;
import com.aragoj.plugins.imagereader.SupportedFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javafx.stage.FileChooser;
import javafx.util.Pair;
import utils.Translator;

public class ImageReaderPluginLoader extends PluginLoader<ImageReaderPlugin> {

  @Override Class<ImageReaderPlugin> getClassInstance() {
    return ImageReaderPlugin.class;
  }

  @Override Plugin.Type getPluginType() {
    return Plugin.Type.IMAGE_READER;
  }

  public Pair<Integer, ImageReaderPlugin> getPluginForFile(String path) {
    return getPluginForFile(-1, path);
  }

  public Pair<Integer, ImageReaderPlugin> getPluginForFile(Integer preferredId, String path) {
    if(plugins == null) return null;
    // Retrieve the preferred plugin to open this (the one that originally opened it)
    if(preferredId != -1){
      ImageReaderPlugin plugin = plugins.get(preferredId);
      if(plugin != null){
        return new Pair<>(preferredId, plugin);
      }
    }
    // If it wasn't found or does not exist, then find the first plugin that can open it
    for(Map.Entry<Integer, ImageReaderPlugin> set : plugins.entrySet()){
      if(pluginSupportsFile(set.getValue(), path)){
        return new Pair<>(set.getKey(), set.getValue());
      }
    }
    return null;
  }

  private boolean pluginSupportsFile(ImageReaderPlugin imageReaderPlugin, String path){
    return Arrays.stream(imageReaderPlugin.getSupportedFormats())
        .anyMatch(supportedFormat -> formatSupportsFile(supportedFormat, path));
  }

  private boolean formatSupportsFile(SupportedFormat supportedFormat, String path) {
    return Arrays.stream(supportedFormat.getExtensions())
        .anyMatch(ext -> ext.toLowerCase().endsWith(path.toLowerCase()) || ext.equals("*"));
  }


  public List<FileChooser.ExtensionFilter> getPluginExtensionFilter(int id)
      throws PluginNotFoundException {
    if(plugins != null){
      ImageReaderPlugin plugin = plugins.get(id);
      if(plugin != null){
        return parseSupportedFormats(Arrays.asList(plugin.getSupportedFormats()));
      }
    }
    throw new PluginNotFoundException("Plugin not found.");
  }

  public List<FileChooser.ExtensionFilter> parseSupportedFormats(List<SupportedFormat> supportedFormats){
    ArrayList<FileChooser.ExtensionFilter> filters = new ArrayList<>();
    ArrayList<String> allExtensions = new ArrayList<>();
    for(SupportedFormat format : supportedFormats){
      if(supportsAllFiles(format.getExtensions())){
        filters.clear();
        filters.add(new FileChooser.ExtensionFilter(Translator.getString("allfiles"), "*.*"));
        return filters;
      }
      allExtensions.addAll(parseExtensions(Arrays.asList(format.getExtensions())));
      filters.add(new FileChooser.ExtensionFilter(format.getDescription(), parseExtensions(Arrays.asList(format.getExtensions()))));
    }
    filters.add(0, new FileChooser.ExtensionFilter(Translator.getString("allimagefilechooser"), allExtensions));
    return filters;
  }

  private List<String> parseExtensions(List<String> list) {
    ArrayList<String> parsed = new ArrayList<>();
    for(String ext : list){
      int index = ext.lastIndexOf(".")+1;
      String f = ext.substring(index);
      f = "*."+f;
      parsed.add(f);
    }
    return parsed;
  }

  private boolean supportsAllFiles(String[] extensions){
    for(String s : extensions){
      if(s.equals("*"))
        return true;
    }
    return false;
  }

}
