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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Input;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Line;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Static class for matching Input to a list of features, and generating a list of features from an input.
 */
public class Features {

    private static final Logger log = LoggerFactory.getLogger(Features.class);

    private static final List<LineFeature<?>> LINE_FEATURES = ImmutableList.of(
        ExpectedSectorsFeature.manager,
        SlopeFeature.manager,
        SymmetryFeature.manager,
        PointsFeature.manager
    );

    private static final List<InputFeature<?>> INPUT_FEATURES = ImmutableList.of(
        CurvesCountFeature.manager
    );

    private static final List<LineSelector<?>> LINE_SELECTORS = ImmutableList.of(
        NthLineSelector.manager
    );

    /**
     * A predicate for matching an input against a particular specification.
     */
    public static class Matcher implements Predicate<Input> {
        private final List<ImmutablePair<Predicate<Input>, String>> matchers;

        /**
         * Create a matcher that requires all of the input matchers to pass.
         *
         * @param matchers A list of pairs of input predicates and the string that defined them.
         */
        private Matcher(List<ImmutablePair<Predicate<Input>, String>> matchers) {
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
            for (ImmutablePair<Predicate<Input>, String> matcher: matchers) {
                boolean test = matcher.left.test(input);
                if (!test) {
                    failedPredicates.add(matcher.right);
                }
            }
            return failedPredicates;
        }

        @Override
        public boolean test(Input input) {
            List<String> failingSpecs = getFailingSpecs(input);
            if (!failingSpecs.isEmpty()) {
                log.info("Failed specs: " + String.join("\r\n\t\t", failingSpecs));
            }
            return failingSpecs.isEmpty();
        }
    }

    /**
     * Given a feature specification, return a predicate which matches Input to that specification.
     *
     * @param feature The feature specification.
     * @return A predicate on Input.
     */
    public static Matcher matcher(String feature) {
        String[] features = feature.split("\n");
        List<ImmutablePair<Predicate<Input>, Boolean>> matchersAndInfo = Arrays.stream(features)
                .map(Features::itemToFeaturePredicate)
                .collect(Collectors.toList());

        List<ImmutablePair<Predicate<Input>, String>> matchers = Streams.zip(matchersAndInfo.stream(),
            Arrays.stream(features), (pair, name) -> ImmutablePair.of(pair.left, name)
        ).collect(Collectors.toList());

        if (matchersAndInfo.stream().noneMatch(pair -> pair.right)) {
            matchers.add(ImmutablePair.of(CurvesCountFeature.manager.deserialize("1")::match,
                "curves: 1 (implicit)"));
        }

        return new Matcher(matchers);
    }

    /**
     * Turn a feature specification into an input predicate and a boolean indicating whether it has a line selector.
     * @param item The feature specification.
     * @return A pair of an input predicate and whether the feature has a line selector.
     */
    private static ImmutablePair<Predicate<Input>, Boolean> itemToFeaturePredicate(String item) {
        for (InputFeature feature : INPUT_FEATURES) {
            if (item.startsWith(feature.tag() + ":")) {
                item = item.substring(feature.tag().length() + 1);
                return ImmutablePair.of(feature.deserialize(item)::match, true);
            }
        }
        LineSelector.Instance selector = null;
        boolean selectorFound = false;
        for (LineSelector selectors : LINE_SELECTORS) {
            if (item.startsWith(selectors.tag() + ":")) {
                item = item.substring(selectors.tag().length() + 1);
                selector = selectors.deserialize(item);
                selectorFound = true;
                break;
            }
        }
        if (selector == null) {
            selector = AnyLineSelector.manager.deserialize(item);
        }
        String subItem = selector.item();
        for (LineFeature feature : LINE_FEATURES) {
            if (subItem.startsWith(feature.tag() + ":")) {
                String finalSubItem = subItem.substring(feature.tag().length() + 1);
                Predicate<Line> linePredicate = line -> feature.deserialize(finalSubItem).match(line);
                return ImmutablePair.of(selector.matcher(linePredicate), selectorFound);
            }
        }
        throw new IllegalArgumentException("Unknown item: " + item);
    }

    /**
     * Generate a feature specification from an Input.
     * @param input The input.
     * @return The feature specification.
     */
    public static String generate(Input input) {
        List<String> features = new ArrayList<>();

        // Run through input, trying all input features
        for (InputFeature<?> feature : INPUT_FEATURES) {
            Collection<String> foundFeatures = feature.generate(input);
            foundFeatures.stream()
                .map(f -> feature.tag() + ": " + f)
                .forEach(features::add);
        }

        Collection<LineSelector<?>> lineSelectorsToUse;
        if (input.getLines().size() == 1) {
            lineSelectorsToUse = Collections.singletonList(AnyLineSelector.manager);
        } else {
            lineSelectorsToUse = LINE_SELECTORS;
        }

        // Run through input, applying all relevant line selectors
        for (LineSelector<?> selector : lineSelectorsToUse) {
            Map<String, Line> selectedLines = selector.generate(input);

            selectedLines.forEach((lineSelectionSpec, line) -> {
                String fullLineSelectionSpec;
                if (selector == AnyLineSelector.manager) {
                    fullLineSelectionSpec = "";
                } else {
                    fullLineSelectionSpec = selector.tag() + ": " + lineSelectionSpec;
                }

                for (LineFeature<?> lineFeature : LINE_FEATURES) {
                    lineFeature.generate(line).stream()
                        .filter(feature -> !feature.isEmpty())
                        .map(feature -> fullLineSelectionSpec + lineFeature.tag() + ": " + feature)
                        .forEach(features::add);
                }
            });
        }

        return String.join("\r\n", features);
    }

    /**
     * Use the static methods.
     */
    private Features() {
    }
}
