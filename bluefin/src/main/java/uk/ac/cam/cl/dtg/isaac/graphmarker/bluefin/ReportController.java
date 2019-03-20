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

import com.fasterxml.jackson.core.JsonProcessingException;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Input;
import uk.ac.cam.cl.dtg.isaac.graphmarker.features.Features;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("checkstyle:all")
@Path("/")
public class ReportController {
    private static final String HEADER = "<html><head><title>Isaac Graph Marker Tuner</title>"
        + "<script src=\"buttons.js\"></script></head><body>";
    private static final String FOOTER = "</body></html>";

    @GET
    @Path("/{filename}.{extension: [^/.]+}")
    public Response staticFile(@PathParam("filename") String filename, @PathParam("extension") String extension) {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resourceUrl = classLoader.getResource(filename + "." + extension);
        if (resourceUrl == null) {
            return Response.status(404).build();
        }
        File file = new File(resourceUrl.getFile());
        return Response.ok(file).build();
    }

    @POST
    @Path("markAnswer")
    public void markAnswer(@QueryParam("to") String to, @QueryParam("from") String from) throws IOException {
        Examples.move(from, to);
    }

    @GET
    public String report(@QueryParam("withoutSuppression") boolean withoutSuppression,
                         @QueryParam("settings") String settingsParam) throws JsonProcessingException {
        final StringBuilder response = new StringBuilder();
        response.append(HEADER);

        final List<Marks> marksList = new ArrayList<>();

        List<ExampleSet> examples = Examples.load();

        CustomSettings settings;
        if (settingsParam == null) {
            settings = new CustomSettings();
        } else {
            try {
                settings = CustomSettings.OBJECT_MAPPER.readValue(settingsParam, CustomSettings.class);
            } catch (IOException e) {
                return "I didn't understand those settings";
            }
        }

        Marker marker = new Marker(settings);

        List<String> fullyCorrectExamples = new ArrayList<>();

        examples.forEach(example -> {
            String fullName = example.getName();
            if (!fullName.equals(example.getId())) {
                fullName += " <small>(" + example.getId() + ")</small>";
            }

            // mark the example set
            Marker.Context markerContext = marker.newContext();
            Marks marks = markerContext.mark(example);
            marksList.add(marks);

            if (marks.allCorrect() && !withoutSuppression) {
                fullyCorrectExamples.add(fullName);
                return;
            }

            // - info about question
            response.append("<h1>").append(fullName).append("</h1>");

            response.append("<h2>Specification</h2>");
            response.append("<table cellspacing=5><tr>");
            boolean canonicalPasses;
            if (example.getCanonical() != null) {
                canonicalPasses = marker.mark(example.getSpecification(), example.getCanonical());
                response.append("<td>").append(ReportHelpers.drawGraph(example.getCanonical(), canonicalPasses ? ReportHelpers.GREY : ReportHelpers.ARGH));
            } else {
                canonicalPasses = true;
            }

            String specification = Arrays.stream(example.getSpecification().split("\r?\n"))
                .map(line -> "<li>" + line + "</li>")
                .collect(Collectors.joining("\r\n"));
            response.append("<td>Features:<ul>").append(specification).append("</li></td>");
            response.append("</tr></table>");

            if (!canonicalPasses) {
                response.append("<b><large>Canonical doesn't pass spec</large></b>");
            }

            response.append(ReportHelpers.marksInfo(marks));

            ReportHelpers.displayForClassification(response, example, markerContext, marks,
                "Incorrect but passing", AnswerStatus.INCORRECT, true);

            ReportHelpers.displayForClassification(response, example, markerContext, marks,
                "Correct but failing", AnswerStatus.CORRECT, false);

            ReportHelpers.displayForClassification(response, example, markerContext, marks,
                "Fails to be classified", AnswerStatus.UNKNOWN, false);

            ReportHelpers.displayForClassification(response, example, markerContext, marks,
                "Passes to be classified", AnswerStatus.UNKNOWN, true);

            if (withoutSuppression) {
                ReportHelpers.displayForClassification(response, example, markerContext, marks,
                    "Incorrect and failing", AnswerStatus.INCORRECT, false);

                ReportHelpers.displayForClassification(response, example, markerContext, marks,
                    "Correct and passing", AnswerStatus.CORRECT, true);
            }
        });

        // overall totals
        response.append("<h1>").append("Overall").append("</h1>")
            .append(ReportHelpers.marksInfo(new Marks(marksList)));

        if (!fullyCorrectExamples.isEmpty()) {
            response.append("<br>Fully correct examples that were suppressed:<ul>");
            fullyCorrectExamples.forEach(name -> response.append("<li>").append(name));
            response.append("</ul>");
        }

        if (withoutSuppression) {
            response.append("<a href=\"?withoutSuppression=false\">Hide boring things</a>");
        } else {
            response.append("<a href=\"?withoutSuppression=true\">Show all without suppression</a>");
        }

        response.append("<br><a href=\"/crossValidate\">Cross-validate</a>");

        // parameter adjustments
        response.append("<h1>Settings</h1>");
        response.append("<form>");
        response.append("<input type=hidden name=withoutSuppression value=").append(withoutSuppression).append(">");

        response.append("<textarea rows=10 cols=80 name=settings>");
        response.append(CustomSettings.OBJECT_MAPPER.writeValueAsString(settings));
        response.append("</textarea>");
        response.append("<br><input type=submit value='Re-run'>");
        response.append("</form>");

        response.append(FOOTER);
        return response.toString();
    }

    @GET
    @Path("crossValidate")
    public String crossValidate(@QueryParam("withoutSuppression") boolean withoutSuppression) {
        final StringBuilder response = new StringBuilder();
        response.append(HEADER);

        List<ExampleSet> examples = Examples.load();

        response.append("<table border=1><tr><th>");
        examples.forEach(example -> {
            response.append("<th>").append(example.getName());
        });
        response.append("</tr>");

        examples.forEach(example -> {
            response.append("<tr><th>").append(example.getName());

            examples.forEach(crossValidator -> {
                response.append("<td>");

                crossValidator.getAnswers().forEach((answerId, answer) -> {
                    AnswerStatus answerStatus = crossValidator.getResults().get(answerId);
                    if (answerStatus != AnswerStatus.CORRECT) {
                        return;
                    }

                    Input input = ReportHelpers.answerToInput.apply(answer);

                    Features.Matcher matcher = new Features().matcher(example.getSpecification());

                    boolean result = matcher.test(input);

                    if (result) {
                        // Would expect a right answer of the crossValidator to fail this, so output it
                        Color color = example == crossValidator ? ReportHelpers.GREEN : ReportHelpers.RED;
                        response.append(ReportHelpers.drawGraph(answer, color)).append("<br>");
                    }
                });
            });

            response.append("</tr>");
        });
        response.append(FOOTER);
        return response.toString();
    }
}
