package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.geom;

import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Intersection;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Line;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a region of the graph, e.g. above the x axis, or on the y axis with y < 0
 */
public class Sector {

    private static final double FAR_AWAY = 1000;
    private static final double SLOP = 0.01;

    private final List<Segment> segments;

    private Sector(List<Segment> segments) {
        this.segments = segments;
    }

    public boolean contains(Point p) {
        return this.segments.stream().allMatch(segment -> segment.inside(p));
    }

    private boolean intersects(Segment s) {
        return this.segments.stream().anyMatch(segment -> segment.intersects(s));
    }

    public Intersection intersects(Line line) {
        boolean allInside = true;
        boolean someInside = false;
        boolean anyIntersections = false;
        Point lastPoint = null;
        for (Point point : line) {
            if (contains(point)) {
                someInside = true;
            } else {
                allInside = false;
            }
            if (lastPoint != null) {
                anyIntersections |= intersects(new Segment(lastPoint, point));
            }
            lastPoint = point;
        }
        return  allInside && !anyIntersections ? Intersection.INSIDE
                : someInside || anyIntersections ? Intersection.INTERSECTS
                : Intersection.OUTSIDE;
    }

    /*public boolean whollyContains(Line line) {
        return false;
    }*/

    static Sector topRight() {
        return builder()
                .setLeft(0)
                .setBottom(0)
                .build();
    }

    static Sector topLeft() {
        return builder()
                .setRight(0)
                .setBottom(0)
                .build();
    }

    static Sector bottomLeft() {
        return builder()
                .setRight(0)
                .setTop(0)
                .build();
    }

    static Sector bottomRight() {
        return builder()
                .setLeft(0)
                .setTop(0)
                .build();
    }

    static Sector positiveXaxis() {
        return builder()
                .setLeft(-SLOP)
                .setRight(SLOP)
                .setBottom(0)
                .build();
    }

    static Sector negativeXaxis() {
        return builder()
                .setLeft(-SLOP)
                .setRight(SLOP)
                .setTop(0)
                .build();
    }

    static Sector positiveYaxis() {
        return builder()
                .setBottom(-SLOP)
                .setTop(SLOP)
                .setLeft(0)
                .build();
    }

    static Sector negativeYaxis() {
        return builder()
                .setBottom(-SLOP)
                .setTop(SLOP)
                .setRight(0)
                .build();
    }


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        double xMin = -FAR_AWAY;
        double xMax = FAR_AWAY;
        double yMin = -FAR_AWAY;
        double yMax = FAR_AWAY;

        private Builder() {
        }

        public Builder setLeft(double x) {
            xMin = x;
            return this;
        }

        public Builder setRight(double x) {
            xMax = x;
            return this;
        }

        public Builder setTop(double y) {
            yMax = y;
            return this;
        }

        public Builder setBottom(double y) {
            yMin = y;
            return this;
        }

        public Sector build() {
            List<Segment> segments = new ArrayList<>();
            if (xMin != -FAR_AWAY) {
                segments.add(new Segment(new Point(xMin, yMax), new Point(xMin, yMin)));
            }
            if (xMax != FAR_AWAY) {
                segments.add(new Segment(new Point(xMax, yMin), new Point(xMax, yMax)));
            }
            if (yMin != -FAR_AWAY) {
                segments.add(new Segment(new Point(xMin, yMin), new Point(xMax, yMin)));
            }
            if (yMax != FAR_AWAY) {
                segments.add(new Segment(new Point(xMax, yMax), new Point(xMin, yMax)));
            }
            return new Sector(segments);
        }
    }
}
