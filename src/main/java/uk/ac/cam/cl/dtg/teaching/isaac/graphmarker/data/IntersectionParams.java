package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data;

import java.util.ArrayList;
import java.util.List;

public class IntersectionParams extends ArrayList<IntersectionParams.IntersectionParam> {
    public static class IntersectionParam implements Comparable<IntersectionParam> {
        private final double t;
        private final boolean inside;

        public IntersectionParam(double t, boolean inside) {
            this.t = t;
            this.inside = inside;
        }

        public double getT() {
            return t;
        }

        public boolean isInside() {
            return inside;
        }

        @Override
        public int compareTo(IntersectionParam o) {
            return Double.compare(t, o.t);
        }
    }

    public IntersectionParams(List<IntersectionParam> ts) {
        super(ts);
    }
}
