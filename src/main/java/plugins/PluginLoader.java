package plugins;

import com.aragoj.plugins.Plugin;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import javafx.stage.FileChooser;

public abstract class PluginLoader<T extends Plugin> {

  HashMap<Integer, T> plugins;

  private static final String PLUGINS_FOLDER_PATH = "plugins";

  abstract Class<T> getClassInstance();

  abstract Plugin.Type getPluginType();

  public HashMap<Integer, T> loadPlugins() throws CouldNotLoadPluginException {
    HashMap<Integer, T> plugins = new HashMap<>();

    URL[] fileUrls = getPluginFileUrls();
    if (fileUrls.length > 0) {
      URLClassLoader urlClassLoader = new URLClassLoader(fileUrls);
      ServiceLoader<T> serviceLoader = ServiceLoader.load(getClassInstance(), urlClassLoader);
      Iterator<T> iter = serviceLoader.iterator();
      while (iter.hasNext()) {
        try {
          T plugin = iter.next();
          if (plugin.getPluginType()
              .equals(getPluginType())) {
            int id = generateIdentifier(plugin);
            if (plugins.containsKey(id)) {
              throw new CouldNotLoadPluginException("Could not load plugin: "
                  + plugin.getPluginName()
                  + ". Reason: Name is used by other plugin");
            }
            plugins.putIfAbsent(generateIdentifier(plugin), plugin);
          }
        } catch (java.util.ServiceConfigurationError e) {
          e.printStackTrace();
        }
      }
    }
    this.plugins = plugins;
    return plugins;
  }

  private int generateIdentifier(T plugin) {
    return Objects.hash(plugin.getPluginName(), plugin.getPluginType());
  }

  private static URL[] getPluginFileUrls() {
    File[] files = getPluginFiles();
    ArrayList<URL> urls = new ArrayList<>();

    for (File file : files) {
      try {
        urls.add(file.toURI()
            .toURL());
      } catch (MalformedURLException e) {
        e.printStackTrace();
      }
    }
    URL[] urlArray = new URL[urls.size()];
    urlArray = urls.toArray(urlArray);
    return urlArray;
  }

  private static File[] getPluginFiles() {
    File pluginFolder = new File(PLUGINS_FOLDER_PATH);
    if (pluginFolder.exists() && pluginFolder.isDirectory()) {
      return pluginFolder.listFiles(file -> file.getPath()
          .toLowerCase()
          .endsWith(".jar"));
    } else {
      return new File[0];
    }
  }

  public static class CouldNotLoadPluginException extends Exception {
    public CouldNotLoadPluginException(String message) {
      super(message);
    }

    public CouldNotLoadPluginException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  public static class PluginNotFoundException extends Exception {
    public PluginNotFoundException(String message) {
      super(message);
    }

    public PluginNotFoundException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
