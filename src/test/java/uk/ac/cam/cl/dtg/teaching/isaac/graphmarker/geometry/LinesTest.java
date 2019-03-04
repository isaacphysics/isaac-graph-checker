package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.geometry;

import org.junit.Test;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Line;

import java.util.List;

import static org.junit.Assert.*;
import static uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.TestHelpers.lineOf;

public class LinesTest {

    @Test
    public void splitOnPointsOfInterest() {
        Line line = lineOf(-10,0, -5,5, 0,0, 5,-5, 10,0);

        List<Line> lines = Lines.splitOnPointsOfInterest(line);

        assertEquals(3, lines.size());
        assertEquals(lineOf(-10,0, -5,5), lines.get(0));
        assertEquals(lineOf(-5,5, 0,0, 5,-5), lines.get(1));
        assertEquals(lineOf(5,-5, 10, 0), lines.get(2));
    }
}