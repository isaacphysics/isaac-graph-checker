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
package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data;

public class PointOfInterest extends Point {
    private final PointType pointType;

    public PointOfInterest(double x, double y, PointType pointType) {
        super(x, y);
        this.pointType = pointType;
    }

    public PointOfInterest(Point point, PointType pointType) {
        super(point.getX(), point.getY());
        this.pointType = pointType;
    }

    public PointType getPointType() {
        return pointType;
    }

    public PointOfInterest minus(Point p) {
        return new PointOfInterest(super.minus(p), this.pointType);
    }

}
