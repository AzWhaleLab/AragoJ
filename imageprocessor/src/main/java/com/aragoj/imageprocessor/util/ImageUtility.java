/*
 * Copyright 2019 franciscoaleixo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aragoj.imageprocessor.util;

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
