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

package uk.ac.cam.cl.dtg.isaac.graphmarker.bluefin;

import org.apache.commons.lang3.tuple.Pair;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Input;
import uk.ac.cam.cl.dtg.isaac.graphmarker.dos.GraphAnswer;
import uk.ac.cam.cl.dtg.isaac.graphmarker.features.Features;
import uk.ac.cam.cl.dtg.isaac.graphmarker.settings.SettingsWrapper;
import uk.ac.cam.cl.dtg.isaac.graphmarker.translation.AnswerToInput;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Marker {

    private final AnswerToInput answerToInput = new AnswerToInput();
    private final Features features;

    public Marker(SettingsWrapper settings) {
        features = new Features(settings);
    }

    public class Context {
        private final Map<String, List<String>> failedFeatures = new HashMap<>();

        public Map<String, List<String>> getFailedFeatures() {
            return failedFeatures;
        }

        public Marks mark(ExampleSet examples) {
            Map<String, List<String>> failedSpecs = examples.getAnswers().entrySet().stream()
                .map(entry -> {
                    Input input = answerToInput.apply(entry.getValue());

                    Features.Matcher matcher = features.matcher(examples.getSpecification());

                    List<String> failures = matcher.getFailingSpecs(input);
                    return Pair.of(entry.getKey(), failures);
                })
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));

            failedFeatures.putAll(failedSpecs);

            return new Marks(
                classify(failedSpecs, examples, AnswerStatus.UNKNOWN),
                classify(failedSpecs, examples, AnswerStatus.CORRECT),
                classify(failedSpecs, examples, AnswerStatus.INCORRECT)
            );
        }

        private Marks.Mark classify(Map<String, List<String>> failedSpecs, ExampleSet examples, AnswerStatus answerStatus) {
            Set<String> itemsOfInterest = examples.getResults().entrySet().stream()
                .filter(entry -> entry.getValue() == answerStatus)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

            return new Marks.Mark(
                failedSpecs.entrySet().stream()
                    .filter(entry -> itemsOfInterest.contains(entry.getKey()))
                    .filter(entry -> entry.getValue().isEmpty())
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet()),
                failedSpecs.entrySet().stream()
                    .filter(entry -> itemsOfInterest.contains(entry.getKey()))
                    .filter(entry -> !entry.getValue().isEmpty())
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet())
            );
        }
    }

    public Context newContext() {
        return new Context();
    }

    public boolean mark(String specification, GraphAnswer graphAnswer) {
        Input input = answerToInput.apply(graphAnswer);

        return features.matcher(specification).test(input);
    }
}
