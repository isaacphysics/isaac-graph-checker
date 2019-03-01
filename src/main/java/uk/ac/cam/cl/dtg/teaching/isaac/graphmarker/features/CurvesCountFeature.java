package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.features;

import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Input;

public class CurvesCountFeature implements InputFeature<CurvesCountFeature.Data> {

    @Override
    public String TAG() {
        return "curves";
    }

    public class Data implements InputFeature.FeatureData {

        private int count;

        private Data(int count) {
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
    public Data deserialize(String featureData) {
        try {
            return new Data(Integer.valueOf(featureData.trim()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Not a number: " + featureData, e);
        }
    }

    @Override
    public String generate(Input expectedInput) {
        return new Data(expectedInput.getLines().size()).serialize();
    }
}
