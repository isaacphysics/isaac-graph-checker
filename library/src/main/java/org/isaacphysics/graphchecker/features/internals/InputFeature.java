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
import org.isaacphysics.graphchecker.settings.SettingsInterface;

import java.util.List;

/**
 * A feature which matches against an input.
 * @param <FeatureInstance> The class representing instances of this feature.
 * @param <SettingsType> The settings type used for this feature.
 */
public abstract class InputFeature<FeatureInstance extends InputFeature.Instance,
                                   SettingsType extends SettingsInterface>
    extends Feature<FeatureInstance, Input, List<String>, SettingsType> {

    /**
     * Constructor to wire up settings.
     * @param settings Settings for this feature.
     */
    protected InputFeature(SettingsType settings) {
        super(settings);
    }

    /**
     * An instance of an InputFeature.
     */
    public abstract class Instance extends AbstractInstance {
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
     * A type of input feature which wraps other features so doesn't have its own parsing or generation or settings.
     * @param <T> The type of instances of this feature.
     */
    abstract static class WrapperFeature<T extends InputFeature.Instance> extends InputFeature<T, SettingsInterface> {

        /**
         * Constructor to wire up settings.
         * @param settings Settings for this feature.
         */
        WrapperFeature(SettingsInterface settings) {
            super(settings);
        }

        /**
         * @deprecated It doesn't make sense to use this.
         * @throws UnsupportedOperationException This operation is never supported.
         */
        @Override
        @Deprecated
        public String tag() {
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
}