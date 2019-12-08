package opencv.utils;

import java.nio.IntBuffer;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritablePixelFormat;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import org.opencv.imgproc.Imgproc;

public class OpenCVUtils {

    public static Mat getMatFromImage(Image image) throws UnsupportedFormatException {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        PixelReader reader = image.getPixelReader();
        PixelFormat.Type formatType = reader.getPixelFormat().getType();
        switch (formatType){
            case INT_ARGB_PRE:
                int[] bufferInt4Pre = new int[width * height * 4];
                WritablePixelFormat<IntBuffer> formatARGBPre = WritablePixelFormat.getIntArgbPreInstance();
                reader.getPixels(0, 0, width, height, formatARGBPre, bufferInt4Pre, 0, width * 4);
                Mat mat = new Mat(height, width, CvType.CV_32SC4);
                mat.put(0, 0, bufferInt4Pre);
                return mat;
            case INT_ARGB:
                int[] bufferInt4 = new int[width * height * 4];
                WritablePixelFormat<IntBuffer> formatARGB = WritablePixelFormat.getIntArgbInstance();
                reader.getPixels(0, 0, width, height, formatARGB, bufferInt4, 0, width * 4);
                Mat mat1 = new Mat(height, width, CvType.CV_32SC4);
                mat1.put(0, 0, bufferInt4);
                return mat1;
            case BYTE_BGRA_PRE:
                byte[] buffer4Pre = new byte[width * height * 4];
                WritablePixelFormat<ByteBuffer> formatBGRAPre = WritablePixelFormat.getByteBgraPreInstance();
                reader.getPixels(0, 0, width, height, formatBGRAPre, buffer4Pre, 0, width * 4);
                Mat mat2 = new Mat(height, width, CvType.CV_8UC4);
                mat2.put(0, 0, buffer4Pre);
                return mat2;
            case BYTE_BGRA:
                byte[] buffer4 = new byte[width * height * 4];
                WritablePixelFormat<ByteBuffer> formatBGRA = WritablePixelFormat.getByteBgraInstance();
                reader.getPixels(0, 0, width, height, formatBGRA, buffer4, 0, width * 4);
                Mat mat3 = new Mat(height, width, CvType.CV_8UC4);
                mat3.put(0, 0, buffer4);
                return mat3;
            case BYTE_RGB:
                // For some reason, we don't have a RGB writable format, so we need to use ByteBGRA
                // and convert it back to BGR
                byte[] buffer3 = new byte[width * height * 4];
                WritablePixelFormat<ByteBuffer> formatRGB = WritablePixelFormat.getByteBgraInstance();
                reader.getPixels(0, 0, width, height, formatRGB, buffer3, 0, width * 4);
                Mat mat4 = new Mat(height, width, CvType.CV_8UC4);
                mat4.put(0, 0, buffer3);
                Mat mat5 = new Mat(height, width, CvType.CV_8UC3);
                Imgproc.cvtColor(mat4, mat5, Imgproc.COLOR_BGRA2BGR);
                return mat5;
        }
        throw new UnsupportedFormatException();
    }

    public static int getCvType(PixelFormat.Type type){
        switch (type){
            case INT_ARGB_PRE:
                return CvType.CV_32SC4;
            case INT_ARGB:
                return CvType.CV_32SC4;
            case BYTE_BGRA_PRE:
                return CvType.CV_8UC4;
            case BYTE_BGRA:
                return CvType.CV_8UC4;
            case BYTE_RGB:
                return CvType.CV_8UC3;
        }
        return CvType.CV_8UC3;
    }

    public static class UnsupportedFormatException extends Exception { }


    public static Image getImageFromMat(Mat frame) {
        MatOfByte buffer = new MatOfByte();
        if(frame.channels() > 3){ // We have alpha channel
            Imgcodecs.imencode(".png", frame, buffer);
        } else {
            Imgcodecs.imencode(".bmp", frame, buffer);
        }
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }

}
