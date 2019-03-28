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
import uk.ac.cam.cl.dtg.isaac.graphmarker.features.internals.LineFeature;
import uk.ac.cam.cl.dtg.isaac.graphmarker.features.internals.LineSelector;
import uk.ac.cam.cl.dtg.isaac.graphmarker.geometry.Lines;
import uk.ac.cam.cl.dtg.isaac.graphmarker.settings.SettingsInterface;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A Selector which will match lines in a Prolog-fashion.
 *
 * We create a name for this line if that name doesn't exist, and then filter the Context based on our LineFeature.
 */
public class MatchingLineSelector extends LineSelector<MatchingLineSelector.Instance, SettingsInterface.None> {

    /**
     * Create an Nth line selector with specified settings.
     * @param settings The settings.
     */
    MatchingLineSelector(SettingsInterface.None settings) {
        super(settings);
    }

    @Override
    public String tag() {
        return "match";
    }

    /**
     * An instance of the MatchingLine selector.
     */
    class Instance extends LineSelector<?, ?>.Instance {
        private final String name;

        /**
         * Create an instance of this selector.
         * @param name The number of the line to select (1-based index).
         * @param item The remainder of this lineFeatureSpec.
         */
        private Instance(String name, String item) {
            super(name + "; ", item);
            this.name = name;
        }

        @Override
        protected Context test(Input input, LineFeature<?, ?>.Instance lineInstance, Context context) {
            return context.makeNewContext(mapping -> lineInstance.test(mapping.get(name)), name);
        }
    }

    @Override
    public Instance deserializeInternal(String item) {
        Pattern pattern = Pattern.compile("\\s*([a-zA-Z]+);\\s*(.*)");
        Matcher matcher = pattern.matcher(item);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Not a MatchingLineSelector: " + item);
        }
        return new Instance(matcher.group(1), matcher.group(2));
    }

    @Override
    public Map<String, Line> generate(Input input) {
        List<Line> lines = input.getLines();

        // Only generate match lines if the lines overlap horizontally.
        if (Lines.noHorizonalOverlap(lines)) {
            return Collections.emptyMap();
        }

        Map<String, Line> map = new HashMap<>();
        for (int i = 0; i < lines.size(); i++) {
            map.put(Context.standardLineName(i) + "; ", lines.get(i));
        }
        return map;
    }
}
