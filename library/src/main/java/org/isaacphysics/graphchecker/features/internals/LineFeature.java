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
package org.isaacphysics.graphchecker.features.internals;

import org.isaacphysics.graphchecker.data.Input;
import org.isaacphysics.graphchecker.features.Context;
import org.isaacphysics.graphchecker.settings.SettingsInterface;
import org.isaacphysics.graphchecker.data.Line;

import java.util.List;
import java.util.function.Predicate;

/**
 * A feature which matches against a line.
 * @param <FeatureInstance> The class representing instances of this feature.
 * @param <SettingsType> The settings type used for this feature.
 */
public abstract class LineFeature<FeatureInstance extends LineFeature.Instance, SettingsType extends SettingsInterface>
    extends Feature<FeatureInstance, Line, List<String>, SettingsType> {

    /**
     * Constructor to wire up settings.
     * @param settings Settings for this feature.
     */
    protected LineFeature(SettingsType settings) {
        super(settings);
    }

    /**
     * An instance of a LineFeature.
     */
    public abstract class Instance extends AbstractInstance {
        /**
         * Create an instance of this feature; this is wrapped for type purposes.
         * @param item The feature specification.
         */
        protected Instance(String item) {
            super(item, false);
        }

        /**
         * Wrap this line feature into an lineFeatureSpec feature that matches if any line matches.
         * @return An input feature instance that recognises the line feature in any line.
         */
        public InputFeature.Instance wrapToItemFeature() {
            return new LineFeatureWrapper(settings()).new Instance(this.getTaggedFeatureData(), this);
        }

        /**
         * Test if this line feature matches this line. Line features cannot have context which is ensured by the above.
         * @param line The line to test.
         * @return True if this line matches this feature.
         */
        public abstract boolean test(Line line);
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
    private class LineFeatureWrapper extends InputFeature.WrapperFeature<LineFeatureWrapper.Instance> {

        /**
         * Constructor to wire up settings.
         * @param settings Settings for this feature.
         */
        private LineFeatureWrapper(SettingsInterface settings) {
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
            public Context test(Input input, Context context) {
                if (input.getLines().stream().anyMatch(lineFeatureInstance)) {
                    return context;
                } else {
                    return null;
                }
            }
        }
    }
}
