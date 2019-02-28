package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.features;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Line;

public class SlopeFeature implements Feature<SlopeFeature.Data> {

    private static final Logger log = LoggerFactory.getLogger(SlopeFeature.class);

    @Override
    public String TAG() { return "slope"; }

    protected class Data implements Feature.FeatureData {

        private final double expectedSlope;

        Data(double expectedSlope) {
            this.expectedSlope = expectedSlope;
        }

        @Override
        public String serialize() {
            return "" + this.expectedSlope;
        }

        @Override
        public boolean match(Line line) {
            // Linear regression
            double actualSlope = lineToSlope(line);

            double ratio = actualSlope / expectedSlope;

            return ratio > 0.5 && ratio < 2;
        }
    }

    @Override
    public Data deserialize(String featureData) {
        double expectedSlope = Double.valueOf(featureData);
        return new Data(expectedSlope);
    }

    @Override
    public String generate(Line expectedLine) {
        return new Data(lineToSlope(expectedLine)).serialize();
    }

    double lineToSlope(Line line) {
        int n = line.getPoints().size();
        double sumX = line.stream().mapToDouble(p -> p.getX()).sum();
        double sumY = line.stream().mapToDouble(p -> p.getY()).sum();
        double sumX2 = line.stream().mapToDouble(p -> p.getX() * p.getX()).sum();
        double sumXY = line.stream().mapToDouble(p -> p.getX() * p.getY()).sum();
        return (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
    }
}
