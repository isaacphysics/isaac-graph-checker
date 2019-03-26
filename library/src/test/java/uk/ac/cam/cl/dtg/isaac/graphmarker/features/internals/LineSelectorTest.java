/**
 * Copyright 2019 University of Cambridge
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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