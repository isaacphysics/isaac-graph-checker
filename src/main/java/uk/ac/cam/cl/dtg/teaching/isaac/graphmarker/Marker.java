package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker;

import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Input;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.dos.GraphAnswer;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.dos.GraphSolutions;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.dos.IsaacAnswerResponse;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.features.Features;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.translation.AnswerToInput;

public class Marker {

    private AnswerToInput answerToInput = new AnswerToInput();

    public IsaacAnswerResponse mark(GraphSolutions question, GraphAnswer graphAnswer) {

        Input input = answerToInput.apply(graphAnswer);

        return question.getAnswers().stream()
            .filter(solution -> Features.matcher(solution.getGraphDefinition()).test(input))
            .findFirst()
            .map(solution -> solution.getResponse())
            .orElse(question.getUnmatchedResponse());
    }
}
