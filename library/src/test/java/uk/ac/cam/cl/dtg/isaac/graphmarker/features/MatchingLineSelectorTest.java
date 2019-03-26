/**
 * Copyright 2019 University of Cambridge
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.cam.cl.dtg.isaac.graphmarker.features;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import uk.ac.cam.cl.dtg.isaac.graphmarker.TestHelpers;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Line;
import uk.ac.cam.cl.dtg.isaac.graphmarker.settings.SettingsWrapper;

import java.util.Map;

import static org.junit.Assert.*;

public class MatchingLineSelectorTest {

    private MatchingLineSelector matchingLineSelector = new MatchingLineSelector(SettingsWrapper.DEFAULT);

    @Test
    public void testDeserialize() {
        MatchingLineSelector.Instance success = matchingLineSelector.deserializeInternal("w; foo");

        assertEquals("foo", success.lineFeatureSpec());
    }

    @Test
    public void testGenerate() {
        Line line1 = TestHelpers.lineOf(x -> x, -10, -10);
        Line line2 = TestHelpers.lineOf(x -> -x, -10, 10);
        Map<String, Line> lineMap = matchingLineSelector.generate(TestHelpers.inputOf(line1,
            line2));

        assertArrayEquals(ImmutableList.of(line1, line2).toArray(), lineMap.values().toArray());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseFailureWithNumberedLine() {
        matchingLineSelector.deserializeInternal("1; foo");
    }

    @Test
    public void testDoesntGenerateForNonOverlappingLines() {
        Line line1 = TestHelpers.lineOf(x -> 1 / x, -10, -0.1);
        Line line2 = TestHelpers.lineOf(x -> 1 / x, 0.1, 10);
        Map<String, Line> lineMap = matchingLineSelector.generate(TestHelpers.inputOf(line1,
            line2));

        assertEquals(0, lineMap.size());
    }
}