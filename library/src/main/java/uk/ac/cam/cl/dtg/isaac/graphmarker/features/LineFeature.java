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
abstract class LineFeature<FeatureInstance extends LineFeature.Instance>
    extends Feature<FeatureInstance, Line, List<String>> {

    /**
     * An instance of a LineFeature.
     */
    abstract class Instance extends AbstractInstance {
        /**
         * Create an instance of this feature; this is wrapped for type purposes.
         * @param item The feature specification.
         */
        protected Instance(String item) {
            super(item, false);
        }
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
    public final FeatureInstance deserialize(String item) {
        return super.deserialize(item);
    }
}
