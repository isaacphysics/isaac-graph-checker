package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.features;

import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Input;

import java.util.List;

public interface InputFeature<FeatureInstance extends InputFeature.Instance> {

    interface Instance {
        boolean match(Input input);
    }

    String TAG();

    FeatureInstance deserialize(String featureData);

    List<String> generate(Input expectedInput);
}