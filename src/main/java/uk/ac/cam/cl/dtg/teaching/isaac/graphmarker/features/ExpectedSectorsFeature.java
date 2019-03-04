package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.features;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.IntersectionParams;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Line;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Point;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.geometry.Sector;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.geometry.Segment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ExpectedSectorsFeature implements LineFeature<ExpectedSectorsFeature.Instance> {

    public static final ExpectedSectorsFeature manager = new ExpectedSectorsFeature();

    private static final Logger log = LoggerFactory.getLogger(ExpectedSectorsFeature.class);

    @Override
    public String TAG() { return "through"; }

    private final List<Sector> orderedSectors;

    protected class Instance implements LineFeature.Instance {
        private final List<Sector> expectedSectors;

        Instance(List<Sector> expectedSectors) {
            this.expectedSectors = expectedSectors;
        }

        @Override
        public boolean match(Line line) {
            List<Set<Sector>> actualSectors = convertLineToSectorSetList(line);
            log.debug("User line passed through sectors: " + actualSectors);
            return match(expectedSectors, 0, actualSectors, 0);
        }

        private boolean match(List<Sector> expected, int i, List<Set<Sector>> actual, int j) {
            boolean expectedFinished = i >= expected.size();
            boolean actualFinished = j >= actual.size();
            if (expectedFinished) {
                return actualFinished;
            }
            if (actualFinished) return false;

            if (actual.get(j).isEmpty()) {
                return match(expected, i, actual, j + 1);
            }

            if (actual.get(j).contains(expected.get(i))) {
                if (match(expected, i, actual, j + 1)) return true;

                if (match(expected, i + 1, actual, j)) return true;
                if (match(expected, i + 1, actual, j + 1)) return true;
            }
            return false;

        }
    }

    private List<Sector> deserializeSectors(String sectors) {
        return Arrays.stream(sectors.split(","))
                .map(s -> s.trim())
                .filter(s -> s.length() > 0)
                .map(s -> Sector.byName(s))
                .collect(Collectors.toList());
    }

    @Override
    public Instance deserialize(String featureData) {
        List<Sector> expectedSectors = deserializeSectors(featureData);
        return new Instance(expectedSectors);
    }

    private ExpectedSectorsFeature() {
        this.orderedSectors = Sector.defaultOrderedSectors;
    }

    ExpectedSectorsFeature(List<Sector> orderedSectors) {
        this.orderedSectors = orderedSectors;
    }

    @Override
    public List<String> generate(Line expectedLine) {
        return Collections.singletonList(Joiner.on(", ").join(convertLineToSectorList(expectedLine)));
    }

    List<Sector> convertLineToSectorList(Line line) {
        List<Set<Sector>> sectors = convertLineToSectorSetList(line);

        List<Sector> output = new ArrayList<>();
        sectors.stream()
            .map(set -> orderedSectors.stream().filter(set::contains).findFirst().orElse(null))
            .forEach(sector -> {
                if (output.isEmpty() || !output.get(output.size() - 1).equals(sector)) {
                    output.add(sector);
                }
            });

        return output;
    }

    private List<Set<Sector>> convertLineToSectorSetList(Line line) {
        List<Set<Sector>> output = new ArrayList<>();

        Point lastPoint = null;
        for (Point point : line) {
            if (lastPoint != null) {
                classifyLineSegment(output, Segment.closed(lastPoint, point));
            }

            Set<Sector> pointSector = classifyPoint(point);

            addSector(output, pointSector);

            lastPoint = point;
        }

        return output;
    }

    private List<Set<Sector>> invalidSectorSets = ImmutableList.of(
        ImmutableSet.of(Sector.topRight, Sector.bottomRight),
        ImmutableSet.of(Sector.topLeft, Sector.bottomLeft),
        ImmutableSet.of(Sector.topRight, Sector.topLeft),
        ImmutableSet.of(Sector.bottomRight, Sector.bottomLeft),
        ImmutableSet.of(Sector.onAxisWithPositiveX, Sector.onAxisWithNegativeX),
        ImmutableSet.of(Sector.onAxisWithPositiveY, Sector.onAxisWithNegativeY)
    );

    private void addSector(List<Set<Sector>> output, Set<Sector> sectors) {
        Objects.requireNonNull(sectors);

        // If you are in an area that contains both sides of an axis say, remove both sides.
        List<Set<Sector>> sectorsToRemove = invalidSectorSets.stream()
            .filter(sectors::containsAll)
            .collect(Collectors.toList());
        sectorsToRemove.forEach(sectors::removeAll);

        if (output.size() == 0 || !output.get(output.size() - 1).equals(sectors)) {
            output.add(sectors);
        }
    }

    private Set<Sector> classifyPoint(Point point) {
        return Sector.classify(point, orderedSectors);
    }

    private void classifyLineSegment(List<Set<Sector>> output, Segment lineSegment) {
        // Calculate when we enter and leave the line segment
        IntersectionParams[] intersectionParams = orderedSectors.stream()
            .map(sector -> sector.intersectionParams(lineSegment))
            .toArray(IntersectionParams[]::new);

        Boolean[] inside = orderedSectors.stream()
            .map(sector -> sector.contains(lineSegment.getStart()))
            .toArray(Boolean[]::new);

        int index = lowestIndex(intersectionParams);
        while (index != -1) {
            IntersectionParams.IntersectionParam intersection = intersectionParams[index].remove(0);

            inside[index] = intersection.isInside();

            // Record all of the sectors we are currently in
            Set<Sector> internalSectors = Streams.zip(
                orderedSectors.stream(),
                Arrays.stream(inside),
                (sector, in) -> in ? sector : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

            addSector(output, internalSectors);

            index = lowestIndex(intersectionParams);
        }
    }

    private int lowestIndex(IntersectionParams[] intersectionParams) {
        int index = -1;
        double minParam = Double.MAX_VALUE;
        for (int i = 0; i < intersectionParams.length; i++) {
            if (intersectionParams[i].size() > 0) {
                double param = intersectionParams[i].get(0).getT();
                if (param < minParam) {
                    index = i;
                    minParam = param;
                }
            }
        }
        return index;
    }
}
