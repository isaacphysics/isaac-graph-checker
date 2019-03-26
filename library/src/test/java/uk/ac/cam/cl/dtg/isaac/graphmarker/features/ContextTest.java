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

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.ac.cam.cl.dtg.isaac.graphmarker.TestHelpers;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Input;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Line;

import java.util.Collections;

public class ContextTest {

    @Test(expected = IllegalArgumentException.class)
    public void setFulfilledAssignmentsToBeEmpty() {
        Input input = TestHelpers.inputOf();

        Context context = new Context(input);

        context.withFulfilledAssignments(Collections.emptySet());
    }

    @Test(expected = IllegalArgumentException.class)
    public void setFulfilledAssignmentsToMissingName() {
        Line line1 = TestHelpers.lineOf(x -> x, -10, 10);
        Input input = TestHelpers.inputOf(line1);

        Context context = new Context(input);

        context.withFulfilledAssignments(Collections.singleton(ImmutableBiMap.of("a", line1)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void setFulfilledAssignmentsToMissingLine() {
        Line line1 = TestHelpers.lineOf(x -> x, -10, 10);
        Line line2missing = TestHelpers.lineOf(x -> -x, -10, 10);
        Input input = TestHelpers.inputOf(line1);

        Context context = new Context(input);

        context = context.putIfAbsent("a");

        context = context.withFulfilledAssignments(Collections.singleton(ImmutableBiMap.of("a", line2missing)));
    }
}