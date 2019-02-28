package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.features;

import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Line;

import java.util.function.Predicate;

public interface Feature<F extends Feature.FeatureData> {

    interface FeatureData {
        String serialize();
        boolean match(Line line);
    }

    String TAG();

    F deserialize(String featureData);

    String generate(Line expectedLine);

    default Predicate<Line> matcher(F data) {
        return line -> data.match(line);
    }
}
