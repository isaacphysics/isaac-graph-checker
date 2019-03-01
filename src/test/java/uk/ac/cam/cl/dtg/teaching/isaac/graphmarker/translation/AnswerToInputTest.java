package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.translation;

import org.junit.Test;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Input;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Line;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Point;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.dos.Curve;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.dos.GraphAnswer;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.TestHelpers.lineOf;

public class AnswerToInputTest {

    private int dirtyTemp = 0;

    private uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.dos.Point toPoint(Point p) {
        return new uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.dos.Point(
            dirtyTemp++,
            p.getX(),
            p.getY()
        );
    }

    private Curve getCurveFromLine(Line line) {
        double minX = line.stream().mapToDouble(Point::getX).min().getAsDouble();
        double maxX = line.stream().mapToDouble(Point::getX).max().getAsDouble();
        double minY = line.stream().mapToDouble(Point::getY).min().getAsDouble();
        double maxY = line.stream().mapToDouble(Point::getY).max().getAsDouble();

        return new Curve(
            line.getPoints().stream()
                .map(this::toPoint)
                .collect(Collectors.toList()),
            minX,
            maxX,
            minY,
            maxY,
            null,
            null,
            null,
            Collections.emptyList(),
            Collections.emptyList(),
            0
        );
    }

    @Test
    public void curvesResortedCorrectly() {
        Line right = lineOf(x -> x, 10, 0);
        Line left = lineOf(x -> x, 0, -10);

        GraphAnswer answer = new GraphAnswer(1000, 1000, Arrays.asList(
            getCurveFromLine(right),
            getCurveFromLine(left)
        ), Collections.emptyList());

        Collections.reverse(left.getPoints());
        Collections.reverse(right.getPoints());

        Input expected = new Input(Arrays.asList(
            left,
            right
        ));

        Input actual = new AnswerToInput().apply(answer);

        assertEquals(expected, actual);
    }
}