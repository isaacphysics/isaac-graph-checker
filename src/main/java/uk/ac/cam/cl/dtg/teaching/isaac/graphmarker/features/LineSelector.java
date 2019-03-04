package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.features;

import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Input;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Line;

import java.util.Map;
import java.util.function.Predicate;

public interface LineSelector<I extends LineSelector.Instance> {

    abstract class Instance {
        private final String item;

        protected Instance(String item) {
            this.item = item;
        }

        public String item() {
            return item;
        }

        abstract Predicate<Input> matcher(Predicate<Line> linePredicate);
    }

    String TAG();

    I deserialize(String instanceData);

    Map<String, Line> generate(Input input);
}
