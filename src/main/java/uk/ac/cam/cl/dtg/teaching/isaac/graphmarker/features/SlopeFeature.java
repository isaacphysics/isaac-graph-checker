package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.features;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.HumanNamedEnum;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Line;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Point;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SlopeFeature implements LineFeature<SlopeFeature.Instance> {

    public static final SlopeFeature manager = new SlopeFeature();

    private static final Logger log = LoggerFactory.getLogger(SlopeFeature.class);

    private static final double SLOPE_THRESHOLD = 4;

    enum Position implements HumanNamedEnum {
        START,
        END,
        ALL
    }

    enum Slope implements HumanNamedEnum {
        FLAT, // Nearly horizontal
        UP, // Nearly vertical going upwards
        DOWN, // Nearly vertical going downwards
        OTHER // Any other slope
    }

    @Override
    public String TAG() { return "slope"; }

    protected class Instance implements LineFeature.Instance {

        private final Map<Position, Slope> expectedSlopes;

        Instance(Map<Position, Slope> expectedSlopes) {
            this.expectedSlopes = expectedSlopes;
        }

        @Override
        public boolean match(Line line) {
            return expectedSlopes.entrySet().stream()
                .allMatch(entry -> {
                    Position position = entry.getKey();
                    Line lineToMeasure = lineAtPosition(line, position);
                    Slope actualSlope = lineToSlope(lineToMeasure);
                    return entry.getValue() == actualSlope;
                });
        }
    }

    @Override
    public Instance deserialize(String featureData) {
        String[] items = featureData.split("\\s*,\\s*");
        return new Instance(Arrays.stream(items)
            .map(item -> {
                String[] parts = item.split("=");
                if (parts.length != 2) {
                    throw new IllegalArgumentException("Incorrect number of slope parts in: " + item);
                }
                Position expectedPosition = Position.valueOf(parts[0].trim().toUpperCase());
                Slope expectedSlope = Slope.valueOf(parts[1].trim().toUpperCase());
                return ImmutablePair.of(expectedPosition, expectedSlope);
            })
            .collect(Collectors.toMap(ImmutablePair::getLeft, ImmutablePair::getRight)));
    }

    @Override
    public List<String> generate(Line expectedLine) {
        return Collections.singletonList(Arrays.stream(Position.values())
        .map(position -> ImmutablePair.of(position, lineToSlope(lineAtPosition(expectedLine, position))))
        .filter(pair -> pair.getRight() != Slope.OTHER)
        .map(pair -> pair.getLeft().humanName() + "=" + pair.getRight().humanName())
        .collect(Collectors.joining(", ")));
    }

    private SlopeFeature() {
    }

    Slope lineToSlope(Line line) {
        Point size = line.getSize();

        if (false) {

            if (size.getX() < 0) {
                size = new Point(-size.getX(), size.getY());
            }
            if (size.getX() == 0) {
                size = new Point(0, size.getY());
            }

            double highIfFlat = size.getX() / size.getY();
            if (Math.abs(highIfFlat) > SLOPE_THRESHOLD) {
                return Slope.FLAT;
            }

            double highIfSteep = size.getY() / size.getX();
            if (Math.abs(highIfSteep) > SLOPE_THRESHOLD) {
                return highIfSteep > 0 ? Slope.UP : Slope.DOWN;
            }

            if (true) {
                return Slope.OTHER;
            }
        }

        int n = line.getPoints().size();
        double n2 = n * n;

        double sumX = line.stream().mapToDouble(Point::getX).sum();
        double sumY = line.stream().mapToDouble(Point::getY).sum();
        double sumX2 = line.stream().mapToDouble(p -> p.getX() * p.getX()).sum();
        double sumY2 = line.stream().mapToDouble(p -> p.getY() * p.getY()).sum();
        double sumXY = line.stream().mapToDouble(p -> p.getX() * p.getY()).sum();
        double xVariance = sumX2 / n - sumX * sumX / n2;

        if (xVariance < 0.001) {
            // Vertical line, which way?
            double difference = 0;
            for (int i = 1; i < n; i++) {
                difference += line.getPoints().get(i).getY() - line.getPoints().get(i - 1).getY();
            }
            Slope slope = difference > 0 ? Slope.UP : Slope.DOWN;

            log.debug("Vertical line with slope" + slope);

            return slope;
        }

        double xyVariance = sumXY / n - sumX * sumY / n2;
        double coefficient = xyVariance / xVariance;

        // From our perspective, UP/DOWN is about the change in Y over this period.
        // So look at change in Y to get the sign of the slope, not the change in Y over X.
        coefficient = Math.abs(coefficient);
        if (size.getY() < 0) {
            coefficient = -coefficient;
        }

        Slope slope = coefficientToSlope(coefficient);

        double yVariance = sumY2 / n - sumY * sumY / n2;
        double r = xyVariance / Math.sqrt(xVariance * yVariance);
        double r2 = r * r;

        log.debug("Line with coefficient " + coefficient + " has slope " + slope + " with r^2=" + r2);

        if (r2 < 0.5) {
            return Slope.OTHER;
        }

        return slope;
    }

    private Slope coefficientToSlope(double slope) {
        if (slope > SLOPE_THRESHOLD) {
            return Slope.UP;
        }

        if (slope < -SLOPE_THRESHOLD) {
            return Slope.DOWN;
        }

        if (slope > -1/SLOPE_THRESHOLD && slope < 1/SLOPE_THRESHOLD) {
            return Slope.FLAT;
        }

        return Slope.OTHER;
    }

    private static Line lineAtPosition(Line line, Position position) {
        Line lineToMeasure = line;
        switch (position) {
            case START:
                lineToMeasure = new Line(line.getPoints().subList(0, 5), Collections.emptyList());
                break;
            case END:
                int size = line.getPoints().size();
                lineToMeasure = new Line(line.getPoints().subList(size - 5, size), Collections.emptyList());
                break;
        }
        return lineToMeasure;
    }
}
