package com.aragoj.opencv.calibration.tools.pointfinder;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Size;

public class CircleGridCornerFinder implements PointFinder {
    public enum Type {SYMMETRIC, ASSYMETRIC}

    private int type;

    public CircleGridCornerFinder(Type t){
        switch (t){
            case SYMMETRIC:
                type = Calib3d.CALIB_CB_SYMMETRIC_GRID;
                break;
            case ASSYMETRIC:
                type = Calib3d.CALIB_CB_ASYMMETRIC_GRID;
                break;
        }
    }
    @Override
    public boolean findCorners(Mat inputFrame, Size boardSize, MatOfPoint2f imageCorners) {
        return Calib3d.findCirclesGrid(inputFrame, boardSize, imageCorners, type + Calib3d.CALIB_CB_CLUSTERING);
    }
}
