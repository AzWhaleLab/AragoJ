package utils;

import com.sun.javafx.scene.text.TextLayout;
import com.sun.javafx.tk.Toolkit;
import javafx.scene.text.Font;


/**
 * From com.sun.javafx.scene.control.skin.Utils
 */
public class TextUtils {
    private static final TextLayout layout = Toolkit.getToolkit().getTextLayoutFactory().createLayout();

    public static double computeTextWidth(Font font, String text, double wrappingWidth) {
        layout.setContent(text != null ? text : "", font.impl_getNativeFont());
        layout.setWrapWidth((float)wrappingWidth);
        return layout.getBounds().getWidth();
    }
}
