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

import org.isaacphysics.graphchecker.settings.SettingsInterface;

/**
 * Something that can be parsed, generated, and configured in our feature handling code.
 * @param <Instance> The type of instances of this thing.
 * @param <InputType> The type of input this can read.
 * @param <GeneratedType> Some kind of collection of feature specifications this can generate.
 * @param <SettingsType> The settings that are being used in this lineFeatureSpec.
 */
abstract class Item<Instance extends Item.AbstractInstance, InputType, GeneratedType,
                    SettingsType extends SettingsInterface> {

    private final SettingsType settings;

    /**
     * Constructor to wire up settings.
     * @param settings Settings for this lineFeatureSpec.
     */
    Item(SettingsType settings) {
        this.settings = settings;
    }

    /**
     * @return The settings for this lineFeatureSpec.
     */
    protected SettingsType settings() {
        return settings;
    }

    /**
     * An instance of an lineFeatureSpec.
     */
    abstract class AbstractInstance {
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

    /**
     * Can an instance of this lineFeatureSpec be created from this specification?.
     *
     * @param item The specification with tag.
     * @return True if this lineFeatureSpec can be deserialized.
     */
    public boolean canDeserialize(String item) {
        return item.startsWith(tag() + ":");
    }

    /**
     * Create an instance of this lineFeatureSpec from the specification provided.
     *
     * @param item The specification with tag.
     * @return The feature instance.
     */
    Instance deserialize(String item) {
        if (!canDeserialize(item)) {
            throw new IllegalArgumentException("Feature deserialized with wrong tag");
        }
        return deserializeInternal(item.substring(tag().length() + 1));
    }

    /**
     * Put our parsing prefix onto the lineFeatureSpec.
     * @param item lineFeatureSpec to be prefixed.
     * @return Prefixed lineFeatureSpec.
     */
    public String prefix(String item) {
        return tag() + ": " + item;
    }

    /**
     * To identify this lineFeatureSpec in the specification when parsing.
     *
     * @return The name of this lineFeatureSpec.
     */
    public abstract String tag();

    /**
     * Create an instance of this lineFeatureSpec from the specification provided.
     *
     * @param featureData The specification with the tag stripped off.
     * @return The lineFeatureSpec instance.
     */
    protected abstract Instance deserializeInternal(String featureData);

    /**
     * Generate a list of specifications for this feature from some input.
     * @param expectedInput Input to be examined.
     * @return The collection of feature specifications.
     */
    public abstract GeneratedType generate(InputType expectedInput);
}
