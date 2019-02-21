package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Represents a series of Points. When drawn, connect with cubic Bezier curves, but in here, assume straight segments.
 */
public class Line implements Iterable<Point> {
    private List<Point> points;

    public Line(List<Point> points) {
        this.points = points;
    }

    public List<Point> getPoints() {
        return points;
    }

    @Override
    @Nonnull
    public Iterator<Point> iterator() {
        return points.iterator();
    }

    public Stream<Point> stream() {
        return points.stream();
    }
}
