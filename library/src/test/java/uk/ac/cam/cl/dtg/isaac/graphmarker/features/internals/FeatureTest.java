package uk.ac.cam.cl.dtg.isaac.graphmarker.features.internals;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.Test;
import uk.ac.cam.cl.dtg.isaac.graphmarker.settings.SettingsInterface;
import uk.ac.cam.cl.dtg.isaac.graphmarker.settings.SettingsWrapper;

public class FeatureTest {

    static class TestFeature extends Feature<TestFeature.Instance, Object,Object,SettingsInterface.None> {

        public TestFeature(SettingsInterface.None settings) {
            super(settings);
        }
        @Override
        public String tag() {
            return null;
        }

        @Override
        protected Instance deserializeInternal(String featureData) {
            return new Instance();
        }

        @Override
        public Object generate(Object expectedInput) {
            return null;
        }

        class Instance extends Feature<TestFeature.Instance, Object,Object,SettingsInterface.None>.AbstractInstance {
            Instance() {
                super("", true);
            }
        }
    }

    @Test(expected = NotImplementedException.class)
    public void testForCoverage() {
        TestFeature f = new TestFeature(SettingsWrapper.DEFAULT);

        f.deserializeInternal("").test(null);
    }
}