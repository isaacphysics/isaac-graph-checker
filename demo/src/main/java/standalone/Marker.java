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
package standalone;

import com.google.common.base.Joiner;
import com.google.common.collect.Streams;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Input;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Line;
import uk.ac.cam.cl.dtg.isaac.graphmarker.data.Point;
import uk.ac.cam.cl.dtg.isaac.graphmarker.dos.GraphAnswer;
import uk.ac.cam.cl.dtg.isaac.graphmarker.dos.GraphSolutionItem;
import uk.ac.cam.cl.dtg.isaac.graphmarker.dos.GraphSolutions;
import uk.ac.cam.cl.dtg.isaac.graphmarker.dos.IsaacAnswerResponse;
import uk.ac.cam.cl.dtg.isaac.graphmarker.features.Features;
import uk.ac.cam.cl.dtg.isaac.graphmarker.translation.AnswerToInput;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Wrapper of Features to take input in the Isaac JSON format and return it in an acceptable format.
 */
public class Marker {

    private final AnswerToInput answerToInput = new AnswerToInput();

    /**
     * Mark an answer against a list of solutions.
     * @param question The list of solutions.
     * @param graphAnswer The answer.
     * @return The response from the list of solutions for the first solution that matched.
     */
    public IsaacAnswerResponse mark(GraphSolutions question, GraphAnswer graphAnswer) {

        Input input = answerToInput.apply(graphAnswer);

        return question.getAnswers().stream()
            .filter(solution -> Features.matcher(solution.getGraphDefinition()).test(input))
            .findFirst()
            .map(GraphSolutionItem::getResponse)
            .orElse(question.getUnmatchedResponse());
    }

    /**
     * Convert an answer into a feature specification.
     * @param graphAnswer The solution to be analysed.
     * @return A specification of all the features in the answer.
     */
    public String generate(GraphAnswer graphAnswer) {
        Input input = answerToInput.apply(graphAnswer);

        String specification = Features.generate(input);

        Predicate<Input> matcher = Features.matcher(specification);
        
        Input simpleInput = simplify(input);

        boolean success = matcher.test(simpleInput);

        String simpleImage = drawInput(input);
        
        return specification + "<br />" + "\r\n"
            + "Simple version matches: " + success + "<br />" + "\r\n"
            + simpleImage;
    }

    private String drawInput(Input input) {

        int width = 250;
        int height = 250;

        // Constructs a BufferedImage of one of the predefined image types.
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Create a graphics which can be used to draw into the buffered image
        Graphics2D g2d = bufferedImage.createGraphics();

        g2d.setColor(Color.white);
        g2d.fillRect(0, 0, width, height);

        g2d.setColor(Color.black);

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

        g2d.setColor(new Color(0.3f, 0.6f, 1.0f));

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

        String lineSpec = Streams.mapWithIndex(input.getLines().stream(),
            (Line l, long i) -> i + ": " + Joiner.on(", ").join(l.getPoints()))
            .collect(Collectors.joining("\r\n<br />"));

        return lineSpec + "\r\n<br />" + "![Simple image](data:image/png;base64," + finalOutput.toString() + ")";
    }

    private Input simplify(Input input) {
        return new Input(
            input.getLines().stream()
                .map(this::simplify)
                .collect(Collectors.toList())
        );
    }

    private Line simplify(Line line) {
        List<Point> points = new ArrayList<>();
        points.add(line.getPoints().get(0));
        line.getPointsOfInterest().forEach(p -> addPoint(points, p));
        addPoint(points, line.getPoints().get(line.getPoints().size() - 1));

        return new Line(
            points,
            line.getPointsOfInterest()
        );
    }

    private void addPoint(List<Point> points, Point p) {
        // TODO: Intercept line on a perpendicular halfway between points[-1] and p and use that point
        Point q = points.get(points.size() - 1).add(p).times(0.5);
        points.add(q);
        points.add(p);
    }
}
