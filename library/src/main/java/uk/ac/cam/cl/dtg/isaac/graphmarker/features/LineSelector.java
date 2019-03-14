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

/**
 * A selector which chooses some lines from the Input and applies a LineFeature to them.
 * @param <SelectorInstance> The class representing instances of this selector.
 */
abstract class LineSelector<SelectorInstance extends LineSelector.Instance>
    extends Parsable<SelectorInstance, Input, Map<String, Line>> {

    /**
     * An instance of a LineSelector that can be used to test against input.
     */
    abstract class Instance {
        private final String item;

        /**
         * Create an instance of a LineSelector.
         *
         * @param item The configuration of the line feature inside this selector.
         */
        Instance(final String item) {
            this.item = item;
        }

        /**
         * Get the configuration of the line feature inside this selector.
         *
         * @return The line feature configuration.
         */
        public String item() {
            return item;
        }

        /**
         * Create an input predicate which will be based on a line predicate.
         *
         * This selector will select zero or more lines from the input, apply the line predicate to some or all of them,
         * and then combine the output of the predicates in some way to give an overall test.
         *
         * @param linePredicate The line predicate to be applied.
         * @return An input predicate.
         */
        abstract Predicate<Input> matcher(Predicate<Line> linePredicate);
    }

    /**
     * Create an instance of this feature from the specification provided.
     *
     * It might look like this doesn't do anything useful. But it does, it helps the Java type-checker understand the
     * code, and what could be more important than that?
     *
     * @param item The specification with tag.
     * @return The feature instance.
     */
    public final SelectorInstance deserialize(String item) {
        return super.deserialize(item);
    }
}