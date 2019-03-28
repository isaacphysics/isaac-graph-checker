/**
 * Copyright 2019 University of Cambridge
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.cam.cl.dtg.isaac.graphmarker.geometry;

import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Intersection;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.IntersectionParams;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Line;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Point;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents a region of the graph, e.g. above the x axis, or on the y axis with y less than 0
 */
public class Sector {

    private final String name;
    private final List<Segment> segments;

    /**
     * Create a sector.
     *
     * Sectors are tested for equality by identity, so you must cache them when you create them if you want to compare
     * them.
     *
     * @param name The name of the sector.
     * @param segments The segments defining the boundaries of this sector.
     */
    Sector(String name, List<Segment> segments) {
        this.name = name;
        this.segments = segments;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Does this sector contain the point?
     * @param p The point to test.
     * @return True if the point is inside this sector.
     */
    public boolean contains(Point p) {
        return this.segments.stream().allMatch(segment -> segment.inside(p));
    }

    /**
     * Does this sector intersect the segment?
     * @param s The segment to test.
     * @return True if the segment intersects with the boundary of this sector.
     */
    private boolean intersects(Segment s) {
        return this.segments.stream().anyMatch(segment -> segment.intersects(s));
    }

    /**
     * Measure whether a line is inside, outside, or intersecting this sector.
     * @param line The line to test.
     * @return Whether a line is inside, outside, or intersecting this sector.
     */
    public Intersection intersects(Line line) {
        boolean allInside = true;
        boolean someInside = false;
        boolean anyIntersections = false;
        Point lastPoint = null;
        for (Point point : line) {
            if (contains(point)) {
                someInside = true;
            } else {
                allInside = false;
            }
            if (lastPoint != null) {
                anyIntersections |= intersects(Segment.closed(lastPoint, point));
            }
            lastPoint = point;
        }
        if (allInside && !anyIntersections) {
            return Intersection.INSIDE;
        } else if (someInside || anyIntersections) {
            return Intersection.INTERSECTS;
        } else {
            return Intersection.OUTSIDE;
        }
    }

    /**
     * Find the parameters of all intersections between a segment and this sector.
     * @param lineSegment The segment to analyse.
     * @return The parameters of each intersection.
     */
    public IntersectionParams intersectionParams(Segment lineSegment) {
        return new IntersectionParams(this.segments.stream()
                .map(segment -> segment.intersectionParam(lineSegment))
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList()));
    }

    /**
     * Create a line which is clipped to be only inside this sector.
     *
     * Any discontinuities are joined by straight lines. For example, clipping a sine wave to the top sector would give
     * a half-rectified wave.
     *
     * @param line The line to clip.
     * @return A new clip that is wholly inside this sector.
     */
    public Line clip(Line line) {
        Line result = line;
        for (Segment segment : this.segments) {
            result = segment.clip(result);
        }
        return result;
    }

}
