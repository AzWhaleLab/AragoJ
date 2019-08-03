package ui.model;

import com.jfoenix.svg.SVGGlyph;

import java.io.IOException;

public interface LayerListItem {
    SVGGlyph getSVG() throws IOException;
    String getPrimaryText();
    void setPrimaryText(String primaryText);
    String getSecondaryText();
    Type getType();
    boolean isVisualElement();

    public enum  Type {
        LINE, EQUATION, AREA
    }
}
