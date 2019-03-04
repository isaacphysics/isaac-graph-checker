package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.features;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Input;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Line;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Features {

    private static final Logger log = LoggerFactory.getLogger(Features.class);

    private static List<LineFeature<?>> lineFeatures = ImmutableList.of(
        ExpectedSectorsFeature.manager,
        SlopeFeature.manager,
        SymmetryFeature.manager,
        PointsFeature.manager
    );

    private static List<InputFeature<?>> inputFeatures = ImmutableList.of(
        CurvesCountFeature.manager
    );

    private static List<LineSelector<?>> lineSelectors = ImmutableList.of(
        NthLineSelector.manager
    );

    public static Predicate<Input> matcher(String feature) {
        String[] features = feature.split("\n");
        List<ImmutablePair<Predicate<Input>, Boolean>> matchersAndInfo = Arrays.stream(features)
                .map(item -> itemToFeaturePredicate(item))
                .collect(Collectors.toList());

        List<ImmutablePair<Predicate<Input>, String>> matchers = Streams.zip(matchersAndInfo.stream(),
            Arrays.stream(features), (pair, name) -> ImmutablePair.of(pair.left, name)
        ).collect(Collectors.toList());

        if (matchersAndInfo.stream().noneMatch(pair -> pair.right)) {
            matchers.add(ImmutablePair.of(CurvesCountFeature.manager.deserialize("1")::match,
                "curves: 1 (implicit)"));
        }

        return input -> {
            List<String> failedPredicates = new ArrayList<>();
            boolean success = true;
            for (ImmutablePair<Predicate<Input>, String> matcher: matchers) {
                boolean test = matcher.left.test(input);
                success &= test;
                if (!test) {
                    failedPredicates.add(matcher.right);
                }
            }
            if (!success) {
                log.info("Failed predicates: " + String.join("\r\n\t\t", failedPredicates));
            }
            return success;
        };
    }

    private static ImmutablePair<Predicate<Input>, Boolean> itemToFeaturePredicate(String item) {
        for (InputFeature feature : inputFeatures) {
            if (item.startsWith(feature.TAG() + ":")) {
                item = item.substring(feature.TAG().length() + 1);
                return ImmutablePair.of(feature.deserialize(item)::match, true);
            }
        }
        LineSelector.Instance selector = null;
        boolean selectorFound = false;
        for (LineSelector selectors : lineSelectors) {
            if (item.startsWith(selectors.TAG() + ":")) {
                item = item.substring(selectors.TAG().length() + 1);
                selector = selectors.deserialize(item);
                selectorFound = true;
                break;
            }
        }
        if (selector == null) {
            selector = AnyLineSelector.manager.deserialize(item);
        }
        String subItem = selector.item();
        for (LineFeature feature : lineFeatures) {
            if (subItem.startsWith(feature.TAG() + ":")) {
                String finalSubItem = subItem.substring(feature.TAG().length() + 1);
                Predicate<Line> linePredicate = line -> feature.deserialize(finalSubItem).match(line);
                return ImmutablePair.of(selector.matcher(linePredicate), selectorFound);
            }
        }
        throw new IllegalArgumentException("Unknown item: " + item);
    }

    public static String generate(Input input) {
        List<String> features = new ArrayList<>();

        // Run through input, trying all input features
        for (InputFeature<?> feature : inputFeatures) {
            Collection<String> foundFeatures = feature.generate(input);
            foundFeatures.stream()
                .map(f -> feature.TAG() + ": "+ f)
                .forEach(features::add);
        }

        Collection<LineSelector<?>> lineSelectorsToUse = input.getLines().size() == 1
            ? Collections.singletonList(AnyLineSelector.manager)
            : lineSelectors;

        // Run through input, applying all relevant line selectors
        for (LineSelector<?> selector : lineSelectorsToUse) {
            Map<String, Line> selectedLines = selector.generate(input);

            selectedLines.forEach((lineSelectionSpec, line) -> {
                String fullLineSelectionSpec = selector == AnyLineSelector.manager ? ""
                    : selector.TAG() + ": " + lineSelectionSpec;

                for (LineFeature<?> lineFeature : lineFeatures) {
                    lineFeature.generate(line).stream()
                        .filter(feature -> !feature.isEmpty())
                        .map(feature -> fullLineSelectionSpec + lineFeature.TAG() + ": " + feature)
                        .forEach(features::add);
                }
            });
        }

        return String.join("\r\n", features);
    }
}
