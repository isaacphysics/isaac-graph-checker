package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Represents a series of Points. When drawn, connect with cubic Bezier curves, but in here, assume straight segments.
 */
public class Line implements Iterable<Point> {
    private final List<Point> points;

    private final List<PointOfInterest> pointsOfInterest;

    public Line(List<Point> points, List<PointOfInterest> pointsOfInterest) {
        this.points = points;
        this.pointsOfInterest = pointsOfInterest;
    }

    public List<Point> getPoints() {
        return points;
    }

    public List<PointOfInterest> getPointsOfInterest() {
        return pointsOfInterest;
    }

    @Override
    @Nonnull
    public Iterator<Point> iterator() {
        return points.iterator();
    }

    public Stream<Point> stream() {
        return points.stream();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Line points1 = (Line) o;
        return Objects.equals(points, points1.points);
    }

    @Override
    public int hashCode() {
        return Objects.hash(points);
    }

}
