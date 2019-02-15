package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.features;

import org.junit.Test;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Point;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.geometry.Sector;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.geometry.Sector.*;
import static uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.geometry.TestHelpers.lineOf;

public class LineFollowsExpectedPathTest {

    private static LineFollowsExpectedPath feature = new LineFollowsExpectedPath(Collections.emptyList());

    @Test
    public void basicLineHasCorrectSectorList() {
        List<Sector> sectorList =  feature.convertLineToSectorList(lineOf(
                new Point(-1, 1),
                new Point(2, -1)
        ));

        assertEquals("[topLeft, onAxisWithPositiveY, topRight, onAxisWithPositiveX, bottomRight]", sectorList.toString());
    }

    @Test
    public void xSquaredMinusTwoHasCorrectSectorList() {
        LineFollowsExpectedPath testFeature = new LineFollowsExpectedPath(Arrays.asList(
                topLeft, onAxisWithNegativeX, bottomLeft, onAxisWithNegativeY, bottomRight, onAxisWithPositiveX, topRight));

        assertTrue(testFeature.match(lineOf(x -> x*x - 2, -5, 5)));
        assertFalse(testFeature.match(lineOf(x -> x*x + 2, -5, 5)));
    }

    @Test
    public void xCubedPlusSquaredMinusTwoHasCorrectSectorList() {
        LineFollowsExpectedPath testFeature = new LineFollowsExpectedPath(Arrays.asList(
                bottomLeft, onAxisWithNegativeX, topLeft, onAxisWithPositiveY, topRight, onAxisWithPositiveX, bottomRight, onAxisWithPositiveX, topRight));

        assertTrue(testFeature.match(lineOf(x -> x*x*x - 3*x*x + 2, -10, 10)));
        assertFalse(testFeature.match(lineOf(x -> x*x - 3*x*x + 2, -10, 10)));
    }

    @Test
    public void cosXFromMinus2PiTo2Pi() {
        LineFollowsExpectedPath testFeature = new LineFollowsExpectedPath(Arrays.asList(
                topLeft, onAxisWithNegativeX, bottomLeft, onAxisWithNegativeX, topLeft, onAxisWithPositiveY, topRight, onAxisWithPositiveX, bottomRight, onAxisWithPositiveX, topRight));

        assertTrue(testFeature.match(lineOf(x -> Math.cos(x), -2 * Math.PI, 2 * Math.PI)));
        assertFalse(testFeature.match(lineOf(x -> Math.cos(x + Math.PI / 2), -2 * Math.PI, 2 * Math.PI)));
    }

    @Test
    public void sinXFromMinus2PiTo2Pi() {
        LineFollowsExpectedPath testFeature = new LineFollowsExpectedPath(Arrays.asList(
                onAxisWithNegativeX, topLeft, onAxisWithNegativeX, bottomLeft, origin, topRight, onAxisWithPositiveX, bottomRight, onAxisWithPositiveX));

        assertTrue(testFeature.match(lineOf(x -> Math.sin(x), -2 * Math.PI, 2 * Math.PI)));
        assertFalse(testFeature.match(lineOf(x -> Math.cos(x), -2 * Math.PI, 2 * Math.PI)));
    }

    @Test
    public void matchExpectedSectorList() {
        LineFollowsExpectedPath feature = new LineFollowsExpectedPath(Arrays.asList(Sector.origin, Sector.topRight));

        assertTrue(feature.match(lineOf(x -> x, 0, 10)));
    }

    @Test
    public void customSectionListMatches() {
        Sector[] sectors = {Sector.topLeft, Sector.bottomRight};
        LineFollowsExpectedPath feature = new LineFollowsExpectedPath(Arrays.asList(sectors), sectors);

        assertTrue(feature.match(lineOf(x -> 2 - x, -5, 5)));
    }
}