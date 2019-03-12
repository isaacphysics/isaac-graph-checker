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
package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.features;

import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Input;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Line;

import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AnyLineSelector implements LineSelector<AnyLineSelector.Instance> {

    public static final AnyLineSelector manager = new AnyLineSelector();

    public String TAG() {
        return "any";
    }

    class Instance extends LineSelector.Instance {

        private Instance(String item) {
            super(item);
        }

        @Override
        Predicate<Input> matcher(Predicate<Line> linePredicate) {
            return input -> input.getLines().stream()
                .anyMatch(linePredicate);
        }
    }

    @Override
    public Instance deserialize(String instanceData) {
        return new Instance(instanceData);
    }

    @Override
    public Map<String, Line> generate(Input input) {
        return input.getLines().stream()
            .collect(Collectors.toMap(ignored -> "", line -> line));
    }

    private AnyLineSelector() {
    }
}
