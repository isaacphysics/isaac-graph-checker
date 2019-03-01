package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.features;

import org.junit.Test;

import static org.junit.Assert.*;

public class NthLineSelectorTest {

    private NthLineSelector nthLineSelector = new NthLineSelector();

    @Test
    public void testParse() {
        NthLineSelector success = nthLineSelector.parse("1; foo");

        assertEquals("foo", success.item());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseFailureWithLine0() {
        NthLineSelector failNumber = nthLineSelector.parse("0; foo");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseFailureWithNonNumberedLine() {
        NthLineSelector failNumber = nthLineSelector.parse("a; foo");
    }

}