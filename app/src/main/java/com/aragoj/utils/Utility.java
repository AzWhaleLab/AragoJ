package com.aragoj.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

public class Utility {

    private static final BigDecimal SQRT_DIG = new BigDecimal(150);
    private static final BigDecimal SQRT_PRE = new BigDecimal(10).pow(SQRT_DIG.intValue());

    /**
     * Private utility method used to compute the square root of a BigDecimal.
     *
     * @author Luciano Culacciatti
     * @url http://www.codeproject.com/Tips/257031/Implementing-SqrtRoot-in-BigDecimal
     */
    private static BigDecimal sqrtNewtonRaphson  (BigDecimal c, BigDecimal xn, BigDecimal precision){
        BigDecimal fx = xn.pow(2).add(c.negate());
        BigDecimal fpx = xn.multiply(new BigDecimal(2));
        BigDecimal xn1 = fx.divide(fpx,2*SQRT_DIG.intValue(), RoundingMode.HALF_DOWN);
        xn1 = xn.add(xn1.negate());
        BigDecimal currentSquare = xn1.pow(2);
        BigDecimal currentPrecision = currentSquare.subtract(c);
        currentPrecision = currentPrecision.abs();
        if (currentPrecision.compareTo(precision) <= -1){
            return xn1;
        }
        return sqrtNewtonRaphson(c, xn1, precision);
    }

    public static double roundTwoDecimals(double number){
        return Math.round(number*100.0)/100.0;
    }

    /**
     * Uses Newton Raphson to compute the square root of a BigDecimal.
     *
     * @author Luciano Culacciatti
     * @url http://www.codeproject.com/Tips/257031/Implementing-SqrtRoot-in-BigDecimal
     */
    public static BigDecimal bigSqrt(BigDecimal c){
        return sqrtNewtonRaphson(c,new BigDecimal(1),new BigDecimal(1).divide(SQRT_PRE));
    }

    public static String getFilePathExtension(String string){
        if(string.contains(".")){
            String[] split = string.split("\\.");
            return split[(split.length == 0) ? 0 : split.length-1];
        }
        return null;
    }

    public static String getFileName(String string){
        if(string.contains(".")){
            String[] split = string.split("\\.");
            return split[(split.length <= 1) ? 0 : split.length-2];
        }
        return null;
    }


    public static String formatFileSize(long size) {
        String hrSize = null;

        double b = size;
        double k = size/1024.0;
        double m = ((size/1024.0)/1024.0);
        double g = (((size/1024.0)/1024.0)/1024.0);
        double t = ((((size/1024.0)/1024.0)/1024.0)/1024.0);

        DecimalFormat dec = new DecimalFormat("0.00");

        if ( t>1 ) {
            hrSize = dec.format(t).concat(" TB");
        } else if ( g>1 ) {
            hrSize = dec.format(g).concat(" GB");
        } else if ( m>1 ) {
            hrSize = dec.format(m).concat(" MB");
        } else if ( k>1 ) {
            hrSize = dec.format(k).concat(" KB");
        } else {
            hrSize = dec.format(b).concat(" Bytes");
        }

        return hrSize;
    }


    private static final String[] SUPPORTED_EXT = {"jpg", "jpeg", "gif", "bmp", "png"};

    public static boolean isImageExtensionSupported(String extension){
        for(int i = 0; i<SUPPORTED_EXT.length; i++){
            if(SUPPORTED_EXT[i].equalsIgnoreCase(extension)){
                return true;
            }
        }
        return false;
    }

}
