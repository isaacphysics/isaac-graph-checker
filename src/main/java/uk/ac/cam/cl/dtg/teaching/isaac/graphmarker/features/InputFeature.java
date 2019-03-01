package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.features;

import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Input;

import java.util.function.Predicate;

public interface InputFeature<F extends InputFeature.FeatureData> {
    interface FeatureData {
        String serialize();
        boolean match(Input input);
    }

    String TAG();

    F deserialize(String featureData);

    String generate(Input expectedInput);

    default Predicate<Input> matcher(F data) {
        return data::match;
    }
}