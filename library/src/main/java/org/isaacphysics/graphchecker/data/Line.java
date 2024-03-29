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
package org.isaacphysics.graphchecker.data;

import jakarta.annotation.Nonnull;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Represents a series of Points. When drawn, connect with cubic Bezier curves, but in here, assume straight segments.
 */
public class Line implements Iterable<Point> {
    private final List<Point> points;

    private final List<PointOfInterest> pointsOfInterest;

    /**
     * Create a line.
     * @param points The points on this line.
     * @param pointsOfInterest Any points of interest on this line.
     */
    public Line(List<Point> points, List<PointOfInterest> pointsOfInterest) {
        this.points = points;
        this.pointsOfInterest = pointsOfInterest;
    }

    /**
     * @return Get the points on this line.
     */
    public List<Point> getPoints() {
        return points;
    }

    /**
     * @return Get the points of interest on this line.
     */
    public List<PointOfInterest> getPointsOfInterest() {
        return pointsOfInterest;
    }

    @Override
    @Nonnull
    public Iterator<Point> iterator() {
        return points.iterator();
    }

    /**
     * @return A Stream of the points on this line.
     */
    public Stream<Point> stream() {
        return points.stream();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Line points1 = (Line) o;
        return Objects.equals(points, points1.points);
    }

    @Override
    public int hashCode() {
        return Objects.hash(points);
    }

}
