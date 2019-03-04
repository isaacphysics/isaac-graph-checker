package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data;

public interface HumanNamedEnum {
    default String humanName() {
        return this.toString().toLowerCase();
    }
}
