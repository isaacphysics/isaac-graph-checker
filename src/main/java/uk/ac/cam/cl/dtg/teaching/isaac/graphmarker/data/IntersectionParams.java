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

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * A list of parameters where one line segment intersects another segment.
 */
public class IntersectionParams extends ArrayList<IntersectionParams.IntersectionParam> {
    /**
     * A parameteric point on the line segment where an intersection occurred.
     */
    public static class IntersectionParam implements Comparable<IntersectionParam> {
        private final double t;
        private final boolean inside;

        /**
         * @param t The parameter defining where on the line this intersection occurs.
         * @param inside Whether this point is on the inside of the other segment.
         */
        public IntersectionParam(double t, boolean inside) {
            this.t = t;
            this.inside = inside;
        }

        /**
         * @return The parameter where this intersection occurred.
         */
        public double getT() {
            return t;
        }

        /**
         * @return If the point of intersect is on the inside of the other segment.
         */
        public boolean isInside() {
            return inside;
        }

        @Override
        public int compareTo(@Nonnull IntersectionParam o) {
            return Double.compare(t, o.t);
        }
    }

    /**
     * Construct a list of intersection parameters.
     * @param ts A list of intersections.
     */
    public IntersectionParams(List<IntersectionParam> ts) {
        super(ts);
    }
}
