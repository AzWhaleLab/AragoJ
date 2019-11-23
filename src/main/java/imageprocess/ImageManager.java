package imageprocess;

import com.aragoj.plugins.imagereader.ImageReaderPlugin;
import com.aragoj.plugins.imagereader.SupportedFormat;
import com.aragoj.plugins.imagereader.image.PixelData;
import com.drew.lang.CompoundException;
import com.sun.javafx.image.impl.ByteBgra;
import com.sun.javafx.image.impl.IntArgb;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import javafx.stage.FileChooser;
import javafx.util.Pair;
import plugins.ImageReaderPluginLoader;
import plugins.PluginLoader;
import utils.scalr.Scalr;

public class ImageManager {

  private ImageReaderPluginLoader pluginLoader;
  private DefaultImageParser defaultImageParser = new DefaultImageParser();

  private Image cachedImage;

  public ImageManager() {
    pluginLoader = new ImageReaderPluginLoader();
  }

  public HashMap<Integer, ImageReaderPlugin> loadImageImportPlugins()
      throws PluginLoader.CouldNotLoadPluginException {
    return pluginLoader.loadPlugins();
  }

  public AJImage retrieveImage(int pluginId, String path)
      throws ImageProcessingException, IOException, ImageReaderPlugin.FormatNotSupported {
    if (defaultImageParser.supportsFile(path) && pluginId == -1) {
      return defaultImageParser.defaultParseImage(path);
    } else {
      Pair<Integer, ImageReaderPlugin> plugin = pluginLoader.getPluginForFile(pluginId, path);
      if (plugin != null) {
        String name = Paths.get(path)
            .getFileName()
            .toString();
        com.aragoj.plugins.imagereader.image.Image image = plugin.getValue()
            .readImage(path);
        int with = image.getPixelData()
            .getWidth();
        int height = image.getPixelData()
            .getHeight();

        AJImage ajImage =
            new AJImage(name, path, with, height, image.getMetadata(), loadThumbnail(image));
        ajImage.setOpenerSourceId(plugin.getKey());
        return ajImage;
      }
    }
    throw new ImageProcessingException("Image could not be parsed: No parser found.");
  }

  public Image getCachedImage() {
    return cachedImage;
  }

  public Image loadImage(int preferredSource, String path)
      throws ImageProcessingException, ImageReaderPlugin.FormatNotSupported {
    if (defaultImageParser.supportsFile(path) && preferredSource == -1) {
      cachedImage = new Image(new File(path).toURI()
          .toString());
      return cachedImage;
    } else {
      Pair<Integer, ImageReaderPlugin> plugin =
          pluginLoader.getPluginForFile(preferredSource, path);
      if (plugin != null) {
        com.aragoj.plugins.imagereader.image.Image image = plugin.getValue()
            .readImage(path);
        PixelData data = image.getPixelData();
        int stride = getScanlineStride(data);

        cachedImage = getImage(data, stride);
        return cachedImage;
      }
    }
    throw new ImageProcessingException("Image could not be parsed: No parser found.");
  }

  public List<FileChooser.ExtensionFilter> getDefaultExtensionFilters() {
    SupportedFormat[] defaultFormats = defaultImageParser.getSupportedFormats();
    return pluginLoader.parseSupportedFormats(new ArrayList<>(Arrays.asList(defaultFormats)));
  }

  public List<FileChooser.ExtensionFilter> getPluginExtensionFilters(int id)
      throws PluginLoader.PluginNotFoundException {
    return pluginLoader.getPluginExtensionFilter(id);
  }

  public Image loadThumbnail(int id, String path) {
    if (defaultImageParser.supportsFile(path)) {
      cachedImage = new Image(new File(path).toURI()
          .toString());
      BufferedImage bufferedImage = SwingFXUtils.fromFXImage(cachedImage, null);

      Scalr.Method method = Scalr.Method.ULTRA_QUALITY;
      if (cachedImage.getWidth() * cachedImage.getHeight() > 3000000) {
        method = Scalr.Method.BALANCED;
      }

      if (cachedImage.getHeight() > cachedImage.getWidth()) {
        return SwingFXUtils.toFXImage(
            Scalr.resize(bufferedImage, method, Scalr.Mode.FIT_TO_HEIGHT, 50,
                (BufferedImageOp) null), null);
      }
      return SwingFXUtils.toFXImage(
          Scalr.resize(bufferedImage, method, Scalr.Mode.FIT_TO_WIDTH, 50, (BufferedImageOp) null),
          null);
    } else {
      ImageReaderPlugin plugin = pluginLoader.getPluginForFile(id, path)
          .getValue();
      if (plugin != null) {
        try {
          com.aragoj.plugins.imagereader.image.Image image = plugin.readImage(path);
          return loadThumbnail(image);
        } catch (ImageReaderPlugin.FormatNotSupported formatNotSupported) {
          formatNotSupported.printStackTrace();
        }
      }
    }
    System.err.println("Image could not be parsed: No parser found.");
    return null;
  }

