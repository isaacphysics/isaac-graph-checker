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
import uk.ac.cam.cl.dtg.isaac.graphmarker.TestHelpers;
import org.junit.Test;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Line;

import java.util.Map;

import static org.junit.Assert.*;
import static uk.ac.cam.cl.dtg.isaac.graphmarker.TestHelpers.inputOf;
import static uk.ac.cam.cl.dtg.isaac.graphmarker.TestHelpers.lineOf;

public class NthLineSelectorTest {

    @Test
    public void testDeserialize() {
        NthLineSelector.Instance success = NthLineSelector.manager.deserializeInternal("1; foo");

        assertEquals("foo", success.item());
    }

    @Test
    public void testGenerate() {
        Line line1 = TestHelpers.lineOf(x -> x, -1, 1);
        Line line2 = TestHelpers.lineOf(x -> -x, -1, 1);
        Map<String, Line> lineMap = NthLineSelector.manager.generate(TestHelpers.inputOf(line1,
            line2));

        assertArrayEquals(ImmutableList.of(line1, line2).toArray(), lineMap.values().toArray());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseFailureWithLine0() {
        NthLineSelector.manager.deserializeInternal("0; foo");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseFailureWithNonNumberedLine() {
        NthLineSelector.manager.deserializeInternal("a; foo");
    }

}