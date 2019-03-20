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

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A feature which matches against a line.
 * @param <FeatureInstance> The class representing instances of this feature.
 */
abstract class LineFeature<FeatureInstance extends LineFeature.Instance, SettingsType extends Item.Settings>
    extends Feature<FeatureInstance, Line, List<String>, SettingsType> {

    public LineFeature(SettingsType settings) {
        super(settings);
    }

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

        /**
         * Wrap this line feature into an item feature that matches if any line matches.
         * @return An input feature instance that recognises the line feature in any line.
         */
        public InputFeature.Instance wrapToItemFeature() {
            return new LineFeatureWrapper(settings).new Instance(this.getTaggedFeatureData(), this);
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

    /**
     * A wrapper that makes an input feature from a line feature. It will match if any line matches.
     */
    class LineFeatureWrapper extends InputFeature.WrapperFeature<LineFeatureWrapper.Instance> {

        public LineFeatureWrapper(Settings settings) {
            super(settings);
        }

        /**
         * An instance of this feature.
         */
        class Instance extends InputFeature<Instance, SettingsType>.Instance {
            private final LineFeature<?, ?>.Instance lineFeatureInstance;

            /**
             * Create an instance of this feature.
             * @param item The specification text that created this feature.
             * @param lineFeatureInstance The line feature instance.
             */
            private Instance(String item, LineFeature<?, ?>.Instance lineFeatureInstance) {
                super(item, false);
                this.lineFeatureInstance = lineFeatureInstance;
            }

            @Override
            public boolean test(Input input) {
                return input.getLines().stream()
                    .anyMatch(lineFeatureInstance);
            }
        }
    }
}
