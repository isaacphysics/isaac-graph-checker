package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data;

import java.util.Iterator;
import java.util.List;

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
    public Iterator<Point> iterator() {
        return points.iterator();
    }
}
