package com.aragoj.utils;

import java.util.Locale;
import java.util.ResourceBundle;

public class Translator {

    private static Locale locale = Locale.ROOT; // default
    private static ResourceBundle bundle;

    public static String getString(String key) {
        if(bundle == null){
            bundle = ResourceBundle.getBundle("strings/Strings", locale);
        }
        return bundle.getString(key);
    }

    public static ResourceBundle getBundle(){
        if(bundle == null){
            bundle = ResourceBundle.getBundle("strings/Strings", locale);
        }
        return bundle;
    }

    public static void setLocale(Locale locale){
        Translator.locale = locale;
    }
}
