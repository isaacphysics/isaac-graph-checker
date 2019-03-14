package uk.ac.cam.cl.dtg.isaac.graphmarker.features;

public class Castable {
    private final String value;
    private Castable(String value) {
        this.value = value;
    }

    public int asInt() {
        return Integer.valueOf(this.value);
    }

    public double asDouble() {
        return Double.valueOf(this.value);
    }

    public String asString() {
        return this.value;
    }

    public static Castable of(int i) {
        return new Castable(Integer.toString(i));
    }

    public static Castable of(double v) {
        return new Castable(Double.toString(v));
    }

    public static Castable of(String s) {
        return new Castable(s);
    }
}
