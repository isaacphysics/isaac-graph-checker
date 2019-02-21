package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.translation;

import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Line;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Point;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.dos.Curve;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.dos.GraphAnswer;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AnswerToLine implements Function<GraphAnswer, Line> {

    @Override
    public Line apply(GraphAnswer graphAnswer) {
        Curve curve = graphAnswer.getCurves().get(0);

        List<Point> points = curve.getPts().stream()
            .map(pt -> new Point(pt.getX(), pt.getY()))
            .collect(Collectors.toList());

        if (points.size() > 2) {
            final int last = points.size() - 1;
            if (points.get(0).getX() > points.get(last).getX()) {
                Collections.reverse(points);
            }
        }

        return new Line(points);
    }
}
