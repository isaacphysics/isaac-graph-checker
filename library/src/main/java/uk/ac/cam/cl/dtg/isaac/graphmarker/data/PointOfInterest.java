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
package uk.ac.cam.cl.dtg.isaac.graphmarker.data;

/**
 * A Point annotated as having some particular type.
 */
public class PointOfInterest extends Point {
    private final PointType pointType;

    /**
     * Create a PointOfInterest.
     * @param x The X co-ordinate.
     * @param y The Y co-ordinate.
     * @param pointType The type of this point.
     */
    public PointOfInterest(double x, double y, PointType pointType) {
        super(x, y);
        this.pointType = pointType;
    }

    /**
     * Create a PointOfInterest with the co-ordinates of an existing point.
     * @param point The existing point.
     * @param pointType The type of this point.
     */
    public PointOfInterest(Point point, PointType pointType) {
        super(point.getX(), point.getY());
        this.pointType = pointType;
    }

    /**
     * @return What type of point of interest this is.
     */
    public PointType getPointType() {
        return pointType;
    }

    /**
     * Subtract another point from this one and return as a new point of interest of the same type.
     * @param p Point to subtract.
     * @return The new point of interest.
     */
    public PointOfInterest minus(Point p) {
        return new PointOfInterest(super.minus(p), this.pointType);
    }

}
