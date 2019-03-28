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
package org.isaacphysics.graphchecker.features;

import com.google.common.collect.ImmutableList;
import org.isaacphysics.graphchecker.TestHelpers;
import org.isaacphysics.graphchecker.settings.SettingsWrapper;
import org.junit.Assert;
import org.junit.Test;
import org.isaacphysics.graphchecker.data.Line;

import java.util.Map;

import static org.junit.Assert.*;

public class NthLineSelectorTest {

    private NthLineSelector nthLineSelector = new NthLineSelector(SettingsWrapper.DEFAULT);

    @Test
    public void testDeserialize() {
        NthLineSelector.Instance success = nthLineSelector.deserializeInternal("1; foo");

        Assert.assertEquals("foo", success.lineFeatureSpec());
    }

    @Test
    public void testGenerate() {
        Line line1 = TestHelpers.lineOf(x -> x, -1, -0.1);
        Line line2 = TestHelpers.lineOf(x -> -x, 0.1, 1);
        Map<String, Line> lineMap = nthLineSelector.generate(TestHelpers.inputOf(line1,
            line2));

        assertArrayEquals(ImmutableList.of(line1, line2).toArray(), lineMap.values().toArray());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseFailureWithLine0() {
        nthLineSelector.deserializeInternal("0; foo");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseFailureWithNonNumberedLine() {
        nthLineSelector.deserializeInternal("a; foo");
    }

    @Test
    public void testDoesntGenerateForOverlappingLines() {
        Line line1 = TestHelpers.lineOf(x -> x, -10, 10);
        Line line2 = TestHelpers.lineOf(x -> -x, -10, 10);
        Map<String, Line> lineMap = nthLineSelector.generate(TestHelpers.inputOf(line1,
            line2));

        assertEquals(0, lineMap.size());
    }

}