package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker;

import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Line;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.dos.GraphAnswer;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.dos.GraphSolutions;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.dos.IsaacAnswerResponse;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.features.Features;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.translation.AnswerToLine;

public class Marker {

    private AnswerToLine answerToLine = new AnswerToLine();

    public IsaacAnswerResponse mark(GraphSolutions question, GraphAnswer graphAnswer) {

        Line line = answerToLine.apply(graphAnswer);

        return question.getAnswers().stream()
            .filter(solution -> Features.matcher(solution.getGraphDefinition()).test(line))
            .findFirst()
            .map(solution -> solution.getResponse())
            .orElse(question.getUnmatchedResponse());
    }
}
