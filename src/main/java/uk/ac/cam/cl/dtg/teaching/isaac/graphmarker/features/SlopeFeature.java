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
        END
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

        if (size.getX() < 0) {
            size = new Point(-size.getX(), size.getY());
        }
        // This test is need because you can get a -0 which then divides to give the opposite infinity to the one you want
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

        return Slope.OTHER;
    }

    private static Line lineAtPosition(Line line, Position position) {
        int size = line.getPoints().size();
        int desired = Math.min(5, size);
        switch (position) {
            case START:
                return new Line(line.getPoints().subList(0, desired), Collections.emptyList());
            case END:
                return new Line(line.getPoints().subList(size - desired, size), Collections.emptyList());
        }
        throw new IllegalArgumentException("Unknown position: " + position);
    }
}
