package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.features;

import org.junit.Test;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Line;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Point;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.geometry.Sector;

import java.util.List;

import static org.junit.Assert.*;
import static uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.geometry.TestHelpers.lineOf;

public class LineFollowsExpectedPathTest {

    private LineFollowsExpectedPath lineFeature = new LineFollowsExpectedPath();

    @Test
    public void convertLineToSectorList() {
        List<Sector> sectorList =  lineFeature.convertLineToSectorList(lineOf(
                new Point(-1, 1),
                new Point(2, -1)
        ));

        assertEquals("[topLeft, onAxisWithPositiveY, topRight, onAxisWithPositiveX, bottomRight]", sectorList.toString());
    }

    @Test
    public void xSquaredMinusTwoHasCorrectSectorList() {
        List<Sector> sectorList =  lineFeature.convertLineToSectorList(lineOf(x -> x*x - 2, -5, 5));

        assertEquals("[topLeft, onAxisWithNegativeX, bottomLeft, onAxisWithNegativeY, bottomRight, onAxisWithPositiveX, topRight]", sectorList.toString());
    }

    @Test
    public void xCubedPlusSquaredMinusTwoHasCorrectSectorList() {
        List<Sector> sectorList =  lineFeature.convertLineToSectorList(lineOf(x -> x*x*x - 3*x*x + 2, -10, 10));

        assertEquals("[bottomLeft, onAxisWithNegativeX, topLeft, onAxisWithPositiveY, topRight, onAxisWithPositiveX, bottomRight, onAxisWithPositiveX, topRight]", sectorList.toString());
    }

    @Test
    public void cosXFromMinus2PiTo2Pi() {
        List<Sector> sectorList =  lineFeature.convertLineToSectorList(lineOf(x -> Math.cos(x), -2 * Math.PI, 2 * Math.PI));

        assertEquals("[topLeft, onAxisWithNegativeX, bottomLeft, onAxisWithNegativeX, topLeft, onAxisWithPositiveY, topRight, onAxisWithPositiveX, bottomRight, onAxisWithPositiveX, topRight]", sectorList.toString());
    }

    @Test
    public void sinXFromMinus2PiTo2Pi() {
        List<Sector> sectorList =  lineFeature.convertLineToSectorList(lineOf(x -> Math.sin(x), -2 * Math.PI, 2 * Math.PI));

        assertEquals("[onAxisWithNegativeX, topLeft, onAxisWithNegativeX, bottomLeft, origin, topRight, onAxisWithPositiveX, bottomRight, onAxisWithPositiveX]", sectorList.toString());
    }
}