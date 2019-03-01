package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.features;

import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Line;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Point;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.geometry.Sector;

public class SymmetryFeature implements LineFeature<SymmetryFeature.Data> {

    @Override
    public String TAG() {
        return "symmetry";
    }

    enum SymmetryType {
        NONE,
        ODD,
        EVEN,
    }

    public class Data implements LineFeature.FeatureData {

        private SymmetryType symmetryType;

        private Data(SymmetryType symmetryType) {
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
    public Data deserialize(String featureData) {
        return new Data(SymmetryType.valueOf(featureData.toUpperCase()));
    }

    @Override
    public String generate(Line expectedLine) {
        return new Data(getSymmetryOfLine(expectedLine)).serialize();
    }

    SymmetryType getSymmetryOfLine(Line line) {
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

        if(Math.abs(xDifference) < 0.1) {
            if (Math.abs(rightSize.getY()) < 0.01 && Math.abs((leftSize.getY())) < 0.01) {
                return SymmetryType.EVEN;
            }
            if (Math.abs(yDifferenceOdd) < 0.1 && Sector.origin.contains(left.getPoints().get(left.getPoints().size() - 1))
                && Sector.origin.contains(right.getPoints().get(0))) {
                return SymmetryType.ODD;
            }
            if (Math.abs(yDifferenceEven) < 0.1) {
                return SymmetryType.EVEN;
            }
        }
        return SymmetryType.NONE;
    }
}
