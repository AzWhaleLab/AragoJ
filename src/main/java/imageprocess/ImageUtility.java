package imageprocess;

import java.util.Arrays;
import java.util.List;

public class ImageUtility {

    private static final String[] XMPInterestDirectories = {"AbsoluteAltitude", "FlightPitchDegree", "FlightRollDegree",
                                                            "FlightYawDegree", "GimbalPitchDegree", "GimbalRollDegree",
                                                            "GimbalYawDegree", "RelativeAltitude"};

    public static double getScaleFactor(double eqFocalLength, double focalLength){
        return eqFocalLength/focalLength;
    }
    public static double getCircleOfConfusion(double scaleFactor){
        return Math.sqrt(24*24 + 36*36) / (scaleFactor * 1440);
    }

    public static double getHyperfocalDistance(double scaleFactor, double focalLength, double aperture){
        return (focalLength*focalLength) / (aperture * getCircleOfConfusion(scaleFactor) * 1000);
    }

    public static double getFOV(double focalLength, double scaleFactor){
        double fd = Math.atan2(36, 2*focalLength*scaleFactor);
        return (fd * 360) / Math.PI;
    }

    public static double getLightValue(double aperture, double shutterSpeed, double iso){
        return Math.log((aperture*aperture*100) / (shutterSpeed*iso))/Math.log(2);
    }

    public static String getXMPTagOfInterest(String directory){
        for(String string : XMPInterestDirectories){
            if(directory.contains(string)){
                return string;
            }
        }
        return null;
    }
}
