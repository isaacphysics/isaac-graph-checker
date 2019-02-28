package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.dos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class IsaacAnswerResponse {
    private boolean correct;
    private ResponseExplanation explanation;

    @JsonCreator
    public IsaacAnswerResponse(@JsonProperty("correct") boolean correct,
                               @JsonProperty("explanation") ResponseExplanation explanation) {
        this.correct = correct;
        this.explanation = explanation;
    }

    public boolean isCorrect() {
        return correct;
    }

    public ResponseExplanation getExplanation() {
        return explanation;
    }

    @Override
    public String toString() {
        return "IsaacAnswerResponse{" +
            "correct=" + correct +
            ", explanation=" + explanation +
            '}';
    }
}
