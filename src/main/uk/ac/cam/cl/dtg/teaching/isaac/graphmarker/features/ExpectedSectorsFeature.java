package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.features;

import com.google.common.base.Joiner;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.Feature;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.IntersectionParams;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Line;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Point;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.geometry.Sector;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.geometry.Segment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ExpectedSectorsFeature implements Feature<ExpectedSectorsFeature.Data> {

    public static String TAG = "through";

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
            return TAG + ":" + Joiner.on(",").join(this.expectedSectors);
        }

        private boolean match(Line line) {
            List<Sector> actualSectors = convertLineToSectorList(line);
            return expectedSectors.equals(actualSectors);
        }

        List<Sector> convertLineToSectorList(Line line) {
            List<Sector> output = new ArrayList<>();

            Point lastPoint = null;
            for (Point point : line) {
                if (lastPoint != null) {
                    classifyLineSegment(output, Segment.closed(lastPoint, point));
                }

                Sector pointSector = classifyPoint(point);

                addSector(output, pointSector);

                lastPoint = point;
            }

            return output;
        }

        private void addSector(List<Sector> output, Sector sector) {
            if (sector == null) {
                return;
            }
            if (output.size() == 0 || !output.get(output.size() - 1).equals(sector)) {
                output.add(sector);
            }
        }

        private Sector classifyPoint(Point point) {
            for (Sector sector : orderedSectors) {
                if (sector.contains(point)) return sector;
            }
            return null;
        }

        private void classifyLineSegment(List<Sector> output, Segment lineSegment) {
            // Calculate when we enter and leave the line segment
            IntersectionParams[] intersectionParams = orderedSectors.stream()
                    .map(sector -> sector.intersectionParams(lineSegment))
                    .toArray(IntersectionParams[]::new);

            // Then for each region, pick the earliest sector on the list that we are in (skyline!)
            Boolean[] inside = orderedSectors.stream()
                    .map(sector -> sector.contains(lineSegment.getStart()))
                    .toArray(Boolean[]::new);

            int index = lowestIndex(intersectionParams);
            while (index != -1) {
                IntersectionParams.IntersectionParam intersection = intersectionParams[index].remove(0);

                inside[index] = intersection.isInside();

                int lowest = lowestSetBit(inside);

                if (lowest != -1) {
                    addSector(output, orderedSectors.get(lowest));
                }

                index = lowestIndex(intersectionParams);
            }
        }

        private int lowestSetBit(Boolean[] bits) {
            for (int i = 0; i < bits.length; i++) {
                if (bits[i]) return i;
            }
            return -1;
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
        assert featureData.startsWith(TAG + ":");
        List<Sector> expectedSectors = deserializeSectors(featureData.substring(TAG.length() + 1));
        return new Data(expectedSectors);
    }

    public ExpectedSectorsFeature() {
        this.orderedSectors = defaultOrderedSectors;
    }

    public ExpectedSectorsFeature(List<Sector> orderedSectors) {
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
