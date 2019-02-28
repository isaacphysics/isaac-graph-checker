package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.dos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Curve {

    private List<Point> pts;
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    private List<Point> endPt;
    private List<Point> interX; // What is this?
    private List<Point> interY; // What is this
    private List<Point> maxima;
    private List<Point> minima;
    private int colorIdx;

    @JsonCreator
    public Curve(@JsonProperty("pts") List<Point> pts,
                 @JsonProperty("minX") double minX,
                 @JsonProperty("maxX") double maxX,
                 @JsonProperty("minY") double minY,
                 @JsonProperty("maxY") double maxY,
                 @JsonProperty("endPt") List<Point> endPt,
                 @JsonProperty("interX") List<Point> interX,
                 @JsonProperty("interY") List<Point> interY,
                 @JsonProperty("maxima") List<Point> maxima,
                 @JsonProperty("minima") List<Point> minima,
                 @JsonProperty("colorIdx") int colorIdx) {
        this.pts = pts;
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.endPt = endPt;
        this.interX = interX;
        this.interY = interY;
        this.maxima = maxima;
        this.minima = minima;
        this.colorIdx = colorIdx;
    }

    public List<Point> getPts() {
        return pts;
    }

    public double getMinX() {
        return minX;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMaxY() {
        return maxY;
    }

    public List<Point> getEndPt() {
        return endPt;
    }

    public List<Point> getInterX() {
        return interX;
    }

    public List<Point> getInterY() {
        return interY;
    }

    public List<Point> getMaxima() {
        return maxima;
    }

    public List<Point> getMinima() {
        return minima;
    }

    public int getColorIdx() {
        return colorIdx;
    }
}