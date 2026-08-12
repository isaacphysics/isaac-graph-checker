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
package org.isaacphysics.graphchecker.features.internals;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.Test;
import org.isaacphysics.graphchecker.settings.SettingsInterface;
import org.isaacphysics.graphchecker.settings.SettingsWrapper;

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