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
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Input;
import org.junit.Test;
import uk.ac.cam.cl.dtg.isaac.graphmarker.settings.SettingsWrapper;

import java.util.List;

import static org.junit.Assert.*;
import static uk.ac.cam.cl.dtg.isaac.graphmarker.TestHelpers.inputOf;
import static uk.ac.cam.cl.dtg.isaac.graphmarker.TestHelpers.lineOf;

public class CurvesCountFeatureTest {

    private CurvesCountFeature curvesCountFeature = new CurvesCountFeature(SettingsWrapper.DEFAULT);

    @Test
    public void simpleCurveCountWorks() {
        List<String> data = curvesCountFeature.generate(TestHelpers.inputOf(
            TestHelpers.lineOf(x -> x, -10, 0),
            TestHelpers.lineOf(x -> x, 0, 10)
        ));

        Input input = TestHelpers.inputOf(TestHelpers.lineOf(x -> 1.0, -10, 10), TestHelpers.lineOf(x -> 0.0, -10, 10));

        assertTrue(curvesCountFeature.deserializeInternal(data.get(0)).test(input));
    }

    @Test
    public void oneCurveDoesntGenerateAFeature() {
        Input input = TestHelpers.inputOf(TestHelpers.lineOf(x -> 1.0, -10, 10));
        assertEquals(0, curvesCountFeature.generate(input).size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void notAnumberThrows() {
        curvesCountFeature.deserializeInternal("foo");
    }
}