package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.features;

import com.google.common.base.Joiner;
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
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class ExpectedSectorsFeature implements Feature<ExpectedSectorsFeature.Data> {

    private static final Logger log = LoggerFactory.getLogger(ExpectedSectorsFeature.class);

    @Override
    public String TAG() { return "through"; }

    private static final List<Sector> defaultOrderedSectors = Arrays.asList(
            Sector.origin,
            Sector.onAxisWithPositiveX,
            Sector.onAxisWithPositiveY,
            Sector.onAxisWithNegativeX,
            Sector.onAxisWithNegativeY,
            Sector.topRight,
            Sector.topLeft,
            Sector.bottomLeft,
            Sector.bottomRight
    );

    private final List<Sector> orderedSectors;

    protected class Data implements Feature.FeatureData {
        private final List<Sector> expectedSectors;

        Data(List<Sector> expectedSectors) {
            this.expectedSectors = expectedSectors;
        }

        @Override
        public String serialize() {
            return Joiner.on(",").join(this.expectedSectors);
        }

        private boolean match(Line line) {
            List<Set<Sector>> actualSectors = convertLineToSectorSetList(line);
            log.error("User line passed through sectors: " + actualSectors);
            return match(expectedSectors, 0, actualSectors, 0);
        }

        private boolean match(List<Sector> expected, int i, List<Set<Sector>> actual, int j) {
            boolean expectedFinished = i >= expected.size();
            boolean actualFinished = j >= actual.size();
            if (expectedFinished) {
                return actualFinished;
            }
            if (actualFinished) return false;

            if (actual.get(j).contains(expected.get(i))) {
                if (match(expected, i, actual, j + 1)) return true;

                // If we're consuming i, then we must consume all j's until the next doesn't contain expected[i]
                // Otherwise, you can get non-real oscillations around axes as the line crosses the axis
                int next = j + 1;
                while (next < actual.size() && actual.get(next).contains(expected.get(i))) next++;
                if (match(expected, i + 1, actual, next - 1)) return true;
                if (match(expected, i + 1, actual, next)) return true;
            }
            if (actual.get(j).isEmpty()) {
                if (match(expected, i, actual, j + 1)) return true;
            }
            return false;

        }

        List<Sector> convertLineToSectorList(Line line) {
            List<Set<Sector>> output = convertLineToSectorSetList(line);

            return output.stream()
                .map(set -> orderedSectors.stream().filter(s -> set.contains(s)).findFirst().orElse(null))
                .reduce(new ArrayList<Sector>(), (list, sector) -> {
                    if (sector != null && (list.isEmpty() || !list.get(list.size() - 1).equals(sector))) {
                        list.add(sector);
                    }
                    return list;
                }, (a, b) -> {
                    a.addAll(b);
                    return a;
                } );
        }

        List<Set<Sector>> convertLineToSectorSetList(Line line) {
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

        private void addSector(List<Set<Sector>> output, Set<Sector> sector) {
            if (sector == null) {
                return;
            }
            if (output.size() == 0 || !output.get(output.size() - 1).equals(sector)) {
                output.add(sector);
            }
        }

        private Set<Sector> classifyPoint(Point point) {
            return orderedSectors.stream()
                .filter(sector -> sector.contains(point))
                .collect(Collectors.toSet());
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

    private List<Sector> deserializeSectors(String sectors) {
        return Arrays.stream(sectors.split(","))
                .map(s -> s.trim())
                .filter(s -> s.length() > 0)
                .map(s -> Sector.byName(s))
                .collect(Collectors.toList());
    }

    @Override
    public Data deserialize(String featureData) {
        List<Sector> expectedSectors = deserializeSectors(featureData);
        return new Data(expectedSectors);
    }

    ExpectedSectorsFeature() {
        this.orderedSectors = defaultOrderedSectors;
    }

    ExpectedSectorsFeature(List<Sector> orderedSectors) {
        this.orderedSectors = orderedSectors;
    }

    @Override
    public Data generate(Line expectedLine) {
        return new Data(new Data(Collections.emptyList()).convertLineToSectorList(expectedLine));
    }

    @Override
    public Predicate<Line> matcher(Data data) {
        return line -> data.match(line);
    }
}
