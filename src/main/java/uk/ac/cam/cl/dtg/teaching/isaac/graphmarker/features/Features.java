package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.features;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.tuple.ImmutablePair;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Input;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Line;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Features {
    private static List<LineFeature> lineFeatures = ImmutableList.of(
        new ExpectedSectorsFeature(),
        new SlopeFeature(),
        new SymmetryFeature()
    );

    private static final CurvesCountFeature curvesCountFeature = new CurvesCountFeature();
    private static List<InputFeature> inputFeatures = ImmutableList.of(
        curvesCountFeature
    );

    private static List<LineSelector> lineSelectors = ImmutableList.of(
        new NthLineSelector()
    );

    public static Predicate<Input> matcher(String feature) {
        String[] features = feature.split("\n");
        List<ImmutablePair<Predicate<Input>, Boolean>> matchersAndInfo = Arrays.stream(features)
                .map(item -> itemToFeaturePredicate(item))
                .collect(Collectors.toList());

        List<Predicate<Input>> matchers = matchersAndInfo.stream()
            .map(pair -> pair.left)
            .collect(Collectors.toList());

        if (matchersAndInfo.stream()
            .noneMatch(pair -> pair.right)) {
            matchers.add(curvesCountFeature.matcher(curvesCountFeature.deserialize("1")));
        }

        return input -> matchers.stream().allMatch(matcher -> matcher.test(input));
    }

    private static ImmutablePair<Predicate<Input>, Boolean> itemToFeaturePredicate(String item) {
        for (InputFeature feature : inputFeatures) {
            if (item.startsWith(feature.TAG() + ":")) {
                item = item.substring(feature.TAG().length() + 1);
                return ImmutablePair.of(feature.matcher(feature.deserialize(item)), true);
            }
        }
        LineSelector selector = LineSelector.any(item);
        boolean selectorFound = false;
        for (LineSelector selectors : lineSelectors) {
            if (item.startsWith(selectors.TAG() + ":")) {
                item = item.substring(selectors.TAG().length() + 1);
                selector = selectors.parse(item);
                selectorFound = true;
                break;
            }
        }
        item = selector.item();
        for (LineFeature feature : lineFeatures) {
            if (item.startsWith(feature.TAG() + ":")) {
                item = item.substring(feature.TAG().length() + 1);
                Predicate<Line> linePredicate = feature.matcher(feature.deserialize(item));
                return ImmutablePair.of(selector.matcher(linePredicate), selectorFound);
            }
        }
        throw new IllegalArgumentException("Unknown item: " + item);
    }
}
