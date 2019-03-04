package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.features;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Line;

import java.util.Map;

import static org.junit.Assert.*;
import static uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.TestHelpers.inputOf;
import static uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.TestHelpers.lineOf;

public class NthLineSelectorTest {

    @Test
    public void testDeserialize() {
        NthLineSelector.Instance success = NthLineSelector.manager.deserialize("1; foo");

        assertEquals("foo", success.item());
    }

    @Test
    public void testGenerate() {
        Line line1 = lineOf(x -> x, -1, 1);
        Line line2 = lineOf(x -> -x, -1, 1);
        Map<String, Line> lineMap = NthLineSelector.manager.generate(inputOf(line1,
            line2));

        assertArrayEquals(ImmutableList.of(line1, line2).toArray(), lineMap.values().toArray());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseFailureWithLine0() {
        NthLineSelector.manager.deserialize("0; foo");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseFailureWithNonNumberedLine() {
        NthLineSelector.manager.deserialize("a; foo");
    }

}