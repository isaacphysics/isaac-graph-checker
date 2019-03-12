/**
 * Copyright 2019 University of Cambridge
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.cam.cl.dtg.isaac.graphmarker.dos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Curve {

    private final List<Point> pts;
    private final double minX;
    private final double maxX;
    private final double minY;
    private final double maxY;
    private final List<Point> endPt;
    private final List<Point> interX; // What is this?
    private final List<Point> interY; // What is this
    private final List<Point> maxima;
    private final List<Point> minima;
    private final int colorIdx;

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
