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
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Line;
import uk.ac.cam.cl.dtg.isaac.graphmarker.features.internals.LineSelector;
import uk.ac.cam.cl.dtg.isaac.graphmarker.geometry.Lines;
import uk.ac.cam.cl.dtg.isaac.graphmarker.settings.SettingsInterface;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A Selector which will test if a particular numbered line in the input matches.
 */
public class NthLineSelector extends LineSelector<NthLineSelector.Instance, SettingsInterface.None> {

    /**
     * Create an Nth line selector with specified settings.
     * @param settings The settings.
     */
    NthLineSelector(SettingsInterface.None settings) {
        super(settings);
    }

    @Override
    public String tag() {
        return "line";
    }

    /**
     * An instance of the NthLine selector.
     */
    class Instance extends LineSelector<?, ?>.Instance {
        private final int n;

        /**
         * Create an instance of this selector.
         * @param n The number of the line to select (1-based index).
         * @param item The remainder of this lineFeatureSpec.
         */
        private Instance(int n, String item) {
            super(n + "; ", item);
            this.n = n;
        }

        @Override
        protected Predicate<Input> matcher(Predicate<Line> linePredicate) {
            return input -> {
                List<Line> lines = input.getLines();
                if (n > lines.size()) {
                    return false; // Not enough lines
                }
                return linePredicate.test(lines.get(n - 1));
            };
        }
    }

    @Override
    public Instance deserializeInternal(String item) {
        Pattern pattern = Pattern.compile("\\s*([1-9][0-9]*);\\s*(.*)");
        Matcher matcher = pattern.matcher(item);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Not an NthLineSelector: " + item);
        }
        return new Instance(Integer.valueOf(matcher.group(1)), matcher.group(2));
    }

    @Override
    public Map<String, Line> generate(Input input) {
        List<Line> lines = input.getLines();
        if (!Lines.noHorizonalOverlap(lines)) {
            return Collections.emptyMap();
        }
        Map<String, Line> map = new HashMap<>();
        for (int i = 0; i < lines.size(); i++) {
            map.put((i + 1) + "; ", lines.get(i));
        }
        return map;
    }
}
