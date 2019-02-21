package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.features;

import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Line;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Features {
    private static List<Feature> featureList = Arrays.asList(new ExpectedSectorsFeature());

    public static Predicate<Line> matcher(String feature) {
        String[] features = feature.split("\n");
        List<Predicate<Line>> matchers = Arrays.stream(features)
                .map(item -> itemToFeaturePredicate(item))
                .collect(Collectors.toList());
        return line -> matchers.stream().allMatch(matcher -> matcher.test(line));
    }

    private static Predicate<Line> itemToFeaturePredicate(String item) {
        for (Feature feature : featureList) {
            if (item.startsWith(feature.TAG() + ":")) {
                item = item.substring(feature.TAG().length() + 1);
                return feature.matcher(feature.deserialize(item));
            }
        }
        throw new IllegalArgumentException("Unknown item: " + item);
    }
}
