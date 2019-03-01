package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.features;

import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Input;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Line;

import java.util.function.Predicate;

public class AnyLineSelector implements LineSelector<AnyLineSelector.Instance> {

    public String TAG() {
        return "any";
    }

    class Instance extends LineSelector.Instance {

        private Instance(String item) {
            super(item);
        }

        @Override
        Predicate<Input> matcher(Predicate<Line> linePredicate) {
            return input -> input.getLines().stream()
                .anyMatch(linePredicate);
        }
    }

    @Override
    public Instance deserialize(String instanceData) {
        return new Instance(instanceData);
    }
}
