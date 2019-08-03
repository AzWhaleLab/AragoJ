package ui.model;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public final class TagRow extends RecursiveTreeObject<TagRow> {
    private final StringProperty tag;
    private final StringProperty value;
    private final BooleanProperty directory;


    private final BooleanProperty toExport;

    public TagRow(String tag, String value, boolean directory) {
        this.tag = new SimpleStringProperty(tag);
        this.value = new SimpleStringProperty(value);
        this.directory = new SimpleBooleanProperty(directory);
        this.toExport = new SimpleBooleanProperty(false);
    }

    public String getTag() {
        return tag.get();
    }

    public StringProperty tagProperty() {
        return tag;
    }

    public String getValue() {
        return value.get();
    }

    public StringProperty valueProperty() {
        return value;
    }

    public boolean isDirectory() {
        return directory.get();
    }

    public BooleanProperty directoryProperty() {
        return directory;
    }

    public boolean isToExportVisible() {
        return toExport.get();
    }

    public boolean isToExportToFile() {
        return toExport.get() && !isDirectory();
    }

    public BooleanProperty toExportProperty() {
        return toExport;
    }

    public void setToExport(boolean toExport) {
        this.toExport.set(toExport);
    }
}
