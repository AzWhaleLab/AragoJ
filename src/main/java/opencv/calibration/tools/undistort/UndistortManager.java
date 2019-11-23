package opencv.calibration.tools.undistort;

import ui.model.ImageItem;
import imageprocess.MetadataUtils;
import opencv.calibration.model.CalibrationModel;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.List;

public class UndistortManager {

    public static final String TMP_DIR = "./tmp/";

    private UndistortRunListener listener;

    public UndistortManager(UndistortRunListener listener){
        this.listener = listener;
    }

    public void undistortImages(CalibrationModel model, List<ImageItem> imageItemList){
        if(model == null || imageItemList == null) {
            if (listener != null) listener.onFinish();
            return;
        }
        new Thread(() -> {
            int size = imageItemList.size();
            for(int i = 0; i<size; i++){
                ImageItem item = imageItemList.get(i);
                if(listener != null) listener.onProgress(i, size, "Undistorting " + item.getName());
                String savePath = undistortImage(model, item);
                if(listener != null) listener.onImageUndistorted(i, savePath);
            }
            if(listener != null) listener.onFinish();
        }).start();

    }

    private String undistortImage(CalibrationModel model, ImageItem imageItem){
        // I/O
        String path = imageItem.getActivePath();
        File srcFile = new File(path);
        String ext = path.substring(path.lastIndexOf("."));
        String savePath = path.substring(0, path.lastIndexOf(".")) + "_u" + ext;
        File tmpFile = getTmpDir(srcFile.getName());

        Mat frame = Imgcodecs.imread(path);
        Size size = frame.size();

        switch (model.getLens()){
            case RECTILINEAR:
                Mat newCameraMtx = Calib3d.getOptimalNewCameraMatrix(
                        model.getIntrinsic(), model.getDistortionCoeffs(), size, 1);
                final Mat undistortedMat = new Mat();
                Imgproc.undistort(frame, undistortedMat, model.getIntrinsic(), model.getDistortionCoeffs(), newCameraMtx);
                Imgcodecs.imwrite(savePath, undistortedMat);
                break;
            case FISHEYE:
                Mat newCameraMtxF = new Mat();
                Mat.eye(3, 3, CvType.CV_64FC1).copyTo(newCameraMtxF);
                newCameraMtxF.put(0, 0, 1.0);
                Calib3d.estimateNewCameraMatrixForUndistortRectify(
                                        model.getIntrinsic(), model.getDistortionCoeffs(),
                                        size, Mat.eye(3, 3, CvType.CV_64FC1), newCameraMtxF, 0, size, 1);
                final Mat undistortedMatF = new Mat();
                Calib3d.undistortImage(frame, undistortedMatF, model.getIntrinsic(), model.getDistortionCoeffs(), newCameraMtxF, size);

                Imgcodecs.imwrite(tmpFile.getPath(), undistortedMatF);
                MetadataUtils.copyMetadata(srcFile, getTmpDir(srcFile.getName()), new File(savePath));
                break;
        }

        return savePath;
    }

    private File getTmpDir(String name){
        File file = new File(TMP_DIR);
        if(!file.exists()) file.mkdirs();
        return new File(TMP_DIR + name);
    }

    public interface UndistortRunListener{
        void onProgress(int currentImage, int totalImages, String description);
        void onImageUndistorted(int currentImage, String savePath);
        void onFinish();
    }
}
