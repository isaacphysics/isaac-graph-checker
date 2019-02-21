package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.dos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class GraphSolutions {
    private List<GraphSolutionItem> answers;
    private IsaacAnswerResponse unmatchedResponse;

    @JsonCreator
    public GraphSolutions(@JsonProperty("answers") List<GraphSolutionItem> answers,
                          @JsonProperty("unmatchedResponse") IsaacAnswerResponse unmatchedResponse) {
        this.answers = answers;
        this.unmatchedResponse = unmatchedResponse;
    }

    public List<GraphSolutionItem> getAnswers() {
        return answers;
    }

    public IsaacAnswerResponse getUnmatchedResponse() {
        return unmatchedResponse;
    }
}
