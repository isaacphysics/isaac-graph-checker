package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.dos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Symbol {
    private String text;
    private double x;
    private double y;

    @JsonCreator
    public Symbol(@JsonProperty("text") String text,
                  @JsonProperty("x") double x,
                  @JsonProperty("y") double y) {
        this.text = text;
        this.x = x;
        this.y = y;
    }

    public String getText() {
        return text;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
