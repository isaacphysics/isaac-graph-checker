package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.features;

import org.junit.Test;

import static org.junit.Assert.*;

public class NthLineSelectorTest {

    @Test
    public void testDeserialize() {
        NthLineSelector.Instance success = NthLineSelector.manager.deserialize("1; foo");

        assertEquals("foo", success.item());
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