package uk.ac.cam.cl.dtg.isaac.graphmarker.features.internals;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.Test;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Input;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Line;
import uk.ac.cam.cl.dtg.isaac.graphmarker.settings.SettingsInterface;
import uk.ac.cam.cl.dtg.isaac.graphmarker.settings.SettingsWrapper;

import java.util.Map;

public class LineSelectorTest {

    static class TestLineSelector extends LineSelector<TestLineSelector.Instance, SettingsInterface.None> {

        public TestLineSelector(SettingsInterface.None settings) {
            super(settings);
        }

        @Override
        public String tag() {
            return null;
        }

        @Override
        protected TestLineSelector.Instance deserializeInternal(String featureData) {
            return new Instance();
        }

        @Override
        public Map<String, Line> generate(Input expectedInput) {
            return null;
        }


        class Instance extends LineSelector<TestLineSelector.Instance, SettingsInterface.None>.Instance {
            Instance() {
                super("", "");
            }
        }
    }

    @Test(expected = NotImplementedException.class)
    public void testForCoverage() {
        TestLineSelector f = new TestLineSelector(SettingsWrapper.DEFAULT);

        f.deserializeInternal("").test(null, null);
    }
}