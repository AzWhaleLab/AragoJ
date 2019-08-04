package com.aragoj.opencv;

import org.opencv.core.Core;

import java.io.File;

public class OpenCVManager {

    public static void loadOpenCV() throws SecurityException, UnsatisfiedLinkError{
        File lib = new File("./libs/"+Core.NATIVE_LIBRARY_NAME + ".dll");
        System.load(lib.getAbsolutePath());
    }
}
