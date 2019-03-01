package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Line;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Point;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.dos.Curve;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.dos.GraphAnswer;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.dos.GraphSolutionItem;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.dos.GraphSolutions;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.dos.IsaacAnswerResponse;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.dos.ResponseExplanation;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.TestHelpers.lineOf;

public class MarkerTest {

    private final IsaacAnswerResponse failedResponse = new IsaacAnswerResponse(false, new ResponseExplanation(
        "markdown", new String[]{}, "content", Collections.singletonList(
        new ResponseExplanation("markdown", new String[]{}, "content",
            "Unfortunately your answer was incorrect."))));

    private final IsaacAnswerResponse successResponse = new IsaacAnswerResponse(true, new ResponseExplanation(
        "markdown", new String[]{}, "content", Collections.singletonList(
        new ResponseExplanation("markdown", new String[]{}, "content",
            "Your answer was correct!"))));
    private final Marker marker = new Marker();

    private GraphSolutions getSolution(String... answers) {

        return new GraphSolutions(Arrays.stream(answers)
            .map(answer -> new GraphSolutionItem(answer, successResponse))
            .collect(Collectors.toList()),
            failedResponse);
    }

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
            null,
            null,
            0
        );
    }

    @Test
    public void markBasicExample() {
        GraphSolutions solution = getSolution("through:  bottomLeft, origin, topRight");

        GraphAnswer correctAnswer = new GraphAnswer(1000, 1000, Collections.singletonList(
            getCurveFromLine(lineOf(x -> x, -10, 10))
        ), Collections.emptyList());

        GraphAnswer wrongAnswer = new GraphAnswer(1000, 1000, Collections.singletonList(
            getCurveFromLine(lineOf(x -> -x, 10, -10))
        ), Collections.emptyList());

        assertEquals(successResponse, marker.mark(solution, correctAnswer));
        assertEquals(failedResponse, marker.mark(solution, wrongAnswer));
    }

    @Test
    public void markTwoCurveExample() {
        GraphSolutions solution = getSolution("line:1; through: bottomLeft\r\nline:2; through: topRight");

        GraphAnswer correctAnswer = new GraphAnswer(1000, 1000, ImmutableList.of(
            getCurveFromLine(lineOf(x -> 1 / x, -10, -0.1)),
            getCurveFromLine(lineOf(x -> 1 / x, 0.1, 10))
        ), Collections.emptyList());

        assertEquals(successResponse, marker.mark(solution, correctAnswer));
    }
}