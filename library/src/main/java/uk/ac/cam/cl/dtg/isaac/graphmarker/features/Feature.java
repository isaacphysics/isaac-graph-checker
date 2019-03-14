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

import java.util.function.Predicate;

/**
 * A feature which matches against some kind of input or generates feature specifications based on some input.
 * @param <FeatureInstance> The class representing instances of this feature.
 * @param <InputType> The type of input this feature processes.
 * @param <GeneratedType> The type of output this feature can generate. Should be some kind of String collection.
 */
public abstract class Feature<FeatureInstance extends Feature.AbstractInstance, InputType, GeneratedType>
    extends Parsable<FeatureInstance, InputType, GeneratedType> {

    /**
     * An instance of a feature.
     */
    abstract class AbstractInstance implements Predicate<InputType> {
        private final String featureData;
        private final boolean lineAware;

        /**
         * Create an instance of a feature.
         * @param featureData The specification for this feature.
         * @param lineAware True if this feature is aware of which lines it is being applied to.
         */
        AbstractInstance(String featureData, boolean lineAware) {
            this.featureData = featureData;
            this.lineAware = lineAware;
        }

        /**
         * @return The full definition for this feature.
         */
        public String getTaggedFeatureData() {
            return prefix(featureData);
        }

        /**
         * @return True if this feature is aware of which lines it is being applied to.
         */
        public boolean isLineAware() {
            return lineAware;
        }
    }
}
