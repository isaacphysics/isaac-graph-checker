package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.features;

import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Line;

import java.util.List;

public interface LineFeature<FeatureInstance extends LineFeature.Instance> {

    interface Instance {
        boolean match(Line line);
    }

    String TAG();

    FeatureInstance deserialize(String featureData);

    List<String> generate(Line expectedLine);
}
