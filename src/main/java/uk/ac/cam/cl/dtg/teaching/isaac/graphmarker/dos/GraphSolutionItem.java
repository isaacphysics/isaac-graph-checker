package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.dos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GraphSolutionItem {
    private String graphDefinition;
    private IsaacAnswerResponse response;

    @JsonCreator
    public GraphSolutionItem(@JsonProperty("graphDefinition") String graphDefinition,
                             @JsonProperty("response") IsaacAnswerResponse response) {
        this.graphDefinition = graphDefinition;
        this.response = response;
    }

    public String getGraphDefinition() {
        return graphDefinition;
    }

    public IsaacAnswerResponse getResponse() {
        return response;
    }
}
