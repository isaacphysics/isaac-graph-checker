package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.features;

import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Line;

public interface LineFeature<FeatureInstance extends LineFeature.Instance> {

    interface Instance {
        String serialize();
        boolean match(Line line);
    }

    String TAG();

    FeatureInstance deserialize(String featureData);

    String generate(Line expectedLine);
}
