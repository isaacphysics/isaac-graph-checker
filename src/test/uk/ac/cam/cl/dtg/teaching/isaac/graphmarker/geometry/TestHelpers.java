package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.geometry;

import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Line;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Point;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TestHelpers {
    public static Line lineOf(Point... points) {
        return new Line(Arrays.asList(points));
    }

    public static Line lineOf(Function<Double, Double> f, double minX, double maxX) {
        int points = 1000;
        double diff = (maxX - minX) / (points - 1);
        return new Line(IntStream.range(0, points)
            .mapToDouble(i -> minX + diff * i)
            .mapToObj(x -> new Point(x, f.apply(x)))
            .collect(Collectors.toList())
        );
    }
}
