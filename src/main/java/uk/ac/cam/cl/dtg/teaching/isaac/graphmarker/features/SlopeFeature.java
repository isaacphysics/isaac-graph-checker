package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.features;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Line;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Point;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class SlopeFeature implements LineFeature<SlopeFeature.Instance> {

    public static final SlopeFeature manager = new SlopeFeature();

    private static final Logger log = LoggerFactory.getLogger(SlopeFeature.class);

    private static final double SLOPE_THRESHOLD = 4;

    enum Position {
        START,
        END,
        ALL
    }

    enum Slope {
        FLAT, // Nearly horizontal
        UP, // Nearly vertical going upwards
        DOWN, // Nearly vertical going downwards
        OTHER, // Any other slope
    }

    @Override
    public String TAG() { return "slope"; }

    protected class Instance implements LineFeature.Instance {

        private final Map<Position, Slope> expectedSlopes;

        Instance(Map<Position, Slope> expectedSlopes) {
            this.expectedSlopes = expectedSlopes;
        }

        @Override
        public String serialize() {
            return expectedSlopes.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(", "));
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
    public String generate(Line expectedLine) {
        return new Instance(Arrays.stream(Position.values())
            .map(position -> ImmutablePair.of(position, lineToSlope(lineAtPosition(expectedLine, position))))
            .filter(pair -> pair.getRight() != Slope.OTHER)
            .collect(Collectors.toMap(ImmutablePair::getLeft, ImmutablePair::getRight))
        ).serialize();
    }

    private SlopeFeature() {
    }

    Slope lineToSlope(Line line) {
        int n = line.getPoints().size();
        double sumX = line.stream().mapToDouble(Point::getX).sum();
        double sumY = line.stream().mapToDouble(Point::getY).sum();
        double sumX2 = line.stream().mapToDouble(p -> p.getX() * p.getX()).sum();
        double sumXY = line.stream().mapToDouble(p -> p.getX() * p.getY()).sum();
        double coefficient = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);

        // From our perspective, UP/DOWN is about the change in Y over this period.
        // So look at change in Y to get the sign of the slope, not the change in Y over X.
        Point size = line.getSize();
        coefficient = Math.abs(coefficient);
        if (size.getY() < 0) {
            coefficient = -coefficient;
        }

        Slope slope = coefficientToSlope(coefficient);

        log.debug("Line with coefficient " + coefficient + " has slope " + slope);
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
