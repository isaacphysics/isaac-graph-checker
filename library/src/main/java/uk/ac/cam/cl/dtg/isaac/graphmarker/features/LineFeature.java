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

import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Line;

import java.util.List;

/**
 * A feature which matches against a line.
 * @param <FeatureInstance> The class representing instances of this feature.
 */
interface LineFeature<FeatureInstance extends LineFeature.Instance> {

    /**
     * An instance of this feature.
     */
    interface Instance {
        /**
         * Does this feature match this line?
         * @param line The line.
         * @return True if this feature matches the line.
         */
        boolean match(Line line);
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
     * Generate a list of specifications for this feature from some line.
     * @param expectedLine Line to be examined.
     * @return The list of feature specifications.
     */
    List<String> generate(Line expectedLine);
}
