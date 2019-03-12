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

interface LineSelector<I extends LineSelector.Instance> {

    abstract class Instance {
        private final String item;

        Instance(String item) {
            this.item = item;
        }

        public String item() {
            return item;
        }

        abstract Predicate<Input> matcher(Predicate<Line> linePredicate);
    }

    String TAG();

    I deserialize(String instanceData);

    Map<String, Line> generate(Input input);
}
