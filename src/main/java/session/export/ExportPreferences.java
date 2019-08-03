package session.export;

import ui.MainApplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;

import static utils.Constants.STTGS_EXPORT_PREFS;

public class ExportPreferences {

    private static List<String> toExportList = new ArrayList<>();

    public static void addToExportList(String s){
        if(!toExportList.contains(s)){
            toExportList.add(s);
        }
    }

    public static void removeToExportList(String s){
        toExportList.remove(s);
    }

    public static List<String> getToExportList() {
        return toExportList;
    }

    public static void exportExportPreferences(){
        Preferences prefs = Preferences.userNodeForPackage(MainApplication.class);
        StringBuilder sb = new StringBuilder();
        for(String s : toExportList){
            sb.append(s).append(";");
        }
        prefs.put(STTGS_EXPORT_PREFS, sb.toString());
    }

    public static void importExportPreferences(){
        toExportList.clear();
        Preferences prefs = Preferences.userNodeForPackage(MainApplication.class);
        String exportPref = prefs.get(STTGS_EXPORT_PREFS, "");
        toExportList.addAll(Arrays.asList(exportPref.split(";")));
    }

    public static boolean containsPreference(String tag){
        return toExportList.contains(tag);
    }
}
