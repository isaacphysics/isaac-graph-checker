package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker;

import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Input;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Line;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Point;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.PointOfInterest;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.PointType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TestHelpers {

    public static Line lineOf(List<Point> points) {
        List<PointOfInterest> pointsOfInterest = new ArrayList<>();

        if (points.size() > 2) {
            Point p1 = points.get(0);
            Point p2 = points.get(1);
            for (int i = 2; i < points.size() - 1; i++) {
                Point p3 = points.get(i);
                if (p1.getY() > p2.getY() && p3.getY() > p2.getY()) {
                    pointsOfInterest.add(new PointOfInterest(p2, PointType.MINIMA));
                }
                if (p1.getY() < p2.getY() && p3.getY() < p2.getY()) {
                    pointsOfInterest.add(new PointOfInterest(p2, PointType.MINIMA));
                }
            }
        }
        return new Line(points, pointsOfInterest);
    }

    public static Line lineOf(Point... points) {
        return lineOf(Arrays.asList(points));
    }

    public static Line lineOf(Function<Double, Double> f, double minX, double maxX) {
        int points = 100;
        double diff = (maxX - minX) / (points - 1);
        return lineOf(IntStream.range(0, points)
            .mapToDouble(i -> minX + diff * i)
            .mapToObj(x -> new Point(x, f.apply(x)))
            .collect(Collectors.toList()));
    }

    public static Input inputOf(Function<Double, Double> f, double minX, double maxX) {
        return new Input(Collections.singletonList(lineOf(f, minX, maxX)));
    }

    public static Input inputOf(Line... lines) {
        return new Input(Arrays.asList(lines));
    }
}