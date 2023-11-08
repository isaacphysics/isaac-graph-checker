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

import java.util.Set;
import java.util.function.Predicate;
import org.apache.commons.lang3.NotImplementedException;
import org.isaacphysics.graphchecker.geometry.Sector;
import org.isaacphysics.graphchecker.settings.SettingsInterface;
import org.isaacphysics.graphchecker.features.Context;

import jakarta.annotation.Nullable;

/**
 * A feature which matches against some kind of input or generates feature specifications based on some input.
 * @param <FeatureInstance> The class representing instances of this feature.
 * @param <InputType> The type of input this feature processes.
 * @param <GeneratedType> The type of output this feature can generate. Should be some kind of String collection.
 * @param <SettingsType> The settings type used for this feature.
 */
abstract class Feature<FeatureInstance extends Feature.AbstractInstance, InputType, GeneratedType,
                       SettingsType extends SettingsInterface>
    extends Item<FeatureInstance, InputType, GeneratedType, SettingsType> {

    /**
     * Constructor to wire up settings.
     * @param settings Settings for this feature.
     */
    Feature(SettingsType settings) {
        super(settings);
    }

    /**
     * An instance of a feature.
     */
    abstract class AbstractInstance
        extends Item<FeatureInstance, InputType, GeneratedType, SettingsType>.AbstractInstance implements Predicate<InputType> {
        /**
         * Create an instance of a feature.
         * @param featureData The specification for this feature.
         * @param lineAware True if this feature is aware of which lines it is being applied to.
         */
        AbstractInstance(String featureData, boolean lineAware) {
            super(featureData, lineAware);
        }

        /**
         * Test if the given input matches this feature instance. If it does match, a new context can be returned.
         *
         * @param inputType The input.
         * @param context The context of this input.
         * @return The existing or new context if there is a match, null if there is no match.
         */
        @Nullable
        public Context test(InputType inputType, Context context) {
            if (test(inputType)) {
                return context;
            } else {
                return null;
            }
        }

        /**
         * A single failure object where an input did not match this feature
         */
        abstract class AbstractFailure<ExpectedFeatureType, ActualFeatureType> {
            private final ExpectedFeatureType expectedFeature;
            private final ActualFeatureType actualFeature;
            private final Integer location;

            /**
             * Create a failure that occurred at a specific location in the expected features.
             * A specific expected feature failed given the actual feature was provided.
             *
             * @param expectedFeature The expected feature that failed to be met
             * @param actualFeature The actual features provided instead
             * @param location An indication of where the failure occurred
             */
            public AbstractFailure(ExpectedFeatureType expectedFeature, ActualFeatureType actualFeature, Integer location) {
                this.expectedFeature = expectedFeature;
                this.actualFeature = actualFeature;
                this.location = location;
            }

            public ExpectedFeatureType getExpectedFeature() {
                return expectedFeature;
            }

            public ActualFeatureType getActualFeature() {
                return actualFeature;
            }

            public int getLocation() {
                return location;
            }

            @Override
            public String toString() {
                throw new NotImplementedException("A derived type must provide an output string.");
            }
        }

        /**
         * Test if the given input matches this feature instance without looking at context.
         *
         * @param inputType The input.
         * @return True if there is a match.
         */
        public boolean test(InputType inputType) {
            throw new NotImplementedException("A derived type must overide this or the function above.");
        }
    }
}
