package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.dos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class GraphAnswer {

    private int canvasWidth;
    private int canvasHeight;

    private List<Curve> curves;

    private List<Symbol> freeSymbols;

    @JsonCreator
    public GraphAnswer(@JsonProperty("canvasWidth") int canvasWidth,
                       @JsonProperty("canvasHeight") int canvasHeight,
                       @JsonProperty("curves") List<Curve> curves,
                       @JsonProperty("freeSymbols") List<Symbol> freeSymbols) {
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        this.curves = curves;
        this.freeSymbols = freeSymbols;
    }

    public int getCanvasWidth() {
        return canvasWidth;
    }

    public int getCanvasHeight() {
        return canvasHeight;
    }

    public List<Curve> getCurves() {
        return curves;
    }

    public List<Symbol> getFreeSymbols() {
        return freeSymbols;
    }
}
