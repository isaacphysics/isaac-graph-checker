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

import org.isaacphysics.graphchecker.geometry.SectorBuilder;
import org.isaacphysics.graphchecker.data.HumanNamedEnum;
import org.isaacphysics.graphchecker.data.Line;
import org.isaacphysics.graphchecker.data.Point;
import org.isaacphysics.graphchecker.data.PointOfInterest;
import org.isaacphysics.graphchecker.data.PointType;
import org.isaacphysics.graphchecker.features.internals.LineFeature;
import org.isaacphysics.graphchecker.geometry.Lines;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An line feature which requires the line to have a specific symmetry.
 *
 * The symmetry is defined by:
 * - Splitting the line into lines between the start point, each point of interest, and the end point.
 *   - if there are an even number of points of interest, we add a split point in the middle of the points of interest.
 * - Calculating the bounding box sizes of each of the lines.
 * - Working from the centre, checking each box is similar in size to the corresponding box on the other side.
 *
 * - For even symmetry, the boxes must be the same size and there must be a turning point near the Y axis.
 * - For odd symmetry, the boxes must have opposite heights and the middle point must be at or near the origin.
 * - For symmetric symmetry, the boxes must be the same sizes.
 * - For anti-symmetric symmetry, the boxes must be opposite sizes.
 *
 * (Note symmetric and anti-symmetric are just even and odd but not aligned to the axis.)
 *
 * So, for example, x^2 has even symmetry because it has one point of interest, so it will be split into two boxes,
 * which will have similar sizes and the point of interest is on the Y axis.
 *
 * (x - 1)(x - 3)(x - 4) will have anti-symmetric symmetry because the boxes are opposite in size but the middle point
 * is not at the origin.
 */
public class SymmetryFeature extends LineFeature<SymmetryFeature.Instance, SymmetryFeature.Settings> {

    /**
     * Create a symmetry feature with specified settings.
     * @param settings The settings.
     */
    SymmetryFeature(Settings settings) {
        super(settings);
    }

    /**
     * The settings for a SymmetryFeature.
     */
    @SuppressWarnings("magicNumber")
    public interface Settings extends SectorBuilder.Settings {
        /**
         * @return The maximum proportion of difference between the two sides for a line to be considered to have some
         *         symmetry.
         */
        default double getSymmetrySimilarity() {
            return 0.4;
        }
    }

    @Override
    public String tag() {
        return "symmetry";
    }

    /**
     * Type of symmetry.
     */
    enum SymmetryType implements HumanNamedEnum {
        NONE,
        ODD,
        EVEN,
        SYMMETRIC,
        ANTISYMMETRIC
    }

    /**
     * An instance of the Symmetry feature.
     */
    public class Instance extends LineFeature<?, ?>.Instance {

        private final SymmetryType symmetryType;

        /**
         * Create an instance which expects the line to have a specific symmetry.
         * @param featureData The specification for this feature.
         * @param symmetryType The expected symmetry.
         */
        private Instance(String featureData, SymmetryType symmetryType) {
            super(featureData);
            this.symmetryType = symmetryType;
        }

        @Override
        public boolean test(Line line) {
            return getSymmetryOfLine(line) == symmetryType;
        }
    }

    @Override
    public Instance deserializeInternal(String featureData) {
        return new Instance(featureData, SymmetryType.valueOf(featureData.trim().toUpperCase()));
    }

    @Override
    public List<String> generate(Line expectedLine) {
        SymmetryType symmetryOfLine = getSymmetryOfLine(expectedLine);
        if (symmetryOfLine != SymmetryType.NONE) {
            return Collections.singletonList(symmetryOfLine.humanName());
        } else {
            return Collections.emptyList();
        }
    }


    /**
     * Calculate the symmetry of a line.
     *
     * @param line The line to calculate.
     * @return The type of symmetry of this line.
     */
    SymmetryType getSymmetryOfLine(Line line) {
        if (line.getPoints().isEmpty()) {
            return SymmetryType.NONE;
        }

        List<PointOfInterest> points = new ArrayList<>(line.getPointsOfInterest());
        if ((points.size() % 2) == 0) {
            Point centre;
            if (points.size() == 0) {
                centre = Lines.getCentreOfPoints(line.getPoints());
            } else {
                centre = Lines.getCentreOfPoints(new ArrayList<>(points));
            }
            PointOfInterest virtualCenter = new PointOfInterest(centre, PointType.VIRTUAL_CENTRE);
            points.add(points.size() / 2, virtualCenter);
        }

        List<Line> subLines = Lines.splitOnPoints(line, points);

        boolean symmetric = true;
        boolean antisymmetric = true;

        int size = subLines.size() / 2;
        for (int i = 0; i  < size; i++) {
            Line left = subLines.get(size - i - 1);
            Line right = subLines.get(size + i);

            Point leftSize = Lines.getSize(left);
            Point rightSize = Lines.getSize(right);

            double xDifference = (rightSize.getX() - leftSize.getX()) / rightSize.getX();
            double yDifferenceOdd = (rightSize.getY() - leftSize.getY()) / rightSize.getY();
            double yDifferenceEven = (rightSize.getY() + leftSize.getY()) / rightSize.getY();

            if (Math.abs(xDifference) < settings().getSymmetrySimilarity()) {
                if (rightSize.getY() == 0 && leftSize.getY() == 0) {
                    continue;
                }
                if (Math.abs(yDifferenceOdd) < settings().getSymmetrySimilarity()) {
                    symmetric = false;
                    continue;
                }
                if (Math.abs(yDifferenceEven) < settings().getSymmetrySimilarity()) {
                    antisymmetric = false;
                    continue;
                }
            }
            symmetric = false;
            antisymmetric = false;
            break;
        }

        PointOfInterest centerPoint = points.get(points.size() / 2);
        if (antisymmetric && settings().getSectorBuilder().byName(SectorBuilder.RELAXED_ORIGIN).contains(centerPoint)) {
            return SymmetryType.ODD;
        } else if (symmetric && Math.abs(centerPoint.getX()) < settings().getAxisSlop()) {
            return SymmetryType.EVEN;
        } else if (symmetric) {
            return SymmetryType.SYMMETRIC;
        } else if (antisymmetric) {
            return SymmetryType.ANTISYMMETRIC;
        }
        return SymmetryType.NONE;
    }

}
