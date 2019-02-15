package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker;

import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Line;

import java.util.function.Predicate;

public interface Feature<F extends Feature.FeatureData> {
    interface FeatureData {
        String serialize();
    }

    String TAG();

    F deserialize(String featureData);

    F generate(Line expectedLine);

    Predicate<Line> matcher(F data);
}
