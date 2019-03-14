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
 * Class for matching Input to a list of features, and generating a list of features from an input.
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
     * Create a feature object for matching or generating with the default configuration.
     */
    public Features() {
    }

    /**
     * Given a feature specification, return a predicate which matches Input to that specification.
     *
     * @param feature The feature specification.
     * @return A predicate on Input.
     */
    public Matcher matcher(String feature) {
        String[] features = feature.split("\n");
        List<InputPredicate> matchers = Arrays.stream(features)
                .map(this::itemToFeaturePredicate)
                .collect(Collectors.toList());

        if (matchers.stream().noneMatch(InputPredicate::isLineAware)) {
            CurvesCountFeature.Instance instance = CurvesCountFeature.manager.deserialize("1");
            matchers.add(new InputPredicate("curves: 1 (implicit)", true) {
                @Override
                public boolean test(Input input) {
                    return instance.match(input);
                }
            });
        }

        return new Matcher(matchers);
    }

    /**
     * Generate a feature specification from an Input.
     * @param input The input.
     * @return The feature specification.
     */
    public String generate(Input input) {
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

    public abstract class InputPredicate implements Predicate<Input> {
        private final String item;
        private final boolean lineAware;

        private InputPredicate(String item, boolean lineAware) {
            this.item = item;
            this.lineAware = lineAware;
        }

        public String getItem() {
            return item;
        }

        public boolean isLineAware() {
            return lineAware;
        }
    }

    public abstract class LinePredicate implements Predicate<Line> {
        private final String item;

        private LinePredicate(String item) {
            this.item = item;
        }

        public String getItem() {
            return item;
        }
    }

    public InputPredicate wrapLinePredicate(LinePredicate linePredicate) {
        return new InputPredicate(
            linePredicate.getItem(),
            false
        ) {
            @Override
            public boolean test(Input input) {
                return input.getLines().stream()
                    .anyMatch(linePredicate);
            }
        };
    }

    /**
     * A predicate for matching an input against a particular specification.
     */
    public class Matcher extends InputPredicate {
        private final List<InputPredicate> matchers;

        /**
         * Create a matcher that requires all of the input matchers to pass.
         *
         * @param matchers A list of input predicates.
         */
        private Matcher(List<InputPredicate> matchers) {
            super(
                matchers.stream()
                    .map(InputPredicate::getItem)
                    .collect(Collectors.joining("\r\n")),
                matchers.stream()
                    .anyMatch(InputPredicate::isLineAware)
            );
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
            for (InputPredicate inputPredicate: matchers) {
                boolean test = inputPredicate.test(input);
                if (!test) {
                    failedPredicates.add(inputPredicate.getItem());
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
     * Turn a feature specification into an input predicate and a boolean indicating whether it has a line selector.
     * @param item The feature specification.
     * @return A pair of an input predicate and whether the feature has a line selector.
     */
    private InputPredicate itemToFeaturePredicate(final String item) {
        for (InputFeature feature : INPUT_FEATURES) {
            if (item.startsWith(feature.tag() + ":")) {
                String featureSpec = item.substring(feature.tag().length() + 1);
                InputFeature.Instance instance = feature.deserialize(featureSpec);
                return new InputPredicate(item, true) {
                    @Override
                    public boolean test(Input input) {
                        return instance.match(input);
                    }
                };
            }
        }
        LineSelector.Instance selector = null;
        String subItem = item;
        for (LineSelector selectors : LINE_SELECTORS) {
            if (item.startsWith(selectors.tag() + ":")) {
                String featureSpec = item.substring(selectors.tag().length() + 1);
                selector = selectors.deserialize(featureSpec);
                subItem = selector.item();
                break;
            }
        }
        final String lineItem = subItem;
        final LineSelector.Instance lineSelector = selector;
        for (LineFeature feature : LINE_FEATURES) {
            if (lineItem.startsWith(feature.tag() + ":")) {
                String featureSpec = lineItem.substring(feature.tag().length() + 1);
                Predicate<Line> linePredicate = line -> feature.deserialize(featureSpec).match(line);
                if (lineSelector != null) {
                    return new InputPredicate(item, true) {
                        @Override
                        public boolean test(Input input) {
                            return lineSelector.matcher(linePredicate).test(input);
                        }
                    };
                } else {
                    return wrapLinePredicate(new LinePredicate(item) {
                        @Override
                        public boolean test(Line line) {
                            return linePredicate.test(line);
                        }
                    });
                }
            }
        }
        throw new IllegalArgumentException("Unknown item: " + item);
    }
}
