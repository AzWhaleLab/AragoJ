package opencv.calibration.model;

import lombok.Data;

@Data
public class CalibrationConfig {
    public enum Pattern {
        CHESSBOARD("Chessboard"),
        CIRCLE_GRID("Circle grid");
        private final String text;
        Pattern(final String text) { this.text = text; }
        @Override public String toString() { return text; }
    }

    public enum Lens {
        RECTILINEAR("Rectilinear"),
        FISHEYE("Fisheye");
        private final String text;
        Lens(final String text) { this.text = text; }
        @Override public String toString() { return text; }
        public static Lens fromString(String text) {
            for (Lens b : Lens.values()) {
                if (b.text.equalsIgnoreCase(text)) {
                    return b;
                }
            }
            return null;
        }
    }

    private Pattern pattern;
    private int hrzPoints;
    private int vertPoints;
    private int pointSize;
    private Lens lens;

    public CalibrationConfig(Pattern pattern, int hrzPoints, int vertPoints, int pointSize, Lens lens) {
        this.pattern = pattern;
        this.hrzPoints = hrzPoints;
        this.vertPoints = vertPoints;
        this.pointSize = pointSize;
        this.lens = lens;
    }

    @Override
    public String toString() {
        return String.valueOf(hrzPoints) + "x" + vertPoints + " " + pattern.toString() + " " + lens;
    }

}
