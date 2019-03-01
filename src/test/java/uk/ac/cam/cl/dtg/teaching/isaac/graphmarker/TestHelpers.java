package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker;

import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Input;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Line;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Point;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TestHelpers {
    public static Line lineOf(Point... points) {
        return new Line(Arrays.asList(points));
    }

    public static Line lineOf(Function<Double, Double> f, double minX, double maxX) {
        int points = 100;
        double diff = (maxX - minX) / (points - 1);
        return new Line(IntStream.range(0, points)
            .mapToDouble(i -> minX + diff * i)
            .mapToObj(x -> new Point(x, f.apply(x)))
            .collect(Collectors.toList())
        );
    }

    public static Input inputOf(Function<Double, Double> f, double minX, double maxX) {
        return new Input(Collections.singletonList(lineOf(f, minX, maxX)));
    }

    public static Input inputOf(Line... lines) {
        return new Input(Arrays.asList(lines));
    }
}