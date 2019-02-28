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

    public Point getSize() {
        if (points.isEmpty()) return new Point(0, 0);

        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;

        for (Point p : points) {
            if (p.getX() < minX) minX = p.getX();
            if (p.getX() > maxX) maxX = p.getX();
            if (p.getY() < minY) minY = p.getY();
            if (p.getY() > maxY) maxY = p.getY();
        }

        double centreX = (maxX + minX) / 2;
        double centreY = (maxY + minY) / 2;

        double diffX = maxX - minX;
        double diffY = maxY - minY;

        double startX = points.get(0).getX();
        double startY = points.get(0).getY();

        double x = startX < centreX ? diffX : -diffX;
        double y = startY < centreY ? diffY : -diffY;

        return new Point(x, y);
    }
}
