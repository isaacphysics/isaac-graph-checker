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
package uk.ac.cam.cl.dtg.isaac.graphmarker.features.internals;

import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Input;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Line;
import uk.ac.cam.cl.dtg.isaac.graphmarker.settings.SettingsInterface;

import java.util.Map;
import java.util.function.Predicate;

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
    public LineSelector(SettingsType settings) {
        super(settings);
    }

    /**
     * An instance of a LineSelector that can be used to test against input.
     */
    public abstract class Instance extends Item.AbstractInstance {
        private final String item;

        /**
         * Create an instance of a LineSelector.
         *
         * @param featureData The configuration of this whole feature.
         * @param item The configuration of the line feature inside this selector.
         */
        public Instance(String featureData, String item) {
            super(featureData, true);
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
        protected abstract Predicate<Input> matcher(Predicate<Line> linePredicate);

        /**
         * Wrap a line feature into an item feature that matches lines selected by this selector.
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
            public boolean test(Input input) {
                return selectorInstance.matcher(lineFeatureInstance).test(input);
            }
        }
    }
}