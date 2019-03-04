package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.features;

import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Input;

import java.util.Collections;
import java.util.List;

public class CurvesCountFeature implements InputFeature<CurvesCountFeature.Instance> {

    public static final CurvesCountFeature manager = new CurvesCountFeature();

    private CurvesCountFeature() {
    }

    @Override
    public String TAG() {
        return "curves";
    }

    public class Instance implements InputFeature.Instance {

        private int count;

        private Instance(int count) {
            this.count = count;
        }

        @Override
        public boolean match(Input input) {
            return input.getLines().size() == count;
        }
    }

    @Override
    public Instance deserialize(String featureData) {
        try {
            return new Instance(Integer.valueOf(featureData.trim()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Not a number: " + featureData, e);
        }
    }

    @Override
    public List<String> generate(Input expectedInput) {
        if (expectedInput.getLines().size() < 2) {
            return Collections.emptyList();
        }
        return Collections.singletonList(Integer.toString(expectedInput.getLines().size()));
    }
}
