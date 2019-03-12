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

import uk.ac.cam.cl.dtg.isaac.graphmarker.data.HumanNamedEnum;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Line;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Point;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.PointOfInterest;
import uk.ac.cam.cl.dtg.isaac.graphmarker.geometry.Lines;
import uk.ac.cam.cl.dtg.isaac.graphmarker.geometry.Sector;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An line feature which requires the line to have a specific symmetry.
 */
public class SymmetryFeature implements LineFeature<SymmetryFeature.Instance> {

    public static final SymmetryFeature manager = new SymmetryFeature();
    public static final double SYMMETRY_SIMILARITY = 0.4;

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
        ANTISYMMETRIC;

        /**
         * @return The type of this symmetric if it is not aligned on the x=0 axis.
         */
        SymmetryType convertToNonAxialSymmetry() {
            switch (this) {
                case EVEN:
                    return SYMMETRIC;
                case ODD:
                    return ANTISYMMETRIC;
                default:
                    return this;
            }
        }
    }

    /**
     * An instance of the Symmetry feature.
     */
    public class Instance implements LineFeature.Instance {

        private final SymmetryType symmetryType;

        /**
         * Create an instance which expects the line to have a specific symmetry.
         * @param symmetryType The expected symmetry.
         */
        private Instance(SymmetryType symmetryType) {
            this.symmetryType = symmetryType;
        }

        @Override
        public boolean match(Line line) {
            return getSymmetryOfLine(line) == symmetryType;
        }
    }

    @Override
    public Instance deserialize(String featureData) {
        return new Instance(SymmetryType.valueOf(featureData.toUpperCase()));
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
     * Use the manager singleton.
     */
    private SymmetryFeature() {
    }

    /**
     * Calculate the symmetry of a line.
     *
     * First, we look for ODD or EVEN symmetry. If those are not found, we find the centre of the line and reposition it
     * so that centre is at the origin. Then we look for symmetry again, and if found, report it in the non-axis aligned
     * form.
     *
     * @param line The line to calculate.
     * @return The type of symmetry of this line.
     */
    SymmetryType getSymmetryOfLine(Line line) {
        SymmetryType standardSymmetryType = getStandardSymmetryType(line);
        if (standardSymmetryType != SymmetryType.NONE) {
            return standardSymmetryType;
        }
        if (line.getPointsOfInterest().size() > 0) {
            List<PointOfInterest> points = line.getPointsOfInterest();
            if ((points.size() % 2) == 1) {
                PointOfInterest center = points.get(points.size() / 2);
                Line newLine = new Line(
                    line.getPoints().stream()
                        .map(p -> p.minus(center))
                        .collect(Collectors.toList()),
                    line.getPointsOfInterest().stream()
                        .map(p -> p.minus(center))
                        .collect(Collectors.toList())
                );

                SymmetryType newSymmetryType = getStandardSymmetryType(newLine);
                return newSymmetryType.convertToNonAxialSymmetry();
            } else {
                PointOfInterest center1 = points.get(points.size() / 2 - 1);
                PointOfInterest center2 = points.get(points.size() / 2);
                @SuppressWarnings("checkstyle:magicNumber")
                Point center = center1.add(center2).times(0.5);

                Line newLine = new Line(
                    line.getPoints().stream()
                        .map(p -> p.minus(center))
                        .collect(Collectors.toList()),
                    line.getPointsOfInterest().stream()
                        .map(p -> p.minus(center))
                        .collect(Collectors.toList())
                );

                SymmetryType newSymmetryType = getStandardSymmetryType(newLine);
                return newSymmetryType.convertToNonAxialSymmetry();
            }
        }
        // TODO: Add else clause here: find centre of all points and use that as the center
        return standardSymmetryType;
    }

    /**
     * Get the ODD or EVEN symmetry of the line by splitting at x=0.
     *
     * @param line The line.
     * @return ODD, EVEN, or NONE symmetry.
     */
    private SymmetryType getStandardSymmetryType(Line line) {
        // Split line at x = 0
        Line left = Sector.left.clip(line);
        Line right = Sector.right.clip(line);

        List<Line> lefts = Lines.splitOnPointsOfInterest(left);
        List<Line> rights = Lines.splitOnPointsOfInterest(right);

        if (lefts.size() != rights.size()) {
            return SymmetryType.NONE;
        }

        Collections.reverse(lefts);

        SymmetryType symmetryType = null;
        boolean innerMost = true;
        for (int i = 0; i < lefts.size(); i++) {
            SymmetryType nextSymmetryType = getSectionSymmetry(lefts.get(i), rights.get(i), innerMost);
            if (nextSymmetryType == null) {
                continue;
            }
            if (innerMost) {
                symmetryType = nextSymmetryType;
                innerMost = false;
            }
            if (symmetryType != nextSymmetryType) {
                return SymmetryType.NONE;
            }
        }

        if (symmetryType == null) {
            return SymmetryType.NONE;
        }

        return symmetryType;
    }

    /**
     * Check if these sections of the left and right hand side of the split are symmetrical.
     *
     * @param left The section of the left-hand side of the line.
     * @param right The section of the right-hand side of the line.
     * @param innerMost True if these are the sections that touch on the split.
     * @return The symmetry shown by these sections.
     */
    private SymmetryType getSectionSymmetry(Line left, Line right, boolean innerMost) {
        Point leftSize = Lines.getSize(left);
        Point rightSize = Lines.getSize(right);

        if (leftSize.getX() == 0 && leftSize.getY() == 0 && rightSize.getX() == 0 && rightSize.getY() == 0) {
            return null;
        }

        double xDifference = (rightSize.getX() - leftSize.getX()) / rightSize.getX();
        double yDifferenceOdd = (rightSize.getY() - leftSize.getY()) / rightSize.getY();
        double yDifferenceEven = (rightSize.getY() + leftSize.getY()) / rightSize.getY();

        if (Math.abs(xDifference) < SYMMETRY_SIMILARITY) {
            if (rightSize.getY() == 0 && leftSize.getY() == 0) {
                return SymmetryType.EVEN;
            }
            if (Math.abs(yDifferenceOdd) < SYMMETRY_SIMILARITY) {
                if (innerMost) {
                    if (Sector.relaxedOrigin.contains(left.getPoints().get(left.getPoints().size() - 1))
                        && Sector.relaxedOrigin.contains(right.getPoints().get(0))) {
                        return SymmetryType.ODD;
                    } else {
                        return SymmetryType.NONE;
                    }
                } else {
                    return SymmetryType.ODD;
                }
            }
            if (Math.abs(yDifferenceEven) < SYMMETRY_SIMILARITY) {
                return SymmetryType.EVEN;
            }
        }
        return SymmetryType.NONE;
    }
}
