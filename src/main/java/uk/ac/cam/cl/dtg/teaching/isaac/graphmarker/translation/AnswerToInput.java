package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.translation;

import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Input;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Line;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Point;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.PointOfInterest;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.PointType;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.dos.Curve;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.dos.GraphAnswer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AnswerToInput implements Function<GraphAnswer, Input> {

    @Override
    public Input apply(GraphAnswer graphAnswer) {
        return new Input(graphAnswer.getCurves().stream()
            .map(this::curveToLine)
            .sorted(Comparator.comparingDouble(a -> a.getPoints().stream().findFirst().map(Point::getX).orElse(0.0)))
            .collect(Collectors.toList()));
    }

    private Line curveToLine(Curve curve) {
        List<Point> points = curve.getPts().stream()
            .map(pt -> new Point(pt.getX(), pt.getY()))
            .collect(Collectors.toList());

        if (points.size() > 2) {
            final int last = points.size() - 1;
            if (points.get(0).getX() > points.get(last).getX()) {
                Collections.reverse(points);
            }
        }

        List<PointOfInterest> pointsOfInterest = Stream.concat(
            curve.getMaxima().stream()
                .map(pt -> new PointOfInterest(pt.getX(), pt.getY(), PointType.MAXIMA)),
            curve.getMinima().stream()
                .map(pt -> new PointOfInterest(pt.getX(), pt.getY(), PointType.MINIMA)))
            .sorted(Comparator.comparingDouble(point -> point.getX()))
            .collect(Collectors.toList());

        return new Line(points, pointsOfInterest);
    }
}
