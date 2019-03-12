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
import java.util.Collections;
import java.util.Map;

@SuppressWarnings("checkstyle:all")
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

        return new GraphSolutions(Collections.singletonList(
            new GraphSolutionItem(String.join("\r\n", answers), success)
        ), failed);
    }

    private final Map<String, GraphSolutions> questionData;

    public MarkerController() {
        questionData = ImmutableMap.of(
            "48cfddd0-8e66-4e2a-b462-fc27aeb97cee",
                getSolution("through:bottomLeft,-Xaxis,topLeft,+Yaxis,topRight"),
            "5b032e4c-e432-455f-925f-8efb8b33c18e",
                getSolution("through:topLeft, +Yaxis, topRight", "points: minima in topRight"),
            "96ee3e16-6fa0-46b5-b9d9-f02d0ba4f077",
                getSolution(
                    "through:bottomLeft,-Yaxis,bottomRight,+Xaxis,topRight,+Xaxis,bottomRight,+Xaxis,topRight",
                    "points:maxima in topRight, minima in bottomRight"
                ),
            "f5e5d9ea-8bc9-4adc-8073-a599b0eb3d58",
                getSolution("curves:2",
                    "line: 1; through:  bottomLeft",
                    "line: 1; slope: start=flat, end=down",
                    "line: 2; through: topRight",
                    "line: 2; slope: start=down, end=flat"),
            "afaaf16b-2415-4662-98bf-306c55cc72d0",
                getSolution("through: topLeft, +Yaxis, topRight", "slope: start=flat, end=up")
        );
    }

    @POST
    @Path("/graph_sketcher_test%7C{question}/answer")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public IsaacAnswerResponse getMarks(@PathParam("question") String questionId,
                                        IsaacAnswer answer) throws Exception {
        if ("graphChoice".equals(answer.getType())) {
            GraphAnswer graphAnswer = om.readValue(answer.getValue(), GraphAnswer.class);

            if (questionId.equals("generate")) {
                String spec = marker.generate(graphAnswer);
                return new IsaacAnswerResponse(true, new ResponseExplanation(
                    "markdown", new String[]{}, "content", Collections.singletonList(
                    new ResponseExplanation("markdown", new String[]{}, "content",
                        spec.replaceAll("\r\n", "<br>")))));
            }
            GraphSolutions question = questionData.get(questionId);

            if (question == null) {
                throw new Exception("Unknown question " + questionId);
            }
            return marker.mark(question, graphAnswer);
        }
        throw new Exception("Unknown answer type " + answer.getType());
    }
}
