package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.features;

import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Line;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Point;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.PointOfInterest;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.geometry.Sector;

import java.util.List;
import java.util.stream.Collectors;

public class SymmetryFeature implements LineFeature<SymmetryFeature.Instance> {

    public static final SymmetryFeature manager = new SymmetryFeature();

    @Override
    public String TAG() {
        return "symmetry";
    }

    enum SymmetryType {
        NONE,
        ODD,
        EVEN,
        SYMMETRIC,
        ANTISYMMETRIC;

        public SymmetryType convertToNonAxialSymmetry() {
            switch(this) {
                case EVEN:
                    return SYMMETRIC;
                case ODD:
                    return ANTISYMMETRIC;
            }
            return this;
        }
    }

    public class Instance implements LineFeature.Instance {

        private SymmetryType symmetryType;

        private Instance(SymmetryType symmetryType) {
            this.symmetryType = symmetryType;
        }

        @Override
        public String serialize() {
            return symmetryType.name();
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
    public String generate(Line expectedLine) {
        return new Instance(getSymmetryOfLine(expectedLine)).serialize();
    }

    private SymmetryFeature() {
    }

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
        return standardSymmetryType;
    }

    // TODO: Check maxima and minima match up in order to check shape
    private SymmetryType getStandardSymmetryType(Line line) {
        // Split line at x = 0
        Line left = Sector.left.clip(line);
        Line right = Sector.right.clip(line);

        if (left.getPoints().size() <= 1 || right.getPoints().size() <= 1) {
            return SymmetryType.NONE;
        }

        Point leftSize = left.getSize();
        Point rightSize = right.getSize();

        double xDifference = (rightSize.getX() - leftSize.getX()) / rightSize.getX();
        double yDifferenceOdd = (rightSize.getY() - leftSize.getY()) / rightSize.getY();
        double yDifferenceEven = (rightSize.getY() + leftSize.getY()) / rightSize.getY();

        if(Math.abs(xDifference) < 0.2) {
            if (Math.abs(rightSize.getY()) < 0.01 && Math.abs((leftSize.getY())) < 0.01) {
                return SymmetryType.EVEN;
            }
            if (Math.abs(yDifferenceOdd) < 0.2
                && Sector.relaxedOrigin.contains(left.getPoints().get(left.getPoints().size() - 1))
                && Sector.relaxedOrigin.contains(right.getPoints().get(0))) {
                return SymmetryType.ODD;
            }
            if (Math.abs(yDifferenceEven) < 0.2) {
                return SymmetryType.EVEN;
            }
        }
        return SymmetryType.NONE;
    }
}
