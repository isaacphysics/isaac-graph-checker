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
package org.isaacphysics.graphchecker.features;

import com.google.common.collect.ImmutableList;
import org.isaacphysics.graphchecker.data.Input;
import org.isaacphysics.graphchecker.features.internals.InputFeature;
import org.isaacphysics.graphchecker.features.internals.LineFeature;
import org.isaacphysics.graphchecker.features.internals.LineSelector;
import org.isaacphysics.graphchecker.settings.SettingsWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.isaacphysics.graphchecker.data.Line;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Class for matching Input to a list of features, and generating a list of features from an input.
 */
public class Features {

    private static final Logger log = LoggerFactory.getLogger(Features.class);

    private final List<LineFeature<?, ?>> lineFeatures;
    private final List<InputFeature<?, ?>> inputFeatures;
    private final List<LineSelector<?, ?>> lineSelectors;
    private final CurvesCountFeature curvesCountFeature;

    /**
     * Create a feature object for matching or generating with the default configuration.
     */
    public Features() {
        this(SettingsWrapper.DEFAULT);
    }

    /**
     * Create a feature object for matching or generating with custom settings.
     * @param settings The settings to use.
     */
    public Features(SettingsWrapper settings) {
        lineFeatures = ImmutableList.of(
            new ExpectedSectorsFeature(settings),
            new SlopeFeature(settings),
            new SymmetryFeature(settings),
            new PointsFeature(settings),
            new UnorderedPointsFeature(settings)
        );
        curvesCountFeature = new CurvesCountFeature(settings);
        inputFeatures = ImmutableList.of(
            curvesCountFeature,
            new IntersectionPointsFeature(settings)
        );
        lineSelectors = ImmutableList.of(
            new NthLineSelector(settings),
            new MatchingLineSelector(settings)
        );
    }

    /**
     * Given a feature specification, return a predicate which matches Input to that specification.
     *
     * @param feature The feature specification.
     * @return A predicate on Input.
     */
    public Matcher matcher(String feature) {
        String[] features = feature.split("\n");
        List<InputFeature<?, ?>.Instance> matchers = Arrays.stream(features)
                .map(item -> itemToFeatureInstance(item.trim()))
                .collect(Collectors.toList());

        if (matchers.stream().noneMatch(InputFeature.Instance::isLineAware)) {
            CurvesCountFeature.Instance instance = curvesCountFeature.oneCurveOnlyImplicitly();
            matchers.add(instance);
        }

        return new Matcher(matchers);
    }

    /**
     * Generate a feature specification from an Input.
     *
     * @param input The input.
     * @return The feature specification.
     */
    public String generate(Input input) {
        List<String> features = new ArrayList<>();

        if (input.getLines().size() == 1) {
            // Don't bother with line selectors
            Line line = input.getLines().get(0);
            features.addAll(generate(line));
        } else {
            for (LineSelector<?, ?> selector : lineSelectors) {
                Map<String, Line> selectedLines = selector.generate(input);

                selectedLines.forEach((lineSelectionSpec, line) -> {
                    features.addAll(generate(line).stream()
                        .map(item -> selector.prefix(lineSelectionSpec + item))
                        .collect(Collectors.toList()));
                });
            }
        }

        // Run through input, trying all input features
        for (InputFeature<?, ?> feature : inputFeatures) {
            Collection<String> foundFeatures = feature.generate(input);
            foundFeatures.stream()
                .map(feature::prefix)
                .forEach(features::add);
        }

        return String.join("\r\n", features);
    }

    /**
     * Generate a list of features that match this line.
     * @param line The line.
     * @return A list of features that match it.
     */
    private List<String> generate(Line line) {
        return lineFeatures.stream()
            .flatMap(lineFeature -> lineFeature.generate(line).stream()
                .filter(feature -> !feature.isEmpty())
                .map(lineFeature::prefix))
            .collect(Collectors.toList());
    }

    /**
     * A predicate for matching an input against a particular specification.
     */
    public class Matcher implements Predicate<Input> {
        private final List<InputFeature<?, ?>.Instance> matchers;

        /**
         * Create a matcher that requires all of the input feature instances to pass.
         *
         * @param matchers A list of input feature instances.
         */
        private Matcher(List<InputFeature<?, ?>.Instance> matchers) {
            this.matchers = matchers;
        }

        /**
         * Get a list of any specifications that an input fails against. Returns an empty list if the input passes.
         *
         * @param input The input to test.
         * @return A list of lines of specification that this input violates.
         */
        public List<String> getFailingSpecs(Input input) {
            List<String> failedPredicates = new ArrayList<>();
            Context context = new Context(input);
            for (InputFeature<?, ?>.Instance inputPredicate: matchers) {
                Context newContext = inputPredicate.test(input, context);
                if (newContext == null) {
                    failedPredicates.add(inputPredicate.getTaggedFeatureData());
                } else {
                    context = newContext;
                }
            }
            return failedPredicates;
        }

        @Override
        public boolean test(Input input) {
            List<String> failingSpecs = getFailingSpecs(input);
            if (!failingSpecs.isEmpty()) {
                log.debug("Failed specs: " + String.join("\r\n\t\t", failingSpecs));
            }
            return failingSpecs.isEmpty();
        }
    }

    /**
     * Turn a feature specification into an input predicate and a boolean indicating whether it has a line selector.
     * @param item The feature specification.
     * @return A pair of an input predicate and whether the feature has a line selector.
     */
    private InputFeature<?, ?>.Instance itemToFeatureInstance(final String item) {
        for (InputFeature<?, ?> feature : inputFeatures) {
            if (feature.canDeserialize(item)) {
                return feature.deserialize(item);
            }
        }

        for (LineSelector<?, ?> selector : lineSelectors) {
            if (selector.canDeserialize(item)) {
                LineSelector<?, ?>.Instance selectorInstance = selector.deserialize(item);
                return selectorInstance.wrapToItemFeature(itemToLineFeature(selectorInstance.lineFeatureSpec()));
            }
        }

        LineFeature<?, ?>.Instance lineFeatureInstance = itemToLineFeature(item);
        return lineFeatureInstance.wrapToItemFeature();
    }

    /**
     * Build a line feature instance from this lineFeatureSpec.
     * @param item The lineFeatureSpec.
     * @return The line feature instance.
     */
    private LineFeature<?, ?>.Instance itemToLineFeature(final String item) {
        for (LineFeature<?, ?> feature : lineFeatures) {
            if (feature.canDeserialize(item)) {
                return feature.deserialize(item);
            }
        }
        throw new IllegalArgumentException("Unknown lineFeatureSpec: " + item);
    }
}
