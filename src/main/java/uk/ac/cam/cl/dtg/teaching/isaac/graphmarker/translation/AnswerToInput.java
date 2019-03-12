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
package uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.translation;

import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Input;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Line;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.Point;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.PointOfInterest;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.data.PointType;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.dos.Curve;
import uk.ac.cam.cl.dtg.teaching.isaac.graphmarker.dos.GraphAnswer;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Converter from JSON answer format to Input.
 *
 * Throws away most of the extraneous details and puts everything (lines, the set of lines, points of interest) in
 * order of increasing x co-ordinate.
 */
public class AnswerToInput implements Function<GraphAnswer, Input> {

    @Override
    public Input apply(final GraphAnswer graphAnswer) {
        return new Input(graphAnswer.getCurves().stream()
            .map(this::curveToLine)
            .sorted(Comparator.comparingDouble(a -> a.getPoints().stream().findFirst().map(Point::getX).orElse(0.0)))
            .collect(Collectors.toList()));
    }

    /**
     * Convert an input Curve into a Line.
     * @param curve Curve to be converted.
     * @return A Line representing that Curve in a normalised format.
     */
    private Line curveToLine(final Curve curve) {
        List<Point> points = curve.getPts().stream()
            .map(pt -> new Point(pt.getX(), pt.getY()))
            .collect(Collectors.toList());

        if (points.size() > 2) {
            final int last = points.size() - 1;
            if (points.get(0).getX() > points.get(last).getX()) {
                Collections.reverse(points);
            }
        }

        List<PointOfInterest> pointsOfInterest = Stream.concat(
            curve.getMaxima().stream()
                .map(pt -> new PointOfInterest(pt.getX(), pt.getY(), PointType.MAXIMA)),
            curve.getMinima().stream()
                .map(pt -> new PointOfInterest(pt.getX(), pt.getY(), PointType.MINIMA)))
            .sorted(Comparator.comparingDouble(Point::getX))
            .collect(Collectors.toList());

        return new Line(points, pointsOfInterest);
    }
}
