package org.isaacphysics.graphchecker.features;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.checkerframework.checker.units.qual.A;
import org.isaacphysics.graphchecker.data.Line;
import org.isaacphysics.graphchecker.data.PointType;
import org.isaacphysics.graphchecker.geometry.Sector;
import org.isaacphysics.graphchecker.geometry.SectorClassifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UnorderedPointsFeature extends PointsFeature{

    /**
     * Create a points feature with specified settings.
     * @param settings The settings.
     */
    UnorderedPointsFeature(SectorClassifier.Settings settings) {
        super(settings);
    }

    @Override
    public String tag() {
        return "has-points";
    }

    protected class Instance extends PointsFeature.Instance {

        /**
         * Create an instance which expects these to appear somewhere in the line, regardless of order.
         *
         * @param featureData    The specification for this feature.
         * @param expectedPoints The points of interest.
         */
        Instance(String featureData, Set<ImmutablePair<PointType, Sector>> expectedPoints) {
            super(featureData, new ArrayList<>(expectedPoints));
        }

        @Override
        public boolean test(Line line){
            for (ImmutablePair<PointType, Sector> expected : expectedPoints) {
                if (line.getPointsOfInterest().stream().noneMatch(actual -> pointsMatch(expected, actual))){
                    return false;
                }
            }
            return true;
        }
    }

    @Override
    public PointsFeature.Instance deserializeInternal(String featureData) {
        String[] items = featureData.split("\\s*,\\s*");
        return new Instance(featureData, Arrays.stream(items).map(this::deserializeItem).collect(Collectors.toSet()));
    }

}
