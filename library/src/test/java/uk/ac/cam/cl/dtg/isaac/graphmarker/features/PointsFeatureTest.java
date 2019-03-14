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

import uk.ac.cam.cl.dtg.isaac.graphmarker.TestHelpers;
import org.junit.Test;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Line;

import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class PointsFeatureTest {

    @Test
    public void simplePointsTest() {
        List<String> data = PointsFeature.manager.generate(TestHelpers.lineOf(x -> x * x, -5, 5));

        Line passLine = TestHelpers.lineOf(x -> Math.abs(x), -5, 5);
        Line failLine = TestHelpers.lineOf(x -> x, -5, 5);

        assertTrue(PointsFeature.manager.deserializeInternal(data.get(0)).test(passLine));
        assertFalse(PointsFeature.manager.deserializeInternal(data.get(0)).test(failLine));
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustProvideTwoArguments() {
        PointsFeature.manager.deserializeInternal("one,two,three");
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustUseCorrectNames() {
        PointsFeature.manager.deserializeInternal("middle, flat");
    }



}