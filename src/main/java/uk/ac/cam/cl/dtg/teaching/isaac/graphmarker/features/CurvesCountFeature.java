package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.features;

import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Input;

public class CurvesCountFeature implements InputFeature<CurvesCountFeature.Instance> {

    public static final CurvesCountFeature manager = new CurvesCountFeature();

    private CurvesCountFeature() {
    }

    @Override
    public String TAG() {
        return "curves";
    }

    public class Instance implements InputFeature.InstanceInterface {

        private int count;

        private Instance(int count) {
            this.count = count;
        }

        @Override
        public String serialize() {
            return Integer.toString(count);
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
    public String generate(Input expectedInput) {
        return new Instance(expectedInput.getLines().size()).serialize();
    }
}
