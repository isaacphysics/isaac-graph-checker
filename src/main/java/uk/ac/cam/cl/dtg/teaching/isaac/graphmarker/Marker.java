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
package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker;

import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Input;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.dos.GraphAnswer;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.dos.GraphSolutionItem;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.dos.GraphSolutions;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.dos.IsaacAnswerResponse;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.features.Features;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.translation.AnswerToInput;

public class Marker {

    private final AnswerToInput answerToInput = new AnswerToInput();

    public IsaacAnswerResponse mark(GraphSolutions question, GraphAnswer graphAnswer) {

        Input input = answerToInput.apply(graphAnswer);

        return question.getAnswers().stream()
            .filter(solution -> Features.matcher(solution.getGraphDefinition()).test(input))
            .findFirst()
            .map(GraphSolutionItem::getResponse)
            .orElse(question.getUnmatchedResponse());
    }

    public String generate(GraphAnswer graphAnswer) {
        Input input = answerToInput.apply(graphAnswer);

        return Features.generate(input);
    }
}
