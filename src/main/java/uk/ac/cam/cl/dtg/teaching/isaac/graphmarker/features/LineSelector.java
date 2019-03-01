package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.features;

import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Input;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Line;

import java.util.function.Predicate;

public interface LineSelector {

    String TAG();

    String item();

    LineSelector parse(String item);

    Predicate<Input> matcher(Predicate<Line> linePredicate);

    public static LineSelector any(final String item) {
        return new LineSelector() {
            @Override
            public String TAG() {
                return null;
            }

            @Override
            public String item() {
                return item;
            }

            @Override
            public LineSelector parse(String item) {
                return this;
            }

            @Override
            public Predicate<Input> matcher(Predicate<Line> linePredicate) {
                return input -> input.getLines().stream()
                    .anyMatch(linePredicate);
            }
        };
    }
}
