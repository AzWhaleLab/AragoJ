package opencv.calibration.ui;

import utils.Utility;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.nio.file.Paths;
import java.util.ArrayList;

@XmlRootElement(name = "calibrationsession")
public class CalibrationSession {
    private ArrayList<CalibrationImageItem> items;
    private String path;

    public CalibrationSession(){
        items = new ArrayList<>();
    }

    /**e
     *
     * @return Last index of items, or -1 if empty
     */
    public int getLastIndex(){
        return items.size()-1;
    }

    public CalibrationImageItem getLast(){
        if(items.size() == 0) return null;
        return items.get(items.size()-1);
    }

    public int size(){
        return items.size();
    }
    public void setItem(int index, CalibrationImageItem item){
        items.set(index, item);
    }

    public CalibrationImageItem getItem(int index){
        return items.get(index);
    }

    @XmlElement(name = "image")
    public ArrayList<CalibrationImageItem> getItems() {
        return items;
    }

    public void addItem(CalibrationImageItem item){
        items.add(item);
    }

    public void removeItem(CalibrationImageItem item){
        items.remove(item);
    }

    public void removeItem(int index){
        items.remove(index);
    }

    public void removeItemByPath(String path){
        for(int i = 0; i<items.size(); i++){
            CalibrationImageItem item = items.get(i);
            if(item.getSourceImagePath().equals(path)){
                items.remove(i);
            }
        }
    }

    public void setPath(String path){
        this.path = path;
    }

    @XmlTransient
    public String getPath() {
        return path;
    }

    public String getName() {
        if(path == null || Paths.get(path) == null) return null;
        return Utility.getFileName(Paths.get(path).getFileName().toString());
    }
}
