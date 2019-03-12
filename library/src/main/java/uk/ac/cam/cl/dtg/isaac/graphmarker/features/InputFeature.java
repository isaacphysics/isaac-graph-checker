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

import java.util.List;

/**
 * A feature which matches against an input.
 * @param <FeatureInstance> The class representing instances of this feature.
 */
interface InputFeature<FeatureInstance extends InputFeature.Instance> {

    /**
     * An instance of a feature.
     */
    interface Instance {
        /**
         * Does this feature match this input?
         * @param input The input.
         * @return True if this feature matches the input.
         */
        boolean match(Input input);
    }

    /**
     * For identify this selector in the input specification when parsing.
     *
     * @return The name of this selector.
     */
    String tag();

    /**
     * Create an instance of this feature from the specification provided.
     *
     * @param featureData The specification.
     * @return The feature instance.
     */
    FeatureInstance deserialize(String featureData);

    /**
     * Generate a list of specifications for this feature from some input.
     * @param expectedInput Input to be examined.
     * @return The list of feature specifications.
     */
    List<String> generate(Input expectedInput);
}