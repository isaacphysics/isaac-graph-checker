package uk.ac.cam.cl.dtg.isaac.graphmarker.features;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import uk.ac.cam.cl.dtg.isaac.graphmarker.TestHelpers;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Input;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Line;
import uk.ac.cam.cl.dtg.isaac.graphmarker.settings.SettingsWrapper;

import java.util.List;

import static org.junit.Assert.*;

public class IntersectionPointsFeatureTest {

    private IntersectionPointsFeature intersectionPointsFeature = new IntersectionPointsFeature(SettingsWrapper.DEFAULT);

    @Test
    public void testDeserialize() {
        IntersectionPointsFeature.Instance instance = intersectionPointsFeature.deserializeInternal("a to b at origin");

        Line line1 = TestHelpers.lineOf(x -> x, -10, 10);
        Line line2 = TestHelpers.lineOf(x -> -x, -10, 10);
        Line line3 = TestHelpers.lineOf(x -> 0.0, -10, 10);
        Line line4 = TestHelpers.lineOf(x -> 3.0, -10, 10);
        Input input = TestHelpers.inputOf(line1, line2, line3, line4);

        Context context = new Context(input);

        Context match = instance.test(input, context);

        assertNotNull(match);

        assertEquals(6, match.getAssignmentsCopy().size());
    }

    @Test
    public void testGenerate() {
        Line line1 = TestHelpers.lineOf(x -> x, -10, 10);
        Line line2 = TestHelpers.lineOf(x -> -x, -10, 10);
        List<String> lines = intersectionPointsFeature.generate(TestHelpers.inputOf(line1, line2));

        assertEquals(1, lines.size());
        assertEquals("A to B at origin", lines.get(0));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testParseFailureWithNumberedLine() {
        intersectionPointsFeature.deserializeInternal("1 to 2 at origin");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseFailureWithUnknownSector() {
        intersectionPointsFeature.deserializeInternal("a to b at foo");
    }
}