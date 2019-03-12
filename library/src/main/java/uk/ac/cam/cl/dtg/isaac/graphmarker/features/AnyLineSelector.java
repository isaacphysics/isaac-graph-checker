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

import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Input;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Line;

import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A Selector which will match if any line in the input matches.
 */
public class AnyLineSelector implements LineSelector<AnyLineSelector.Instance> {

    public static final AnyLineSelector manager = new AnyLineSelector();

    @Override
    public String tag() {
        return "any";
    }

    /**
     * An instance of the AnyLine selector.
     */
    class Instance extends LineSelector.Instance {

        /**
         * Create an instance of this selector.
         * @param item The remainder of this item.
         */
        private Instance(String item) {
            super(item);
        }

        @Override
        Predicate<Input> matcher(final Predicate<Line> linePredicate) {
            return input -> input.getLines().stream()
                .anyMatch(linePredicate);
        }
    }

    @Override
    public Instance deserialize(final String instanceData) {
        return new Instance(instanceData);
    }

    @Override
    public Map<String, Line> generate(final Input input) {
        return input.getLines().stream()
            .collect(Collectors.toMap(ignored -> "", line -> line));
    }

    /**
     * There is only one of these, so make the constructor private.
     *
     * Use the manager singleton.
     */
    private AnyLineSelector() {
    }
}