  private Image loadThumbnail(com.aragoj.plugins.imagereader.image.Image image) {
    PixelData data = image.getPixelData();
    int stride = getScanlineStride(data);
    Image image1 = getImage(data, stride);
    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image1, null);

    Scalr.Method method = Scalr.Method.ULTRA_QUALITY;
    if (data.getWidth() * data.getHeight() > 3000000) {
      method = Scalr.Method.BALANCED;
    }

    if (data.getHeight() > data.getWidth()) {
      return SwingFXUtils.toFXImage(
          Scalr.resize(bufferedImage, method, Scalr.Mode.FIT_TO_HEIGHT, 50, (BufferedImageOp) null),
          null);
    }
    return SwingFXUtils.toFXImage(
        Scalr.resize(bufferedImage, method, Scalr.Mode.FIT_TO_WIDTH, 50, (BufferedImageOp) null),
        null);
  }

  private Image getImage(PixelData data, int stride) {
    switch (data.getPixelFormat()
        .getType()) {
      case INT_ARGB:
        IntArgb.ToIntArgbPreConverter()
            .convert((int[]) data.getBuffer()
                .array(), 0, stride, (int[]) data.getBuffer()
                .array(), 0, stride, data.getWidth(), data.getHeight());
      case INT_ARGB_PRE:
        return Image.impl_fromPlatformImage(
            com.sun.prism.Image.fromIntArgbPreData((IntBuffer) data.getBuffer(), data.getWidth(),
                data.getHeight(), stride));
      case BYTE_BGRA:
        ByteBgra.ToByteBgraPreConverter()
            .convert((byte[]) data.getBuffer()
                .array(), 0, stride, (byte[]) data.getBuffer()
                .array(), 0, stride, data.getWidth(), data.getHeight());
      case BYTE_BGRA_PRE:
        return Image.impl_fromPlatformImage(
            com.sun.prism.Image.fromByteBgraPreData((ByteBuffer) data.getBuffer(), data.getWidth(),
                data.getHeight(), stride));
      case BYTE_RGB:
        return Image.impl_fromPlatformImage(
            com.sun.prism.Image.fromByteRgbData((ByteBuffer) data.getBuffer(), data.getWidth(),
                data.getHeight(), stride));
      case BYTE_INDEXED_BGRA:
        int[] colors = ((PixelData.IndexedFormat) data.getPixelFormat()).getColors();
        WritableImage image1 = new WritableImage(data.getWidth(), data.getHeight());
        PixelFormat pixelFormat = WritablePixelFormat.createByteIndexedInstance(colors);
        image1.getPixelWriter()
            .setPixels(0, 0, data.getWidth(), data.getHeight(), pixelFormat, data.getBuffer(),
                stride);
        return image1;
      case BYTE_INDEXED_BGRA_PRE:
        int[] colors2 = ((PixelData.IndexedFormat) data.getPixelFormat()).getColors();
        WritableImage image2 = new WritableImage(data.getWidth(), data.getHeight());
        PixelFormat pixelFormat2 =
            WritablePixelFormat.createByteIndexedPremultipliedInstance(colors2);
        image2.getPixelWriter()
            .setPixels(0, 0, data.getWidth(), data.getHeight(), pixelFormat2, data.getBuffer(),
                stride);
        return image2;
    }
    return null;
  }

  private int getScanlineStride(PixelData pixelData) {
    switch (pixelData.getPixelFormat()
        .getType()) {
      case INT_ARGB_PRE:
      case INT_ARGB:
      case BYTE_BGRA_PRE:
      case BYTE_BGRA:
      case BYTE_INDEXED_BGRA:
      case BYTE_INDEXED_BGRA_PRE:
        return pixelData.getWidth() * 4;
      case BYTE_RGB:
        return pixelData.getWidth() * 3;
    }
    return -1;
  }

  public void clearTmpFolder() {
    File file = new File("./tmp");
    if (file.isDirectory()) {
      File[] files = file.listFiles();
      if (files != null && files.length > 0) {
        for (File f : files) {
          f.delete();
        }
      }
    }
  }

  public static class ImageProcessingException extends CompoundException {

    public ImageProcessingException(String msg) {
      super(msg);
    }

    public ImageProcessingException(Throwable exception) {
      super(exception);
    }

    public ImageProcessingException(String msg, Throwable innerException) {
      super(msg, innerException);
    }
  }
}
