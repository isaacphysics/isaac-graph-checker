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
package uk.ac.cam.cl.dtg.isaac.graphmarker.data;

import java.util.List;
import java.util.Objects;

/**
 * A user's input, made up of a list of lines.
 */
public class Input {
    private final List<Line> lines;

    /**
     * Create input with the specified lines.
     * @param lines The lines.
     */
    public Input(List<Line> lines) {
        this.lines = lines;
    }

    /**
     * Get the lines in this input.
     * @return The lines.
     */
    public List<Line> getLines() {
        return lines;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Input input = (Input) o;
        return Objects.equals(lines, input.lines);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lines);
    }
}
