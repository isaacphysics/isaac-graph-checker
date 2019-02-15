package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.features;

import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.IntersectionParams;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Line;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Point;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.geometry.Sector;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.geometry.Segment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LineFollowsExpectedPath {

    private final Sector[] orderedSectors = new Sector[] {
        Sector.origin(),
        Sector.onAxisWithPositiveX(),
        Sector.onAxisWithPositiveY(),
        Sector.onAxisWithNegativeX(),
        Sector.onAxisWithNegativeY(),
        Sector.topRight(),
        Sector.topLeft(),
        Sector.bottomLeft(),
        Sector.bottomRight()
    };

    public List<Sector> convertLineToSectorList(Line line) {
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
        IntersectionParams[] intersectionParams = Arrays.stream(orderedSectors)
                .map(sector -> sector.intersectionParams(lineSegment))
                .toArray(IntersectionParams[]::new);

        // Then for each region, pick the earliest sector on the list that we are in (skyline!)
        Boolean[] inside = Arrays.stream(orderedSectors)
                .map(sector -> sector.contains(lineSegment.getStart()))
                .toArray(Boolean[]::new);

        int index = lowestIndex(intersectionParams);
        while (index != -1) {
            inside[index] = !inside[index];

            int lowest = lowestSetBit(inside);

            if (lowest != -1) {
                addSector(output, orderedSectors[lowest]);
            }

            intersectionParams[index].remove(0);

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
                double param = intersectionParams[i].get(0);
                if (param < minParam) {
                    index = i;
                    minParam = param;
                }
            }
        }
        return index;
    }
}
