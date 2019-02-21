package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.standalone;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.Marker;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.dos.GraphAnswer;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.dos.GraphSolutions;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.dos.GraphSolutionItem;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.dos.IsaacAnswer;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.dos.IsaacAnswerResponse;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.dos.ResponseExplanation;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/isaac-api/api/questions")
public class MarkerController {

    private final ObjectMapper om = new ObjectMapper();

    private final Marker marker = new Marker();

    private GraphSolutions getSolution(String... answers) {
        IsaacAnswerResponse failed = new IsaacAnswerResponse(false, new ResponseExplanation(
            "markdown", new String[]{}, "content", Collections.singletonList(
            new ResponseExplanation("markdown", new String[]{}, "content",
                "Unfortunately your answer was incorrect."))));

        IsaacAnswerResponse success = new IsaacAnswerResponse(true, new ResponseExplanation(
            "markdown", new String[]{}, "content", Collections.singletonList(
            new ResponseExplanation("markdown", new String[]{}, "content",
                "Your answer was correct!"))));

        return new GraphSolutions(Arrays.stream(answers)
            .map(answer -> new GraphSolutionItem(answer, success))
            .collect(Collectors.toList()),
            failed);
    }

    private final Map<String, GraphSolutions> questionData;

    public MarkerController() throws IOException {
        questionData = ImmutableMap.of(
            "48cfddd0-8e66-4e2a-b462-fc27aeb97cee", getSolution("through:bottomLeft,-Xaxis,topLeft,+Yaxis,topRight")
        );
    }

    @POST
    @Path("/graph_sketcher_test%7C{question}/answer")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public IsaacAnswerResponse getMarks(@PathParam("question") String questionId,
                                        IsaacAnswer answer) throws Exception {
        GraphSolutions question = questionData.get(questionId);
        if ("graphChoice".equals(answer.getType())) {
            GraphAnswer graphAnswer = om.readValue(answer.getValue(), GraphAnswer.class);
            return marker.mark(question, graphAnswer);
        }
        throw new Exception("Unknown answer type " + answer.getType());
    }
}
