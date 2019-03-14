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

import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Input;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * An input feature which requires a specific number of lines to be drawn.
 */
public class CurvesCountFeature extends InputFeature<CurvesCountFeature.Instance> {

    /**
     * Create a curve count feature with specified settings.
     * @param settings The settings.
     */
    CurvesCountFeature(Settings settings) {
        super(settings);
    }

    @Override
    public Map<String, Castable> defaults() {
        return Collections.emptyMap();
    }

    @Override
    public String tag() {
        return "curves";
    }

    /**
     * An instance of the CurvesCount feature.
     */
    public class Instance extends InputFeature<?>.Instance {

        private final int count;

        /**
         * Create a curve count feature.
         * @param count Required number of curves.
         */
        private Instance(int count) {
            super("" + count, true);
            this.count = count;
        }

        /**
         * Create an implicit curve count feature that requires a single curve.
         */
        private Instance() {
            super("1 (implicitly)", false);
            this.count = 1;
        }

        @Override
        public boolean test(Input input) {
            return input.getLines().size() == count;
        }
    }

    /**
     * Create an implicit curve count feature that requires a single curve.
     * @return A curve count instance with count = 1.
     */
    public Instance oneCurveOnlyImplicitly() {
        return new Instance();
    }

    @Override
    protected Instance deserializeInternal(String featureData) {
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
