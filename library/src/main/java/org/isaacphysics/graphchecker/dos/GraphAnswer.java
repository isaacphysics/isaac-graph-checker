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
package org.isaacphysics.graphchecker.dos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class GraphAnswer {

    private final int canvasWidth;
    private final int canvasHeight;

    private final List<Curve> curves;

    private final List<Symbol> freeSymbols;

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
