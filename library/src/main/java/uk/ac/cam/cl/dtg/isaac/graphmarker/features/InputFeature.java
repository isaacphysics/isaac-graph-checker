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
abstract class InputFeature<FeatureInstance extends InputFeature.Instance>
    extends Feature<FeatureInstance, Input, List<String>> {

    /**
     * An instance of an InputFeature.
     */
    abstract class Instance extends AbstractInstance {
        /**
         * Create an instance of this feature; this is wrapped for type purposes.
         * @param item The feature specification.
         * @param lineAware Whether this feature is aware of lines.
         */
        protected Instance(String item, boolean lineAware) {
            super(item, lineAware);
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
     * A type of input feature which wraps other features so doesn't have its own parsing or generation.
     * @param <T> The type of instances of this feature.
     */
    private static class WrapperFeature<T extends InputFeature.Instance> extends InputFeature<T> {

        /**
         * @deprecated It doesn't make sense to use this.
         * @throws UnsupportedOperationException This operation is never supported.
         */
        @Override
        @Deprecated
        protected String tag() {
            throw new UnsupportedOperationException();
        }

        /**
         * @deprecated It doesn't make sense to use this.
         * @throws UnsupportedOperationException This operation is never supported.
         */
        @Override
        @Deprecated
        protected T deserializeInternal(String featureData) {
            throw new UnsupportedOperationException();
        }

        /**
         * @deprecated It doesn't make sense to use this.
         * @throws UnsupportedOperationException This operation is never supported.
         */
        @Override
        @Deprecated
        public List<String> generate(Input expectedInput) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String prefix(String item) {
            return item;
        }
    }

    /**
     * A wrapper that makes an input feature from a line selector and a line feature.
     */
    static class LineSelectorWrapperFeature extends WrapperFeature<LineSelectorWrapperFeature.Instance> {
        /**
         * An instance of this feature.
         */
        class Instance extends InputFeature<Instance>.Instance {

            private final LineSelector<?>.Instance selectorInstance;
            private final LineFeature<?>.Instance lineFeatureInstance;

            /**
             * Create an instance of this feature.
             * @param item The specification text that created this feature.
             * @param selectorInstance The line selector instance.
             * @param lineFeatureInstance The line feature instance.
             */
            private Instance(String item, LineSelector<?>.Instance selectorInstance,
                               LineFeature<?>.Instance lineFeatureInstance) {
                super(item, true);
                this.selectorInstance = selectorInstance;
                this.lineFeatureInstance = lineFeatureInstance;
            }

            @Override
            public boolean test(Input input) {
                return selectorInstance.matcher(lineFeatureInstance).test(input);
            }
        }

        /**
         * Create an instance of this feature.
         * @param item The specification text that created this feature.
         * @param selectorInstance The line selector instance.
         * @param lineFeatureInstance The line feature instance.
         * @return An input feature instance that recognises the line feature in the selected line(s).
         */
        public Instance wrap(String item, LineSelector<?>.Instance selectorInstance,
                             LineFeature<?>.Instance lineFeatureInstance) {
            return new Instance(item, selectorInstance, lineFeatureInstance);
        }
    }

    /**
     * A wrapper that makes an input feature from a line feature. It will match if any line matches.
     */
    static class LineFeatureWrapper extends WrapperFeature<LineFeatureWrapper.Instance> {
        /**
         * An instance of this feature.
         */
        class Instance extends InputFeature<LineFeatureWrapper.Instance>.Instance {
            private final LineFeature<?>.Instance lineFeatureInstance;

            /**
             * Create an instance of this feature.
             * @param item The specification text that created this feature.
             * @param lineFeatureInstance The line feature instance.
             */
            private Instance(String item, LineFeature<?>.Instance lineFeatureInstance) {
                super(item, false);
                this.lineFeatureInstance = lineFeatureInstance;
            }

            @Override
            public boolean test(Input input) {
                return input.getLines().stream()
                    .anyMatch(lineFeatureInstance);
            }
        }

        /**
         * Create an instance of this feature.
         * @param item The specification text that created this feature.
         * @param lineFeatureInstance The line feature instance.
         * @return An input feature instance that recognises the line feature in any line.
         */
        public Instance wrap(String item, LineFeature.Instance lineFeatureInstance) {
            return new Instance(item, lineFeatureInstance);
        }
    }
}