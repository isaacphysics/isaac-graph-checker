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

import org.apache.commons.lang3.NotImplementedException;
import org.isaacphysics.graphchecker.data.Input;
import org.isaacphysics.graphchecker.data.Line;
import org.isaacphysics.graphchecker.features.Context;
import org.isaacphysics.graphchecker.settings.SettingsInterface;

import java.util.Map;

/**
 * A selector which chooses some lines from the Input and applies a LineFeature to them.
 * @param <SelectorInstance> The class representing instances of this selector.
 * @param <SettingsType> The settings type used for this feature.
 */
public abstract class LineSelector<SelectorInstance extends LineSelector.Instance,
                                   SettingsType extends SettingsInterface>
    extends Item<SelectorInstance, Input, Map<String, Line>, SettingsType> {

    /**
     * Constructor to wire up settings.
     * @param settings Settings for this feature.
     */
    protected LineSelector(SettingsType settings) {
        super(settings);
    }

    /**
     * An instance of a LineSelector that can be used to test against input.
     */
    public abstract class Instance extends Item.AbstractInstance {
        private final String lineFeatureSpec;

        /**
         * Create an instance of a LineSelector.
         *
         * @param featureData The configuration of this whole feature.
         * @param lineFeatureSpec The configuration of the line feature inside this selector.
         */
        public Instance(String featureData, String lineFeatureSpec) {
            super(featureData, true);
            this.lineFeatureSpec = lineFeatureSpec;
        }

        /**
         * Get the configuration of the line feature inside this selector.
         *
         * @return The line feature configuration.
         */
        public String lineFeatureSpec() {
            return lineFeatureSpec;
        }

        /**
         * Test an input with this selector and a line feature instance.
         *
         * This selector will select zero or more lines from the input, apply the line predicate to some or all of them,
         * and then combine the output of the predicates in some way to give an overall test.
         *
         * @param input The input  to be tested.
         * @param instance The line predicate to be applied.
         * @param context The context for this test.
         * @return New or existing context if there is a match, null if there is no match.
         */
        protected Context test(Input input, LineFeature<?, ?>.Instance instance, Context context) {
            if (test(input, instance)) {
                return context;
            } else {
                return null;
            }
        }

        /**
         * Test an input with this selector and a line feature instance without a context.
         *
         * This selector will select zero or more lines from the input, apply the line predicate to some or all of them,
         * and then combine the output of the predicates in some way to give an overall test.
         *
         * @param input The input  to be tested.
         * @param instance The line predicate to be applied.
         * @return True if there is a match.
         */
        protected boolean test(Input input, LineFeature<?, ?>.Instance instance) {
            throw new NotImplementedException("A derived type must implement this or the function above.");
        }

        /**
         * Wrap a line feature into an lineFeatureSpec feature that matches lines selected by this selector.
         * @param instance The line predicate to wrap.
         * @return An input feature instance that recognises the line feature in the selected line.
         */
        public InputFeature.Instance wrapToItemFeature(LineFeature<?, ?>.Instance instance) {
            return new LineSelectorWrapperFeature(settings()).new Instance(this, instance);
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
    public final SelectorInstance deserialize(String item) {
        return super.deserialize(item);
    }

    /**
     * A wrapper that makes an input feature from a line selector and a line feature.
     */
    static class LineSelectorWrapperFeature extends InputFeature.WrapperFeature<LineSelectorWrapperFeature.Instance> {

        /**
         * Constructor to wire up settings.
         * @param settings Settings for this feature.
         */
        LineSelectorWrapperFeature(SettingsInterface settings) {
            super(settings);
        }

        /**
         * An instance of this feature.
         */
        class Instance extends InputFeature.WrapperFeature<LineSelectorWrapperFeature.Instance>.Instance {

            private final LineSelector<?, ?>.Instance selectorInstance;
            private final LineFeature<?, ?>.Instance lineFeatureInstance;

            /**
             * Create an instance of this feature.
             * @param selectorInstance The line selector instance.
             * @param lineFeatureInstance The line feature instance.
             */
            private Instance(LineSelector<?, ?>.Instance selectorInstance,
                             LineFeature<?, ?>.Instance lineFeatureInstance) {
                super(selectorInstance.getTaggedFeatureData() + lineFeatureInstance.getTaggedFeatureData(), true);
                this.selectorInstance = selectorInstance;
                this.lineFeatureInstance = lineFeatureInstance;
            }

            @Override
            public Context test(Input input, Context context) {
                return selectorInstance.test(input, lineFeatureInstance, context);
            }
        }
    }
}