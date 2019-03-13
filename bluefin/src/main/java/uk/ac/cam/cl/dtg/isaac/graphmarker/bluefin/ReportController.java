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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Input;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Point;
import uk.ac.cam.cl.dtg.isaac.graphmarker.dos.GraphAnswer;
import uk.ac.cam.cl.dtg.isaac.graphmarker.translation.AnswerToInput;

import javax.imageio.ImageIO;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("checkstyle:all")
@Path("/")
public class ReportController {

    private static final String HEADER = "<html><head><script src=\"buttons.js\"></script></head><body>";
    private static final String FOOTER = "</body></html>";

    public static final Color BLUE = new Color(0.3f, 0.6f, 1.0f);
    public static final Color RED = new Color(1.0f, 0.3f, 0.6f);
    public static final Color GREEN = new Color(0.2f, 0.8f, 0.4f);
    public static final Color ORANGE = new Color(1.0f, 0.6f, 0.3f);
    public static final Color GREY = new Color(0.5f, 0.5f, 0.5f);
    public static final Color ARGH = new Color(1.0f, 0.5f, 0.3f);

    private final ObjectMapper om = new ObjectMapper();
    private final AnswerToInput answerToInput = new AnswerToInput();

    @GET
    @Path("/{filename}.{extension: [^/.]+}")
    public Response staticFile(@PathParam("filename") String filename, @PathParam("extension") String extension) {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(filename + "." + extension).getFile());
        return Response.ok(file).build();
    }

    @POST
    @Path("markAnswer")
    public void markAnswer(@QueryParam("to") String to, @QueryParam("from") String from) throws IOException {
        Examples.move(from, to);
    }

    @GET
    public String report() {
        final StringBuilder response = new StringBuilder();
        response.append(HEADER);

        final List<Marks> marksList = new ArrayList<>();

        List<ExampleSet> examples = Examples.load();

        Marker marker = new Marker();

        List<String> fullyCorrectExamples = new ArrayList<>();

        examples.forEach(example -> {
            // mark the example set
            Marker.Context markerContext = marker.newContext();
            Marks marks = markerContext.mark(example);
            marksList.add(marks);

            if (marks.allCorrect()) {
                fullyCorrectExamples.add(example.getName());
                return;
            }

            // - info about question
            response.append("<h1>").append(example.getName()).append("</h1>");

            response.append("<h2>Specification</h2>");
            response.append("<table cellspacing=5><tr>");
            boolean canonicalPasses = marker.mark(example.getSpecification(), example.getCanonical());
            if (example.getCanonical() != null) {
                response.append("<td>").append(drawGraph(example.getCanonical(), canonicalPasses ? GREY : ARGH));
            }
            String specification = Arrays.stream(example.getSpecification().split("\r?\n"))
                .map(line -> "<li>" + line + "</li>")
                .collect(Collectors.joining("\r\n"));
            response.append("<td>Features:<ul>").append(specification).append("</li></td>");
            response.append("</tr></table>");

            if (!canonicalPasses) {
                response.append("<b><large>Canonical doesn't pass spec</large></b>");
            }

            response.append(marksInfo(marks));

            displayForClassification(response, example, markerContext, marks,
                "Incorrect but passing", AnswerStatus.INCORRECT, true);

            displayForClassification(response, example, markerContext, marks,
                "Correct but failing", AnswerStatus.CORRECT, false);

            displayForClassification(response, example, markerContext, marks,
                "Fails to be classified", AnswerStatus.UNKNOWN, false);

            displayForClassification(response, example, markerContext, marks,
                "Passes to be classified", AnswerStatus.UNKNOWN, true);
        });

        // overall totals
        response.append("<h1>").append("Overall").append("</h1>")
            .append(marksInfo(new Marks(marksList)));

        response.append("Fully correct examples that were suppressed: ")
            .append(String.join(", ", fullyCorrectExamples));

        // parameter adjustments

        response.append(FOOTER);
        return response.toString();
    }

    private void displayForClassification(StringBuilder response, ExampleSet example, Marker.Context markerContext,
                                          Marks marks, String name, AnswerStatus status, boolean passed) {
        Marks.Mark mark = marks.get(status);
        ImmutableList<String> answers = mark.get(passed);

        String correct = "<button class='Correct'>Correct</button>";
        String incorrect = "<button class='Incorrect'>Incorrect</button>";

        String buttons = status == AnswerStatus.UNKNOWN ? "<td>" + correct + "<br><br>" + incorrect + "<td>"
            : status == AnswerStatus.CORRECT ? "<td>" + incorrect + "<td>"
            : status == AnswerStatus.INCORRECT ? "<td>" + correct + "<td>"
            : "<td><td>";

        if (answers.size() > 0) {
            response.append("<h2>").append(name).append("</h2>");
            response.append("<table>");
            answers.forEach(exampleGraph -> {
                String fullId = example.getId() + "/" + status.name().toLowerCase() + "/" + exampleGraph;
                response.append("<tr id=\"").append(fullId).append("\"><td>");
                response.append(drawGraph(example.getAnswers().get(exampleGraph), colorLookup(status, passed)));

                response.append(buttons);

                List<String> failedFeatures = markerContext.getFailedFeatures().get(exampleGraph);
                if (failedFeatures.size() > 0) {
                    response.append("<p>Failing features:<ul>")
                        .append(failedFeatures.stream()
                            .map(f -> "<li>" + f + "</li>")
                            .collect(Collectors.joining("\r\n"))
                        )
                        .append("</ul></p>");
                }
                response.append("</tr>");
            });
            response.append("</table>");
        }
    }

    private Color colorLookup(AnswerStatus status, boolean passed) {
        switch (status) {
            case UNKNOWN:
                return passed ? GREEN : BLUE;
            case CORRECT:
                return passed ? GREEN : ORANGE;
            case INCORRECT:
                return passed ? RED : BLUE;
        }
        return Color.YELLOW;
    }

    private String drawGraph(GraphAnswer graphAnswer, Color color) {
        Input input = answerToInput.apply(graphAnswer);

        int width = 300;
        int height = 200;

        // Constructs a BufferedImage of one of the predefined image types.
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Create a graphics which can be used to draw into the buffered image
        Graphics2D g2d = bufferedImage.createGraphics();

        g2d.setColor(color == ARGH ? Color.BLACK : Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        g2d.setColor(Color.LIGHT_GRAY);

        g2d.drawLine(
            width / 2,
            0,
            width / 2,
            height
        );

        g2d.drawLine(
            0,
            height / 2,
            width,
            height / 2
        );

        g2d.setColor(color);

        input.getLines().forEach(line -> {
            Point lastPoint = null;
            for (Point point : line.getPoints()) {
                if (lastPoint != null) {
                    g2d.drawLine(
                        (int) (lastPoint.getX() * width + width / 2),
                        (int) (-lastPoint.getY() * height + height / 2),
                        (int) (point.getX() * width + width / 2),
                        (int) (-point.getY() * height + height / 2)
                    );
                }
                lastPoint = point;
            }
        });

        // Disposes of this graphics context and releases any system resources that it is using.
        g2d.dispose();

        ByteArrayOutputStream finalOutput = new ByteArrayOutputStream();
        OutputStream base64output = Base64.getEncoder().wrap(finalOutput);

        try {
            ImageIO.write(bufferedImage, "png", ImageIO.createImageOutputStream(base64output));
            base64output.close();
        } catch (IOException e) {
            return "Failed to write image, error was:" + e;
        }

        return "<img src=\"data:image/png;base64," + finalOutput.toString() + "\">";
    }

    private String marksInfo(Marks marks) {
        return "<table><tr>"
            + markInfo("Unknown", marks.getUnknown(), false, false)
            + markInfo("Correct", marks.getCorrect(), true, false)
            + markInfo("Incorrect", marks.getIncorrect(), false, true)
            + "</tr></table>";
    }

    private String markInfo(String name, Marks.Mark mark, boolean failIsError, boolean passIsError) {
        int fails = mark.getFails().size();
        int passes = mark.getPasses().size();
        boolean hasFails = fails > 0;
        boolean hasPasses = passes > 0;
        boolean failError = failIsError && hasFails;
        boolean passError = passIsError && hasPasses;

        if (!hasFails && !hasPasses) {
            return "<td><td>";
        }

        return "<td>" + wrapIf(failError || passError, "b", name) + "<td>"
            + (hasFails ? wrapIf(failError, "b", "wrong: " + fails) : "")
            + (hasPasses ? wrapIf(passError, "b", " right: " + passes) : "");
    }

    private String wrapIf(boolean doWrap, String wrapperTag, String inner) {
        return doWrap ?
            "<" + wrapperTag + ">" + inner + "</" + wrapperTag + ">"
        :   inner;
    }
}
