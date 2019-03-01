package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.features;

import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Input;

public interface InputFeature<FeatureInstance extends InputFeature.InstanceInterface> {

    interface InstanceInterface {
        String serialize();
        boolean match(Input input);
    }

    String TAG();

    FeatureInstance deserialize(String featureData);

    String generate(Input expectedInput);
}