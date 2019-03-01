package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data;

import java.util.List;
import java.util.Objects;

public class Input {
    private List<Line> lines;

    public Input(List<Line> lines) {
        this.lines = lines;
    }

    public List<Line> getLines() {
        return lines;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Input input = (Input) o;
        return Objects.equals(lines, input.lines);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lines);
    }
}
