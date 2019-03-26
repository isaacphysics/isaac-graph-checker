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
package standalone;

import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Input;
import uk.ac.cam.cl.dtg.isaac.graphmarker.dos.GraphAnswer;
import standalone.dos.GraphSolutionItem;
import standalone.dos.GraphSolutions;
import standalone.dos.IsaacAnswerResponse;
import uk.ac.cam.cl.dtg.isaac.graphmarker.features.Features;
import uk.ac.cam.cl.dtg.isaac.graphmarker.translation.AnswerToInput;

/**
 * Wrapper of Features to take input in the Isaac JSON format and return it in an acceptable format.
 */
public class Marker {

    private final AnswerToInput answerToInput = new AnswerToInput();

    /**
     * Mark an answer against a list of solutions.
     * @param question The list of solutions.
     * @param graphAnswer The answer.
     * @return The response from the list of solutions for the first solution that matched.
     */
    public IsaacAnswerResponse mark(GraphSolutions question, GraphAnswer graphAnswer) {

        Input input = answerToInput.apply(graphAnswer);

        return question.getAnswers().stream()
            .filter(solution -> new Features().matcher(solution.getGraphDefinition()).test(input))
            .findFirst()
            .map(GraphSolutionItem::getResponse)
            .orElse(question.getUnmatchedResponse());
    }

    /**
     * Convert an answer into a feature specification.
     * @param graphAnswer The solution to be analysed.
     * @return A specification of all the features in the answer.
     */
    public String generate(GraphAnswer graphAnswer) {
        Input input = answerToInput.apply(graphAnswer);

        return new Features().generate(input);
    }
}
