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
package uk.ac.cam.cl.dtg.isaac.graphmarker.features;

import org.junit.Test;

public class InputFeatureTest {

    private final LineSelector.LineSelectorWrapperFeature lineSelectorWrapperFeature =
        new LineSelector.LineSelectorWrapperFeature(Settings.NONE);

    @Test(expected =  UnsupportedOperationException.class)
    @SuppressWarnings("deprecation")
    public void testTagThrows() {
        lineSelectorWrapperFeature.tag();
    }

    @Test(expected =  UnsupportedOperationException.class)
    @SuppressWarnings("deprecation")
    public void testDeserializeInternalThrows() {
        lineSelectorWrapperFeature.deserializeInternal("");
    }

    @Test(expected =  UnsupportedOperationException.class)
    @SuppressWarnings("deprecation")
    public void testGenerateThrows() {
        lineSelectorWrapperFeature.generate(null);
    }
}