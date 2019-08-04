package com.aragoj.opencv.calibration.tools.pointfinder;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;

// https://docs.opencv.org/3.3.1/d9/d0c/group__calib3d.html#ga93efa9b0aa890de240ca32b11253dd4a

public class ChessboardCornerFinder implements PointFinder {

    private static final int FINDER_FLAGS =  Calib3d.CALIB_CB_FAST_CHECK;


    @Override
    public boolean findCorners(Mat inputFrame, Size boardSize, MatOfPoint2f imageCorners) {
        boolean found = Calib3d.findChessboardCorners(inputFrame, boardSize, imageCorners, FINDER_FLAGS);
        if(found){
            TermCriteria term = new TermCriteria(TermCriteria.EPS | TermCriteria.MAX_ITER, 300, 0.0001);
            Imgproc.cornerSubPix(inputFrame, imageCorners, new Size(11, 11), new Size(-1, -1), term);
        }
        return found;
    }
}
