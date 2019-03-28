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
package org.isaacphysics.graphchecker.bluefin;

import com.google.common.collect.ImmutableList;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Input;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Point;
import uk.ac.cam.cl.dtg.isaac.graphmarker.dos.GraphAnswer;
import uk.ac.cam.cl.dtg.isaac.graphmarker.translation.AnswerToInput;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

enum ReportHelpers {
    ;
    private static final Color BLUE = new Color(0.3f, 0.6f, 1.0f);
    static final Color RED = new Color(1.0f, 0.3f, 0.6f);
    static final Color GREEN = new Color(0.2f, 0.8f, 0.4f);
    private static final Color ORANGE = new Color(1.0f, 0.6f, 0.3f);
    static final Color GREY = new Color(0.5f, 0.5f, 0.5f);
    static final Color ARGH = new Color(1.0f, 0.5f, 0.3f);
    static final AnswerToInput answerToInput = new AnswerToInput();

    static void displayForClassification(StringBuilder response, ExampleSet example, Marker.Context markerContext,
                                         Marks marks, String name, AnswerStatus status, boolean passed) {
        Marks.Mark mark = marks.get(status);
        ImmutableList<String> answers = mark.get(passed);

        String correct = "<button class='correct'>Correct</button>";
        String incorrect = "<button class='incorrect'>Incorrect</button>";
        String delete = "<br><br><button class='delete'>Delete</button>";

        String buttons = status == AnswerStatus.UNKNOWN ?
                passed ? "<td>" + correct + "<br><br>" + incorrect + delete + "<td>"
                       : "<td>" + incorrect + "<br><br>" + correct + delete + "<td>"
            : status == AnswerStatus.CORRECT ? "<td>" + incorrect + delete + "<td>"
            : status == AnswerStatus.INCORRECT ? "<td>" + correct + delete + "<td>"
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

    private static Color colorLookup(AnswerStatus status, boolean passed) {
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

    static String drawGraph(GraphAnswer graphAnswer, Color color) {
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
            uk.ac.cam.cl.dtg.isaac.graphmarker.data.Point lastPoint = null;
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

    static String marksInfo(Marks marks) {
        return "<table><tr>"
            + markInfo("Unknown", marks.getUnknown(), false, false)
            + markInfo("Correct", marks.getCorrect(), true, false)
            + markInfo("Incorrect", marks.getIncorrect(), false, true)
            + "</tr></table>";
    }

    private static String markInfo(String name, Marks.Mark mark, boolean failIsError, boolean passIsError) {
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

    private static String wrapIf(boolean doWrap, String wrapperTag, String inner) {
        return doWrap ?
            "<" + wrapperTag + ">" + inner + "</" + wrapperTag + ">"
        :   inner;
    }
}
